package speed;

import com.habr.cron.Cron;
import com.habr.cron.novar.Schedule;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Тест скорости выполнения разных алгоритмов.
 * Измеренная скорость: 52мкс (для худшего случая)
 */
public class Benchmark
{
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
    private static final int LOOP_COUNT = 100000; // 100 тыс. раз повторений для сглаживания погрешности измерений
                                                  // можно было бы и больше, но алгоритм NoVar очень медленный

    public static void main(String args[]) throws Exception
    {
        for (String data[] : TEST_DATES)
        {
            Date date = fmt.parse(data[1]);
            String schedule = data[0];
            Cron cron;
            long nanos;


//            cron = new com.habr.cron.novar.Schedule(schedule);
//            nanos = runCronBenchmark(cron, date);
//            System.out.println(
//                    String.format("[NoVar] %s %s  - %d nsec",
//                            schedule,
//                            data[1],
//                            nanos)
//            );


//            cron = new com.habr.cron.ilya.Schedule(schedule);
//            nanos = runCronBenchmark(cron, date);
//            System.out.println(
//                    String.format("[Ilya] %s %s  - %d nsec",
//                            schedule,
//                            data[1],
//                            nanos)
//            );


            cron = new com.habr.cron.dev.Schedule(schedule);
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
            // генерация событий с частотой 1мсек; ожидается получение 2021.30.09 12:00:00.003
            {"*.*.* *:*:*.*",          "2021.30.09 12:00:00.002"},

            // Примеры от cadovvl
            {"*.4.6,7 * *:*:*.1,2,3-5,10-20/3",          "2001.01.01 00:00:00.000"},
            {"*.4.6,7 * *:*:*.1,2,3-5,10-20/3",          "2080.05.05 12:00:00.000"},
            {"2100.12.31 23:59:59.999",          "2000.01.01 00:00:00.000"},
            {"2100.12.31 23:59:59.999",          "2080.05.05 00:00:00.000"},

            // очень сложная дата для всех алгоритмов: ожидается 29.02.2048 12:00:00
            {"*.02.29 6 12:00:00",               "2021.01.01 12:00:00.000"},

            // complexity schedule; expected 30.04.2021 12:14:34.000
            {"*.*.20-32/5 5 12:14:34",              "2021.01.31 12:14:33.177"},

    };

}
