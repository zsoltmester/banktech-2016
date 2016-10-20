import java.lang.management.ManagementFactory;

public class Threads
{
    public static void main(String[] args)
    {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0, pid.indexOf('@'));
        System.out.println(pid);
        new Thread(new Example(10)).start();
        new Thread(new Example(100)).start();
        new Thread(new Example(1000)).start();
    }

    static class Example implements Runnable
    {
        final int number;

        Example(int number)
        {
            this.number = number;
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                System.err.println("Sleep interrupted!");
            }
            System.err.print(number + " ");
        }
    }
}
