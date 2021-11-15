package com.habr.cron.ilya;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class CalendarDigitsTest
{
    private final ScheduleModel model;

    public CalendarDigitsTest() throws ScheduleFormatException
    {
        model = new ScheduleModel();
        model.setModelFor(ScheduleElements.HOURS, RangeList.ASTERISK);
        model.setModelFor(ScheduleElements.MINUTES, RangeList.ASTERISK);
        model.setModelFor(ScheduleElements.SECONDS, RangeList.ASTERISK);
        model.initDefaults();
        model.check("*:*:*");
        model.fixup();
    }

    private CalendarDigits createDigits() throws ScheduleFormatException
    {
        return new CalendarDigits(new MatcherPool(model), new GregorianCalendar());
    }

    private CalendarDigits digits;

    @BeforeMethod
    public void setUp() throws Exception
    {
        digits = createDigits();
    }

    @Test
    public void testGetAsDate() throws Exception
    {
        MatcherPool pool = new MatcherPool(model);
        Calendar calendar = new GregorianCalendar();
        Date now = new Date();
        calendar.setTime(now);
        CalendarDigits digits = new CalendarDigits(pool, calendar);

        assertEquals(digits.getAsDate(), now);
    }

    @Test
    public void testToString() throws Exception
    {
        MatcherPool pool = new MatcherPool(model);

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 9);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 49);
        calendar.set(Calendar.SECOND, 34);
        calendar.set(Calendar.MILLISECOND, 476);

        CalendarDigits digits = new CalendarDigits(pool, calendar);

        assertEquals(digits.toString(), "2021.10.20 17:49:34.476");
    }

    @Test
    public void testNext() throws Exception
    {
        digits.next();
        digits.next();
        assertTrue(digits.isDay());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNextInLast_MustThrowException() throws Exception
    {
        digits.gotoDigit(ScheduleElements.MILLIS);
        digits.next();
    }

    @Test
    public void testPrev() throws Exception
    {
        digits.gotoDigit(ScheduleElements.HOURS);
        digits.prev();
        assertTrue(digits.isDay());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testPrevInFirst_MustThrowException() throws Exception
    {
        digits.gotoDigit(ScheduleElements.YEAR);
        digits.prev();
    }

    @Test
    public void testGotoDay() throws Exception
    {
        digits.gotoDay();
        assertTrue(digits.isDay());
    }

    @Test
    public void testGotoDigit() throws Exception
    {
        digits.gotoDigit(ScheduleElements.DAY_OF_MONTH);
        assertTrue(digits.isDay());

        digits.gotoDigit(ScheduleElements.YEAR);
        assertTrue(digits.isFirst());

        digits.gotoDigit(ScheduleElements.MILLIS);
        assertTrue(digits.isLast());
    }

    @Test
    public void testIsLast() throws Exception
    {
        digits.gotoDigit(ScheduleElements.MILLIS);
        assertTrue(digits.isLast());
        digits.prev();
        assertFalse(digits.isLast());
        digits.next();
        assertTrue(digits.isLast());
    }

    @Test
    public void testIsFirst() throws Exception
    {
        assertTrue(digits.isFirst());
        digits.next();
        assertFalse(digits.isFirst());
        digits.prev();
        assertTrue(digits.isFirst());
    }

    @Test
    public void testIsDay() throws Exception
    {
        digits.gotoDigit(ScheduleElements.DAY_OF_MONTH);
        assertTrue(digits.isDay());
    }
}