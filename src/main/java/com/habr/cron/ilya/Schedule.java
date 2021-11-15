package com.habr.cron.ilya;

import com.habr.cron.CronBase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Реализация тестового задания от KaminskyIlya.
 * Среднее время поиска значения - 1,2 мксек (от 3400 до 4700 тактов)
 * Это много, поэтому делается попытка уменьшить расход тактов.
 *
 * Thread-Safe. Unmodifiable.
 */
public class Schedule extends CronBase
{
    private enum Direction { FORWARD, BACKWARD }
    private enum Equality { OR_EQUAL, NO_EQUAL }

    private static final class SearchMode
    {
        public final Direction direction;
        public final Equality equality;

        public SearchMode(Direction direction, Equality equality) {
            this.direction = direction;
            this.equality = equality;
        }

        public boolean toZero()
        {
            return direction == Direction.FORWARD;
        }

        public boolean canEqual()
        {
            return equality == Equality.OR_EQUAL;
        }
    }



    /**
     * Creates an empty instance that will match
     * the schedule as "*.*.* * *:*:*.*" (every 1 msec).
     */
    public Schedule() throws ScheduleFormatException
    {
        this("*.*.* * *:*:*.*");
    }

    /**
     * Creates instance for specified schedule defined by string.
     *
     * @param schedule see format in {@link CronBase}
     * @throws ScheduleFormatException
     */
    public Schedule(String schedule) throws ScheduleFormatException
    {
        this.schedule = schedule; // used in toString() method only; can be deleted

        Parser parser = new Parser();
        parser.parse(schedule);

        ScheduleModel model = parser.getScheduleModel();
        pool = new MatcherPool(model);

        weekDay = pool.getWeekDayMatcher();
    }

    public Date NearestEvent(Date d) {
        return findEvent(d, new SearchMode(Direction.FORWARD, Equality.OR_EQUAL));
    }

    public Date NearestPrevEvent(Date d) {
        return findEvent(d, new SearchMode(Direction.BACKWARD, Equality.OR_EQUAL));
    }

    public Date NextEvent(Date d) {
        return findEvent(d, new SearchMode(Direction.FORWARD, Equality.NO_EQUAL));
    }

    public Date PrevEvent(Date d) {
        return findEvent(d, new SearchMode(Direction.BACKWARD, Equality.NO_EQUAL));
    }








    private final ScheduleItemMatcher weekDay;
    private final MatcherPool pool; // pool of schedule's matchers





    /**
     * The main function of finding a date that meets the schedule and search mode.
     * It works for a maximum of 7 checks and 6 increments (if no days of the week are specified).
     *
     * @param time the start date to search
     * @param mode the search mode (direction and equality)
     * @return a date suitable for the conditions (may be equal to the original time if mode.equality == OR_EQUAL)
     * @throws IllegalStateException it is not possible to find a date that meets the schedule.
     * For example, when a schedule of the form is set "2020.01.02 10-20/2:*:*.*",
     * and it's 2021 and mode.direction == FORWARD.
     */
    private Date findEvent(Date time, SearchMode mode)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(time);
        CalendarDigits digits = new CalendarDigits(pool, calendar);

        // direct date search in the schedule from a year to milliseconds (maximum of 8 iterations)
        while ( isCanSearchDown(digits, mode) )
        {
            digits.next();
        }

        // we have almost found the date, it remains to check the compliance with the day of the week
        return fixWeekDay(digits, mode);
    }




    /**
     * Implements a direct search for the nearest date from a given date in the schedule.
     *
     * @param digits the digits of calendar
     * @param mode the search mode (direction and equality)
     * @return <b>false</b>, if you can not continue further and an unambiguous result is obtained.
     *         <b>true</b>, if you need to go down to a lower level
     *
     * @throws IllegalStateException if the current date is out of the range of acceptable values
     * and we have no more options that we could offer.
     */
    private boolean isCanSearchDown(CalendarDigits digits, SearchMode mode)
    {
        boolean toZero = mode.toZero();
        int value = digits.getValue();

        // the current value of the element is less than the allowed lower limit
        if ( isBelow(value, digits, mode) )
        {
            digits.initialize(toZero);
            return false;
        }

        // the current value of the element is above the allowed upper limit
        if ( isAbove(value, digits, mode) )
        {
            if ( digits.isFirst() )
                throw new IllegalArgumentException("Out of schedule interval");

            digits.prev();
            digits.increment(toZero);
            return false;
        }

        // the current value of the element within the bounds
        if ( digits.match(value) ) // if the item exactly matches the schedule
        {
            boolean isLast = digits.isLast();

            // we end the search if this is the last element and we are allowed to return the exact equivalent
            if ( isLast && mode.canEqual() ) return false;

            // if this is not the last level, let's go down
            if ( !isLast ) return true;
        }

        // the current value of the element within the bounds, but not match for schedule;
        // or it is milliseconds and we must return no equal value
        digits.increment(toZero);
        return false;
    }




    /**
     * Corrects the found date in accordance with the restrictions set by the acceptable days of the week.
     *
     * @param digits the digits of calendar
     * @return adjusted date
     * @throws IllegalStateException if a suitable date cannot be found in the schedule
     */
    private Date fixWeekDay(CalendarDigits digits, SearchMode mode)
    {
        Calendar calendar = digits.getCalendar();
        CalendarElement element = new CalendarElement(Calendar.DAY_OF_WEEK, calendar);

        while ( !weekDay.match( calendar.get(Calendar.DAY_OF_WEEK), element ) )
        {
            digits.gotoDay(); // let's start incrementing the date from the day of the month
            digits.increment(mode.toZero());
        }

        return digits.getAsDate();
    }




    /**
     * Checks whether the value of the calendar element is lower/higher (depending on the search direction)
     * from the allowed boundaries in the schedule.
     *
     * @param value the value of the element being checked
     * @param digit the digits of calendar
     * @param mode the search mode (direction)
     * @return true, if below
     */
    private boolean isBelow(int value, CalendarDigits digit, SearchMode mode)
    {
        return mode.toZero() ? digit.isBelow(value) : digit.isAbove(value);
    }

    /**
     * Checks whether the value of the calendar element is higher/lower (depending on the search direction)
     * from the allowed boundaries in the schedule.
     *
     * @param value the value of the element being checked
     * @param digit the digits of calendar
     * @param mode the search mode (direction)
     * @return true, if above
     */
    private boolean isAbove(int value, CalendarDigits digit, SearchMode mode)
    {
        return mode.toZero() ? digit.isAbove(value) : digit.isBelow(value);
    }


    private final String schedule;

    @Override
    public String toString() {
        return schedule;
    }
}
