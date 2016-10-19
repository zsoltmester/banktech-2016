import java.util.*;

public class ThreadWork {

    private static final int NR_OF_THREADS = 1000;

    public static void main(String[] args) {
        List<Thread> threads = fillTheads(NR_OF_THREADS);
        threads.stream().forEach(Thread::start);
    }

    public static List fillTheads(int nrOfThreads) {
        List<Thread> l = new ArrayList<>();
        for(int i = 0; i < nrOfThreads; i++) {
            l.add(new Thread(
                () -> { System.out.println(Thread.currentThread().getName() + " started"); }
            ));
        }
        return l;
    }
}
