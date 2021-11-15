package com.habr.cron.ilya;

import java.util.GregorianCalendar;

import static com.habr.cron.ilya.ScheduleElements.*;

/**
 * Builder of calendar elements' matcher's set.
 */
class MatcherPool
{
    private final ScheduleItemMatcher pool[] = new ScheduleItemMatcher[8];

    public MatcherPool(ScheduleModel model) throws ScheduleFormatException
    {
        // create matcher's for schedule model
        for ( ScheduleElements element : ScheduleElements.values() )
        {
            RangeList ranges = model.getModelFor(element);
            ScheduleItemMatcher matcher = createMatcherFor(ranges, element);

            if ( element == DAY_OF_MONTH )
            {
                matcher = fixMatcherForSpecialDay(ranges, matcher); // fix for 'magic' number
                matcher.setDynamicRange();
            }

            pool[element.ordinal()] = matcher;
        }


        try {
            fixYearsForLastFebruaryDay(model); // fix schedule for "???.02.29"
        }
        catch (IllegalStateException e)
        {
            throw new ScheduleFormatException(e.getMessage(), model.toString());
        }
    }

    public ScheduleItemMatcher[] getMatcherPool()
    {
        return pool;
    }

    public ScheduleItemMatcher getWeekDayMatcher()
    {
        return pool[DAY_OF_WEEK.ordinal()];
    }








    private ScheduleItemMatcher createMatcherFor(RangeList ranges, ScheduleElements element)
    {
        return ranges.isAlone() ?
                createSingleRange(ranges.getSingle(), element) // for schedule: 'a-b/n' or 'a', 'a-b'
                :
                createMultiRange(ranges); // for schedule: 'a,b-c,d,e-f/g,...'
    }


    private ScheduleItemMatcher createSingleRange(Range range, ScheduleElements element)
    {
        if ( range.isAsterisk() )
            return !range.isStepped() ?
                    new IntervalMatcher(element.javaMin, element.javaMax)
                    :
                    new SteppingMatcher(element.javaMin, element.javaMax, range.step);

        // if this special value of 'last day of month'
        if ( element == DAY_OF_MONTH && range.isLastDay() )
            return new LastDayOfMonthMatcher(); // special matcher for 'magic day'

        if ( range.isConstant() ) // single const value
            return new ConstantMatcher(range.min);

        return range.isStepped() ? // interval
                new SteppingMatcher(range.min, range.max, range.step)
                :
                new IntervalMatcher(range.min, range.max);
    }



    private ScheduleItemMatcher createMultiRange(RangeList ranges)
    {
        ScheduleItemMatcher matcher = getBestMatcherFor(ranges);
        MapMatcher map = (MapMatcher )matcher;

        for (Range range : ranges)
            map.addRange(range.min, range.max, range.step);

        map.finishRange();
        return matcher;
    }


    private ScheduleItemMatcher getBestMatcherFor(RangeList ranges)
    {
        int min = ranges.getMinimum();
        int max = ranges.getMaximum();

        boolean large = (max - min) > 64;
        return large ? new BitMapMatcher(min, max) : new HashMapMatcher(min, max);
    }





    /**
     * Fix standard matcher, if we have special day of month in schedule (as one of value of list).
     *
     * @param ranges model of schedule of day
     * @param matcher source matcher for proxy
     * @return proxy-matcher
     */
    private ScheduleItemMatcher fixMatcherForSpecialDay(RangeList ranges, ScheduleItemMatcher matcher)
    {
        if ( ranges.isList() )
        {
            for (Range range : ranges)
            {
                // user specified range with 'magic' value in list ',?-32,'
                // or specified single 'magic' value in list  ,32,
                if ( range.isByLastDay() )
                {
                    return new LastDayOfMonthProxy(matcher);
                }
            }
        }
        return matcher;
    }


    /**
     * Checks special schedule situation: User want only 29-th day in February.
     * This condition forces us to search only for leap years.
     * Leave only leap years in model.
     */
    private void fixYearsForLastFebruaryDay(ScheduleModel model)
    {
        RangeList monthRanges = model.getModelFor(MONTH);
        RangeList dayRanges = model.getModelFor(DAY_OF_MONTH);

        if ( monthRanges.isAlone() & dayRanges.isAlone() )
        {
            Range m = monthRanges.getSingle();
            Range d = dayRanges.getSingle();

            if ( m.isFebruary() && d.isLeapDay() ) // schedule is ?.2.29 ?
            {
                ScheduleItemMatcher matcher = pool[YEAR.ordinal()]; // current year matcher, created by schedule
                pool[YEAR.ordinal()] = applyLeapYearsForLastFebruaryDay(matcher);

            }
        }
    }

    /**
     * Setup only leaps years constraints, if user write ?.02.29 schedule (only Feb, and only 29)
     *
     * @param planned current year matcher
     * @return new schedule for year
     * @throws IllegalStateException
     */
    private ScheduleItemMatcher applyLeapYearsForLastFebruaryDay(ScheduleItemMatcher planned)
    {
        int min = findNearestLeapYearFrom(YEAR.min, +1); // for 2000 it returns 2000; for 2001 - 2004
        int max = findNearestLeapYearFrom(YEAR.max, -1); // for 2100 it returns 2096

        BitMapMatcher filtered = new BitMapMatcher(min, max);
        int count = 0;
        for (int year = min; year <= max; year += 4) // intersect all leap years with source schedule
            if ( planned.match(year, null) )
            {
                filtered.addValue(year);
                count++;
            }

        if ( count == 0 )
            throw new IllegalStateException("The schedule specifies only a leap day (02.29), " +
                    "but there is no selected year is a leap year. " +
                    "The schedule does not contain any events.");

        return filtered;
    }


    /**
     * Find nearest or current leap year for 'start' (includes 'start').
     *
     * For example, for (2021, true) returns 2024,
     * for (2021, false) returns 2020.
     *
     * @param year to start search
     * @param v search direction (+1 - forward, -1 - backward)
     * @return start, if it's leap, of next leap year
     */
    private int findNearestLeapYearFrom(int year, int v)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        while ( !calendar.isLeapYear(year) )
            year += v;

        return year;
    }
}
