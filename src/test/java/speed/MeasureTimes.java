package speed;

import com.habr.cron.Cron;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Измерение времени выполнения оригинального алгоритма автора.
 * Используется для построения графика зависимости времени выполнения от входного аргумента.
 */
public class MeasureTimes
{
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

    public static void main(String args[]) throws Exception
    {
        Date date = fmt.parse("2012.01.01 12:00:00.001");

        //Cron novar = new com.habr.cron.novar.Schedule("*/4.01.01 12:00:00.000");
        //plotTimes(novar, date, (int) TimeUnit.MILLISECONDS.toSeconds(1), 300, 1000);

//        Cron ilya = new com.habr.cron.ilya.Schedule("*/4.01.01 12:00:00.000");
//        plotTimes(ilya, date, (int) TimeUnit.DAYS.toSeconds(31), 300, 10000);

        Cron dev = new com.habr.cron.dev.Schedule("*/4.01.01 12:00:00.000");
        plotTimes(dev, date, (int) TimeUnit.HOURS.toSeconds(1), 300, 50000);
    }


    /**
     * @param schedule планировщик расписания
     * @param date начальная дата для приращения
     * @param step приращение времени в цикле в сек.
     * @param count кол-во данных для вывода
     * @param loops кол-во циклов измерения функции (для усреднения показателей), чем выше значение,
     *              тем точнее измерение, но дольше выполнение
     */
    private static void plotTimes(Cron schedule, Date date, int step, int count, int loops) throws Exception
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        for (int i = 0; i < count; i++)
        {
            long nanos = measureNearestEvent(schedule, calendar.getTime(), loops);

            System.out.println(
                    nanos
                            + "\t" + fmt.format(calendar.getTime())
            );

            calendar.add(Calendar.SECOND, step);
        }
    }




    private static long measureNearestEvent(Cron schedule, Date date, int loops) throws Exception
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < loops; i++)
        {
            schedule.NearestEvent(date);
        }
        long n2 = System.nanoTime();

        return (n2 - n1) / loops;
    }

}
