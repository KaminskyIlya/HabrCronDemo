package speed;

import com.habr.cron.Cron;
import com.habr.cron.dev.Schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Тест скорости выполнения оптимизированного алгоритма от Ilya Kaminsky.
 */
public class DevBenchmark
{
    private static final int LOOP_COUNT = 5000000; // 5 миллионов итераций для проверки
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

    public static void main(String args[]) throws Exception
    {
        for (String data[] : TEST_DATES)
        {
            Date date = fmt.parse(data[1]);
            String schedule = data[0];
            Cron cron;
            long nanos;


            cron = new Schedule(schedule);
            nanos = runCronBenchmark(cron, date);
            System.out.println(
                    String.format("[Dev] %s %s  - %d nsec",
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
    private static final String[][] TEST_DATES = new String[][] {
            // сложная дата для алгоритма NoVar; ожидается 2016.01.01 12:00:00
            {"*/4.01.01 12:00:00.000", "2012.01.01 12:00:00.001"},
            // генерация событий с частотой 1мсек; ожидается 2021.30.09 12:00:00.002
            {"*.*.* *:*:*.*",          "2021.30.09 12:00:00.002"},

            // Примеры от cadovvl
            {"*.4.6,7 * *:*:*.1,2,3-5,10-20/3",          "2001.01.01 00:00:00.000"},
            {"*.4.6,7 * *:*:*.1,2,3-5,10-20/3",          "2080.05.05 12:00:00.000"},
            {"2100.12.31 23:59:59.999",          "2000.01.01 00:00:00.000"},
            {"2100.12.31 23:59:59.999",          "2080.05.05 00:00:00.000"},
    };
}
