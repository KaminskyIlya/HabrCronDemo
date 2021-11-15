package com.habr.cron.ilya;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Calendar;

import static org.testng.Assert.*;

public class BitMapMatcherTest
{
    private BitMapMatcher matcher;

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testOutOfRange() throws Exception
    {
        BitMapMatcher matcher = new BitMapMatcher(1, 100);
        matcher.addRange(101, 200, 1);
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        // Setup schedule as "1,2,7-17/3,204,320-624/5,827-927"
        matcher = new BitMapMatcher(1, 927); // IMPORTANT: 1 - minimal available value
        matcher.addRange(1,2,1);        // 1-2/1 = 1,2
        matcher.addRange(7,17,3);       // 7-17/3 = 7,10,13,16
        matcher.addRange(204,204,1);    // 204
        matcher.addRange(320,624,5);    // 320-624/5
        matcher.addRange(827,927,1);    // 827-927
        matcher.finishRange();
    }

    @Test
    public void testMatch() throws Exception
    {
        assertTrue(matcher.match(1, null));
        assertTrue(matcher.match(2, null));
        assertTrue(matcher.match(7, null));
        assertTrue(matcher.match(10, null));
        assertTrue(matcher.match(13, null));
        assertTrue(matcher.match(16, null));
        assertTrue(matcher.match(204, null));
        assertTrue(matcher.match(320, null));
        assertTrue(matcher.match(325, null));
        assertTrue(matcher.match(620, null));
        assertTrue(matcher.match(827, null));
        assertTrue(matcher.match(927, null));

        assertFalse(matcher.match(-1, null));
        assertFalse(matcher.match(0, null));
        assertFalse(matcher.match(3, null));
        assertFalse(matcher.match(8, null));
        assertFalse(matcher.match(321, null));
        assertFalse(matcher.match(624, null));
        assertFalse(matcher.match(928, null));
        assertFalse(matcher.match(1000, null));
    }

    @Test
    public void testMatchForDynamicRange() throws Exception
    {
        BitMapMatcher matcher = new BitMapMatcher(27, 31);
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




    @Test
    public void testGetMajor() throws Exception
    {
        assertEquals(matcher.getMajor(3, null), 7);
        assertEquals(matcher.getMajor(4, null), 7);
        assertEquals(matcher.getMajor(7, null), 10);
        assertEquals(matcher.getMajor(8, null), 10);
        assertEquals(matcher.getMajor(17, null), 204);
        assertEquals(matcher.getMajor(204, null), 320);
        assertEquals(matcher.getMajor(320, null), 325);
        assertEquals(matcher.getMajor(620, null), 827);
        assertEquals(matcher.getMajor(926, null), 927);
        assertEquals(matcher.getMajor(927, null), 928);
        assertEquals(matcher.getMajor(1000, null), 1001);
    }



    @Test
    public void testGetMinor() throws Exception
    {
        assertEquals(matcher.getMinor(-1, null), -2);
        assertEquals(matcher.getMinor(0, null), -1);
        assertEquals(matcher.getMinor(1, null), 0);
        assertEquals(matcher.getMinor(2, null), 1);
        assertEquals(matcher.getMinor(3, null), 2);
        assertEquals(matcher.getMinor(4, null), 2);
        assertEquals(matcher.getMinor(7, null), 2);
        assertEquals(matcher.getMinor(8, null), 7);
        assertEquals(matcher.getMinor(10, null), 7);
        assertEquals(matcher.getMinor(11, null), 10);
        assertEquals(matcher.getMinor(12, null), 10);
        assertEquals(matcher.getMinor(13, null), 10);
        assertEquals(matcher.getMinor(14, null), 13);
        assertEquals(matcher.getMinor(203, null), 16);
        assertEquals(matcher.getMinor(204, null), 16);
        assertEquals(matcher.getMinor(205, null), 204);
        assertEquals(matcher.getMinor(827, null), 620);
        assertEquals(matcher.getMinor(927, null), 926);
        assertEquals(matcher.getMinor(1000, null), 927);
    }




    @Test
    public void testGetLow() throws Exception
    {
        BitMapMatcher m = new BitMapMatcher(1, 927);
        m.addRange(3, 5, 1);        // 3-5/1 = 3,4,5
        m.addRange(7, 17, 3);       // 7-17/3 = 7,10,13,16
        m.addRange(204, 204, 1);    // 204
        m.addRange(320, 624, 5);    // 320-624/5
        m.addRange(827, 927, 1);    // 827-927
        m.finishRange();

        assertEquals(m.getLow(null), 3);
        assertEquals(matcher.getLow(null), 1);
    }

    @Test
    public void testGetHigh() throws Exception
    {
        BitMapMatcher m = new BitMapMatcher(1, 927);
        m.addRange(3, 5, 1);        // 3-5/1 = 3,4,5
        m.addRange(7, 17, 3);       // 7-17/3 = 7,10,13,16
        m.addRange(204, 204, 1);    // 204
        m.addRange(320, 624, 5);    // 320-624/5
        m.addRange(827, 900, 1);    // 827-927
        m.finishRange();

        assertEquals(m.getHigh(null), 900);
        assertEquals(matcher.getHigh(null), 927);
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
