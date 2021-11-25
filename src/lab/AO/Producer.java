package lab.AO;

import lab.Globals.Global;

import java.util.Random;

public class Producer implements Runnable{
    private final int maxPortion;
    private static final Random random = new Random(1);
    private final Proxy proxy;
    private final long workTime;
    private final int meanwhileTasksNo;

    public Producer(int maxBufferSize, Proxy proxy, long workTime, int meanwhileTasksNo){
        maxPortion = maxBufferSize/2;
        this.proxy = proxy;
        this.workTime = workTime;
        this.meanwhileTasksNo = meanwhileTasksNo;
    }

    @Override
    public void run() {
        while(Global.OPERATIONS>0){
//            System.out.println("OPERATIONS: "+Global.OPERATIONS);
            Future future = proxy.produce(getAmountToProduce());
            while (!future.isReady()  )     //&& Global.OPERATIONS>0                       // asynchroniczne oczekiwanie na wykonanie zleconego zadania
                this.doSomeWork();                              // producent w czasie czekania wykonuje inne zadania, sprawdzając po
                                                                // każdym zadaniu czy zadanie na zasobach współdzielonych zostało wykonane
        }
    }

    public void doSomeWork(){
        try {
            Thread.sleep(workTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println("Waiting: "+Thread.currentThread().getName());
//        Global.setExtraTasksDone(Global.getExtraTasksDone() + 1);
    }

    private int getAmountToProduce(){
        return random.nextInt(maxPortion)+1;
    }
}
