package com.habr.cron.dev;

import com.habr.cron.dev.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * NOTE: This was a concept of class. Used in benchmark only.
 * The concept was suggested by the user @mayorovp in https://habr.com/ru/post/589667/comments/#comment_23717693
 *
 * Интервалы пока без шага (т.е простые 10-20). Полагается, что они в расписании идут упорядоченными
 * и не пересекаются.
 *
 */
class ListOfIntervalMatcher implements DigitMatcher, com.habr.cron.dev.MapMatcher
{
    /**
     * Arrays of intervals
     */
    private final int[] min;
    private final int[] max;

    private int low = -1; // minimal & maximal allowed values according the schedule
    private int high = 1001; // MUST initialized in finishRange()

    private static final int NOT_FOUND = -1; // index not existing interval
    private static final int FIRST = 0; // index of first interval
    private final int LAST; // index of last interval


    public ListOfIntervalMatcher(int count)
    {
        LAST = count-1;
        min = new int[count];
        max = new int[count];
    }


    /**
     * Trying to search the interval that contains a value
     *
     * @return index of interval or -1 (NOT_FOUND), if not found
     */
    private int search(int value)
    {
        // use binary search algorithm
        int left = NOT_FOUND, right = LAST;
        while (left < right)
        {
            int i = (left + right + 1)/2; // probe this index

            if ( min[i] <= value )
                left = i;

            else
                right = i-1;
        }
        return left;
    }

    public boolean match(int value)
    {
        int index = search(value);
        return index != NOT_FOUND && value <= max[index];
    }

    public boolean isAbove(int value)
    {
        return value > high;
    }

    public boolean isBelow(int value)
    {
        return value < low;
    }

    public int getNext(int value)
    {
        int index = search(value);

        if ( index != NOT_FOUND && value < max[index] )
            return value + 1;

        if ( index < LAST )
            return min[index+1];

        throw new AssertionError("This code MUST not be reachable!");
    }

    public int getPrev(int value)
    {
        int index = search(value);

        if ( index != NOT_FOUND && value > min[index] )
            return value - 1;

        if ( index > FIRST )
            return min[index-1];

        throw new AssertionError("This code MUST not be reachable!");
    }

    public boolean hasNext(int value)
    {
        return value < high;
    }

    public boolean hasPrev(int value)
    {
        return value > low;
    }

    public int getLow()
    {
        return low;
    }

    public int getHigh()
    {
        return high;
    }




    private int top = FIRST;

    /**
     * Пока полагаем, что интервалы будут упорядочены и не пересекаться, а step всегда = 1.
     *
     * @param from begin value (include)
     * @param to end value (include)
     * @param step between values
     */
    public void addRange(int from, int to, int step)
    {
        assert top <= LAST; // защита от переполнения числа интервалов (ведь мы же используем голые массивы)

        min[top] = from;
        max[top] = to;
        top++;
    }

    public void finishRange()
    {
        low = min[FIRST];
        high = max[LAST];
    }

    public void addValue(int value)
    {
        throw new NotImplementedException(); // пока одиночные интервалы не поддерживаем
    }
}
