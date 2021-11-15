package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.habr.cron.ilya.ScheduleElements.MONTH;
import static com.habr.cron.ilya.ScheduleElements.DAY_OF_MONTH;
import static org.testng.Assert.*;

public class CalendarDigitsSpecialTest
{
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");


    @Test(dataProvider = "resetToZero_DataProvider")
    public void testResetToZero(String schedule, ScheduleElements element, String moment, String expected) throws Exception
    {
        ScheduleModel model = createModel(schedule);
        CalendarDigits digits = createDigitsFor(model, moment);


        digits.gotoDigit(element);
        //int posBefore = digits.index();

        digits.initialize(true); // test that
        assertEquals(digits.toString(), expected);

        // not need to test
        //int posAfter = digits.index();
        //assertEquals(posBefore, posAfter, "Active digit was changed!");
    }
    @DataProvider
    private Object[][] resetToZero_DataProvider()
    {
        // schedule, element to start from, input date moment, expected date
        return new Object[][] {
                {"*.3,11.29,31 1 00:00:00.000", DAY_OF_MONTH, "2021.02.01 12:31:48.561", "2021.03.29 00:00:00.000"},
                {"*.3,11.29,31 1 00:00:00.000", DAY_OF_MONTH, "2020.02.01 12:31:48.561", "2020.02.29 00:00:00.000"},
                {"*.3,11.29 1 00:00:00.000", DAY_OF_MONTH, "2021.02.01 12:31:48.1", "2021.03.29 00:00:00.000"},
                {"*.3,11.29 1 00:00:00.000", DAY_OF_MONTH, "2021.01.01 12:31:48.2", "2021.01.29 00:00:00.000"},
                {"*.3,11.29 1 00:00:00.000", MONTH, "2021.01.01 12:31:48.3", "2021.03.29 00:00:00.000"},
                {"*.3,11.29 1 00:00:00.000", DAY_OF_MONTH, "2021.03.01 12:31:48.4", "2021.03.29 00:00:00.000"},
                {"*.3,11.31 1 00:00:00.000", DAY_OF_MONTH, "2021.04.01 12:31:48.5", "2022.03.31 00:00:00.000"},
                {"*.3,11.31 1 00:00:00.000", MONTH, "2021.04.01 12:31:48.6", "2021.03.31 00:00:00.000"},
                {"*.3,11.29,31 1 00:00:00.000", DAY_OF_MONTH, "2021.04.01 12:31:48.7", "2021.04.29 00:00:00.000"},
                {"*.*.29 1 00:00:00.000", DAY_OF_MONTH, "2021.02.01 12:31:48.8", "2021.03.29 00:00:00.000"},
                {"*.*.29 1 00:00:00.000", DAY_OF_MONTH, "2021.01.01 12:31:48.9", "2021.01.29 00:00:00.000"},
        };
    }


    @Test(dataProvider = "resetToMax_DataProvider")
    public void testResetToMax(String schedule, ScheduleElements element, String moment, String expected) throws Exception
    {
        ScheduleModel model = createModel(schedule);
        CalendarDigits digits = createDigitsFor(model, moment);

        digits.gotoDigit(element);
        digits.initialize(false);

        Date date = digits.getAsDate();
        String actual = df.format(date);

        assertEquals(actual, expected);
    }
    @DataProvider
    private Object[][] resetToMax_DataProvider()
    {
        // schedule, element to start from, input date moment, expected date
        return new Object[][] {
                {"*.3,11.29 1 00:00:00.000", DAY_OF_MONTH, "2021.02.01 12:31:48.1", "2020.11.29 00:00:00.000"},
                {"*.3,11.29,31 1 00:00:00.000", DAY_OF_MONTH, "2021.02.01 12:31:48.1", "2020.11.29 00:00:00.000"},
                {"*.3,11.31 1 00:00:00.000", DAY_OF_MONTH, "2021.02.01 12:31:48.1", "2020.03.31 00:00:00.000"},
                {"2021.2.4,29,30 12:00:00", MONTH, "2021.03.29 12:31:48.9", "2021.02.04 12:00:00.000"},
        };
    }


    /**
     * Helps to create model from schedule.
     *
     * @param schedule standard text implementation
     * @return the model
     * @throws ScheduleFormatException
     */
    private ScheduleModel createModel(String schedule) throws ScheduleFormatException
    {
        Parser parser = new Parser();
        parser.parse(schedule);
        return parser.getScheduleModel();
    }

    /**
     * Helps to create a digits for specified schedule and moment of time.
     *
     * @param model schedule
     * @param moment current time in string presentation
     * @return the digits
     * @throws ScheduleFormatException
     * @throws ParseException
     */
    private CalendarDigits createDigitsFor(ScheduleModel model, String moment) throws ScheduleFormatException, ParseException
    {
        MatcherPool pool = new MatcherPool(model);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(df.parse(moment));
        return new CalendarDigits(pool, calendar);
    }

}