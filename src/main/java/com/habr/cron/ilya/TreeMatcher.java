package com.habr.cron.ilya;

import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * NOTE: This was a concept of class. Used in benchmark only.
 * Works slowly than BitMapMatcher
 *
 * Matcher of calendar element for list of ranges: a-b,c-d/n,e,f-g.
 *
 * Designed for checking milliseconds and years (elements who has long ranges).
 * It is used when the number of acceptable values is set by a small number of ranges,
 * and the percentage of coverage of the entire range is less than 20% (each 5-th).
 *
 * Difficulty:
 *  matching one value - O(log N)
 *  find nearest value - O(log N)
 * Used memory:
 *  about 20 bytes per element.
 * Unmodified object. Thread-safe.
 */
@Deprecated
public class TreeMatcher extends MatcherBase implements MapMatcher
{
    private final NavigableSet<Integer> set;

    public TreeMatcher(int min, int max)
    {
        super(min, max);
        set = new TreeSet<Integer>();
    }

    public void addRange(int from, int to, int step)
    {
        for (int i = from; i <= to; i += step)
        {
            set.add(i);
        }
    }

    public void finishRange() {
        // nothing to do
    }

    public void addValue(int value)
    {
        set.add(value);
    }

    public boolean match(int value, CalendarElement element)
    {
        return inBounds(value, element) && set.contains(value);
    }

    public int getMajor(int value, CalendarElement element)
    {
        Integer next = set.higher(value);
        return next != null ? next : max + 1;
    }

    public int getMinor(int value, CalendarElement element)
    {
        Integer prev = set.lower(value);
        return prev != null ? prev : min - 1;
    }

    @Override
    public int getLow(CalendarElement element)
    {
        int value = Math.max(min, super.getLow(element));
        return set.contains(value) ? value : getMajor(value, element);
    }

    @Override
    public int getHigh(CalendarElement element)
    {
        int value = Math.min(max, super.getHigh(element));
        return set.contains(value) ? value : getMinor(value, element);
    }
}
