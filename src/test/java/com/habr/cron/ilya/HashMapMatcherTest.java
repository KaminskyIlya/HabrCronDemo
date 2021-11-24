package com.habr.cron.ilya;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.habr.cron.ilya.HashMapMatcher.NO_NEXT_VALUE;
import static com.habr.cron.ilya.HashMapMatcher.NO_PREV_VALUE;
import static org.testng.Assert.*;

/**
 * Test for HashMapMatcher
 */
public class HashMapMatcherTest
{
    private HashMapMatcher matcher;

    @BeforeMethod
    public void setUp() throws Exception
    {
        // Setup schedule as "1,2,4,7-17/3,20-24/5,27"
        matcher = new HashMapMatcher(1, 27); // IMPORTANT: 1 - minimal available value
        matcher.addRange(1,2,1);    // 1-2/1 = 1,2
        matcher.addRange(4,4,1);    // 4
        matcher.addRange(7,17,3);   // 7-17/3 = 7,10,13,16
        matcher.addRange(20,24,5);  // 20-24/5 = 20
        matcher.addRange(27,27,1);  // 27
        matcher.finishRange();
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testConstructWithDiapasonOverflow_MustThrowAssertionError() throws Exception
    {
        HashMapMatcher matcher = new HashMapMatcher(1, 1000);
    }

    @Test
    public void testAddValue() throws Exception
    {
        HashMapMatcher matcher = new HashMapMatcher(1, 5);
        matcher.addValue(4);
        matcher.finishRange();
        assertTrue(matcher.match(4, null));
        assertFalse(matcher.match(1, null));
        assertFalse(matcher.match(5, null));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testAddValueOutOfBounds_MustThrowAssertionError() throws Exception {
        HashMapMatcher matcher = new HashMapMatcher(1, 5);
        matcher.addValue(6);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testAddRangeBelowTheBorder_MustThrowAssertionError() throws Exception {
        HashMapMatcher matcher = new HashMapMatcher(1, 5);
        matcher.addRange(-1, 5, 1);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testAddRangeAboveTheBorder_MustThrowAssertionError() throws Exception {
        HashMapMatcher matcher = new HashMapMatcher(1, 5);
        matcher.addRange(1, 6, 1);
    }

    @Test(dataProvider = "matchData")
    public void testMatch(int value, boolean expected) throws Exception
    {
        boolean actual = matcher.match(value, null);
        assertEquals(actual, expected);
    }
    @DataProvider(name = "matchData")
    public Object[][] matchDataProvider()
    {
        // value, expected result
        return new Object[][]{
                {-3, false},
                {1, true},
                {2, true},
                {3, false},
                {4, true},
                {7, true},
                {8, false},
                {10, true},
                {13, true},
                {16, true},
                {17, false},
                {19, false},
                {20, true},
                {21, false},
                {27, true},
                {28, false},
                {1000, false},
        };
    }


    @Test
    public void testMatchForDynamicRange() throws Exception
    {
        HashMapMatcher matcher = new HashMapMatcher(27, 31);
        matcher.addRange(27, 31, 1);
        matcher.finishRange();
        matcher.setDynamicRange();

        CalendarElement element = createElement(Calendar.DAY_OF_MONTH, "2021.02.01"); // non-leap year
        assertFalse(matcher.match(26, element)); // out of range
        assertTrue(matcher.match(27, element)); // in range, and valid day
        assertTrue(matcher.match(28, element)); // in range and last day in February
        assertFalse(matcher.match(29, element)); // in range, but no leap day in this year
        assertFalse(matcher.match(30, element)); // in range, but overflow for February
        assertFalse(matcher.match(31, element)); // overflow

        element = createElement(Calendar.DAY_OF_MONTH, "2020.02.01"); // leap year
        assertTrue(matcher.match(29, element)); // test leap day
        assertFalse(matcher.match(30, element)); // in range, but overflow for February

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertTrue(matcher.match(30, element)); // is Ok, last day in month
        assertFalse(matcher.match(31, element)); // but overflow for April
    }


    @Test(dataProvider = "getMajor")
    public void testGetMajor(int value, int expected) throws Exception
    {
        int actual = matcher.getMajor(value, null);
        assertEquals(actual, expected, "HashMapMatcher return wrong next value.");
    }

    @DataProvider(name = "getMajor")
    public Object[][] getMajorDataProvider()
    {
        // value, expected result
        return new Object[][]{
                {-3, 1},
                {-1, 1},
                {0, 1},
                {1, 2},
                {2, 4},
                {3, 4},
                {4, 7},
                {5, 7},
                {6, 7},
                {7,  10},
                {8,  10},
                {9,  10},
                {10, 13},
                {11, 13},
                {12, 13},
                {13, 16},
                {14, 16},
                {15, 16},
                {16, 20},
                {17, 20},
                {18, 20},
                {19, 20},
                {20, 27},
                {21, 27},
                {22, 27},
                {23, 27},
                {24, 27},
                {25, 27},
                {26, 27},
                {27, NO_NEXT_VALUE},
                {28, NO_NEXT_VALUE},
                {29, NO_NEXT_VALUE},
                {30, NO_NEXT_VALUE},
                {31, NO_NEXT_VALUE},
                {32, NO_NEXT_VALUE},
                {1000, NO_NEXT_VALUE},
        };
    }



    @Test(dataProvider = "getMinor")
    public void testGetMinor(int value, int expected) throws Exception
    {
        int actual = matcher.getMinor(value, null);
        assertEquals(actual, expected, "HashMapMatcher return wrong prev value.");
    }

    @DataProvider(name = "getMinor")
    public Object[][] getMinorDataProvider()
    {
        // value, expected result
        return new Object[][]{
                {-1, NO_PREV_VALUE},
                {0, NO_PREV_VALUE},
                {1, NO_PREV_VALUE},
                {2, 1},
                {3, 2},
                {4, 2},
                {5, 4},
                {6, 4},
                {7,  4},
                {8,  7},
                {9,  7},
                {10, 7},
                {11, 10},
                {12, 10},
                {13, 10},
                {14, 13},
                {15, 13},
                {16, 13},
                {17, 16},
                {18, 16},
                {19, 16},
                {20, 16},
                {21, 20},
                {22, 20},
                {23, 20},
                {24, 20},
                {25, 20},
                {26, 20},
                {27, 20},
                {28, 27},
                {29, 27},
                {30, 27},
                {31, 27},
                {32, 27},
                {1000, 27},
        };
    }

    @Test
    public void testGetHigh() throws Exception
    {
        HashMapMatcher matcher = new HashMapMatcher(1, 30);
        matcher.addRange(1, 5, 1);
        matcher.addRange(20, 30, 2);
        matcher.finishRange();
        matcher.setDynamicRange();

        CalendarElement element = createElement(Calendar.DAY_OF_MONTH, "2021.02.01");
        assertEquals(matcher.getHigh(element), 28);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertEquals(matcher.getHigh(element), 30);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.05.01");
        assertEquals(matcher.getHigh(element), 30);


        matcher = new HashMapMatcher(1, 31);
        matcher.addRange(1, 5, 1);
        matcher.addRange(21, 31, 2);
        matcher.finishRange();
        matcher.setDynamicRange();

        element = createElement(Calendar.DAY_OF_MONTH, "2021.04.01");
        assertEquals(matcher.getHigh(element), 29);

        element = createElement(Calendar.DAY_OF_MONTH, "2021.05.01");
        assertEquals(matcher.getHigh(element), 31);
    }

    @Test
    public void testGetLow() throws Exception
    {
        HashMapMatcher matcher = new HashMapMatcher(1, 31);
        matcher.addRange(2, 5, 1);
        matcher.addRange(21, 31, 2);
        matcher.finishRange();
        assertEquals(matcher.getLow(null), 2);
    }


    @Test
    public void testLongRanges() throws Exception
    {
        HashMapMatcher matcher = new HashMapMatcher(100, 151);
        matcher.addRange(100, 101, 1);
        matcher.addRange(150, 151, 1);
        matcher.finishRange();
        assertEquals(matcher.getLow(null), 100);
        assertEquals(matcher.getHigh(null), 151);

        assertEquals(matcher.getMajor(99, null), 100);
        assertEquals(matcher.getMajor(100, null), 101);
        assertEquals(matcher.getMajor(101, null), 150);
        assertEquals(matcher.getMajor(150, null), 151);
        assertEquals(matcher.getMajor(151, null), NO_NEXT_VALUE);//overflow

        assertEquals(matcher.getMinor(152, null), 151);
        assertEquals(matcher.getMinor(151, null), 150);
        assertEquals(matcher.getMinor(150, null), 101);
        assertEquals(matcher.getMinor(101, null), 100);
        assertEquals(matcher.getMinor(100, null), NO_PREV_VALUE); //overflow
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