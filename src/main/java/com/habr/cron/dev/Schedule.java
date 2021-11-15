package com.habr.cron.dev;

import com.habr.cron.Cron;
import com.habr.cron.CronBase;

import java.util.Date;
import java.util.TimeZone;


/**
 * Optimized version of CronEx.
 * Thread-Safe. Unmodifiable.
 */
public class Schedule implements Cron
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
     * the schedule as "*.*.* * *:*:*.*" (every 1 ms).
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
        this.schedule = schedule;

        Parser parser = new Parser();
        parser.parse(schedule);

        ScheduleModel model = parser.getScheduleModel();
        pool = new MatcherPool(model);
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




    /**
     * Create instance for quick serial generation events.
     * Generator does not consume memory. Works faster. No memory leaks.
     * Generator non thread safe and mutable. Don't cache generator.
     *
     * @param start date to start serial
     * @param forward direction mode; true - is forward, false - is backward.
     * @return generator instance. Not thread safe.
     * The outside process must work with generator with synchronized instruction.
     */
    public ScheduleEventsGenerator getEventsGenerator(Date start, boolean forward)
    {
        SearchMode mode = new SearchMode(forward ? Direction.FORWARD : Direction.BACKWARD, Equality.NO_EQUAL);
        return new EventsGenerator(start, mode);
    }







    private final MatcherPool pool; // pool of schedule's matchers
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC"); // default work timezone

    /**
     * The main function of finding a date that meets the schedule and search mode.
     * It works for a maximum of 8 checks (if no days of the week are specified).
     * Days of the week can add a few more checks.
     *
     * @param date the start date of the search
     * @param mode the search mode (direction and severity)
     * @return suitable date for the conditions (can be equal to the original if mode.equality == OR_EQUAL)
     * @throws IllegalStateException it is not possible to find a date that meets the schedule,
     * for example, when a schedule of the form is set "20.01.02 10-20/2:*:*.*",
     * and now it's 2021 and mode.direction == FORWARD.
     */
    private Date findEvent(Date date, SearchMode mode)
    {
        GregCalendar calendar = new GregCalendar(date, UTC);
        CalendarDigits digits = new CalendarDigits(pool, calendar, mode.toZero());

        while ( isCanSearchDown(digits, calendar, mode.canEqual()) )
        {
            digits.next();
        }

        return fixWeekDay(digits, calendar);
    }


    /**
     * Makes events generator.
     * TODO: test it
     */
    private final class EventsGenerator implements ScheduleEventsGenerator
    {
        // mutable objects
        private final GregCalendar calendar;
        private final CalendarDigits digits;
        private Date date;

        public EventsGenerator(Date start, SearchMode mode)
        {
            calendar = new GregCalendar(start, UTC);
            digits = new CalendarDigits(pool, calendar, mode.toZero());

            while ( isCanSearchDown(digits, calendar, mode.canEqual()) )
            {
                digits.next();
            }

            date = fixWeekDay(digits, calendar);
        }

        public Date last()
        {
            return date;
        }

        public Date next()
        {
            digits.gotoLastDigit();
            digits.increment();
            return date = fixWeekDay(digits, calendar);
        }

        public String schedule()
        {
            return schedule;
        }

        @Override
        public String toString() {
            return schedule;
        }
    }

    /**
     * Implements a direct search for the nearest date from a given date in the schedule.
     *
     * @param digit digits of calendar
     * @return false, if you can not continue further and an unambiguous result is obtained
     *         true, if you need to go down to a lower level
     *
     * @throws IllegalStateException if the current date is out of the range of acceptable values
     * and we have no more options that we could offer.
     */
    private boolean isCanSearchDown(CalendarDigits digit, GregCalendar calendar, boolean canEqual)
    {
        int value = digit.getValue();

        if ( digit.isBelow(value) ) // the current value of the element is less than the allowed lower limit
        {
            digit.initialize();
            return false;
        }

        if ( digit.isAbove(value) ) // the current value of the element is above the allowed upper limit
        {
            digit.prev(); // for YEAR throws IllegalStateException
            digit.increment();
            return false;
        }

        // the current value is within the boundaries
        if ( digit.match(value) && calendar.isCorrect() ) // if the item matches the schedule
        {
            boolean isLast = digit.isLast();

            // we end the search if this is the last element and we are allowed to return the exact equivalent
            if ( isLast && canEqual ) return false;

            // if this is not the last level, let's go down below
            if ( !isLast ) return true;
        }

        digit.increment();
        return false; // search is complete
    }



    /**
     * Corrects the found date in accordance with the restrictions set by the acceptable days of the week.
     *
     * @param digits digits of calendar
     * @return fixed date
     * @throws IllegalStateException if a suitable date cannot be found in the schedule
     */
    private Date fixWeekDay(CalendarDigits digits, GregCalendar calendar)
    {
        DaysMap weekMap = pool.getWeekDaysMap();
        if ( !weekMap.isAsterisk() )
        {
            if ( !weekMap.contains(calendar.getDayOfWeek()) )
            {
                findBestDate(digits, calendar, weekMap);

                digits.gotoHours();
                digits.initialize();
            }
        }
        return calendar.asDate();
    }





    private void findBestDate(CalendarDigits digits, GregCalendar calendar, DaysMap weekMap)
    {
        do
        {
            // find a day in the month that corresponds to the schedule of days of the week
            if ( findBestDay(digits, calendar, weekMap) ) break;

            // find a month in the year that exactly contains at least one day that falls on the selected days of the week
            if ( !findBestMonth(digits, calendar, weekMap, false) )
            {
                // find the next year that exactly contains at least one day that falls on the selected days of the week
                findBestYear(digits, calendar, weekMap);

                // find a month in the next year that exactly contains at least one day that falls on the selected days of the week
                findBestMonth(digits, calendar, weekMap, true);
            }
            /*
            There may be a situation where findBestMonth has found a suitable month,
            but the resetDate() automatically moved to another month,
            because it takes into account the schedule and overflows.
            And in the new month there are no dates falling on the selected days of the week.
            Therefore, we are forced to repeat the cycle again.
            In other words, this cycle can be performed more than 2 times (in extremely rare cases).
            */
        }
        while ( true ); // until we get an IllegalStateException when searching for the year
    }


    private void findBestYear(CalendarDigits digits, GregCalendar calendar, DaysMap weekMap)
    {
        byte ly = pool.getLeapYearMap().getMap();
        byte ny = pool.getNormalYearMap().getMap();
        int year = calendar.year;
        byte ym;

        digits.gotoYear();
        do
        {
            if ( !digits.hasNext(year) )
                throw new IllegalStateException(); // we went beyond the schedule

            year = digits.getNext(year);
            byte yearMap = GregCalendar.isLeap(year) == 1 ? ly : ny;
            ym = DaysMap.rollMapByYear(yearMap, year);
        }
        while ( !weekMap.intersects(ym) );

        calendar.year = year;
        digits.gotoMonth();
        digits.resetDate(); // here sometimes it can be calendar.year != year
        // this is not a problem, because the findBestDate() will correct this in a loop
    }


    private boolean findBestMonth(CalendarDigits digits, GregCalendar calendar, DaysMap weekMap, boolean repeat)
    {
        boolean isLeap = calendar.isLeap();
        int month = calendar.month;

        byte janMap = DaysMap.rollMapByYear(pool.getMonthDaysMap().getMap(), calendar.year);
        byte mm = DaysMap.rollMapByMonth(janMap, month, isLeap);

        digits.gotoMonth();

        // the 'repeat' suppresses the check for 'intersects' in the first iteration of the loop
        // when we need to start the search from the current calendar value, repeat = false
        // when we need to start the search strictly from the beginning of the year, then repeat = true
        while ( !(repeat && weekMap.intersects(mm)) )
        {
            if ( !digits.hasNext(month) ) return false;

            month = digits.getNext(month);
            mm = DaysMap.rollMapByMonth(janMap, month, isLeap);

            repeat = true; // now you can do a check intersects
        }

        calendar.month = month;
        digits.gotoDay();
        digits.resetDate(); // here sometimes it can be calendar.month != month
        return true; // this is not a problem, because the findBestDate() will correct this in a loop
    }


    private boolean findBestDay(CalendarDigits digits, GregCalendar calendar, DaysMap weekMap)
    {
        int day = calendar.day;
        byte dm = (byte)(1 << calendar.getDayOfWeek());

        digits.gotoDay();

        while ( !weekMap.intersects(dm) )
        {
            if ( !digits.hasNext(day) ) return false;

            int next = digits.getNext(day);
            int shift;
            if ( digits.toZero )
            {
                shift = next - day;
            }
            else
            {
                shift = 7 - (day - next) % 7; // reverse to roll '>>'
            }
            dm = DaysMap.rollWeekMap(dm, shift);
            day = next;
        }

        calendar.day = day;
        return true;
    }











    private final String schedule;

    @Override
    public String toString() {
        return schedule;
    }

}
