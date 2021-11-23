package com.habr.cron.dev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

/**
 * Этот класс предназначен для сбора метрик эффективности IntervalList vs. BitMap
 * Измеряются такие параметры:
 *  - кол-во интервалов         2..16
 *  - общее кол-во значений     1%..99%
 *  - кучность значений по диапазонам 0-999, 100-199, ... 900-1000 (процент покрытия)
 *  для каждого матчера
 *  - время на поиск
 *  - время на матчинг
 *  относительные результаты:
 *  - A: во сколько раз поиск оказался лучше у IntervalList?
 *  - B: во сколько раз матчинг оказался лучше у IntervalList?
 *
 *  Критерий полезности: максимум A*B
 */
public class ListOfIntervalsMeasures
{
    private final static int MEASURES_LOOPS = 100000; // кол-во циклов на измерение одной функции
    private static final int MAX_RANGES = 16; // максимальное число диапазонов
    private static final int MAX_TRIES_FOR_RANGE = 100; // кол-во попыток измерений для каждого процента заполнения
    private static final int COVERAGE_MAX_STEPS = 11; // кол-во ступений изменения процента покрытия
    private static final int GROUPING_MAX_GROUPS = 100; // кол-во групп, по которым смотрим статистику распределения
    private static final int LOW = 0;
    private static final int HIGH = 999;
    private static final int COUNT = HIGH+1;

    static Random random;


    // результат одного бенчмарка
    private static final class BenchResult implements Comparable<BenchResult>
    {
        float ratio;
        float groups[];

        public BenchResult(Benchmark benchmark)
        {
            ratio = benchmark.getMatchRatio() * benchmark.getSearchRatio();
            groups = benchmark.getGroups100();
        }

        public int compareTo(BenchResult o)
        {
            return ratio < o.ratio ? -1 : 1;
        }
    }

    // здесь будем хранить результаты всех измерений для указанного кол-ва диапазона ranges
    private static  BenchResult results[] = new BenchResult[COVERAGE_MAX_STEPS * MAX_TRIES_FOR_RANGE];



    public static void main(String args[]) throws Exception
    {
        //checkPredictions(2);
        collectMetrics();
    }


    /**
     * Демонстрирует работу натренированной нейронной сети.
     * Предсказывает какой матчер был бы лучше на основе распределения значений по диапазону.
     * И тут же делается реальный бенчмарк, чтобы проверить насколько сошлись предсказания.
     *
     * @throws Exception
     */
    private static void checkPredictions(int ranges) throws Exception
    {
        random = new Random(1L); // заменить на другое число
        int count_of_miss = 0;

        for (int values = 10, step = 0; values <= 1000; values += 99, step++) // с 1% до 100% с шагом в 9,9%
        {
            // делаем по 100 пробных измерений при данном % заполнения
            for (int tries = 0; tries < MAX_TRIES_FOR_RANGE; tries++)
            {
                Benchmark benchmark = new Benchmark(ranges, values);
                benchmark.run();

                float ratio = benchmark.getMatchRatio() * benchmark.getSearchRatio();
                boolean expected = ratio > 1.5; // какой матчер ожидается использовать?

                boolean actual = MatcherSelectorSolver.solve( benchmark.getGrouping() );

                System.out.println(ratio + "\t" + actual);

                if ( expected != actual )
                {
                    count_of_miss++;
                    //System.out.println(String.format("Промашка: коэффициент %f", ratio));
                }
            }
        }

        System.out.println(String.format("Всего промахов: %d из %d", count_of_miss, MAX_TRIES_FOR_RANGE*COVERAGE_MAX_STEPS));
    }


    /**
     * Выполняет сбор метрик производительности матчеров и записывает это в файлы.
     * В последующем данные файлов будут использованы для тренировки нейронных сетей.
     *
     * @throws Exception
     */
    private static void collectMetrics() throws Exception
    {
        random = new Random(1L);

        for (int ranges = 2; ranges <= MAX_RANGES; ranges++) // увеличиваем кол-во диапазонов до 16-ти
        {
            generateBenchResults(ranges);
            //visualizeBenchResults(ranges);
        }
    }



