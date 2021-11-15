package com.habr.cron.ilya;

import java.util.Calendar;

/**
 * Java's calendar element.
 */
class CalendarElement
{
    private final int fieldId;

    private final Calendar calendar;

    public CalendarElement(int fieldId, Calendar calendar)
    {
        this.fieldId = fieldId;
        this.calendar = calendar;
    }

    public int getMax()
    {
        return calendar.getActualMaximum(fieldId);
    }

    public int getMin()
    {
        return calendar.getActualMinimum(fieldId);
    }

    public int getValue()
    {
        return calendar.get(fieldId);
    }

    public void setValue(int value)
    {
        calendar.set(fieldId, value);
    }

/*
    public int getLeastMax()
    {
        return calendar.getLeastMaximum(fieldId);
    }
*/
}
