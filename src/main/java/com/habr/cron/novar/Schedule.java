package com.habr.cron.novar;

import com.habr.cron.Cron;
import com.habr.cron.CronBase;
import com.habr.cron.ilya.ScheduleFormatException;

import javax.crypto.ShortBufferException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Реализация тестового задания от автора оригинальной статьи @novar
 *
 * Бечмарки:
 *  В худшем случае: дата * /4.01.01 12:00:00 для даты 2012.01.01 12:00:00.001 будет выполнено 1186 итераций,
 *  186 созданий объекта DateTime или манипуляций календарем (как здесь).
 *  Скорость работы худшего случая: 52мкс.
 */
public class Schedule extends CronBase
{
    private static final String _placeholder = "*";
    private static final String _defaultMilliseconds = "0";

    private byte[] _years = new byte[101]; // год (2000-2100)
    private byte[] _months =       new byte[12]; // месяц (1-12)
    private byte[] _days =         new byte[32]; // число месяца (1-31 или 32)
    private byte[] _weekDays =     new byte[7]; // день недели (0-6)
    private byte[] _hours =        new byte[24]; // часы (0-23)
    private byte[] _minutes =      new byte[60]; // минуты (0-59)
    private byte[] _seconds =      new byte[60]; // секунды (0-59)
    private byte[] _milliseconds = new byte[1000]; // миллисекунды (0-999)

    /**
     * Создает пустой экземпляр, который будет соответствовать
     * расписанию типа "*.*.* * *:*:*.*" (раз в 1 мс).
     */
    public Schedule() throws ScheduleFormatException
    {
        // Вполне себе простое расписание "*/4.01.01 12:00:00.000"
        _years[2012-2000] = 1; _years[2016-2000] = 1; _years[2020-2000] = 1;
        _months[0] = 1;
        _days[0] = 1;
        Arrays.fill(_weekDays, (byte)1); // все дни недели
        _hours[12] = 1;
        _minutes[0] = 1;
        _seconds[0] = 1;
        _milliseconds[0] = 1;
    }


