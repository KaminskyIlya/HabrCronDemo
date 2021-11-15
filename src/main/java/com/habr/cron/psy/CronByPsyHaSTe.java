package com.habr.cron.psy;

import com.habr.cron.Cron;

import java.util.Date;

/**
 * Реализация задания от PzixelLovaSchedule (PsyHaSTe)
 */
public class CronByPsyHaSTe implements Cron
{
    public CronByPsyHaSTe()
    {
        this("*.*.* * *:*:*.*");
    }

    public CronByPsyHaSTe(String scheduleString)
    {

    }

/*
    private final ScheduleInterval yearInterval;
    private final ScheduleInterval monthInterval;
    private final ScheduleInterval dayInterval;
    private final ScheduleInterval dayOfWeekInterval;
    private final ScheduleInterval hoursInterval;
    private final ScheduleInterval minutesInterval;
    private final ScheduleInterval secondsInterval;
    private final ScheduleInterval millisInterval;
*/



    public Date NearestEvent(Date d)
    {
        return null;
    }

    public Date NearestPrevEvent(Date d)
    {
        return null;
    }

    public Date NextEvent(Date d)
    {
        return null;
    }

    public Date PrevEvent(Date d)
    {
        return null;
    }
}
