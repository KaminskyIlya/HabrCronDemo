package com.habr.cron.ilya;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.habr.cron.ilya.ScheduleElements.*;

/**
 * Virtual digits of the date.
 * Non thread-safe. Don't caching. Internal using only.
 */
class CalendarDigits implements CalendarDigitMatcher
{
    /**
     * Matcher's pool
     */
    private final ScheduleItemMatcher[] pool;
    /**
     * Java Calendar engine (Gregorian calendar)
     */
    private final Calendar calendar;
    /**
     * The parts of calendar for each digit.
     */
    private final CalendarElement calendarElements[];
    /**
     * Current position of active digit
     */
    private int current = 0;


    /**
     * Create digits for current date
     *
     * @param poolStorage source of matcher's pool
     * @param calendar current date through calendar engine
     */
    public CalendarDigits(MatcherPool poolStorage, Calendar calendar)
    {
        this.pool = poolStorage.getMatcherPool();
        this.calendar = calendar;
        this.calendarElements = new CalendarElement[DIGIT.length];
        initCalendarElements();
    }










    private static final ScheduleElements[] DIGIT = // array of calendar's digits
            new ScheduleElements[]{
                    YEAR, MONTH, DAY_OF_MONTH,
                    HOURS, MINUTES, SECONDS, MILLIS
            };
    private static final int DAY_DIGIT_INDEX = 2; // index in DIGIT array

    private void initCalendarElements()
    {
        assert calendar != null;

        for (int i = 0; i < DIGIT.length; i++)
        {
            calendarElements[i] = new CalendarElement(DIGIT[i].fieldId, calendar);
        }
    }



    /**
     * go to a next matcher
     *
     * @throws IllegalStateException if this digit was the last
     */
    public void next()
    {
        current++;
        if ( current >= DIGIT.length ) throw new IllegalStateException();
    }

    /**
     * go to a previous matcher
     *
     * @throws IllegalStateException if this digit was the first
     */
    public void prev()
    {
        current--;
        if (current < 0) throw new IllegalStateException();
    }

    /**
     * go to matcher for days
     */
    public void gotoDay()
    {
        current = DAY_DIGIT_INDEX;
    }

    /**
     * Go to specified digit.
     * Makes the matcher for specified calendar's element as current
     *
     * @param element type of the digit
     * @throws IllegalArgumentException when element is wrong (for example, DAY_OF_WEEK)
     */
    public void gotoDigit(ScheduleElements element)
    {
        current = indexOf(element);
    }

    private int indexOf(ScheduleElements element)
    {
        for (int i = 0; i < DIGIT.length; i++)
        {
            if (DIGIT[i] == element) return i;
        }
        throw new IllegalArgumentException();
    }

    int index() // package only, for tests
    {
        return current;
    }

    /**
     * @return true, is this digit of the milliseconds
     */
    public boolean isLast()
    {
        return current == DIGIT.length-1;
    }

    /**
     * @return true, is this digit of the year
     */
    public boolean isFirst()
    {
        return current == 0;
    }

    /**
     * @return true, is this digit of the day of month
     */
    public boolean isDay()
    {
        return current == DAY_DIGIT_INDEX;
    }

    /**
     * @return true, if this digit is month or year
     */
    public boolean isBeforeDay()
    {
        return current < DAY_DIGIT_INDEX;
    }

    /**
     * @return a current value of active digit
     */
    public int getValue()
    {
        return element().getValue();
    }

    /**
     * sets the new value of active digit
     *
     * @param value
     */
    public void setValue(int value)
    {
        element().setValue(value);
    }

    /**
     * Reset all calendar digits to 'zero' according to the schedule, starts from this digit.
     *
     * @param toZero true, if need reset to minimum; false, if need reset to maximum
     * @throws IllegalStateException if we can't implement this operation (schedule restriction)
     */
    public void initialize(boolean toZero)
    {
        resetOrIncrementDigits(toZero, true);
    }

