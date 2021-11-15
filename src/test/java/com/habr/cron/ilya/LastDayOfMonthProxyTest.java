package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.habr.cron.ilya.LastDayOfMonthMatcher.LAST_DAY_OF_MONTH_CODE;
import static org.testng.Assert.*;

public class LastDayOfMonthProxyTest
{
    private static final SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd");
    private final ScheduleItemMatcher matcher;

    // create schedule "1,2,17-25/2,32"
    public LastDayOfMonthProxyTest()
    {
        HashMapMatcher map = new HashMapMatcher(1, LAST_DAY_OF_MONTH_CODE);
        map.addRange(1, 2, 1);
        map.addRange(17, 25, 2);
        map.addRange(LAST_DAY_OF_MONTH_CODE, LAST_DAY_OF_MONTH_CODE, 1);
        map.finishRange();
        matcher = new LastDayOfMonthProxy(map);
    }



    @Test(dataProvider = "testMatch_DataProvider")
    public void testMatch(Date sourceDate, int dayToMatch, boolean expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);

        boolean actual = matcher.match(dayToMatch, element);
        assertEquals(actual, expected);
    }
    @DataProvider
    private Object[][] testMatch_DataProvider() throws ParseException
    {
        return new Object[][] {
                // February, leap year
                {f.parse("2000.02.01"), 1, true},
                {f.parse("2000.02.01"), 2, true},
                {f.parse("2000.02.01"), 3, false},
                {f.parse("2000.02.01"), 17, true},
                {f.parse("2000.02.01"), 18, false},
                {f.parse("2000.02.01"), 19, true},
                {f.parse("2000.02.01"), 28, false},
                {f.parse("2000.02.01"), 29, true},  // last day
                {f.parse("2000.02.01"), 30, false},
                {f.parse("2000.02.01"), 31, false},
                {f.parse("2000.02.01"), 32, false},

                // February, no leap year
                {f.parse("2100.02.01"), 31, false},
                {f.parse("2100.02.01"), 30, false},
                {f.parse("2100.02.01"), 29, false},
                {f.parse("2100.02.01"), 28, true},  // last day

                // another months
                {f.parse("2021.10.01"), 1, true},
                {f.parse("2021.10.01"), 3, false},
                {f.parse("2021.10.01"), 30, false},
                {f.parse("2021.10.01"), 31, true}, // last day
                {f.parse("2021.10.01"), 32, false},

                {f.parse("2021.09.01"), 29, false},
                {f.parse("2021.09.01"), 30, true},  // last day
                {f.parse("2021.09.01"), 31, false},
                {f.parse("2021.09.01"), 32, false},
                {f.parse("2021.09.01"), 33, false},
        };
    }



    @Test(dataProvider = "getMajor_DataProvider")
    public void testGetMajor(Date sourceDate, int expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);
        int curValue = calendar.get(Calendar.DAY_OF_MONTH);

        int actual = matcher.getMajor(curValue, element);
        assertEquals(actual, expected);
    }
    @DataProvider
    private Object[][] getMajor_DataProvider() throws ParseException
    {
        return new Object[][] {
                // leap month
                {f.parse("2000.02.01"), 2},
                {f.parse("2000.02.02"), 17},
                {f.parse("2000.02.03"), 17},
                {f.parse("2000.02.17"), 19},
                {f.parse("2000.02.25"), 29},
                {f.parse("2000.02.26"), 29},
                {f.parse("2000.02.29"), LAST_DAY_OF_MONTH_CODE},

                // no leap month
                {f.parse("2100.02.01"), 2},
                {f.parse("2100.02.02"), 17},
                {f.parse("2100.02.03"), 17},
                {f.parse("2100.02.17"), 19},
                {f.parse("2100.02.25"), 28},
                {f.parse("2100.02.28"), LAST_DAY_OF_MONTH_CODE},

                // another months
                {f.parse("2021.10.01"), 2},
                {f.parse("2021.10.02"), 17},
                {f.parse("2021.10.03"), 17},
                {f.parse("2021.10.17"), 19},
                {f.parse("2021.10.25"), 31},

                {f.parse("2021.11.01"), 2},
                {f.parse("2021.11.02"), 17},
                {f.parse("2021.11.03"), 17},
                {f.parse("2021.11.17"), 19},
                {f.parse("2021.11.25"), 30},
        };
    }


    @Test(dataProvider = "getHigh_DataProvider")
    public void testGetHigh(Date sourceDate, int expected) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(sourceDate);

        CalendarElement element = new CalendarElement(Calendar.DAY_OF_MONTH, calendar);

        int actual = matcher.getHigh(element);
        assertEquals(actual, expected);
    }
    @DataProvider
    private Object[][] getHigh_DataProvider() throws ParseException
    {
        return new Object[][]{
                {f.parse("2000.02.01"), 29}, // leap month
                {f.parse("2100.02.01"), 28}, // no leap month
                {f.parse("2100.10.01"), 31},
                {f.parse("2100.09.01"), 30},
        };
    }
}