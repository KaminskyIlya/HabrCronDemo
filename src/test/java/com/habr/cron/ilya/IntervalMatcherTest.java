package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class IntervalMatcherTest
{
    @Test(dataProvider = "matchDataProvider")
    public void testMatch(Range range, int value, boolean expected) throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(range.min, range.max);
        boolean actual = matcher.match(value, null);
        assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] matchDataProvider()
    {
        return new Object[][] {
                {new Range(5, 63),    5,      true},
                {new Range(5, 63),    6,      true},
                {new Range(5, 63),   63,      true},
                {new Range(5, 63),    4,      false},
                {new Range(5, 63),   64,      false},
        };
    }

    @Test
    public void testGetMajor() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(1, 5);
        assertEquals(matcher.getMajor(1, null), 2);
        assertEquals(matcher.getMajor(5, null), 6);
        assertEquals(matcher.getMajor(0, null), 1);
    }

    @Test
    public void testGetMinor() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(1, 5);
        assertEquals(matcher.getMinor(1, null), 0);
        assertEquals(matcher.getMinor(5, null), 4);
        assertEquals(matcher.getMinor(0, null), -1);
    }

    @Test
    public void testIsAbove() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(6, 8);
        assertTrue(matcher.isAbove(9, null));
        assertFalse(matcher.isAbove(8, null));
        assertFalse(matcher.isAbove(7, null));
        assertFalse(matcher.isAbove(6, null));
        assertFalse(matcher.isAbove(5, null));
    }

    @Test
    public void testIsBelow() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(6, 8);
        assertTrue(matcher.isBelow(5, null));
        assertFalse(matcher.isBelow(6, null));
        assertFalse(matcher.isBelow(7, null));
        assertFalse(matcher.isBelow(8, null));
        assertFalse(matcher.isBelow(9, null));
    }


    @Test
    public void testInBounds() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(1, 30);
        assertTrue(matcher.inBounds(6, null));
        assertTrue(matcher.inBounds(7, null));
        assertTrue(matcher.inBounds(30, null));

        // But for february in dynamic range
        matcher.setDynamicRange();
        CalendarElement element = createElement(Calendar.DAY_OF_MONTH, "2021.02.01");
        assertTrue(matcher.inBounds(28, element));
        assertFalse(matcher.inBounds(29, element)); // not a leap year
    }


    @Test
    public void testGetHigh() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(1, 30);
        matcher.setDynamicRange();

        CalendarElement element = createElement(Calendar.DAY_OF_MONTH, "2021.02.01");
        assertEquals(matcher.getHigh(element), 28);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertEquals(matcher.getHigh(element), 30);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.05.01");
        assertEquals(matcher.getHigh(element), 30);


        matcher = new IntervalMatcher(1, 31);
        matcher.setDynamicRange();

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertEquals(matcher.getHigh(element), 30);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.05.01");
        assertEquals(matcher.getHigh(element), 31);
    }

    @Test
    public void testGetLow() throws Exception
    {
        IntervalMatcher matcher = new IntervalMatcher(1, 30);
        assertEquals(matcher.getLow(null), 1);
    }




    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");

    private CalendarElement createElement(int field, String moment) throws ParseException
    {
        Calendar calendar = new GregorianCalendar();
        CalendarElement element = new CalendarElement(field, calendar);
        calendar.setTime(df.parse(moment));
        return element;
    }

}