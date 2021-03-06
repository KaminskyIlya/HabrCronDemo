package com.habr.cron;

import java.util.Date;

/**
 * Интерфейс, соответствующий тестовому заданию.
 */
public interface Cron
{
    /**
     * Возвращает следующий ближайший к заданному времени момент в расписании или
     * само заданное время, если оно есть в расписании.
     *
     * @param d заданное время
     * @return ближайший момент времени в расписании
     */
    Date NearestEvent(Date d);

    /**
     * Возвращает предыдущий ближайший к заданному времени момент в расписании или
     * само заданное время, если оно есть в расписании.
     *
     * @param d заданное время
     * @return ближайший момент времени в расписании
     */
    Date NearestPrevEvent(Date d);

    /**
     * Возвращает следующий момент времени в расписании.
     *
     * @param d заданное время
     * @return ближайший момент времени в расписании
     */
    Date NextEvent(Date d);

    /**
     * Возвращает предыдущий момент времени в расписании.
     *
     * @param d заданное время
     * @return ближайший момент времени в расписании
     */
    Date PrevEvent(Date d);
}
