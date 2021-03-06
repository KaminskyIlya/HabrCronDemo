package com.habr.cron.dev;

import static com.habr.cron.dev.ScheduleElements.*;

/**
 * Not thread safe. Stateful class.
 */
class CalendarDigits implements DigitMatcher
{
    private final DigitMatcher matchers[];
    private final GregCalendar calendar;

    private int current = FIRST;
    final boolean toZero;

    public CalendarDigits(MatcherPool pool, GregCalendar calendar, boolean resetMode)
    {
        DigitMatcher m[] = pool.getMatcherPool();
        DigitMatcher dayMatcher = m[DAY_OF_MONTH.ordinal()];
        LastDayOfMonthProxy proxy = new LastDayOfMonthProxy(dayMatcher, calendar);

        matchers = new DigitMatcher[]{
                m[YEAR.ordinal()],      // 0 = FIRST = YEAR_IDX
                m[MONTH.ordinal()],     // 1 = MONTH_IDX
                proxy,                  // 2 = DAY_IDX
                m[HOURS.ordinal()],     // 3 = HOURS_IDX
                m[MINUTES.ordinal()],   // 4
                m[SECONDS.ordinal()],   // 5
                m[MILLIS.ordinal()]     // 6 = LAST
        };
        this.calendar = calendar;
        this.toZero = resetMode;
    }

    private static final int FIRST = 0; // year matcher
    private static final int LAST = 6; // millis matcher = matchers.length - 1

    private static final int YEAR_IDX = 0;
    private static final int MONTH_IDX = 1;
    private static final int DAY_IDX = 2;
    private static final int HOURS_IDX = 3;

    private static final String OUT_MESSAGE = "Out of schedule interval";




    /**
     * go to a next matcher
     */
    public void next()
    {
        current++;
    }

    /**
     * go to a previous matcher
     *
     * @throws IllegalStateException if this digit was the first
     */
    public void prev()
    {
        if ( current-- < FIRST ) throw new IllegalStateException(OUT_MESSAGE);
    }

    /**
     * @return true, is this digit of the milliseconds
     */
    public boolean isLast()
    {
        return current == LAST;
    }

    public void gotoYear()
    {
        current = YEAR_IDX;
    }

    public void gotoMonth()
    {
        current = MONTH_IDX;
    }

    public void gotoDay()
    {
        current = DAY_IDX;
    }

    public void gotoHours()
    {
        current = HOURS_IDX;
    }

    public void gotoLastDigit()
    {
        current = LAST;
    }

    /**
     * @return a current value of active digit
     */
    public int getValue()
    {
        return calendar.getValue(current);
    }



    public boolean match(int value)
    {
        return matchers[current].match(value);
    }

    public boolean isAbove(int value)
    {
        return toZero ? matchers[current].isAbove(value) : matchers[current].isBelow(value);
    }

    public boolean isBelow(int value)
    {
        return toZero ? matchers[current].isBelow(value) : matchers[current].isAbove(value);
    }

    public int getLow()
    {
        return toZero ? matchers[current].getLow() : matchers[current].getHigh();
    }

    public int getHigh()
    {
        throw new AssertionError("This code MUST not be called");
    }

    public int getNext(int value)
    {
        return toZero ? matchers[current].getNext(value) : matchers[current].getPrev(value);
    }

    public boolean hasNext(int value)
    {
        return toZero ? matchers[current].hasNext(value) : matchers[current].hasPrev(value);
    }

    public int getPrev(int value)
    {
        throw new AssertionError("This code MUST not be called");
    }

    public boolean hasPrev(int value)
    {
        throw new AssertionError("This code MUST not be called");
    }






    /**
     * Reset all calendar digits to 'zero' according to the schedule, starts from this digit.
     *
     * @throws IllegalStateException if we can't implement this operation (schedule restriction)
     */
    public void increment()
    {
        resetOrIncrementDigits(false);
    }

    /**
     * Increments a current digit to next, and reset to 'zero' all minor digits.
     */
    public void initialize()
    {
        resetOrIncrementDigits(true);
    }



    /**
     * Resets only date to 'zero' (not a time) and may increment major digits, when
     * gets overflow in day of month.
     *
     * @throws IllegalStateException then we out of schedule (year out of range)
     */
    public void resetDate()
    {
        boolean init = true;

        do
        {
            if ( current != DAY_IDX )
            {
                if ( init )
                    calendar.setValue(current, getLow());

                else
                {
                    int prev = getValue();
                    if ( init = hasNext(prev) )
                        calendar.setValue(current, getNext(prev));
                }
            }
            else
                init = tryToSetupDayOfMonth(init);

            if ( init ) current++; else current--;
        }
        while ( YEAR_IDX <= current && current <= DAY_IDX  );


        if ( current < FIRST ) throw new IllegalStateException(OUT_MESSAGE); // out of schedule bounds (on top)
    }





    /**
     * Resets digits to 'zero' and may increment major digits, when
     * gets overflow in day of month.
     *
     * @param init reset to initial (true) or increment (false)?
     * @throws IllegalStateException then we out of schedule (year out of range)
     */
    private void resetOrIncrementDigits(boolean init)
    {
        do
        {
            if ( current != DAY_IDX )
            {
                if ( init )
                    calendar.setValue(current, getLow());

                else
                {
                    int prev = getValue();
                    if ( init = hasNext(prev) )
                        calendar.setValue(current, getNext(prev));
                }
            }
            else
                init = tryToSetupDayOfMonth(init);

            if ( init ) current++; else current--;
        }
        while ( FIRST <= current && current <= LAST);


        if ( current < FIRST ) throw new IllegalStateException(OUT_MESSAGE); // out of schedule bounds (on top)
    }

    /**
     * Try to sets the day of month to a `zero` or to a `next value`.
     * If `toZero` is true, and `init` is true - sets day of month to minimum.
     * If `toZero` is true, and `init` is false - increments day of month to a `next value`.
     * If `toZero` is false, and `init` is true - sets day of month to maximum.
     * If `toZero` is false, and `init` is false - decrements day of month to a `previous value`.
     *
     * @param init true, if need initialize; false - if need increment/decrement
     * @return true, if we can setup day
     */
    private boolean tryToSetupDayOfMonth(boolean init)
    {
        if ( init )
        {
            int initial = getLow();

            boolean correct = match(initial);

            if ( !correct )
            {
                correct = hasNext(initial);
                if ( correct )
                {
                    initial = getNext(initial);
                    correct = calendar.isCorrectDay(initial) && match(initial);
                }
            }

            if ( correct )
                calendar.day = initial;


            return correct;
        }
        else
        {
            if ( !hasNext(calendar.day) ) return false;

            int next = getNext(calendar.day);
            boolean correct = calendar.isCorrectDay(next) && match(next);

            if ( correct )
                calendar.day = next;

            return correct;
        }
    }
}
