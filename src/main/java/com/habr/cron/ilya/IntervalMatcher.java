package com.habr.cron.ilya;

/**
 * Matcher of calendar element for type values as 'range without step' (a-b)
 * and also 'asterisk'(*)
 * Unmodified object. Thread-safe.
 *
 * Difficulty:
 *  matching one value - O(1)
 *  find nearest value - O(1)
 * Used memory:
 *  9 bytes
 */
class IntervalMatcher extends MatcherBase
{
    public IntervalMatcher(int min, int max)
    {
        super(min, max);
    }

    public boolean match(int value, CalendarElement element)
    {
        return inBounds(value, element);
    }

    public int getMajor(int value, CalendarElement element)
    {
        return value + 1; // possible overflow
    }

    public int getMinor(int value, CalendarElement element)
    {
        return value - 1; // possible overflow
    }
}
