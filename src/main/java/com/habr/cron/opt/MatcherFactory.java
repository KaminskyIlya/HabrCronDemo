package com.habr.cron.opt;

/**
 * Helper class.
 * Help to select the best map matcher.
 */
public class MatcherFactory
{
    /**
     * Help to select the best matcher for specified ranges list.
     * Constructs and initializes matcher.
     *
     * @param ranges source ranges for initialize
     * @param element schedule element for which the matcher is being created
     * @return the best instance that uses the source ranges.
     */
    public static DigitMatcher createInstance(RangeList ranges, ScheduleElements element)
    {
        return ranges.isAlone() ?
                    createSimpleMatcher(ranges.getSingle(), element)
                :
                    createMapMatcher(ranges, element);
    }



    private static DigitMatcher createSimpleMatcher(Range range, ScheduleElements element)
    {
        if ( range.isAsterisk() ) // * or */n
            return !range.isStepped() ?
                    new IntervalMatcher(element.min, element.max)
                    :
                    new SteppingMatcher(element.min, element.max, range.step);

        if ( range.isConstant() ) // single const value
            return new ConstantMatcher(range.min);

        return range.isStepped() ? // interval
                new SteppingMatcher(range.min, range.max, range.step)
                :
                new IntervalMatcher(range.min, range.max);
    }



    private static DigitMatcher createMapMatcher(RangeList ranges, ScheduleElements element)
    {
        int min = ranges.getMinimum();
        int max = ranges.getMaximum();

        boolean large = (max - min) > HashMapMatcher.RANGE_LIMIT;
        boolean overflow = (element.max - element.min) >= Byte.MAX_VALUE;

        // for small ranges we can use simple hashMap
        if ( !(large || overflow) )
        {
            HashMapMatcher hash = new HashMapMatcher(min, max);
            setRanges(hash, ranges);
            return hash;
        }

        // if ranges have complexity - we use bit map
        if ( !ranges.isSimpleRanges() )
        {
            BitMapMatcher bits = new BitMapMatcher(min, max);
            setRanges(bits, ranges);
            return bits;
        }

        // sorts ranges and combines overlapped
        ranges.optimize();

        // after optimize we can get only one range (for example: '10-20,15-30' = '10-30')
        if ( ranges.isAlone() )
            return createSimpleMatcher(ranges.getSingle(), element);


        //
        // Ok. Now we will choose one of two: bits or list.
        //
        BitMapMatcher bits = new BitMapMatcher(min, max);
        setRanges(bits, ranges);

        // When count of ranges over than 8 the bits map will always better
        if ( ranges.getCount() > 8 ) return bits;

        // different intervals with and without steps requires different algo
        DigitMatcher list = ranges.isSimpleIntervals() ?
                new ListOfIntervalsMatcher(ranges.getCount())
            :
                new ListOfRangesMatcher(ranges.getCount());

        setRanges((MapMatcher) list, ranges);

        // run real speed benchmarks
        return selectBestMatcher(bits, list);
    }


    private static void setRanges(MapMatcher matcher, RangeList ranges)
    {
        for (Range range : ranges)
        {
            matcher.addRange(range.min, range.max, range.step);
        }
        matcher.finishRange();
    }


    private static DigitMatcher selectBestMatcher(DigitMatcher bits, DigitMatcher list)
    {
        float listMatchTime = measureMatch(list);
        float listSearchTime = measureSearch(list);
        float bitsMatchTime = measureMatch(bits);
        float bitsSearchTime = measureSearch(bits);

        float matchRatio = bitsMatchTime / listMatchTime;
        float searchRatio = bitsSearchTime / listSearchTime;
        float ratio = matchRatio * searchRatio;

        return ratio > 1 ? list : bits;
    }


    private static float measureSearch(DigitMatcher matcher)
    {
        int low = matcher.getLow();
        int high = matcher.getHigh();

        long n1 = System.nanoTime();
        for (int v = low; v <= high; v++)   matcher.getNext(v);
        long n2 = System.nanoTime();

        return ((float)(n2 - n1)) / (high - low + 1);
    }

    private static float measureMatch(DigitMatcher matcher)
    {
        int low = matcher.getLow();
        int high = matcher.getHigh();

        long n1 = System.nanoTime();
        for (int v = low; v <= high; v++)   matcher.match(v);
        long n2 = System.nanoTime();

        return ((float)(n2 - n1)) / (high - low + 1);
    }
}