    public Schedule(String schedule) throws ScheduleFormatException
    {
        if ( schedule == null )
            throw new IllegalArgumentException();

        int dotPosition1 = schedule.indexOf('.');
        int dotPosition2 = nextChar(schedule, '.', dotPosition1);
        int dotPosition3 = nextChar(schedule, '.', dotPosition2);

        int colonPosition1 = schedule.indexOf(':');
        int colonPosition2 = nextChar(schedule, ':', colonPosition1);
        if ( colonPosition2 < 0 )
            throw new ScheduleFormatException("Неверно задано время", schedule);

        int spacePosition1 = schedule.indexOf(' ');
        int spacePosition2 = nextChar(schedule, ' ', spacePosition1);


        StringEx scheduleString = new StringEx(schedule);

        String yearPart;
        String monthPart;
        String dayPart;
        String weekDayPart;
        String hourPart;
        String minutePart;
        String secondsPart;
        String millisecondPart;

        if ((dotPosition1 >=0) &&
            (dotPosition2 >= 0) &&
            (dotPosition3 >= 0) &&
            (colonPosition1 > dotPosition2) &&
            (dotPosition3 > colonPosition2))
        {
            // yyyy.MM.dd w HH:mm:ss.fff  или  yyyy.MM.dd HH:mm:ss.fff
            yearPart = scheduleString.substring(0, dotPosition1);
            monthPart = scheduleString.substring(dotPosition1 + 1, dotPosition2 - dotPosition1 - 1);
            dayPart = scheduleString.substring(dotPosition2 + 1, spacePosition1 - dotPosition2 - 1);

            weekDayPart = (spacePosition2 < 0) ?
                    _placeholder
                :
                    scheduleString.substring(spacePosition1 + 1, spacePosition2  - spacePosition1 - 1);

            hourPart = (spacePosition2 < 0) ?
                    scheduleString.substring(spacePosition1 + 1, colonPosition1 - spacePosition1 - 1)
                :
                    scheduleString.substring(spacePosition2 + 1, colonPosition1 - spacePosition2 - 1);

            minutePart = scheduleString.substring(colonPosition1 + 1, colonPosition2 - colonPosition1 - 1);
            secondsPart = scheduleString.substring(colonPosition2 + 1, dotPosition3 - colonPosition2 - 1);
            millisecondPart = scheduleString.substring(dotPosition3 + 1);
        }
        else
        {
            if ((dotPosition1 >= 0) &&
                (dotPosition2 >= 0) &&
                (dotPosition3 < 0) &&
                (colonPosition1 > dotPosition2))
            {
                // yyyy.MM.dd w HH:mm:ss  или  yyyy.MM.dd HH:mm:ss
                yearPart = scheduleString.substring(0, dotPosition1);
                monthPart = scheduleString.substring(dotPosition1 + 1, dotPosition2 - dotPosition1 - 1);
                dayPart = scheduleString.substring(dotPosition2 + 1, spacePosition1 - dotPosition2 - 1);
                weekDayPart = (spacePosition2 < 0) ?
                        _placeholder :
                        scheduleString.substring(spacePosition1 + 1, spacePosition2 - spacePosition1 - 1);
                hourPart = (spacePosition2 < 0) ?
                        scheduleString.substring(spacePosition1 + 1, colonPosition1 - spacePosition1 - 1) :
                        scheduleString.substring(spacePosition2 + 1, colonPosition1 - spacePosition2 - 1);
                minutePart = scheduleString.substring(colonPosition1 + 1, colonPosition2 - colonPosition1 - 1);
                secondsPart = scheduleString.substring(colonPosition2 + 1);
                millisecondPart = _defaultMilliseconds;
            }
            else
            {
                if ((dotPosition1 >= 0) &&
                    (dotPosition2 < 0) &&
                    (colonPosition2 < dotPosition1))
                {
                    // HH:mm:ss.fff
                    yearPart = _placeholder;
                    monthPart = _placeholder;
                    dayPart = _placeholder;
                    weekDayPart = _placeholder;
                    hourPart = scheduleString.substring(0, colonPosition1);
                    minutePart = scheduleString.substring(colonPosition1 + 1, colonPosition2 - colonPosition1 - 1);
                    secondsPart = scheduleString.substring(colonPosition2 + 1, dotPosition1 - colonPosition2 - 1);
                    millisecondPart = scheduleString.substring(dotPosition1 + 1);
                }
                else
                {
                    if (dotPosition1 < 0)
                    {
                        // HH:mm:ss
                        yearPart = _placeholder;
                        monthPart = _placeholder;
                        dayPart = _placeholder;
                        weekDayPart = _placeholder;
                        hourPart = scheduleString.substring(0, colonPosition1);
                        minutePart = scheduleString.substring(colonPosition1 + 1, colonPosition2 - colonPosition1 - 1);
                        secondsPart = scheduleString.substring(colonPosition2 + 1);
                        millisecondPart = _defaultMilliseconds;
                    }
                    else
                    {
                        throw new ScheduleFormatException("Не возможно разобрать расписание", scheduleString.toString());
                    }
                }
            }
        }

        ParsePart (yearPart,        _years,        2000);
        ParsePart (monthPart,       _months,       1);
        ParsePart (dayPart,         _days,         1);
        ParsePart (weekDayPart,     _weekDays,     0);
        ParsePart (hourPart,        _hours,        0);
        ParsePart (minutePart,      _minutes,      0);
        ParsePart (secondsPart,     _seconds,      0);
        ParsePart (millisecondPart, _milliseconds, 0);
    }

    private int nextChar(String s, char c, int pos)
    {
        return  (pos < 0) ? -1 : s.indexOf(c, pos + 1);
    }



    public static void main(String args[]) throws Exception
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

