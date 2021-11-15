package com.habr.cron.novar;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.testng.Assert.*;

public class ScheduleTest
{
    private static final SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    private static final SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Test(enabled = false)
    public void testSchedule() throws Exception
    {
        Schedule schedule = new Schedule("*/4.01.01 12:00:00.000");
        Date current = d.parse("2012-01-01 12:00:00.001");

        Date actual = schedule.NearestEvent(current);
        Date expected = d.parse("2016-01-01 12:00:00.000");
        assertEquals(actual, expected);
    }

    @Test(dataProvider = "nextEvent_DataProvider")
    public void testNearestEvent(String schedule, String sourceDate, String expectedDate) throws Exception
    {
        Schedule s = new Schedule(schedule);
        Date date = f.parse(sourceDate);

        Date actual = s.NearestEvent(date);
        Date expected = f.parse(expectedDate);
        assertEquals(actual, expected, f.format(actual) + "  " + f.format(expected));
    }

    @DataProvider
    private Object[][] nextEvent_DataProvider() throws ParseException
    {
        return new Object[][] {
                {"*.*.20-32 12:00:00", // schedule
                        "30.04.2021 12:00:00.001", // current date
                        "20.05.2021 12:00:00.000"  // expected date
                },
                {"*.*.29 12:00:00", // schedule
                        "29.01.2021 13:00:00.000", // current date
                        "29.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.31 12:00:00", // schedule
                        "31.01.2021 12:00:00.001", // current date
                        "31.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.* * *:*:*.1,2,3-5,10-20/3", // schedule
                        "31.12.2020 23:59:59.020", // current date
                        "01.01.2021 00:00:00.001"  // expected date
                },
                {"*.*.* * *:*:*.100", // schedule
                        "01.01.2020 00:00:00.099", // current date
                        "01.01.2020 00:00:00.100"  // expected date
                },
                {"*.*.* * *:*:*.1,2,3-5,10-20/3", // schedule
                        "01.01.2020 00:00:00.011", // current date
                        "01.01.2020 00:00:00.013"  // expected date
                },
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "05.08.2021 00:00:00.500", // current date
                        "06.08.2021 00:00:00.000"  // expected date
                },
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "02.08.2021 00:00:00.500", // current date
                        "02.08.2021 00:00:00.500"  // expected date
                },
                {"*.*.* * *:*:*.*", // schedule
                        "31.12.2099 23:59:59.999", // current date
                        "31.12.2099 23:59:59.999"  // expected date
                },
                {"*.*.* * *:*:*.*", // schedule
                        "01.01.2000 00:00:00.001", // current date
                        "01.01.2000 00:00:00.001"  // expected date
                },
                {"2100.12.31 23:59:59.999", // schedule
                        "01.01.2000 00:00:00.000", // current date
                        "31.12.2100 23:59:59.999"
                },
                {"*.*.29,30 12:00:00", // schedule
                        "31.01.2021 12:00:00.000", // current date
                        "29.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.30 12:00:00", // schedule
                        "04.02.2021 12:00:00.000", // current date
                        "30.03.2021 12:00:00.000"  // expected date
                },
        };
    }



    @Test(dataProvider = "specialWeekDayCases")
    public void testSpecialCasesForWeekDays(String schedule, String sourceDate, String expectedDate) throws Exception
    {
        Schedule s = new Schedule(schedule);
        Date date = f.parse(sourceDate);

        Date actual = s.NearestEvent(date);
        Date expected = f.parse(expectedDate);
        assertEquals(actual, expected,
                "\n" +
                        f.format(expected) + " <- expected" +
                        "\n" +
                        f.format(actual) + " <- actual ");
    }
    @DataProvider
    private Object[][] specialWeekDayCases()
    {
        return new Object[][]{
                // последний день месяца в воскресенье
                {"*.*.32 0 12:14:34", // schedule
                        "31.05.2021 12:14:33.177", // current date
                        "31.10.2021 12:14:34.000"  // expected date
                },
                // последний день месяца в четверг
                {"*.*.32 4 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "30.09.2021 12:14:34.000"  // expected date
                },
                // *.9.*/2 1-5 10:00:00.000 означает 10:00 во все дни с пн. по пт. по нечетным числам в сентябре
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                        "03.09.2020 12:00:00.000", // current date
                        "07.09.2020 10:00:00.000"  // expected date
                },
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                        "03.09.2020 00:00:00.000", // current date
                        "03.09.2020 10:00:00.000"  // expected date
                },
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                        "30.09.2020 12:00:00.000", // current date
                        "01.09.2021 10:00:00.000"  // expected date
                },
                // каждую миллисекунду по понедельникам, средам и пятницам
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "05.08.2021 00:00:00.500", // current date
                        "06.08.2021 00:00:00.000"  // expected date
                },
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "02.08.2021 00:00:00.500", // current date
                        "02.08.2021 00:00:00.500"  // expected date
                },
                // раз в неделю каждую пятницу, начиная с 1-го числа
                {"*.*.*/7 5 14:12:13.567", // schedule
                        "08.11.2021 12:00:00.000", // current date
                        "01.04.2022 14:12:13.567"  // expected date
                },
                // январь и октябрь, числа 5, 12, 19, 26, по понедельникам
                {"*.1,10.5-26/7 1 12:15:11.320", // schedule
                        "05.10.2021 12:15:11.319", // current date
                        "05.01.2026 12:15:11.320"  // expected date
                },
                {"*.1,10.5-26/7 1 12:15:11.320", // schedule
                        "19.11.2021 12:15:11.319", // current date
                        "05.01.2026 12:15:11.320"  // expected date
                },
                // 29-е февраля в субботу  (11 проверок, 6 итераций на поиск года)
                {"*.02.29 6 12:00:00", // schedule
                        "01.01.2021 12:00:00.000", // current date
                        "29.02.2048 12:00:00.000"  // expected date
                },
                // 31-е во вторник
                {"*.*.31 2 12:14:34", // schedule
                        "31.05.2021 12:14:33.177", // current date
                        "31.08.2021 12:14:34.000"  // expected date
                },
                // 31-е в субботу
                {"*.*.31 6 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "31.07.2021 12:14:34.000"  // expected date
                },
                // 30-е в четверг
                {"*.*.30 4 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "30.09.2021 12:14:34.000"  // expected date
                },
                // 31-е в среду (хитрость в том, что условный "31-е февраля" выпадает на среду,
                // но resetDate() автоматом приведет к 31.03)
                {"*.*.31 3 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "31.03.2021 12:14:34.000"  // expected date
                },
                // any of the numbers 20-32/5 on Friday (the 32nd - is the last day of the month)
                {"*.*.20-32/5 5 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "30.04.2021 12:14:34.000"  // expected date
                },
                // any of the numbers 20-32/5 on Monday (the 32nd - is the last day of the month)
                {"*.*.27-32/2 1 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "29.03.2021 12:14:34.000"  // expected date
                },
        };
    }


}