package com.habr.cron.ilya;

import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class ConstantMatcherTest
{
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");


    @Test
    public void testMatch() throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(20);

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.HOUR_OF_DAY, calendar);

        assertFalse(matcher.match(21, element));
        assertTrue(matcher.match(20, element));
    }

    /*
     * Test special case for February leap day
     */
    @Test
    public void testMatch_FebruaryLeapDay(/*Range range, int value, CalendarElement element, boolean expected*/) throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(29);
        matcher.setDynamicRange();

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);

        calendar.setTime(df.parse("2021.02.01"));
        assertFalse(matcher.match(29, element));

        calendar.setTime(df.parse("2020.02.29"));
        assertTrue(matcher.match(29, element));
    }

    /*
     * Test dynamic range for day of month
     */
    @Test
    public void testMatch_MonthOverflow() throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(31);
        matcher.setDynamicRange();

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);

        calendar.setTime(df.parse("2021.04.01"));
        assertFalse(matcher.match(31, element));

        calendar.setTime(df.parse("2020.05.01"));
        assertTrue(matcher.match(31, element));
    }




    @Test
    public void testGetMajor() throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(20);

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.MINUTE, calendar);

        assertEquals(matcher.getMajor(1, element), 20);
        assertEquals(matcher.getMajor(19, element), 20);
        assertEquals(matcher.getMajor(20, element), 21);
        assertEquals(matcher.getMajor(22, element), 23);
    }




    @Test
    public void testGetMinor() throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(20);

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.MINUTE, calendar);

        assertEquals(matcher.getMinor(19, element), 18);
        assertEquals(matcher.getMinor(20, element), 19);
        assertEquals(matcher.getMinor(21, element), 20);
        assertEquals(matcher.getMinor(22, element), 20);
    }


    @Test
    public void testIsAbove() throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(20);

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.SECOND, calendar);

        assertTrue(matcher.isAbove(21, element));
        assertFalse(matcher.isAbove(20, element));
        assertFalse(matcher.isAbove(19, element));
    }


    @Test
    public void testIsBelow() throws Exception
    {
        ConstantMatcher matcher = new ConstantMatcher(20);

        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(Calendar.SECOND, calendar);

        assertTrue(matcher.isBelow(19, element));
        assertFalse(matcher.isBelow(20, element));
        assertFalse(matcher.isBelow(21, element));
    }
}