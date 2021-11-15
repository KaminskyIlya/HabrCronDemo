package com.habr.cron.dev;

/**
 * Special matcher for days of week.
 * Used optimal algorithm.
 *
 * Difficulty:
 *  matching one value - O(1)
 *  find nearest value - O(n)
 * Used memory:
 *  9 bytes
 */
@Deprecated // Not need; replaced by DaysMap
class WeekDaysMatcher implements MapMatcher
{
    private int min = -1; // first valid day of week
    private int max = 7; // last valid day of week
    private byte mask = 0; // mask of valid days in week


    public boolean match(int value)
    {
        return (mask & (byte)(1 << (value%7))) != 0;
    }

    public boolean isAbove(int value)
    {
        return value > max;
    }

    public boolean isBelow(int value)
    {
        return value < min;
    }

    public int getNext(int value)
    {
        value++;
        for ( byte b = (byte)(1 << value); value <= 6; value++, b <<= 1)
        {
            if ( (mask & b) != 0 ) break;
        }
        return value;
    }

    public int getPrev(int value)
    {
        value--;
        for ( byte b = (byte)(1 << value); value >= 0; value--, b >>= 1)
        {
            if ( (mask & b) != 0 ) break;
        }
        return value;
    }

    public boolean hasNext(int value)
    {
        return value < max;
    }

    public boolean hasPrev(int value)
    {
        return value > min;
    }

    public int getLow()
    {
        return min;
    }

    public int getHigh()
    {
        return max;
    }


    public byte getMask()
    {
        return mask;
    }




    /**
     * Adds a values range in schedule map.
     * Ranges can overlap without problems.
     *
     * @param from begin value (include), 0..6
     * @param to end value (include), 0..6
     * @param step between values, 1..6
     */
    public void addRange(int from, int to, int step)
    {
        for (int i = from; i <= to; i += step)
        {
            mask |= (byte)(1 << i);
        }
    }

    /**
     * Utility function for complete initialization of map.
     */
    public void finishRange()
    {
        byte b = 1;

        for (int i = 0; i < ScheduleElements.DAY_OF_WEEK.max; i++)
        {
            if ( (mask & b) != 0 )
            {
                if ( min < 0 ) min = i;
                max = i;
            }
        }
    }

    /**
     * Adds a single value to the bit map.
     *
     * @param value to add in, 0..6
     */
    public void addValue(int value)
    {
        mask |= (byte)(1 << value);
    }
}
