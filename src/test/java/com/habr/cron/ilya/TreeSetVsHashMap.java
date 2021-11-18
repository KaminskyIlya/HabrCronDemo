package com.habr.cron.ilya;


import java.util.Random;


/**
 * Сравниваем скорость работы матчеров: TreeMatcher и HashMapMatcher
 * при одинаковых условиях работы.
 * По результатам теста: HashMap уделывает TreeMap: 13ns/53ns по всем тестам
 */
public class TreeSetVsHashMap
{
    private final static int POINTS_COUNT = 40; //не более 40 (не более 67%)
    private final static long SEED = 1L;
    private final static int LOOP_COUNT = 10000000; // число циклов для замера


    static final Range range = new Range(0, 60);
    static TreeMatcher tree;
    static HashMapMatcher hash;
    static Random random;


    public static void main(String args[]) throws Exception
    {
        random = new Random(SEED);

        tree = new TreeMatcher(range.min, range.max);
        hash = new HashMapMatcher(range.min, range.max);

        // заполним оба чекера случаными расписаниями
        fillRandomPoints();

        // покажем расчеты параметров
        showExpectedValues(range);

        // тест на проверку значения match
        matchBenchmarkTest("Проверка скорости выборки hashMap", hash);
        matchBenchmarkTest("Проверка скорости выборки treeSet", tree);

        // тест на поиск следующего значения next
        majorBenchmarkTest("Проверка скорости поиска hashMap", hash);
        majorBenchmarkTest("Проверка скорости поиска treeSet", tree);
    }

    private static void majorBenchmarkTest(String testName, ScheduleItemMatcher matcher)
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++)
        {
            int v = (int)(random.nextFloat() * range.max);
            matcher.getMajor(v, null);
        }
        long n2 = System.nanoTime();

        float ns = ((float)(n2 - n1)) / LOOP_COUNT;
        System.out.println("--------------------");
        System.out.println(testName);
        System.out.println("Nanos = " + ns);
    }

    private static void matchBenchmarkTest(String testName, ScheduleItemMatcher matcher)
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++)
        {
            int v = (int)(random.nextFloat() * range.max);
            matcher.match(v, null);
        }
        long n2 = System.nanoTime();

        float ns = ((float)(n2 - n1)) / LOOP_COUNT;
        System.out.println("--------------------");
        System.out.println(testName);
        System.out.println("Nanos = " + ns);
    }




    private static void fillRandomPoints()
    {
        for (int i = 0; i < POINTS_COUNT; i++)
        {
            int v = (int)(random.nextFloat() * range.max);

            tree.addRange(v, v, 1);
            hash.addRange(v, v, 1);
        }
        hash.finishRange();
    }

    private static void showExpectedValues(Range range)
    {
        int c1 = countRealPoints(hash);
        int c2 = countRealPoints(tree);

        if ( c1 != c2 ) throw new AssertionError();

        float mean = ((float)c1) / range.max;

        int loops = (int)(1f / mean);
        int nodes = (int)(Math.log(c1) / Math.log(2));

        System.out.println("Длина диапазона: " + range);
        System.out.println("Кол-во точек: " + c1 + " (" + mean*100 + "%)");
        System.out.println("Среднее кол-во итераций цикла на поиск значения: " + loops);
        System.out.println("Максимальное кол-во узлов дерева на поиск значения: " + nodes);
    }

    private static int countRealPoints(ScheduleItemMatcher matcher)
    {
        int count = 0;
        for (int i = range.min; i <= range.max; i++)
        {
            if ( matcher.match(i, null) ) count++;
        }
        return count;
    }
}
