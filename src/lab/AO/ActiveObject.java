package lab.AO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActiveObject {
    final long workingTime;
    final long producerConsumerWorkingTime;
    final int meanwhileTasksNo;
    final int bufferSize;
    final int producersNo;
    final int consumersNo;
    final Proxy proxy;
    final Servant servant;

    private final List<Thread> producersConsumersList;

    public ActiveObject(long workingTime, long producerConsumerWorkingTime, int meanwhileTasksNo, int bufferSize, int producersNo, int consumersNo){

        this.workingTime = workingTime;
        this.producerConsumerWorkingTime = producerConsumerWorkingTime;
        this.meanwhileTasksNo = meanwhileTasksNo;
        this.bufferSize = bufferSize;
        this.producersNo = producersNo;
        this.consumersNo = consumersNo;

        servant = new Servant(bufferSize,workingTime);
        proxy = new Proxy(servant);
        producersConsumersList = new ArrayList<>();

        for(int i =0;i<consumersNo;i++){
            Thread thread = new Thread(new Consumer(bufferSize,proxy,producerConsumerWorkingTime,meanwhileTasksNo));
            thread.setName(String.format( "Consumer no:%s", i));
            producersConsumersList.add(thread);
        }

        for(int i =0;i<producersNo;i++){
            Thread thread = new Thread(new Producer(bufferSize,proxy,producerConsumerWorkingTime,meanwhileTasksNo));
            thread.setName(String.format( "Producer no:%s", i));
            producersConsumersList.add(thread);
        }

        Collections.shuffle(producersConsumersList);
    }

    public void startWorking(){
        for(Thread thread : producersConsumersList)
            thread.start();
    }

    public void waitForWorkEnd(){
        for(Thread thread : producersConsumersList) {
            try {
//                System.out.println("Waiting for thread: "+thread.getName());
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
