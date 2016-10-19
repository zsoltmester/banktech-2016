import java.util.Map;
import java.util.HashMap;

public class ThreadSleeper {

    private Map<String, Thread> registeredThreads =  new HashMap<>();

    public synchronized void registerThread(String name, Thread thread){
        if (name==null) throw new IllegalArgumentException();
        registeredThreads.put(name, thread);
    }

    public synchronized void sleepThread(String name, int duration) throws InterruptedException {
        if (registeredThreads.containsKey(name)){
            registeredThreads.get(name).sleep(duration);
        }
    }

    public static void main(String[] args) throws Exception {
        // B - nincs NPE
        ThreadSleeper ts = new ThreadSleeper();
        ts.registerThread("npe", null);
        ts.sleepThread("npe", 1);
        ts.sleepThread(null, 1);
    }
}
