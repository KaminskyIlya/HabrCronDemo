package com.habr.cron.ilya;

import java.util.Calendar;

/**
 *  Constants and constraints for schedule elements and used format.
 */
enum ScheduleElements
{
    // Java Calendar field name, bounds according schedule format,  bounds specified for Java Calendar

    YEAR(Calendar.YEAR, 2000, 2100,             2000, 2100),

    MONTH(Calendar.MONTH, 1, 12,                0, 11),

    DAY_OF_MONTH(Calendar.DAY_OF_MONTH, 1, 31,  1, 31),

    DAY_OF_WEEK(Calendar.DAY_OF_WEEK, 0, 6,     1, 7),

    HOURS(Calendar.HOUR_OF_DAY, 0, 23,          0, 23),

    MINUTES(Calendar.MINUTE, 0, 59,             0, 59),

    SECONDS(Calendar.SECOND, 0, 59,             0, 59),

    MILLIS(Calendar.MILLISECOND, 0, 999,        0, 999);


    /**
     * field of java.util.Calendar
     */
    final int fieldId;
    /**
     * The minimum value according to the schedule format
     */
    final int min;
    /**
     * The maximum value according to the schedule format
     */
    final int max;
    /**
     * The minimum value specified for the Java's Calendar class.
     */
    final int javaMin;
    /**
     * The maximum value specified for the Java's Calendar class.
     */
    final int javaMax;

    ScheduleElements(int fieldId, int min, int max, int javaMin, int javaMax)
    {
        this.fieldId = fieldId;
        this.min = min;
        this.max = max;
        this.javaMin = javaMin;
        this.javaMax = javaMax;
    }
}