    private static void generateBenchResults(int ranges) throws IOException
    {
        File output = new File("out", ranges + ".csv");
        PrintWriter pw = new PrintWriter(new FileWriter(output, false), true);
        pw.println("ranges;match;search;coverage;groups");

        // плавно изменяем процент покрытия диапазона значениями (COVERAGE_MAX_STEPS шагов)
        for (int values = 10, step = 0; values <= 1000; values += 99, step++) // с 1% до 100% с шагом в 9,9%
        {
            // делаем по 100 пробных измерений при данном % заполнения
            for (int tries = 0; tries < MAX_TRIES_FOR_RANGE; tries++)
            {
                Benchmark benchmark = new Benchmark(ranges, values);
                benchmark.run();

                results[step * MAX_TRIES_FOR_RANGE + tries] = new BenchResult(benchmark);

                writeToFile(pw, benchmark);
                //writeToScreen(benchmark);
            }
            System.out.println(ranges + "\t" + step);
        }
        pw.close();
    }


    private static void visualizeBenchResults(int ranges) throws IOException
    {
        // создаем визуализацию распределения
        BufferedImage img = new BufferedImage(
                GROUPING_MAX_GROUPS + 1,
                COVERAGE_MAX_STEPS * MAX_TRIES_FOR_RANGE,
                BufferedImage.TYPE_BYTE_GRAY);

        // сортируем результаты так, чтобы ratio убывал
        Arrays.sort(results);

        // максимальный коэффициент ускорения (из-за сортировки он в самом конце)
        final float MAX_RATIO = results[results.length-1].ratio;

        for (int step = 0; step < COVERAGE_MAX_STEPS; step++)
        {
            for (int tries = 0; tries < MAX_TRIES_FOR_RANGE; tries++)
            {
                int y = step * MAX_TRIES_FOR_RANGE + tries;
                BenchResult result = results[y];

                for (int group = 0; group < GROUPING_MAX_GROUPS; group++)
                {
                    int c = (int) (255 * result.groups[group]);
                    Color color = new Color(c, c, c);
                    img.setRGB(group, y, color.getRGB());
                }

                {
                    int c = (int) (255 * (result.ratio / MAX_RATIO));
                    Color color = new Color(c, c, c);
                    img.setRGB(GROUPING_MAX_GROUPS, y, color.getRGB());
                }
            }
        }
        ImageIO.write(img, "png", new File("out", ranges + ".png"));
    }


    private static void writeToScreen(Benchmark benchmark)
    {
        System.out.println(
                String.format("[%d] coverage:%f match:%f search:%f ratio:%f schedule:%s grouping:%s",
                        benchmark.getRanges(),
                        benchmark.getCoverage(),
                        benchmark.getMatchRatio(),
                        benchmark.getSearchRatio(),
                        benchmark.getMatchRatio() * benchmark.getSearchRatio(),
                        benchmark.asSchedule(),
                        Arrays.toString(benchmark.getGrouping())
                                .replaceAll("[\\[|\\]]", "")
                                .replaceAll(",", ";")
                                .replaceAll("\\.", ",")
                ));
    }

    private static void writeToFile(PrintWriter pw, Benchmark benchmark)
    {
        pw.println(
                String.format("%d;%f;%f;%f;%s",
                        benchmark.getRanges(),
                        benchmark.getMatchRatio(),
                        benchmark.getSearchRatio(),
                        benchmark.getCoverage(),
                        Arrays.toString(benchmark.getGrouping())
                                .replaceAll("[\\[|\\]]", "")
                                .replaceAll(",", ";")
                                .replaceAll("\\.", ",")
                ));
    }


    private static final class Benchmark
    {
        ListOfIntervalsMatcher list;
        BitMapMatcher bits;
        RangeList ranges;

        final int realCount; // реальное число значений в диапазоне, которое требуется создать

        float listMatchTime = 1;
        float listSearchTime = 1;
        float bitsMatchTime = 1;
        float bitsSearchTime = 1;


