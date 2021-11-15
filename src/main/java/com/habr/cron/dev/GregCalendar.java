package com.habr.cron.dev;

import java.util.Date;
import java.util.TimeZone;

/**
 * Optimized version of Gregorian calendar system.
 * Minimalistic version.
 */
public class GregCalendar
{
    public int year;
    public int month;
    public int day;

    public int hours;
    public int minutes;
    public int seconds;
    public int milliseconds;


    private static final int defaultTimeZoneOffset = TimeZone.getDefault().getRawOffset();


    public GregCalendar(Date date, TimeZone tz)
    {
        this(date.getTime(), tz.getRawOffset());
    }

    public GregCalendar(long timestamp)
    {
        this(timestamp, defaultTimeZoneOffset);
    }


    private static final int[] ELAPSED_DAYS = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};

    /*
        Very optimized version.
        IMPORTANT NOTICE: works correct only range 2000..2100; Don't use out of interval
     */
    public GregCalendar(long timestamp, int tzOffset)
    {
        int time = (int) (timestamp % 86400000);
        int days = (int) (timestamp / 86400000);     // days since January 1, 1970

        time += tzOffset;

        milliseconds = time % 1000;     time /= 1000;
        seconds =      time % 60;       time /= 60;
        minutes =      time % 60;       time /= 60;
        hours =        time % 24;

        int y = (days<<2)/1461; // Gregorian years elapsed  since 1970 ( = days / 365.25 )
        int l = (y+1)>>2;     // Gregorian leap days since 1970  ( = (y+1)/4 )
        int d = days - y*365 - l;   // Number of day passed from begin of year
        if ( d > 364 && ((y+2)&3) > 0 ) // overflow control ( (y+2)&3 = is this a leap year?)
        {
            y++;
            l = (y+1)>>2;
            d = days - y*365 - l;
        }

        y += 1970;

        int m;
        int lp = (y & 3) == 0 && y != 2100 ? 1 : 0;
        int lm = 59+lp;

        if ( d < lm ) // until 28/29th Feb
        {
            m = d / 31;
            d -= ELAPSED_DAYS[m] - 1;
        }
        else // since 1st March
        {
            m = (d - lm) / 31 + 2; // March
            int max = MAX_DAYS[m];

            d -= ELAPSED_DAYS[m] - 1 + lp;
            if ( d > max ) { d -= max; m++; }
        }

        year = y;
        month = m + 1;
        day = d;
    }

    public Date asDate() // returns always in UTC time!
    {
        int y = year - 1970;
        int leaps = (y + 1)>>2;
        int days = y * 365 + leaps - 1;
        int lp = month > 2 && (year & 3) == 0 && year != 2100 ? 1 : 0;

        days += ELAPSED_DAYS[month-1] + lp;

        days += day; // days since 1 Jan 1970

        int time = hours;
        time = time * 60 + minutes;
        time = time * 60 + seconds;
        time = time * 1000 + milliseconds;

        return new Date(days * 86400000L + time);
    }



    public boolean isLeap()
    {
        return isLeap(year) == 1;
    }

    public static int isLeap(int year)
    {
        return (4 - year % 4)/4 - (100 - year % 100)/100 + (400 - year % 400)/400; // 1 or 0 (zero for non leap yeas);
    }

    public boolean isCorrect()
    {
        return day <= maxDays(year, month);
    }

    public boolean isCorrectDay(int day)
    {
        return day <= maxDays(year, month);
    }

    private static final int MAX_DAYS[] = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public int getMaxDay()
    {
        return maxDays(year, month);
    }

    // returns the number of days in a month (numbering months from 1)
    public static int maxDays(int year, int month)
    {
        // this algorithm is faster than the lower one (in comments)
        int result = MAX_DAYS[month-1];
        if ( month == 2 ) result += isLeap(year);
        return result;

        /*int leap = isLeap(year); // 1 или 0

        int m = month;
        int base = 30;     // the base number of days of the month
        int sub = m / 8;   // equal to 1, starting from August
        int add = (m + sub) % 2;   // equal to 1 for January, March, May, July, September, November
        int feb = m == 2 ? 1 : 0;

        return base + add - (2 - leap) * feb;*/
    }

    /**
     * Day of week
     * @return 0 - sunday, 1 - monday, ... 6 - saturday
     */
    public int getDayOfWeek()
    {
        int a = (14 - month)/12;
        int y = year - a;
        int m = month + 12 * a - 2;
        return (day + (31 * m)/12 + y + y/4 - y/100 + y/400) % 7;
    }



    public void setValue(int fieldId, int value)
    {
        switch (fieldId)
        {
            case 0: //Calendar.YEAR:
                year = value;       break;

            case 1: //Calendar.MONTH:
                month = value;      break;

            case 2: //Calendar.DAY_OF_MONTH:
                day = value;        break;

            case 3: //Calendar.HOUR_OF_DAY:
                hours = value;      break;

            case 4: //Calendar.MINUTE:
                minutes = value;    break;

            case 5: //Calendar.SECOND:
                seconds = value;    break;

            case 6: //Calendar.MILLISECOND:
                milliseconds = value;   break;
        }
    }

    public int getValue(int fieldId)
    {
        switch (fieldId)
        {
            case 0: //Calendar.YEAR:
                return year;

            case 1: //Calendar.MONTH:
                return month;

            case 2: //Calendar.DAY_OF_MONTH:
                return day;

            case 3: //Calendar.HOUR_OF_DAY:
                return hours;

            case 4: //Calendar.MINUTE:
                return minutes;

            case 5: //Calendar.SECOND:
                return seconds;

            case 6: //Calendar.MILLISECOND:
                return milliseconds;
        }

        throw new AssertionError("This code is MUST BE unreachable!");
    }
}
