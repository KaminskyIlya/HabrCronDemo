package com.habr.cron.ilya;

/**
 * A common interface for classes that check the compliance of a calendar digit
 * with a condition set for it.  The calendar digit can be: year, month, day of the month,
 * day of the week, hours, minutes, seconds, milliseconds.
 */
interface CalendarDigitMatcher
{
    /**
     * Match the current value by schedule for this calendar element.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     */
    boolean match(int value);

    /**
     * Checks whether the calendar element goes beyond the upper border.
     * For example, seconds should be in the range 1-10 by condition, but the current value is 12.
     * So this function will return true.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @return true, if above
     */
    boolean isAbove(int value);

    /**
     * Checks whether the calendar element goes beyond the lower border.
     * For example, the clock should be conditionally in the range 0-23, but the current value is -1.
     * So this function will return true.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @return true, if below
     */
    boolean isBelow(int value);

    /**
     * Checks whether the current value is out of bounds,
     * according of schedule for this calendar element.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @return true, if in bounds, and false, if overflowed
     */
    boolean inBounds(int value);

    /**
     * Returns a value, lager the current,
     * according to the step of changing the calendar item.
     *
     * For example, if we have "* /3" in schedule of 'days' (each 3-th day of month),
     * then after 5-th day returns 7-th, according to the series: 1,4,7,11,14...
     *
     * IMPORTANT: This function does not check for overflow
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @return nearest lager value
     */
    int getMajor(int value);

    /**
     * Returns a value, less then the current,
     * according to the step of changing the calendar item.
     *
     * For example, if we have "* /3" in schedule of 'days' (each 3-th day of month),
     * then after 6-th day returns 4-th, according to the series: 1,4,7,11,14...
     *
     * IMPORTANT: This function does not check for overflow
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @return nearest smaller value
     */
    int getMinor(int value);

    /**
     * @return the minimum value for this calendar element, according schedule and current date
     */
    int getLow();

    /**
     * @return the maximum value for this calendar element, according schedule and current date
     */
    int getHigh();
}
