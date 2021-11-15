package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class SteppingMatcherTest
{

    @Test(expectedExceptions = {AssertionError.class})
    public void testConstructWithSingleStepMustAssertionError() throws Exception
    {
        ScheduleItemMatcher matcher = new SteppingMatcher(1, 1, 1);
    }

    @Test(dataProvider = "matchDataProvider")
    public void testMatch(Range range, int value, boolean expected) throws Exception
    {
        ScheduleItemMatcher matcher = new SteppingMatcher(range.min, range.max, range.step);
        boolean actual = matcher.match(value, null);
        assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] matchDataProvider()
    {
        return new Object[][] {
            {new Range(5, 63, 3),   5,      true},
            {new Range(5, 63, 3),   6,      false},
            {new Range(5, 63, 3),   7,      false},
            {new Range(5, 63, 3),   8,      true},
            {new Range(5, 63, 3),   9,      false},
            {new Range(5, 63, 3),  10,      false},
            {new Range(5, 63, 3),  11,      true},
            {new Range(5, 63, 3),  62,      true},
            {new Range(5, 63, 3),  63,      false},
            {new Range(5, 63, 3),  64,      false},
            {new Range(5, 63, 3),  65,      false}, // overflow above
            {new Range(5, 63, 3),   0,      false}, // overflow below
        };
    }

    @Test(dataProvider = "majorDataProvider")
    public void testGetMajor(Range range, int value, int expected) throws Exception
    {
        ScheduleItemMatcher matcher = new SteppingMatcher(range.min, range.max, range.step);
        int next = matcher.getMajor(value, null);
        assertEquals(next, expected);
    }

    @DataProvider
    private Object[][] majorDataProvider()
    {
        return new Object[][] {
            {new Range(5, 63, 3),   -1,     5}, // not happens in production code
            {new Range(5, 63, 3),   4,      5}, //
            {new Range(5, 63, 3),   0,      5}, //
            {new Range(5, 63, 3),   5,      8},
            {new Range(5, 63, 3),   6,      8},
            {new Range(5, 63, 3),   7,      8},
            {new Range(5, 63, 3),   8,      11},
            {new Range(5, 63, 3),   9,      11},
            {new Range(5, 63, 3),   10,     11},
            {new Range(5, 63, 3),   63,     65}, // correct for this method (although overflowed)
            {new Range(5, 63, 3),   65,     68}, // correct for this method (although overflowed)
        };
    }

    @Test(dataProvider = "minorDataProvider")
    public void testGetMinor(Range range, int value, int expected) throws Exception
    {
        ScheduleItemMatcher matcher = new SteppingMatcher(range.min, range.max, range.step);
        int next = matcher.getMinor(value, null);
        assertEquals(next, expected);
    }

    @DataProvider
    private Object[][] minorDataProvider()
    {
        return new Object[][] {
            {new Range(5, 63, 3),   -1,    -2}, // not happens in production code
            {new Range(5, 63, 3),   3,      2}, // not happens in production code
            {new Range(5, 63, 3),   4,      3}, // not happens in production code
            {new Range(5, 63, 3),   5,      2},
            {new Range(5, 63, 3),   6,      5},
            {new Range(5, 63, 3),   7,      5},
            {new Range(5, 63, 3),   8,      5},
            {new Range(5, 63, 3),   9,      8},
            {new Range(5, 63, 3),   10,     8},
            {new Range(5, 63, 3),   11,     8},
            {new Range(5, 63, 3),   12,     11},
            {new Range(5, 63, 3),   13,     11},
            {new Range(5, 63, 3),   14,     11},
            {new Range(5, 63, 3),   15,     14},
            {new Range(5, 63, 3),   63,     62},
            {new Range(5, 63, 3),   64,     62}, // not happens in production code
            {new Range(5, 63, 3),   65,     62}, // not happens in production code
            {new Range(5, 63, 3),   66,     65}, // not happens in production code
        };
    }



    @Test
    public void testGetHigh() throws Exception
    {
        SteppingMatcher matcher = new SteppingMatcher(1, 30, 2);
        matcher.setDynamicRange();

        CalendarElement element = createElement(Calendar.DAY_OF_MONTH, "2021.02.01");
        assertEquals(matcher.getHigh(element), 27); // can be 29, but is February

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertEquals(matcher.getHigh(element), 29);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.05.01");
        assertEquals(matcher.getHigh(element), 29);


        matcher = new SteppingMatcher(1, 31, 2);
        matcher.setDynamicRange();

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertEquals(matcher.getHigh(element), 29);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.05.01");
        assertEquals(matcher.getHigh(element), 31);
    }

    @Test
    public void testGetLow() throws Exception
    {
        SteppingMatcher matcher = new SteppingMatcher(1, 30, 2);
        assertEquals(matcher.getLow(null), 1);

        matcher = new SteppingMatcher(5, 30, 2);
        assertEquals(matcher.getLow(null), 5);
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