    /**
     * Increments a current digit to next, and reset to 'zero' all minor digits.
     * TODO: нужны тесты
     * @param toZero true, if need reset to minimum; false, if need reset to maximum
     */
    public void increment(boolean toZero)
    {
        resetOrIncrementDigits(toZero, false);
    }

    /**
     * Clears day for protect to overflow due month or year setup.
     */
    private void clearDay()
    {
        int c = current;
        gotoDay();
        element().setValue(element().getMin());
        current = c;
    }

    /**
     * Resets digits to 'zero' and may increment major digits, when
     * gets overflow in day of month.
     *
     * @param toZero to minimum (true) or maximum (false)?
     * @param init reset to initial (true) or increment (false)?
     * @throws IllegalStateException then we out of schedule (year out of range)
     */
    private void resetOrIncrementDigits(boolean toZero, boolean init)
    {
        while ( true )
        {
            if ( !isDay() )
            {
                if ( init )
                {
                    if ( isBeforeDay() ) clearDay(); // protect day in month overflow
                    setValue(toZero ? getLow() : getHigh());
                    if ( isLast() ) break;
                }
                else
                {
                    int value = getValue();
                    value = toZero ? getMajor(value) : getMinor(value);

                    if ( init = inBounds(value) )
                    {
                        if ( isBeforeDay() ) clearDay(); // protect day in month overflow
                        setValue(value);
                        if ( isLast() ) break;
                    }
                }
            }
            else
                init = tryToSetupDayOfMonth(toZero, init);

            if ( init ) next(); else prev();
        }
    }

    /**
     * Try to sets the day of month to a `zero` or to a `next value`.
     * If toZero is true, and init is true - sets day of month to minimum.
     * If toZero is true, and init is false - increments day of month to a `next value`.
     * If toZero is false, and init is true - sets day of month to maximum.
     * If toZero is false, and init is false - decrements day of month to a `previous value`.
     *
     * @param toZero direction of initialization or scroll (minimal/maximal, next/prev)
     * @param init true, if need initialize; false - if need increment/decrement
     * @return true, if we can setup day
     */
    private boolean tryToSetupDayOfMonth(boolean toZero, boolean init)
    {
        if ( toZero )
        {
            int value = init ? getLow() : getMajor(getValue()); // start reset from minimum
            while ( !match(value) )
            {
                value = getMajor(value);
                if ( !inBounds(value) ) return false;
            }
            setValue(value);
            return true;
        }
        else
        {
            int value = init ? getHigh() : getMinor(getValue()); // start reset from maximum
            while ( !match(value) )
            {
                value = getMinor(value);
                if ( !inBounds(value) ) return false;
            }
            setValue(value);
            return true;
        }
    }







    /**
     * @return matcher for calendar element of current processing
     */
    private ScheduleItemMatcher matcher()
    {
        return pool[ DIGIT[current].ordinal() ];
    }

    /**
     * @return calendar's element for current digit
     */
    private CalendarElement element()
    {
        return calendarElements[current];
    }




    public boolean match(int value)
    {
        return matcher().match(value, element());
    }

    public boolean isAbove(int value)
    {
        return matcher().isAbove(value, element());
    }

    public boolean isBelow(int value)
    {
        return matcher().isBelow(value, element());
    }

    public boolean inBounds(int value)
    {
        return matcher().inBounds(value, element());
    }

    public int getMajor(int value)
    {
        return matcher().getMajor(value, element());
    }

    public int getMinor(int value)
    {
        return matcher().getMinor(value, element());
    }

    public int getLow()
    {
        return matcher().getLow(element());
    }

    public int getHigh()
    {
        return matcher().getHigh(element());
    }

    public Date getAsDate()
    {
        return calendar.getTime();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

    @Override
    public String toString()
    {
        return df.format(getAsDate());
    }
}
