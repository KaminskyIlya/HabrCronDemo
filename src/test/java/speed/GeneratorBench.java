package speed;

import com.habr.cron.ScheduleEventsGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GeneratorBench
{
    private static final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    private static final int LOOP_COUNT = 100000;

    public static void main(String args[]) throws Exception
    {
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (String data[] : TEST_DATES)
        {
            Date date = fmt.parse(data[1]);
            String schedule = data[0];
            long nanos;

            {
                com.habr.cron.dev.Schedule cron = new com.habr.cron.dev.Schedule(schedule);
                ScheduleEventsGenerator generator = cron.getEventsGenerator(date, true);

                nanos = runCronBenchmark(generator);
                System.out.println(
                        String.format("[Dev.Gen] %s [%s .. %s] - %d nsec",
                                schedule,
                                data[1],
                                fmt.format(generator.last()),
                                nanos)
                );
            }
            {
                com.habr.cron.opt.Schedule cron = new com.habr.cron.opt.Schedule(schedule);
                ScheduleEventsGenerator generator = cron.getEventsGenerator(date, true);

                nanos = runCronBenchmark(generator);
                System.out.println(
                        String.format("[Opt.Gen] %s [%s .. %s] - %d nsec",
                                schedule,
                                data[1],
                                fmt.format(generator.last()),
                                nanos)
                );
            }
        }
    }

    private static long runCronBenchmark(ScheduleEventsGenerator generator) throws Exception
    {
        long n1 = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++)
        {
            generator.next();
        }
        long n2 = System.nanoTime();

        return (n2 - n1) / LOOP_COUNT;
    }

    private static final String[][] TEST_DATES = new String[][] {
            // generate with step 1ms
            {"*:*:*.*", "17.11.2021 14:00:00.001"},
            // generate with step 5ms
            {"*:*:*.5", "17.11.2021 14:00:00.000"},
            // generate with step 1sec
            {"*:*:*", "17.11.2021 14:00:00.000"},
            // complexity schedule; expected 26.01.2026 12:46:39.320 at the end
            {"*.1,10.5-26/7 1 12:*:*.320", "01.01.2000 00:00:00.000"},
            // complexity schedule; expected 31.03.2027 12:46:39.000 at the end
            {"*.*.31 3 12:*:*", "01.01.2000 00:00:00.000"},

            {"*:*:*.100-200,400-600", "01.01.2021 00:00:00.000"},
            {"*:*:*.100-200/10,400-600/10", "01.01.2021 00:00:00.000"},
            {"*:*:*.100-200,150-160", "01.01.2021 00:00:00.000"}, //will converted to single interval
            {"*:*:*.100-101,150-151", "01.01.2021 00:00:00.000"},
    };
}
