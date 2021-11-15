package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.testng.Assert.*;
/**
 * Standard test for correct work.
 */
public class ScheduleTest
{
    private static final SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");


    @Test(dataProvider = "nextEvent_DataProvider")
    public void testNextEvent(String schedule, String sourceDate, String expectedDate) throws Exception
    {
        Schedule s = new Schedule(schedule);
        Date date = f.parse(sourceDate);

        Date actual = s.NextEvent(date);
        Date expected = f.parse(expectedDate);
        assertEquals(actual, expected,
                "\n" +
                        f.format(expected) + " <- expected" +
                        "\n" +
                        f.format(actual) + " <- actual ");
    }
    @DataProvider
    private Object[][] nextEvent_DataProvider() throws ParseException
    {
        return new Object[][] {
                {"*.*.* *:*:*.*",          "30.09.2021 12:00:00.002",
                                           "30.09.2021 12:00:00.003",
                },
                // проверка корретности обработки интервалов х-32 для дней месяца
                {"*.*.20-32 12:00:00", // schedule
                                            "30.04.2021 12:00:00.000", // current date
                                            "20.05.2021 12:00:00.000"  // expected date
                },
                {"*.*.20-32 12:00:00", // schedule
                                            "31.01.2021 12:00:00.000", // current date
                                            "20.02.2021 12:00:00.000"  // expected date
                },
                {"*.*.20-32 12:00:00", // schedule
                                            "31.01.2021 11:00:00.000", // current date
                                            "31.01.2021 12:00:00.000"  // expected date
                },
                // прыжок через февраль, т.к. в нём 29 дней
                {"*.*.29-32 12:00:00", // schedule
                                            "31.01.2021 12:00:00.000", // current date
                                            "29.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.29-32 12:00:00", // schedule
                                            "31.01.2020 12:00:00.000", // current date
                                            "29.02.2020 12:00:00.000"  // expected date
                },
                // 32-й день означает последнее число месяца
                {"*.*.32 12:00:00", // schedule
                                            "28.04.2021 12:00:00.000", // current date
                                            "30.04.2021 12:00:00.000"  // expected date
                },
                {"*.*.32 12:00:00", // schedule
                                            "29.04.2021 12:00:00.000", // current date
                                            "30.04.2021 12:00:00.000"  // expected date
                },
                {"*.*.32 12:00:00", // schedule
                                            "30.04.2021 12:00:00.000", // current date
                                            "31.05.2021 12:00:00.000"  // expected date
                },
                {"*.*.32 12:00:00", // schedule
                                            "31.03.2021 12:00:00.000", // current date
                                            "30.04.2021 12:00:00.000"  // expected date
                },
                {"*.2.32 12:00:00", // schedule
                                            "31.03.2020 12:00:00.000", // current date
                                            "28.02.2021 12:00:00.000"  // expected date
                },
                {"*.2.32 12:00:00", // schedule
                                            "31.01.2021 12:00:00.000", // current date
                                            "28.02.2021 12:00:00.000"  // expected date
                },
                {"*.2.32 12:00:00", // schedule
                                            "31.03.2021 12:00:00.000", // current date
                                            "28.02.2022 12:00:00.000"  // expected date
                },
                {"*.2.32 12:00:00", // schedule
                                            "31.01.2020 12:00:00.000", // current date
                                            "29.02.2020 12:00:00.000"  // expected date
                },
                {"*.*.32 12:00:00", // schedule
                                            "31.01.2020 12:00:00.000", // current date
                                            "29.02.2020 12:00:00.000"  // expected date
                },
                {"*.*.32 12:00:00", // schedule
                                            "31.01.2021 12:00:00.000", // current date
                                            "28.02.2021 12:00:00.000"  // expected date
                },
                {"*.*.32 12:00:00", // schedule
                                            "31.01.2020 11:00:00.000", // current date
                                            "31.01.2020 12:00:00.000"  // expected date
                },
                // *.*.01 01:30:00 означает 01:30 по первым числам каждого месяца
                {"*.*.01 01:30:00", // schedule
                                            "31.12.2020 01:30:00.000", // current date
                                            "01.01.2021 01:30:00.000"  // expected date
                },
                {"*.*.01 01:30:00", // schedule
                                            "01.01.2020 01:00:00.000", // current date
                                            "01.01.2020 01:30:00.000"  // expected date
                },
                // *:00:00 означает начало любого часа
                {"*:00:00", // schedule
                                            "31.12.2020 23:59:59.999", // current date
                                            "01.01.2021 00:00:00.000"  // expected date
                },
                {"*:00:00", // schedule
                                            "01.01.2020 00:00:00.000", // current date
                                            "01.01.2020 01:00:00.000"  // expected date
                },
                // *.9.*/2 1-5 10:00:00.000 означает 10:00 во все дни с пн. по пт. по нечетным числам в сентябре
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                                            "30.09.2020 12:00:00.000", // current date
                                            "01.09.2021 10:00:00.000"  // expected date
                },
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                                            "03.09.2020 12:00:00.000", // current date
                                            "07.09.2020 10:00:00.000"  // expected date
                },
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                                            "03.09.2020 00:00:00.000", // current date
                                            "03.09.2020 10:00:00.000"  // expected date
                },
                // (для часов) */4 означает 0,4,8,12,16,20
                {"*.*.* * */4:*:*", // schedule
                                            "01.01.2020 00:00:00.000", // current date
                                            "01.01.2020 00:00:01.000"  // expected date
                },
                {"*.*.* * */4:*:*", // schedule
                                            "31.12.2020 21:00:00.000", // current date
                                            "01.01.2021 00:00:00.000"  // expected date
                },
                // 1,2,3-5,10-20/3 означает список 1,2,3,4,5,10,13,16,19
                {"*.*.* * *:*:*.1,2,3-5,10-20/3", // schedule
                                            "31.12.2020 23:59:59.020", // current date
                                            "01.01.2021 00:00:00.001"  // expected date
                },
                {"*.*.* * *:*:*.1,2,3-5,10-20/3", // schedule
                                            "01.01.2020 00:00:00.011", // current date
                                            "01.01.2020 00:00:00.013"  // expected date
                },
                // 100-600/3 проверка корректности работы BitMapMatcher
                {"*.*.* * *:*:*.3-5,100-600/3", // schedule
                                            "01.01.2021 23:59:59.001", // current date
                                            "01.01.2021 23:59:59.003"  // expected date
                },
                // каждую миллисекунду по понедельникам, средам и пятницам
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                                            "05.08.2021 00:00:00.500", // current date
                                            "06.08.2021 00:00:00.000"  // expected date
                },
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                                            "02.08.2021 00:00:00.500", // current date
                                            "02.08.2021 00:00:00.501"  // expected date
                },
                // проверка шага в 1 миллисекунду
                {"*.*.* * *:*:*.100", // schedule
                                            "01.01.2020 00:00:00.099", // current date
                                            "01.01.2020 00:00:00.100"  // expected date
                },
                {"*.*.* * *:*:*.*", // schedule
                                            "31.12.2099 23:59:59.999", // current date
                                            "01.01.2100 00:00:00.000"  // expected date
                },
                {"*.*.* * *:*:*.*", // schedule
                                            "01.01.2000 00:00:00.001", // current date
                                            "01.01.2000 00:00:00.002"  // expected date
                },
                // максимальное кол-во итераций для алгоритма NoVar, у текущего - минимальное
                {"2100.12.31 23:59:59.999", // schedule
                                            "01.01.2000 00:00:00.000", // current date
                                            "31.12.2100 23:59:59.999"
                },
                // тест защиты от переполнения дня месяца (февраля, и других)
                {"*.*.29,30 12:00:00", // schedule
                                            "31.01.2021 12:00:00.000", // current date
                                            "29.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.30 12:00:00", // schedule
                                            "04.02.2021 12:00:00.000", // current date
                                            "30.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.29 12:00:00", // schedule
                                            "29.01.2021 13:00:00.000", // current date
                                            "29.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.29 12:00:00", // schedule
                                            "29.01.2020 13:00:00.000", // current date
                                            "29.02.2020 12:00:00.000"  // expected date
                },
                {"*.*.31 12:00:00", // schedule
                                            "31.01.2021 12:00:00.001", // current date
                                            "31.03.2021 12:00:00.000"  // expected date
                },
                {"*.*.31 12:00:00", // schedule
                                            "31.03.2021 12:00:00.001", // current date
                                            "31.05.2021 12:00:00.000"  // expected date
                },
                {"*.2.4,29,30 12:00:00", // schedule
                                            "04.02.2021 12:00:00.000", // expected date
                                            "04.02.2022 12:00:00.000", // current date
                },
                {"*.*.4,29,30 12:00:00", // schedule
                                            "04.02.2021 12:00:00.000", // expected date
                                            "04.03.2021 12:00:00.000", // current date
                },
                {"*.*.31 12:00:00", // schedule
                                            "31.01.2021 12:00:00.000", // expected date
                                            "31.03.2021 12:00:00.000",  // current date
                },
                // тесты из статей
                {"*/4.01.01 12:00:00.000", // schedule
                        "01.01.2012 12:00:00.001", // expected date
                        "01.01.2016 12:00:00.000",  // current date
                },
                {"*.*.* *:*:*.*", // schedule
                        "09.30.2021 12:00:00.002", // expected date
                        "09.30.2021 12:00:00.003",  // current date
                },
                {"*.4.6,7 * *:*:*.1,2,3-5,10-20/3", // schedule
                        "01.01.2001 00:00:00.000", // expected date
                        "06.04.2001 00:00:00.001",  // current date
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
                                            "02.08.2021 00:00:00.501"  // expected date
                },
                // раз в неделю каждую пятницу, начиная с 1-го числа
                {"*.*.*/7 5 12:00:00", // schedule
                                            "08.11.2021 12:00:00.000", // current date
                                            "01.04.2022 12:00:00.000"  // expected date
                },
                // январь и октябрь, числа 5, 12, 19, 26, по понедельникам
                {"*.1,10.5-26/7 1 12:00:00", // schedule
                                            "05.10.2021 12:00:00.000", // current date
                                            "05.01.2026 12:00:00.000"  // expected date
                },
                {"*.1,10.5-26/7 1 12:00:00", // schedule
                                            "19.11.2021 12:00:00.000", // current date
                                            "05.01.2026 12:00:00.000"  // expected date
                },
                // 29-е февраля в субботу
                {"*.02.29 6 12:00:00", // schedule
                                            "01.01.2021 12:00:00.000", // current date
                                            "29.02.2048 12:00:00.000"  // expected date
                },
                // последний день месяца во воскресенье
                {"*.*.32 0 12:14:34", // schedule
                        "31.05.2021 12:14:33.177", // current date
                        "31.10.2021 12:14:34.000"  // expected date
                },
        };
    }





    @Test(dataProvider = "prevEvent_DataProvider")
    public void testPrevEvent(String schedule, String sourceDate, String expectedDate) throws Exception
    {
        Schedule s = new Schedule(schedule);
        Date date = f.parse(sourceDate);

        Date actual = s.PrevEvent(date);
        Date expected = f.parse(expectedDate);
        assertEquals(actual, expected,
                "\n" +
                f.format(expected) + " <- expected" +
                "\n" +
                f.format(actual) + " <- actual ");
    }
    @DataProvider
    private Object[][] prevEvent_DataProvider() throws ParseException
    {
        return new Object[][] {
                // проверка корретности обработки интервалов х-32 для дней месяца
                {"*.*.20-32 12:00:00", // schedule
                        "20.05.2021 12:00:00.000", //current date
                        "30.04.2021 12:00:00.000", // expected date
                },
                {"*.*.20-32 12:00:00", // schedule
                        "20.05.2021 12:00:00.000",  // expected date
                        "30.04.2021 12:00:00.000", // current date
                },
                {"*.*.20-32 12:00:00", // schedule
                        "20.02.2021 12:00:00.000",  // expected date
                        "31.01.2021 12:00:00.000", // current date
                },
                {"*.*.20-32 12:00:00", // schedule
                        "31.01.2021 13:00:00.000",  // expected date
                        "31.01.2021 12:00:00.000", // current date
                },
                // прыжок через февраль, т.к. в нём 28/29 дней
                {"*.*.29-32 12:00:00", // schedule
                        "29.03.2021 12:00:00.000",  // expected date
                        "31.01.2021 12:00:00.000", // current date
                },
                {"*.*.29-32 12:00:00", // schedule
                        "29.02.2020 12:00:00.000",  // expected date
                        "31.01.2020 12:00:00.000", // current date
                },
                {"2021.2.4,29,30 12:00:00", // schedule
                        "29.03.2021 12:00:00.000", // current date
                        "04.02.2021 12:00:00.000"}, // expected date
                {"2021.*.4,29,30 12:00:00", // schedule
                        "03.03.2021 12:00:00.000", // current date
                        "04.02.2021 12:00:00.000"}, // expected date
                {"*.*.31 12:00:00", // schedule
                        "30.03.2021 12:00:00.000", // current date
                        "31.01.2021 12:00:00.000"}, // expected date
                // 32-й день означает последнее число месяца
                {"*.*.32 12:00:00", // schedule
                        "31.05.2021 12:00:00.000",  // expected date
                        "30.04.2021 12:00:00.000", // current date
                },
                {"*.*.32 12:00:00", // schedule
                        "30.04.2021 12:00:00.000",  // expected date
                        "31.03.2021 12:00:00.000", // current date
                },
                {"*.*.32 12:00:00", // schedule
                        "28.02.2021 12:00:00.000",  // expected date
                        "31.01.2021 12:00:00.000", // current date
                },
                {"*.*.32 12:00:00", // schedule
                        "31.01.2020 13:00:00.000",  // expected date
                        "31.01.2020 12:00:00.000", // current date
                },
                {"*.2.32 12:00:00", // schedule
                        "28.02.2021 12:00:00.000",  // expected date
                        "29.02.2020 12:00:00.000", // current date
                },
                {"*.2.32 12:00:00", // schedule
                        "27.02.2021 12:00:00.000",  // expected date
                        "29.02.2020 12:00:00.000", // current date
                },
                {"*.2.32 12:00:00", // schedule
                        "28.02.2022 12:00:00.000",  // expected date
                        "28.02.2021 12:00:00.000", // current date
                },
                // *.*.01 01:30:00 означает 01:30 по первым числам каждого месяца
                {"*.*.01 01:30:00", // schedule
                        "01.01.2021 01:30:00.000",  // expected date
                        "01.12.2020 01:30:00.000", // current date
                },
                {"*.*.01 01:30:00", // schedule
                        "01.01.2020 01:50:00.000",  // expected date
                        "01.01.2020 01:30:00.000", // current date
                },
                // *:00:00 означает начало любого часа
                {"*:00:00", // schedule
                        "01.01.2021 00:00:00.000",  // expected date
                        "31.12.2020 23:00:00.000", // current date
                },
                {"*:00:00", // schedule
                        "01.01.2020 01:00:00.000",  // expected date
                        "01.01.2020 00:00:00.000", // current date
                },
                // *.9.*/2 1-5 10:00:00.000 означает 10:00 во все дни с пн. по пт. по нечетным числам в сентябре
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                        "01.09.2021 08:00:00.000",  // expected date
                        "29.09.2020 10:00:00.000", // current date
                },
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                        "07.09.2020 08:00:00.000",  // expected date
                        "03.09.2020 10:00:00.000", // current date
                },
                {"*.9.*/2 1-5 10:00:00.000", // schedule
                        "03.09.2020 12:00:00.000",  // expected date
                        "03.09.2020 10:00:00.000", // current date
                },
                // (для часов) */4 означает 0,4,8,12,16,20
                {"*.*.* * */4:*:*", // schedule
                        "01.01.2020 00:00:03.000",  // expected date
                        "01.01.2020 00:00:02.000", // current date
                },
                {"*.*.* * */4:*:*", // schedule
                        "01.01.2020 00:03:00.000",  // expected date
                        "01.01.2020 00:02:59.000", // current date
                },
                {"*.*.* * */4:*:*", // schedule
                        "01.01.2020 03:00:00.000",  // expected date
                        "01.01.2020 00:59:59.000", // current date
                },
                {"*.*.* * */4:*:*", // schedule
                        "01.01.2021 01:00:00.000",  // expected date
                        "01.01.2021 00:59:59.000", // current date
                },
                {"*.*.* * */4:00:00", // schedule
                        "01.01.2021 00:00:00.000",  // expected date
                        "31.12.2020 20:00:00.000", // current date
                },
                // *.*.* * 1-16:00:00 означает каждый час с часу ночи до четырёх дня
                {"*.*.* * 1-16:00:00", // schedule
                        "01.01.2021 00:00:00.000",  // expected date
                        "31.12.2020 16:00:00.000", // current date
                },
                // *.*.* * 1-16/4:00:00 означает список 4,5,9,13
                {"*.*.* * 1-16/4:00:00", // schedule
                        "01.01.2021 00:00:00.000",  // expected date
                        "31.12.2020 13:00:00.000", // current date
                },
                // 1,2,3-5,10-20/3 означает список 1,2,3,4,5,10,13,16,19
                {"*.*.* * *:*:*.1,2,3-5,10-20/3", // schedule
                        "01.01.2021 00:00:00.000",  // expected date
                        "31.12.2020 23:59:59.019", // current date
                },
                {"*.*.* * *:*:*.1,2,3-5,10-20/3", // schedule
                        "01.01.2020 00:00:00.015",  // expected date
                        "01.01.2020 00:00:00.013", // current date
                },
                // 100-600/3 проверка корректности работы BitMapMatcher
                {"*.*.* * *:*:*.30-50/2,100-600/3", // schedule
                        "01.01.2021 23:59:59.020", // expected date
                        "01.01.2021 23:59:58.598", // current date
                },
                // каждую миллисекунду по понедельникам, средам и пятницам
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "06.08.2021 00:00:00.000",  // expected date
                        "04.08.2021 23:59:59.999", // current date
                },
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "02.08.2021 00:00:00.501",  // expected date
                        "02.08.2021 00:00:00.500", // current date
                },
                // проверка шага в 1 миллисекунду
                {"*.*.* * *:*:*.100", // schedule
                        "01.01.2020 00:00:00.199",  // expected date
                        "01.01.2020 00:00:00.100", // current date
                },
                {"*.*.* * *:*:*.*", // schedule
                        "01.01.2100 00:00:00.000",  // expected date
                        "31.12.2099 23:59:59.999", // current date
                },
                {"*.*.* * *:*:*.*", // schedule
                        "01.01.2000 00:00:00.002",  // expected date
                        "01.01.2000 00:00:00.001", // current date
                },
                {"2000.12.31 23:00:00.999", // schedule
                        "31.12.2100 22:59:59.999", // expected date
                        "31.12.2000 23:00:00.999", // current date
                },
                // тест защиты от переполнения дня месяца (февраля, и других)
                {"*.*.29,30 12:00:00", // schedule
                        "29.03.2021 12:00:00.000",  // expected date
                        "30.01.2021 12:00:00.000", // current date
                },
                {"*.*.31 12:00:00", // schedule
                        "30.03.2021 12:00:00.000",  // expected date
                        "31.01.2021 12:00:00.000", // current date
                },
                {"*.*.29 12:00:00", // schedule
                        "29.03.2021 12:00:00.000",  // expected date
                        "29.01.2021 12:00:00.000", // current date
                },
                {"*.*.29 12:00:00", // schedule
                        "29.02.2020 12:00:00.000",  // expected date
                        "29.01.2020 12:00:00.000", // current date
                },
                {"*.*.31 12:00:00", // schedule
                        "31.03.2021 12:00:00.000",  // expected date
                        "31.01.2021 12:00:00.000", // current date
                },
                {"*.*.31 12:00:00", // schedule
                        "31.05.2021 12:00:00.000",  // expected date
                        "31.03.2021 12:00:00.000", // current date
                },
                {"*.2.4,29,30 12:00:00", // schedule
                        "04.02.2022 12:00:00.000", // current date
                        "04.02.2021 12:00:00.000", // expected date
                },
                {"*.*.4,29,30 12:00:00", // schedule
                        "04.02.2021 12:00:00.000", // expected date
                        "30.01.2021 12:00:00.000", // current date
                },
                {"*.*.31 12:00:00", // schedule
                        "31.03.2021 12:00:00.000", // expected date
                        "31.01.2021 12:00:00.000",  // current date
                },
        };
    }







    //
    // Special cases
    //

    @Test(dataProvider = "nextEvent_WithOverflow", expectedExceptions = IllegalStateException.class)
    public void testNextEvent_WithOutOfRange(String schedule, String sourceDate, String expectedDate) throws Exception
    {
        Schedule s = new Schedule(schedule);
        Date date = f.parse(sourceDate);

        Date actual = s.NextEvent(date);
        Date expected = f.parse(expectedDate);
        assertEquals(actual, expected);
    }
    @DataProvider
    private Object[][] nextEvent_WithOverflow() throws ParseException
    {
        return new Object[][] {
                {"*.*.* * *:*:*.*", // schedule
                        "31.12.2100 23:59:59.999", // current date
                        "01.01.2101 00:00:00.000" // expected date - out of valid range (according format)
                },
                {"2021.2.4,29,30 12:00:00", // schedule
                        "04.02.2021 12:00:00.000", // expected date
                        "04.02.2022 12:00:00.000", // current date - out of valid range (according schedule)
                },
        };
    }





    @Test(dataProvider = "specialWeekDayCases")
    public void testSpecialCasesForWeekDays(String schedule, String sourceDate, String expectedDate) throws Exception
    {
        Schedule s = new Schedule(schedule);
        Date date = f.parse(sourceDate);

        Date actual = s.NextEvent(date);
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
                // *.9.*/2 1-5 10:00:00.000 means 10:00 on all days from Mon. to Fri. on odd numbers in September
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
                // every millisecond on Mondays, Wednesdays and Fridays
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "05.08.2021 00:00:00.500", // current date
                        "06.08.2021 00:00:00.000"  // expected date
                },
                {"*.*.* 1,3,5 *:*:*.*", // schedule
                        "02.08.2021 00:00:00.500", // current date
                        "02.08.2021 00:00:00.501"  // expected date
                },
                // once a week every Friday, starting from the 1st
                {"*.*.*/7 5 14:12:13.567", // schedule
                        "08.11.2021 12:00:00.000", // current date
                        "01.04.2022 14:12:13.567"  // expected date
                },
                // January and October, numbers 5, 12, 19, 26, on Mondays
                {"*.1,10.5-26/7 1 12:15:11.320", // schedule
                        "05.10.2021 12:15:11.319", // current date
                        "05.01.2026 12:15:11.320"  // expected date
                },
                {"*.1,10.5-26/7 1 12:15:11.320", // schedule
                        "19.11.2021 12:15:11.319", // current date
                        "05.01.2026 12:15:11.320"  // expected date
                },
                // Saturday, February 29th
                {"*.02.29 6 12:00:00", // schedule
                        "01.01.2021 12:00:00.000", // current date
                        "29.02.2048 12:00:00.000"  // expected date
                },
                // 31st on Tuesday
                {"*.*.31 2 12:14:34", // schedule
                        "31.05.2021 12:14:33.177", // current date
                        "31.08.2021 12:14:34.000"  // expected date
                },
                // the last day of the month is Sunday
                {"*.*.32 0 12:14:34", // schedule
                        "31.05.2021 12:14:33.177", // current date
                        "31.10.2021 12:14:34.000"  // expected date
                },
                // the last day of the month is Thursday
                {"*.*.32 4 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "30.09.2021 12:14:34.000"  // expected date
                },
                // 31st on Saturday
                {"*.*.31 6 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "31.07.2021 12:14:34.000"  // expected date
                },
                // 30th on Thursday
                {"*.*.30 4 12:14:34", // schedule
                        "31.01.2021 12:14:33.177", // current date
                        "30.09.2021 12:14:34.000"  // expected date
                },
                // 31st on Wednesday (the trick is that the conditional "February 31st" falls on Wednesday,
                // but resetDate() automatically takes us to 31.03, which is the date satisfying the condition)
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
