package com.habr.cron.ilya;


import java.util.Calendar;

import static com.habr.cron.ilya.LastDayOfMonthMatcher.LAST_DAY_OF_MONTH_CODE;
import static com.habr.cron.ilya.LastDayOfMonthMatcher.FEBRUARY_LAST_DAY;
import static com.habr.cron.ilya.LastDayOfMonthMatcher.FEBRUARY_LEAP_DAY;

/**
 * Range of valid values.
 * Simple element of schedule (any expression between comma).
 */
final class Range implements Comparable<Range>
{
    public static final Range ASTERISK = new Range();

    int min = -1;
    int max = -1;
    int step = 1;
    boolean asterisk = false;

    /**
     * Create asterisk range '*'
     */
    public Range()
    {
        this.asterisk = true;
    }

    /**
     * Create asterisk range '* /n'
     */
    public Range(int step, boolean dummy)
    {
        this.step = step;
        this.asterisk = true;
    }

    /**
     * Create range with step: 'a-b/n'
     */
    public Range(int min, int max, int step)
    {
        this.min = min;
        this.max = max;
        this.step = step;
    }

    /**
     * Create range without step: 'a-b'
     */
    public Range(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    /**
     * Create single value range 'x'
     */
    public Range(int value)
    {
        this.min = value;
        this.max = value;
    }




    public boolean isAsterisk() {
        return asterisk;
    }

    public boolean isStepped() {
        return step > 1;
    }

    public int getValue()
    {
        assert isConstant() && step == 1;
        return min;
    }

    /**
     * @return true, if range is single value 'x'
     */
    public boolean isConstant()
    {
        return min == max && !asterisk;
    }

    /**
     * @return true, if range a single 'magic' value for day
     */
    public boolean isLastDay()
    {
        return isConstant() && max == LAST_DAY_OF_MONTH_CODE;
    }

    /**
     * @return true, if range is single 'leap' day of year
     */
    public boolean isLeapDay()
    {
        return isConstant() && max == FEBRUARY_LEAP_DAY;
    }

    /**
     * @return true, if range is single february
     */
    public boolean isFebruary()
    {
        return isConstant() && max == Calendar.FEBRUARY+1;
    }

    /**
     * @return true, if range is 'x-32'
     */
    public boolean isByLastDay()
    {
        return min != max && max == LAST_DAY_OF_MONTH_CODE;
    }

    /**
     * Do shift range by a value
     */
    public void shiftBy(int value)
    {
        min += value; max += value;
    }


    @Override
    public String toString()
    {
        if ( isAsterisk() )
            return isStepped() ? "*/" + step : "*";

        if ( isConstant() ) return Integer.toString(min);

        return isStepped() ? min + "-" + max + "/" + step : min + "-" + max;
    }

    public int compareTo(Range o)
    {
        if ( max < o.min ) return -1; // exactly left       xxx  ooo
        if ( min > o.max ) return 1; // exactly right       ooo  xxx

        if ( min < o.min && max < o.max ) return -1; // intersect in left   xxoxoo
        if ( min > o.min && max > o.max ) return 1; // intersect in right   ooxoxx

        // min >= o.min && max <= o.max
        // min < o.min && max > o.max
        return 0; // included in 'o', or contains 'o'
    }
}