        // Коварная дата для нашего расписания: "2012.01.01 12:00:00.001"
        // на одну миллисекунду больше, чем ближайшая меньшая дата
        // до следующей даты - 31,622,400,000 - миллисекунд
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2012);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        Date input = calendar.getTime();
        System.out.println("Входная дата: " + fmt.format(input));

        Schedule cron = new Schedule();
        Date output = cron.NearestEvent(input);
        System.out.println("Найденная дата: " + fmt.format(output));
    }















    public Date NearestEvent(Date t1)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(t1);

        //int counter = 0; // счетчик итераций

        while (true)
        {
            int y = calendar.get(Calendar.YEAR);
            int yearOffset = y - 2000;
            if ((yearOffset < 0) || (yearOffset >= _years.length))
            {
                throw new IndexOutOfBoundsException("Year out of range");
            }

            if (_years[yearOffset] > 0)
            {
                int m = calendar.get(Calendar.MONTH);
                if (_months[m] > 0)
                {
                    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                    boolean isLastDayInMonth = (day+1) == maxDays;
                    // 32-й день означает последнее число месяца
                    if (((_days[day] > 0) || (isLastDayInMonth && (_days[31] > 0))) && (_weekDays[dayOfWeek] > 0))
                    {
                        int h = calendar.get(Calendar.HOUR_OF_DAY);
                        if ( _hours[h] > 0 )
                        {
                            int n = calendar.get(Calendar.MINUTE);
                            if ( _minutes[n] > 0 )
                            {
                                int s = calendar.get(Calendar.SECOND);
                                if ( _seconds[s] > 0 )
                                {
                                    int ms = calendar.get(Calendar.MILLISECOND);
                                    do {
                                        //counter++;
                                        if ( _milliseconds[ms] > 0 )
                                        {
                                            calendar.set(Calendar.MILLISECOND, ms);
                                            //System.out.println("Сделано итераций: " + counter);
                                            return calendar.getTime();
                                        }
                                        ms++;
                                    } while (ms < _milliseconds.length);
                                    calendar.set(Calendar.MILLISECOND, 0);
                                }
                                calendar.set(Calendar.MILLISECOND, 0);
                                calendar.add(Calendar.SECOND, 1);
                            }
                            else
                            {
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);
                                calendar.add(Calendar.MINUTE, 1);
                            }
                        }
                        else
                        {
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            calendar.add(Calendar.HOUR_OF_DAY, 1);
                        }
                    }
                    else
                    {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
                else
                {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.MONTH, 1);
                }
            }
            else
            {
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.YEAR, 1);
            }
            //counter++;
        } // end of while
    }


    public Date NearestPrevEvent(Date d)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Date NextEvent(Date d)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Date PrevEvent(Date d)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }




    private void ParsePart(String partStr, byte[] allowedValues, int offset) throws ScheduleFormatException
    {
        // Каждую часть даты/времени можно задавать в виде списков и диапазонов.
        // Например:
        //     1,2,3-5,10-20/3
        //     означает список 1,2,3,4,5,10,13,16,19
        // Дробью задается шаг в списке.
        // Звездочка означает любое возможное значение.

        if (partStr.length() < 1)
        {
            throw new ScheduleFormatException("Invalid part", partStr);
        }

        int idx = 0;
        while (idx < partStr.length())
        {
            int number1;
            int number2;
            if (partStr.charAt(idx) == '*')
            {
                idx++;
                number1 = 0;
                number2 = allowedValues.length - 1;
            }
            else
            {
                number1 = ParseNumber (partStr, idx) - offset;
                idx = nextIdx(partStr, idx);

                if ((number1 < 0) || (number1 >= allowedValues.length))
                {
                    throw new ScheduleFormatException ("Number out of range.", partStr);
                }

                number2 = number1;
                if ((idx < partStr.length()) && (partStr.charAt(idx) == '-'))
                {
                    idx++;
                    number2 = ParseNumber (partStr, idx) - offset;
                    idx = nextIdx(partStr, idx);

                    if ((number2 < 0) || (number2 >= allowedValues.length))
                    {
                        throw new ScheduleFormatException ("Number out of range.", partStr);
                    }
                }
            }

            int period = 1;
            if (idx < partStr.length())
            {
                switch (partStr.charAt(idx))
                {
                    case '/':
                        idx++;
                        period = ParseNumber (partStr, idx);
                        idx = nextIdx(partStr, idx);
                        break;
                    case ',':
                        idx++;
                        break;
                    default:
                        throw new ScheduleFormatException ("Invalid character.", partStr);
                }
            }

            for (int i = number1; i <= number2; i += period)
            {
                allowedValues[i] = 1;
            }
        }
    }

    private int ParseNumber(String s,  int idx)
    {
        int number = 0;
        char c;

        while ( idx < s.length() )
        {
            c = s.charAt(idx);
            if ( (c < '0') || (c > '9') ) break;


            number = 10 * number + c - '0';
            idx++;
        }

        return number;
    }

    private int nextIdx(String s,  int idx)
    {
        char c;

        while ( idx < s.length() )
        {
            c = s.charAt(idx);
            if ( (c < '0') || (c > '9') ) break;


            idx++;
        }

        return idx;
    }

    private static class StringEx
    {
        private final String source;

        public StringEx(String schedule)
        {
            this.source = schedule;
        }

        public String substring(int start, int len)
        {
            return source.substring(start, start + len);
        }

        public String substring(int start)
        {
            return source.substring(start);
        }

        @Override
        public String toString() {
            return source;
        }
    }
}
