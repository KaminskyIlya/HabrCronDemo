package com.habr.cron.dev;

import java.util.Arrays;
import java.util.Random;

/**
 * Этот класс предназначен для сбора метрик эффективности IntervalList vs. BitMap
 * Измеряются такие параметры:
 *  - кол-во интервалов         2..16
 *  - общее кол-во значений     1%..99%
 *  - кучность значений по диапазонам 0-999, 100-199, ... 900-1000 (процент покрытия)
 *  - M: сколько памяти отъедает
 *  для каждого матчера
 *  - время на поиск
 *  - время на матчинг
 *  относительные результаты:
 *  - A: во сколько раз поиск оказался лучше у IntervalList?
 *  - B: во сколько раз матчинг оказался лучше у IntervalList?
 *
 *  Критерий полезности: максимум A+B, при минимуме M.
 */
public class IntervalListMeasures
{
    private final static int LOOP_COUNT = 100000;
    private static final int MAX_RANGES = 16;
    private static final int LOW = 0;
    private static final int HIGH = 999;
    private static final int COUNT = HIGH+1;

    static Random random;


    public static void main(String args[]) throws Exception
    {
        random = new Random(1L);

        System.out.println("ranges;coverage;ratio;grouping");
        for (int ranges = 2; ranges <= MAX_RANGES; ranges++) // увеличиваем кол-во диапазонов до 16-ти
        {
            for (int values = 10; values < 1000; values += 100 ) // увеличиваем процент заполнения с 1% до 100% с шагом в 10%
            {
                for (int tries = 1; tries < 100; tries++)
                {
                    Benchmark benchmark = new Benchmark(ranges, values);
                    benchmark.run();

                    float A = benchmark.getMatchRatio();
                    float B = benchmark.getSearchRatio();

                    //TODO: сделать вывод в CSV-файл для последующего анализа
                    System.out.println(
                            String.format("TEST [%d, %f] match:%f search:%f total:%f schedule:%s grouping:%s",
//                            String.format("%d;%f;%f;%s",
                                    ranges,
                                    benchmark.getCoverage(),
                                    benchmark.getMatchRatio(),
                                    benchmark.getSearchRatio(),
                                    (A + B) / 2,
                                    benchmark.asSchedule(),
//                                    benchmark.getMemoryUsage(),
                                    Arrays.toString(benchmark.getGrouping())
                                            .replaceAll("[\\[|\\]]", "")
                                            .replaceAll(",", ";")
                                            .replaceAll("\\.", ",")
                            ));
                }
            }
        }
    }




    private static final class Benchmark
    {
        ListOfIntervalMatcher list;
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
            generateRanges();
            setupRanges();
        }

        public void run()
        {
            bitsMatchTime = measureMatch(bits);
            listMatchTime = measureMatch(list);
            bitsSearchTime = measureSearch(bits);
            listSearchTime = measureSearch(list);
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




        private void generateRanges()
        {
            // границы диапазонов будут здесь; потом их отсортируем
            int bounds[] = new int[ranges.getCount()*2];

            // заполним их случайными величинами
            for (int i = 0; i < bounds.length; i++)
                bounds[i] = random.nextInt(COUNT); //0..999

            Arrays.sort(bounds); // отсортируем

            for (int i = 0; i < ranges.getCount(); i++)
            {
                int lo = bounds[i*2 + 0];
                int hi = bounds[i*2 + 1];
                ranges.add(new Range(lo, hi, 1));
            }
        }




        private float measureSearch(DigitMatcher matcher)
        {
            long n1 = System.nanoTime();
            for (int i = 0; i < LOOP_COUNT; i++)
            {
                int v = (int)(random.nextFloat() * COUNT);
                matcher.getNext(v);
            }
            long n2 = System.nanoTime();

            return ((float)(n2 - n1)) / LOOP_COUNT;
        }
        private float measureMatch(DigitMatcher matcher)
        {
            long n1 = System.nanoTime();
            for (int i = 0; i < LOOP_COUNT; i++)
            {
                int v = (int)(random.nextFloat() * COUNT);
                matcher.match(v);
            }
            long n2 = System.nanoTime();

            return ((float)(n2 - n1)) / LOOP_COUNT;
        }


        private void setupRanges()
        {
            list = new ListOfIntervalMatcher(ranges.getCount());
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
