package com.habr.cron.ilya;

/**
 * Matcher of calendar element for type values as 'range with step' (a-b/n)
 * and also 'asterisk with step' (* / n)
 * Unmodified object. Thread-safe.
 *
 * Difficulty:
 *  matching one value - O(1)
 *  find nearest value - O(1)
 * Used memory:
 *  13 bytes
 */
class SteppingMatcher extends MatcherBase
{
    private final int step;

    public SteppingMatcher(int start, int stop, int step)
    {
        super(start, stop);
        assert step > 1;
        this.step = step;
    }

    public boolean match(int value, CalendarElement element)
    {
        boolean match = (value - min) % step == 0;
        return match && inBounds(value, element);
    }

    public int getMajor(int value, CalendarElement element)
    {
        int r = (value - min) % step;
        return value >= min ? value - r + step : min;
    }

    public int getMinor(int value, CalendarElement element)
    {
        if ( value < min ) return value - 1;
        int r = (value - min) % step;
        return r > 0 ? value - r : value - step;
    }

    @Override
    public int getLow(CalendarElement calendar)
    {
        int x = Math.max(min, super.getLow(calendar)) - min;
        return x - x % step + min;
    }

    @Override
    public int getHigh(CalendarElement calendar)
    {
        int x = Math.min(max, super.getHigh(calendar)) - min;
        return x - x % step + min;
    }
}
