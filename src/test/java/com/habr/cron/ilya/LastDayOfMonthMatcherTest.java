package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class LastDayOfMonthMatcherTest
{
    private static final SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd");
    private final ScheduleItemMatcher matcher = new LastDayOfMonthMatcher();

    @Test(dataProvider = "matchLastDays_DataProvider")
    public void testMatchForLastDays(Date sourceDate, boolean expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);
        int value = calendar.get(Calendar.DAY_OF_MONTH);

        boolean actual = matcher.match(value, element);
        assertEquals(actual, expected);
    }

    @DataProvider // only last days
    private Object[][] matchLastDays_DataProvider() throws ParseException
    {
        return new Object[][] {
                {f.parse("2000.02.29"), true},
                {f.parse("2100.02.29"), false},
                {f.parse("2100.02.28"), true},
                {f.parse("2021.02.29"), false},
                {f.parse("2021.02.28"), true},
                {f.parse("2020.02.29"), true},
                {f.parse("2021.10.31"), true},
                {f.parse("2021.10.30"), false},
        };
    }



    @Test(dataProvider = "major_DataProvider")
    public void testGetMajor(Date sourceDate, int expected) throws Exception 
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);
        int value = calendar.get(Calendar.DAY_OF_MONTH);

        int actual = matcher.getMajor(value, element);
        assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] major_DataProvider() throws ParseException
    {
        int max = LastDayOfMonthMatcher.LAST_DAY_OF_MONTH_CODE + 1;
        return new Object[][] {
                {f.parse("2021.10.31"), max},
                {f.parse("2021.10.30"), max},
                {f.parse("2021.10.01"), max},
                {f.parse("2000.02.29"), max},
                {f.parse("2100.02.29"), max},
                {f.parse("2100.02.28"), max},
                {f.parse("2021.02.29"), max},
                {f.parse("2021.02.28"), max},
                {f.parse("2020.02.29"), max},
        };
    }


    @Test(dataProvider = "minor_DataProvider")
    public void testGetMinor(Date sourceDate, int expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);
        int value = calendar.get(Calendar.DAY_OF_MONTH);

        int actual = matcher.getMinor(value, element);
        assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] minor_DataProvider() throws ParseException
    {
        return new Object[][] {
                {f.parse("2021.10.31"), -1},
                {f.parse("2021.10.30"), -1},
                {f.parse("2021.10.01"), -1},
                {f.parse("2000.02.29"), -1},
                {f.parse("2100.02.29"), -1},
                {f.parse("2100.02.28"), -1},
                {f.parse("2021.02.29"), -1},
                {f.parse("2021.02.28"), -1},
                {f.parse("2020.02.29"), -1},
        };
    }



    @Test(dataProvider = "getLowAndHigh_DataProvider")
    public void testGetLowAndGetHigh(Date sourceDate, int expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);

        int actual = matcher.getLow(element);
        assertEquals(actual, expected);

        actual = matcher.getHigh(element);
        assertEquals(actual, expected);
    }

    @DataProvider
    private Object[][] getLowAndHigh_DataProvider() throws ParseException
    {
        // return only actual last day in current month
        return new Object[][] {
                {f.parse("2000.02.29"), 29}, // leap
                {f.parse("2000.02.20"), 29}, // leap
                {f.parse("2100.02.28"), 28},
                {f.parse("2100.02.27"), 28},
                {f.parse("2100.02.20"), 28},

                {f.parse("2021.02.28"), 28},
                {f.parse("2021.02.21"), 28},
                {f.parse("2020.02.29"), 29}, // leap

                {f.parse("2021.09.01"), 30},
                {f.parse("2021.10.31"), 31},
                {f.parse("2021.10.30"), 31},
                {f.parse("2021.10.29"), 31},
                {f.parse("2021.10.01"), 31},
        };
    }


    @Test(dataProvider = "getIsBelow_DataProvider")
    public void testIsBelow(Date sourceDate, int value, boolean expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);

        boolean actual = matcher.isBelow(value, element);
        assertEquals(actual, expected);
    }
    @DataProvider
    private Object[][] getIsBelow_DataProvider() throws ParseException
    {
        // return only actual last day in current month
        return new Object[][] {
                {f.parse("2000.02.01"), 28, true},  // leap year
                {f.parse("2000.02.01"), 29, false}, // leap
                {f.parse("2000.02.01"), 30, false}, // leap
                {f.parse("2000.02.01"), 31, false}, // leap
                {f.parse("2000.02.01"), 32, false}, // leap

                {f.parse("2100.02.01"), 27, true},  // no leap year
                {f.parse("2100.02.01"), 28, false},
                {f.parse("2100.02.01"), 29, false},

                {f.parse("2021.02.01"), 27, true},  // no leap year
                {f.parse("2021.02.01"), 28, false},
                {f.parse("2021.02.01"), 29, false},

                {f.parse("2021.09.01"), 28, true},
                {f.parse("2021.09.01"), 29, true},
                {f.parse("2021.09.01"), 30, false},
                {f.parse("2021.09.01"), 31, false},

                {f.parse("2021.10.01"), 29, true},
                {f.parse("2021.10.01"), 30, true},
                {f.parse("2021.10.01"), 31, false},
                {f.parse("2021.10.01"), 32, false},

                {f.parse("2021.10.01"), 29, true},
                {f.parse("2021.10.01"), 30, true},
                {f.parse("2021.10.01"), 31, false},
                {f.parse("2021.10.01"), 32, false},
        };
    }
}