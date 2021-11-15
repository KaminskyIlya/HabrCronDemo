package com.habr.cron.dev;

/**
 * Proxy-matcher for days, then in condition used single magic day ',32,'
 * (makes proxy for 'HashMapMatcher')
 * Unmodified object. Thread-safe.
 */
class LastDayOfMonthProxy implements DigitMatcher
{
    private final DigitMatcher matcher;
    private final GregCalendar calendar;

    public LastDayOfMonthProxy(DigitMatcher matcher, GregCalendar calendar)
    {
        this.matcher = matcher;
        this.calendar = calendar;
    }

    public boolean match(int value)
    {
        int min = matcher.getLow();
        int max = matcher.getHigh();

        if ( min < max && max < 29 )
            return matcher.match(value);


        int actualMax = calendar.getMaxDay();

        if ( min == max && max == 32 ) // it's a magic day
        {
            return value == actualMax;
        }
        else  // it's a standard range, at may be magic day in last
        {
            max = Math.min(actualMax, max);

            return min <= value && value <= max && matcher.match(value);
        }
    }

    public int getNext(int value)
    {
        int actualMax = calendar.getMaxDay();
        if ( value < getHigh() ) // value in range 1..27
        {
            int next = matcher.getNext(value); // can return overflow (> actualMax)
            // we return actual value of 'last day' (29,30,31), or next value in range 2..28
            return next < actualMax ? next : actualMax;
        }
        else // value in range 28..31
            return 32; // return overflow (for any month)
    }

    public int getPrev(int value)
    {
        int actualMax = calendar.getMaxDay();
        if ( value > getLow() )
        {
            int prev = matcher.getPrev(value);
            return prev < actualMax ? prev : actualMax;
        }
        else 
            return -1;
    }

    public boolean hasNext(int value)
    {
        return value < getHigh();
    }
    
    public boolean hasPrev(int value)
    {
        return value > getLow();
    }

    public int getLow()
    {
        return Math.min(calendar.getMaxDay(), matcher.getLow());
    }

    public int getHigh()
    {
        return Math.min(calendar.getMaxDay(), matcher.getHigh());
    }

    public boolean isAbove(int value)
    {
        return value > getHigh();
    }

    public boolean isBelow(int value)
    {
        return value < getLow();
    }

    public int getLow(GregCalendar calendar)
    {
        return Math.min(matcher.getLow(), calendar.getMaxDay());
    }

    public int getHigh(GregCalendar calendar)
    {
        return Math.min(matcher.getHigh(), calendar.getMaxDay());
    }
}