        public Benchmark(int countOrRanges, int countOfValues)
        {
            realCount = countOfValues;
            ranges = new RangeList(countOrRanges);
            generateRanges(countOrRanges);
            setupRanges();
        }

        public void run()
        {
            bitsMatchTime = measureMatch(bits);
            listMatchTime = measureMatch(list);
            bitsSearchTime = measureSearch(bits);
            listSearchTime = measureSearch(list);
        }

        public int getRanges()
        {
            return ranges.getCount();
        }

        /**
         * @return кучность заполнения в виде 10 значений с % покрытия интервалов 0..99, 100-199, ...
         */
        public float[] getGrouping()
        {
            float result[] = new float[10];
            // выберем матчер, который оказался сейчас быстрее
            DigitMatcher matcher = bitsMatchTime < listMatchTime ? bits : list;

            for (int i = 0; i < 10; i++) // 10 групп
            {
                int count = 0;

                for (int v = i*100; v < i*100+99; v++) // по 100 значений в группе
                    if ( matcher.match(v) ) count++;

                result[i] = ((float)count) / 100; // по 100 значений в группе
            }
            return result;
        }

        public float[] getGroups100()
        {
            float result[] = new float[GROUPING_MAX_GROUPS];
            // выберем матчер, который оказался сейчас быстрее
            DigitMatcher matcher = bitsMatchTime < listMatchTime ? bits : list;
            final int N = COUNT / GROUPING_MAX_GROUPS; // 10

            for (int i = 0; i < GROUPING_MAX_GROUPS; i++) // GROUPING_MAX_GROUPS групп
            {
                int count = 0;
                int j = i*N;

                for (int v = j+0; v < j+N; v++) // по N значений в группе
                    if ( matcher.match(v) ) count++;

                result[i] = ((float)count) / N; // по 100 значений в группе
            }
            return result;
        }

        public float getCoverage()
        {
            return ((float)realCount) / COUNT;
        }

        public int getMemoryUsage()
        {
            return 8 * ranges.getCount();
        }

        /**
         * @return во сколько раз List быстрее по времени Bits?
         */
        public float getSearchRatio()
        {
            return bitsSearchTime / listSearchTime;
        }

        public float getMatchRatio()
        {
            return bitsMatchTime / listMatchTime;
        }

        /**
         * @return использованное в бенчмарке расписание
         */
        public String asSchedule()
        {
            return ranges.toString();
        }




        private void generateRanges(int count)
        {
            // границы диапазонов будут здесь; потом их отсортируем
            int bounds[] = new int[count*2];

            // заполним их случайными величинами
            for (int i = 0; i < bounds.length; i++)
                bounds[i] = random.nextInt(COUNT); //0..999

            Arrays.sort(bounds); // отсортируем

            for (int i = 0; i < count; i++)
            {
                int lo = bounds[i*2 + 0];
                int hi = bounds[i*2 + 1];
                ranges.add(new Range(lo, hi, 1));
            }
        }




        private float measureSearch(DigitMatcher matcher)
        {
            long n1 = System.nanoTime();
            for (int i = 0; i < MEASURES_LOOPS; i++)
            {
                int v = (int)(random.nextFloat() * COUNT);
                matcher.getNext(v);
            }
            long n2 = System.nanoTime();

            return ((float)(n2 - n1)) / MEASURES_LOOPS;
        }
        private float measureMatch(DigitMatcher matcher)
        {
            long n1 = System.nanoTime();
            for (int i = 0; i < MEASURES_LOOPS; i++)
            {
                int v = (int)(random.nextFloat() * COUNT);
                matcher.match(v);
            }
            long n2 = System.nanoTime();

            return ((float)(n2 - n1)) / MEASURES_LOOPS;
        }


        private void setupRanges()
        {
            list = new ListOfIntervalsMatcher(ranges.getCount());
            setRanges(list, ranges);

            bits = new BitMapMatcher(LOW, HIGH);
            setRanges(bits, ranges);
        }

        private void setRanges(MapMatcher matcher, RangeList ranges)
        {
            for (Range range : ranges)
            {
                matcher.addRange(range.min, range.max, range.step);
            }
            matcher.finishRange();
        }
    }

}
