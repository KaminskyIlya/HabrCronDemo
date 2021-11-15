package com.habr.cron.psy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class ScheduleInterval
{
    private final int begin;
    private final int end;

    public int getBegin()
    {
        return begin;
    }

    public int getEnd()
    {
        return end;
    }

    private final Set<Integer> _allowedPoints;
    private final Map<Integer, Integer> _nextPointCache;
    private final Map<Integer, Integer> _previousPointCache;

    public ScheduleInterval(int begin, int end)
    {
        this.begin = begin;
        this.end = end;

        _allowedPoints = new TreeSet<Integer>();
        _nextPointCache = new HashMap<Integer, Integer>();
        _previousPointCache = new HashMap<Integer, Integer>();
    }

    public boolean IsPointAllowed(int point)
    {
        return _allowedPoints.contains(point);
    }

    public int NextPoint(int point)
    {
        Integer nextValue = _nextPointCache.get(point);
        if ( nextValue == null )
        {
            nextValue = NextPointUncached(point);
            _nextPointCache.put(point, nextValue);

        }
        return nextValue;
    }

    private int NextPointUncached(int point)
    {
        if (point >= end)
        {
            return point + 1;
        }
        point++;
        while ( !_allowedPoints.contains(point) && point <= end )
            point++;

        return point;
    }

    public void ChangePointAllowance(int point, boolean value)
    {
        if ( value )
            _allowedPoints.add(point);

        else
            _allowedPoints.remove(point);
    }

    public static ScheduleInterval CreateAllowedInterval(int begin, int end)
    {
        ScheduleInterval result = new ScheduleInterval(begin, end);
        for (int i = begin; i <= end; i++)
        {
            result._allowedPoints.add(i);
        }

        return result;
    }
}
