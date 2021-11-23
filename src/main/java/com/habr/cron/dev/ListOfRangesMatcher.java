package com.habr.cron.dev;

/**
 * Special matcher for milliseconds.
 * Handles a complex schedules, such as '40,100-120/2,200-300/3,500-501'.
 * Intervals MUST be ordered in ascending and not be intersects if they have a different step!
 * Used if a small set of ranges is specified (not over 8).
 *
 * Difficulty:
 *  matching one value - O(log n)
 *  find nearest value - O(log n)
 * Used memory:
 *  104 bytes maximum, 20 bytes minimum
 *
 *  Maximum loop are: 3 for search range.
 *
 * The concept was suggested by the user @mayorovp in
 * https://habr.com/ru/post/589667/comments/#comment_23717693
 */
class ListOfRangesMatcher implements DigitMatcher, MapMatcher
{
    /**
     * Arrays of intervals bounds
     */
    private final int[] min;
    private final int[] max;
    private final int[] step;

    private int low = -1; // minimal & maximal allowed values according the schedule
    private int high = 1001; // MUST initialized in finishRange()

    private static final int NOT_FOUND = -1; // index not existing interval
    private static final int FIRST = 0; // index of first interval
    private final int LAST; // index of last interval


    public ListOfRangesMatcher(int count)
    {
        LAST = count-1;
        min = new int[count];
        max = new int[count];
        step = new int[count];
    }


    /**
     * Trying to search the interval that contains a value
     *
     * @return index of interval or -1 (NOT_FOUND)
     */
    private int search(int value)
    {
        // use binary search algorithm
        int left = NOT_FOUND, right = LAST;
        while (left < right)
        {
            int i = (left + right + 1)/2; // probe this index

            if ( min[i] <= value )
                left = i;

            else
                right = i-1;
        }
        return left;
    }

    public boolean match(int value)
    {
        int index = search(value);

        return index != NOT_FOUND
                && value <= max[index]
                && (value - min[index]) % step[index] == 0;
    }


    public int getNext(int value)
    {
        int index = search(value);

        if ( index != NOT_FOUND && value < max[index] )
        {
            int s = step[index];
            int r = (value - min[index]) % s;
            return value - r + s;
        }

        if ( index < LAST )
            return min[index+1];

        return value+1; // this should not happen if you call hasNext() before
    }


    public int getPrev(int value)
    {
        int index = search(value);

        if ( index == NOT_FOUND )
            return value-1; // this should not happen if you call hasPrev() before

        if ( value > max[LAST] )
            return max[LAST];

        if ( value > min[index] )
        {
            int s = step[index];
            int r = (value - min[index]) % s;
            return r > 0 ? value - r : value - s;
        }

        // this should not happen if you call hasPrev() before
        return index > FIRST ? max[index-1] : value-1;
    }



    public boolean isAbove(int value)
    {
        return value > high;
    }

    public boolean isBelow(int value)
    {
        return value < low;
    }

    public boolean hasNext(int value)
    {
        return value < high;
    }

    public boolean hasPrev(int value)
    {
        return value > low;
    }

    public int getLow()
    {
        return low;
    }

    public int getHigh()
    {
        return high;
    }





    private int top = FIRST;

    /**
     * Intervals MUST be ordered in ascending and not be intersects!
     *
     * @param from begin value (include)
     * @param to end value (include)
     * @param dist distance between values
     */
    public void addRange(int from, int to, int dist)
    {
        assert top <= LAST; // overflow protected

        min[top] = from;
        max[top] = to - (to - from)%dist;
        step[top] = dist;
        top++;
    }

    public void finishRange()
    {
        low = min[FIRST];
        high = max[LAST];
    }

    public void addValue(int value)
    {
        throw new UnsupportedOperationException(); // a single constant not supported
    }
}
