package com.habr.cron.opt;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.testng.Assert.*;

public class GregCalendarTest
{
    private final SimpleDateFormat format;

    public GregCalendarTest()
    {
        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test(dataProvider = "testConstructorData")
    public void testConstructor(String dateString) throws Exception
    {
        Date source = format.parse(dateString);
        GregCalendar calendar = new GregCalendar(source, format.getTimeZone());

        Calendar cal = new GregorianCalendar(format.getTimeZone());
        cal.setTime(source);

        assertEquals(calendar.year, cal.get(Calendar.YEAR));
        assertEquals(calendar.month, cal.get(Calendar.MONTH)+1);
        assertEquals(calendar.day, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(calendar.hours, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(calendar.minutes, cal.get(Calendar.MINUTE));
        assertEquals(calendar.seconds, cal.get(Calendar.SECOND));
        assertEquals(calendar.milliseconds, cal.get(Calendar.MILLISECOND));
        assertEquals(calendar.getDayOfWeek(), cal.get(Calendar.DAY_OF_WEEK)-1);
    }
    @DataProvider
    private Object[][] testConstructorData()
    {
        // IMPORTANT: all dates in UTC time!  (+0000)
        return new Object[][]{
                {"01.01.1970 00:00:00.000"},
                {"31.12.1970 23:59:59.999"},
                {"01.01.1971 00:00:00.000"},
                {"31.12.1971 23:59:59.999"},
                {"01.01.1972 00:00:00.000"},
                {"31.12.1972 23:59:59.999"},
                {"01.01.1973 00:00:00.000"},
                {"31.12.1973 23:59:59.999"},
                {"01.01.1974 00:00:00.000"},
                {"31.12.1974 23:59:59.999"},
                {"01.01.1975 00:00:00.000"},
                {"31.12.1975 23:59:59.999"},
                {"01.01.1976 00:00:00.000"},
                {"31.12.1976 23:59:59.999"},
                {"01.01.1977 00:00:00.000"},
                {"31.12.1977 23:59:59.999"},
                {"01.01.1978 00:00:00.000"},
                {"31.12.1978 23:59:59.999"},
                {"01.01.1979 00:00:00.000"},
                {"31.12.1979 23:59:59.999"},

                {"01.01.2020 00:00:00.173"},
                {"31.12.2020 23:59:59.711"},
                {"31.12.2020 23:59:59.999"},

                {"01.01.1971 00:00:00.173"},
                {"01.01.1970 00:00:00.173"},
                {"31.12.1971 23:59:59.999"},
                {"31.12.1972 23:59:59.999"},
                {"31.12.1973 23:59:59.999"},

                {"30.04.2021 12:11:13.145"}, // equals to "30.04.2021 12:11:13.145 +0000"
                {"30.12.2020 01:49:49.112"},
                {"30.12.2020 23:59:59.111"},
                {"31.12.2020 23:59:59.999"},
                {"31.12.2021 23:59:59.711"},
                {"31.12.2021 23:59:59.999"},
                {"28.10.2021 10:32:58.176"},
                {"15.02.2008 11:31:33.173"},
                {"17.05.2017 11:31:33.173"},
                {"01.02.1970 11:31:33.173"},

                {"01.01.2100 00:00:00.000"},
                {"31.12.2100 23:59:59.999"},
        };
    }
    @Test(dataProvider = "testConstructorData")
    public void testAsDate(String dateString) throws Exception
    {
        Date source = format.parse(dateString);
        GregCalendar calendar = new GregCalendar(source, TimeZone.getTimeZone("UTC"));

        String actual = format.format(calendar.asDate());
        assertEquals(actual, dateString);
    }

    @Test
    public void testIsLeap() throws Exception
    {
        GregCalendar calendar = new GregCalendar(0);
        calendar.year = 2000;
        assertTrue(calendar.isLeap());
        calendar.year = 2020;
        assertTrue(calendar.isLeap());
        calendar.year = 2100;
        assertFalse(calendar.isLeap());
        calendar.year = 2021;
        assertFalse(calendar.isLeap());
        calendar.year = 1970;
        assertFalse(calendar.isLeap());
    }

    @Test
    public void testIsCorrect() throws Exception
    {
        GregCalendar calendar = new GregCalendar(0);
        calendar.year = 2021;
        calendar.month = 2;
        calendar.day = 28;
        assertTrue(calendar.isCorrect());
        calendar.day = 29;
        assertFalse(calendar.isCorrect());
        calendar.year = 2020;
        assertTrue(calendar.isCorrect());
    }

    @Test
    public void testGetDayOfWeek() throws Exception {

    }

    @Test
    public void testSetValue() throws Exception {

    }

    @Test
    public void testGetValue() throws Exception {

    }
}