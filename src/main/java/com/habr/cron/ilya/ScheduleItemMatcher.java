package com.habr.cron.ilya;

/**
 * A common interface for classes that check the compliance of a calendar element
 * with a condition set for it.  The calendar element condition can be: asterisk,
 * asterisk with step, constant value, range, stepping range, and a list of of the last three.
 * For each type of element, its own instance of the class is created
 * that implements this interface in the best way.
 */
interface ScheduleItemMatcher
{
    /**
     * Used for calendar elements, who has floating bounds (days of months, week of months, ...)
     */
    void setDynamicRange();

    /**
     * Match the current value by schedule for this calendar element.
     * And also check's bounds.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @param element info of a current calendar element
     */
    boolean match(int value, CalendarElement element);

    /**
     * Checks whether the calendar element goes beyond the upper border.
     * For example, seconds should be in the range 1-10 by condition, but the current value is 12.
     * So this function will return true.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @param element info of a current calendar element
     * @return true, if above
     */
    boolean isAbove(int value, CalendarElement element);

    /**
     * Checks whether the calendar element goes beyond the lower border.
     * For example, the clock should be conditionally in the range 0-23, but the current value is -1.
     * So this function will return true.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @param element info of a current calendar element
     * @return true, if below
     */
    boolean isBelow(int value, CalendarElement element);

    /**
     * Checks whether the current value is out of bounds,
     * according of schedule for this calendar element.
     *
     * @param value the calendar element value (year, month, ... hour,...)
     * @param element info of a current calendar element
     * @return true, if in bounds, and false, if overflowed
     */
    boolean inBounds(int value, CalendarElement element);

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
     * @param element info of a current calendar element
     * @return nearest lager value
     */
    int getMajor(int value, CalendarElement element);

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
     * @param element info of a current calendar element
     * @return nearest smaller value
     */
    int getMinor(int value, CalendarElement element);

    /**
     * @param element info of a current calendar element
     * @return the manimum value for this calendar element, according schedule
     */
    int getLow(CalendarElement element);

    /**
     * @param element info of a current calendar element
     * @return the maximum value for this calendar element, according schedule
     */
    int getHigh(CalendarElement element);
}
