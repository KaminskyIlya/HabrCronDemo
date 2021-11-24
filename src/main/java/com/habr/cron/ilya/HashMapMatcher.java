package com.habr.cron.ilya;

import java.util.Arrays;

/**
 * Matcher of calendar element for list of ranges: a-b,c-d/n,e,f-g.
 *
 * Designed for checking months, days, hours, minutes, seconds.
 * The maximum range of acceptable values is 0-63.
 * It is used when the number of acceptable values is set by a small number of ranges.
 *
 * Difficulty:
 *  matching one value - O(1)
 *  find nearest value - O(1)
 * Used memory:
 *  128 bytes maximum, 10 bytes minimum
 */
class HashMapMatcher extends MatcherBase implements MapMatcher
{
    public static final int MAX_DISTANCE = 64;
    public static final int OVERFLOW_DISTANCE = Byte.MAX_VALUE; //127
    private static final byte NO_NEXT = Byte.MAX_VALUE; // 127
    private static final byte NO_PREV = -1;

    /**
     * Bitmap with valid values. Each restricted value has bit 1.
     */
    private long map;

    /**
     * Links of indexes for a values that follows next.
     * For example, if after 5-th day must be 7-th day, and after 7-th day must be 11-th,
     * then next[4] = next[5]= 7,  next[6] = next[7] = next[8] = next[9] = 11.
     * If there is no next value, then NO_NEXT is used.
     */
    private final byte[] next;
    /**
     *
     * The same table as the 'next', only in the opposite direction.
     * If there is no next value, then NO_PREV is used.
     */
    private final byte[] prev;



    public HashMapMatcher(int min, int max)
    {
        super(min, max);

        assert max > min && max - min <= MAX_DISTANCE;

        next = new byte[max - min + 1]; Arrays.fill(next, NO_NEXT);
        prev = new byte[max - min + 1]; Arrays.fill(prev, NO_PREV);
    }

    public void addRange(int from, int to, int step)
    {
        assert min <= from && to <= max;

        for (int offset = from - min; offset <= to - min; offset += step)
        {
            long bit = 1L << offset;

            map |= bit;
        }
    }

    public void addValue(int value)
    {
        assert min <= value && value <= max;

        long bit = 1L << (value - min);

        map |= bit;
    }

    public void finishRange()
    {
        byte p = NO_PREV;
        for (int i = min, j = 1; i < max; i++, j++)
        {
            boolean has = isAllowed(i);
            if ( has ) p = (byte) i;
            prev[j] = p;
        }

        byte n = NO_NEXT;
        for (int i = max, j = next.length-1; i >= min; i--, j--)
        {
            next[j] = n;
            boolean has = isAllowed(i);
            if ( has ) n = (byte) i;
        }
    }




    public boolean match(int value, CalendarElement element)
    {
        return inBounds(value, element) && isAllowed(value);
    }


    public int getMajor(int value, CalendarElement element)
    {
        if ( value >= max ) return NO_NEXT;
        if ( value < min ) return min;
        return next[value - min];
    }

    public int getMinor(int value, CalendarElement element)
    {
        if ( value <= min ) return NO_PREV;
        if ( value > max ) return max;
        return prev[value - min];
    }

    @Override
    public int getLow(CalendarElement element)
    {
        int x = super.getLow(element);
        return isAllowed(x) ? x : getMajor(x, element);
    }

    @Override
    public int getHigh(CalendarElement element)
    {
        int x = super.getHigh(element);
        return isAllowed(x) ? x : getMinor(x, element);
    }






    private boolean isAllowed(int value)
    {
        long bit = 1L << (value - min);

        return (map & bit) != 0;
    }

}
