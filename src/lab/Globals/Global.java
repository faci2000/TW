package lab.Globals;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Global{
    public static Map<String,Integer> producersWaitingTimes = new HashMap<>();
    public static Map<String,Integer> consumersWaitingTimes = new HashMap<>();
    public static Random RAND = new Random(1);
    public static int PRODUCERS = 10;
    public static int CONSUMERS = 10;
    public static int MAX_PRODUCTS = 100;
    public static int OPERATIONS = 100000;
    public static long PRODUCED = 0;
    public static long CONSUMED = 0;
    public static long CPUTIME = 0;
    private static long EXTRA_TASKS_DONE = 1;

    public static int getAmountToProduceConsume(){
        return RAND.nextInt(MAX_PRODUCTS/2)+1;
    }

    synchronized public static long getExtraTasksDone() {
        return EXTRA_TASKS_DONE;
    }

    synchronized public static void setExtraTasksDone(long extraTasksDone) {
        EXTRA_TASKS_DONE = extraTasksDone;
    }

}
