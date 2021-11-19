package com.habr.cron.dev;

import java.util.Random;

/**
 * Сравниваем скорость работы матчеров: ListOfIntervalMatcher и BitMapMatcher
 * при одинаковых условиях работы.
 *
 * Предварительно: ListOfIntervalMatcher способен обогнать BitMapMatcher на определенных расписаниях.
 * TODO: Нужно выявить ситуации, при которых ListOfIntervalMatcher однозначно быстрее:
 *  - минимальное кол-во интервалов в списке
 *  - кол-во значений (процент покрытия)
 *  - разброс значений (в одной куче или расбросаны по всему диапазону)
 *  Нужно не только найти эти условия эмпирическим путём, но и придумать алгоритм их выявления.
 */
public class IntervalListVsBitMap
{
    private final static long SEED = 1L;
    private final static int LOOP_COUNT = 10000000;


    static final Range range = new Range(0, 1000);
    static ListOfIntervalMatcher list;
    static BitMapMatcher bits;
    static Random random;
    static RangeList ranges;


    public static void main(String args[]) throws Exception
    {
        random = new Random(SEED);

        // заполним оба чекера случаными расписаниями
        fillPredefinedIntervals();

        // покажем расчеты параметров
        showExpectedValues(range);

        // тест на проверку значения match
        matchBenchmarkTest("Проверка скорости выборки bitmap", bits);
        matchBenchmarkTest("Проверка скорости выборки list", list);

        // тест на поиск следующего значения next
        nextBenchmarkTest("Проверка скорости поиска bitmap", bits);
        nextBenchmarkTest("Проверка скорости поиска list", list);
    }

    private static void nextBenchmarkTest(String testName, DigitMatcher matcher)
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++)
        {
            int v = (int)(random.nextFloat() * range.max);
            matcher.getNext(v);
        }
        long n2 = System.nanoTime();

        float ns = ((float)(n2 - n1)) / LOOP_COUNT;
        System.out.println("--------------------");
        System.out.println(testName);
        System.out.println("Nanos = " + ns);
    }

    private static void matchBenchmarkTest(String testName, DigitMatcher matcher)
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++)
        {
            int v = (int)(random.nextFloat() * range.max);
            matcher.match(v);
        }
        long n2 = System.nanoTime();

        float ns = ((float)(n2 - n1)) / LOOP_COUNT;
        System.out.println("--------------------");
        System.out.println(testName);
        System.out.println("Nanos = " + ns);
    }




    private static void fillPredefinedIntervals()
    {
        ranges = new RangeList(2); // положим, что у нас только 4 интервала
        ranges.add(new Range(1, 500));
        ranges.add(new Range(601, 1000));
//        ranges.add(new Range(201, 299));
//        ranges.add(new Range(300, 400));
//        ranges.add(new Range(600, 700));
//        ranges.add(new Range(900, 1000));

        list = new ListOfIntervalMatcher(ranges.getCount());
        bits = new BitMapMatcher(range.min, range.max);

        setRanges(list, ranges);
        setRanges(bits, ranges);

        /*for (int i = 0; i < POINTS_COUNT; i++)
        {
            int v = (int)(random.nextFloat() * range.max);

            list.addRange(v, v, 1);
            bits.addRange(v, v, 1);
        }*/
    }

    private static void setRanges(MapMatcher matcher, RangeList ranges)
    {
        for (Range range : ranges)
        {
            matcher.addRange(range.min, range.max, range.step);
        }
        matcher.finishRange();
    }

    private static void showExpectedValues(Range range)
    {
        int c1 = countRealPoints(bits);
        int c2 = countRealPoints(list);

        if ( c1 != c2 ) throw new AssertionError();

        float mean = ((float)c1) / range.max;
        int loops = (int)(1f / mean);

        System.out.println("Диапазон: " + ranges);
        System.out.println("Кол-во точек: " + c1 + " (" + mean*100 + "%)");
        System.out.println("Среднее кол-во итераций цикла на поиск значения: " + loops);
        System.out.println("Количество диапазонов: " + ranges.getCount());
    }

    private static int countRealPoints(DigitMatcher matcher)
    {
        int count = 0;
        for (int i = range.min; i <= range.max; i++)
        {
            if ( matcher.match(i) ) count++;
        }
        return count;
    }
}
