package com.habr.cron.ilya;

/**
 * Matcher of calendar element for type value 'last day of month'.
 * Unmodified object. Thread-safe.
 *
 * Difficulty:
 *  matching one value - O(1)
 *  find nearest value - O(1)
 * Used memory:
 *  9 bytes
 */
class LastDayOfMonthMatcher extends MatcherBase
{
    public static final int LAST_DAY_OF_MONTH_CODE = 32;
    public static final int FEBRUARY_LAST_DAY = 28; // NOTE: Only for Gregorian calendar, see below
    public static final int FEBRUARY_LEAP_DAY = 29;

/*
    static
    {
        GregorianCalendar calendar = new GregorianCalendar();
        FEBRUARY_LAST_DAY = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
    }
*/

    public LastDayOfMonthMatcher()
    {
        super(LAST_DAY_OF_MONTH_CODE, LAST_DAY_OF_MONTH_CODE);
        setDynamicRange();
    }

    public boolean match(int value, CalendarElement element)
    {
        return value == getHigh(element);
    }

    public int getMajor(int value, CalendarElement element)
    {
        return LAST_DAY_OF_MONTH_CODE + 1; // returns always biggest possible value
    }

    public int getMinor(int value, CalendarElement element)
    {
        return -1; // return possible minimum of day for all months
    }

    public boolean isBelow(int value, CalendarElement calendar)
    {
        return value < getHigh(calendar);
    }

    @Override
    public int getLow(CalendarElement calendar)
    {
        return calendar.getMax(); // we have only one valid value: the last day of month
    }

    @Override
    public int getHigh(CalendarElement calendar)
    {
        return calendar.getMax(); // we have only one valid value: the last day of month
    }
}
