package speed;

import com.habr.cron.Cron;
import com.habr.cron.novar.Schedule;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Сравнительное измерение скорости работы двух алгоритмов из ветки dev (текущая разработка)
 * и ветки opt (стабильная оптимизированная версия).
 */
public class DevVsOpt
{
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
    private static final int LOOP_COUNT = 100000; // 10 тыс. раз повторений для сглаживания погрешности измерений

    public static void main(String args[]) throws Exception
    {
        for (String data[] : TEST_DATES)
        {
            Date date = fmt.parse(data[1]);
            String schedule = data[0];
            Cron cron;
            long nanos;

            cron = new com.habr.cron.dev.Schedule(schedule);
            nanos = runCronBenchmark(cron, date);
            System.out.println(
                    String.format("[Develop] %s %s  - %d nsec",
                            schedule,
                            data[1],
                            nanos)
            );

            cron = new com.habr.cron.opt.Schedule(schedule);
            nanos = runCronBenchmark(cron, date);
            System.out.println(
                    String.format("[Optimized] %s %s  - %d nsec",
                            schedule,
                            data[1],
                            nanos)
            );
        }
    }


    private static long runCronBenchmark(Cron schedule, Date date) throws Exception
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++)
        {
            schedule.NearestEvent(date);
        }
        long n2 = System.nanoTime();

        return (n2 - n1) / LOOP_COUNT;
    }


    // расписания и даты, подаваемые на вход
    private static final String[][] TEST_DATES = new String[][]
            {
                    // compare milliseconds performance
                    {"*:*:*.100-200,150-160",               "2021.01.01 00:00:00.000"}, // expected 01.01.2021 00:00:00.100
                    {"*:*:*.100-200,400-600",               "2021.01.01 00:00:00.000"}, // expected 01.01.2021 00:00:00.100
                    {"*:*:*.100-200,400-600",               "2021.01.01 00:00:00.300"}, // expected 01.01.2021 00:00:00.400
                    {"*:*:*.10-20,120-130,140-150,260-290,310-315,410-420,520-530,640-650",
                            "2021.01.01 00:00:00.300"}, // expected 01.01.2021 00:00:00.310
                    {"*:*:*.10-20,120-130,140-150,260-290,310-315,410-420,520-530,640-650,760-790,970-999",
                            "2021.01.01 00:00:00.300"}, // expected 01.01.2021 00:00:00.310

            };

}