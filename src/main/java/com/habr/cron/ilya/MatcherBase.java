package com.habr.cron.ilya;

/**
 * Base class of all matcher's.
 */
abstract class MatcherBase implements ScheduleItemMatcher
{
    private boolean dynamic = false;

    protected final int min;
    protected final int max;

    public MatcherBase(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    public void setDynamicRange()
    {
        dynamic = true;
    }

    public boolean isAbove(int value, CalendarElement calendar)
    {
        return value > getHigh(calendar);
    }

    public boolean isBelow(int value, CalendarElement calendar)
    {
        return value < min;
    }

    public boolean inBounds(int value, CalendarElement calendar)
    {
        return min <= value && value <= getHigh(calendar);
    }

    public int getLow(CalendarElement calendar)
    {
        return min;
    }

    public int getHigh(CalendarElement calendar)
    {
        return !dynamic ? max : Math.min(max, calendar.getMax());
    }
}
