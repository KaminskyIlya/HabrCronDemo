package com.habr.cron;

import com.habr.cron.ilya.ScheduleFormatException;

/**
 * Абстрактная реализация интерфейса планировщика
 */
public abstract class CronBase implements Cron
{
    /**
     * Создает пустой экземпляр, который будет соответствовать
     * расписанию типа "*.*.* * *:*:*.*" (раз в 1 мс).
     */
    protected CronBase()
    {

    }

    /**
     * Создает экземпляр из строки с представлением расписания.
     * @param schedule строка расписания
     * Формат строки:
     *     yyyy.MM.dd w HH:mm:ss.fff
     *     yyyy.MM.dd HH:mm:ss.fff
     *     HH:mm:ss.fff
     *     yyyy.MM.dd w HH:mm:ss
     *     yyyy.MM.dd HH:mm:ss
     *     HH:mm:ss
     * Где yyyy - год (2000-2100)
     *     MM - месяц (1-12)
     *     dd - число месяца (1-31 или 32). 32 означает последнее число месяца
     *     w - день недели (0-6). 0 - воскресенье, 6 - суббота
     *     HH - часы (0-23)
     *     mm - минуты (0-59)
     *     ss - секунды (0-59)
     *     fff - миллисекунды (0-999). Если не указаны, то 0
     * Каждую часть даты/времени можно задавать в виде списков и диапазонов.
     * Например:
     *     1,2,3-5,10-20/3
     *     означает список 1,2,3,4,5,10,13,16,19
     * Дробью задаетс¤ шаг в списке.
     * Звездочка означает любое возможное значение.
     * Например (дл¤ часов):
     *     * /4
     *     означает 0,4,8,12,16,20
     * Вместо списка чисел месяца можно указать 32. Это означает последнее
     * число любого месяца.
     * Пример:
     *     *.9.* /2 1-5 10:00:00.000
     *     означает 10:00 во все дни с пн. по пт. по нечетным числам в сентябре
     *     *:00:00
     *     означает начало любого часа
     *     *.*.01 01:30:00
     *     означает 01:30 по первым числам каждого месяца
     */
    protected CronBase(String schedule)
    {

    }

}