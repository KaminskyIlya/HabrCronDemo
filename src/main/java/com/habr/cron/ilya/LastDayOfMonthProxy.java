package com.habr.cron.ilya;

import static com.habr.cron.ilya.LastDayOfMonthMatcher.LAST_DAY_OF_MONTH_CODE;
import static com.habr.cron.ilya.ScheduleElements.DAY_OF_MONTH;

/**
 * Proxy-matcher for days, then in condition used single magic day ',32,'
 * (makes proxy for 'HashMapMatcher')
 * Unmodified object. Thread-safe.
 */
class LastDayOfMonthProxy extends MatcherBase
{
    private final ScheduleItemMatcher matcher;

    public LastDayOfMonthProxy(ScheduleItemMatcher matcher)
    {
        super(DAY_OF_MONTH.min, DAY_OF_MONTH.max);
        this.matcher = matcher;
        this.setDynamicRange();
        matcher.setDynamicRange();
    }

    public boolean match(int value, CalendarElement element)
    {
        int actualMax = getHigh(element);
        // makes standard check, if it's not last day; else check for 'last day'
        return value < actualMax ? matcher.match(value, element) : element.getMax() == value;

    }

    public int getMajor(int value, CalendarElement element)
    {
        int actualMax = getHigh(element);
        if ( value < actualMax ) // value in range 1..27
        {
            int next = matcher.getMajor(value, element); // can return overflow (> actualMax)
            // we return actual value of 'last day' (29,30,31), or next value in range 2..28
            return next < actualMax ? next : actualMax;
        }
        else // value in range 28..31
            return LAST_DAY_OF_MONTH_CODE; // return overflow (for any month)
    }

    public int getMinor(int value, CalendarElement element)
    {
        return matcher.getMinor(value, element);
    }

}
