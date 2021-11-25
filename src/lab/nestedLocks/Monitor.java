package lab.nestedLocks;

import lab.Globals.Global;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor {
    public static long workingTime;
    final long producerConsumerWorkingTime;
    public static int meanwhileTasksNo;
    final int bufferSize;
    final int producersNo;
    final int consumersNo;
    private final List<Thread> producer_consumers;
    private final Product products;

    public Monitor(long workingTime, long producerConsumerWorkingTime, int meanwhileTasksNo, int bufferSize, int producersNo, int consumersNo){

        Monitor.workingTime = workingTime;
        this.producerConsumerWorkingTime = producerConsumerWorkingTime;
        Monitor.meanwhileTasksNo = meanwhileTasksNo;
        this.bufferSize = bufferSize;
        this.producersNo = producersNo;
        this.consumersNo = consumersNo;

        products = new Product(producerConsumerWorkingTime);

        producer_consumers = new ArrayList<>();
        for(int i = 0; i< consumersNo; i++){
            Thread thread = new Thread(new Consumer(products));
            thread.setName(String.format( "Consumer no:%s", i));
            producer_consumers.add(thread);
            Global.consumersWaitingTimes.put(thread.getName(),0);
        }
        for(int i = 0; i< producersNo; i++){
            Thread thread = new Thread(new Producer(products));
            thread.setName(String.format( "Producer no:%s", i));
            producer_consumers.add(thread);
            Global.producersWaitingTimes.put(thread.getName(),0);
        }
        Collections.shuffle(producer_consumers);

    }
    public void startWorking(){
        for(Thread thread : producer_consumers){
            thread.start();
        }
    }

    public void waitForWorkEnd(){
        for(Thread thread : producer_consumers){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    public static void main(String[] args) throws InterruptedException {
////        Product products = new Product();
//        List<Thread> producer_consumers = new ArrayList<>();
//        for(int i = 0; i< Global.CONSUMERS; i++){
//            Thread thread = new Thread(new Consumer(products));
//            thread.setName(String.format( "Consumer no:%s", i));
//            producer_consumers.add(thread);
//            Global.consumersWaitingTimes.put(thread.getName(),0);
//        }
//        for(int i = 0; i< Global.PRODUCERS; i++){
//            Thread thread = new Thread(new Producer(products));
//            thread.setName(String.format( "Producer no:%s", i));
//            producer_consumers.add(thread);
//            Global.producersWaitingTimes.put(thread.getName(),0);
//        }
//        Collections.shuffle(producer_consumers);
//        long startTime = System.nanoTime();
//        long cpuTime = 0;
//        for(Thread thread : producer_consumers){
//            thread.start();
//        }
//        for(Thread thread : producer_consumers){
//            thread.join();
//        }
//        long endTime = System.nanoTime();
//        System.out.printf("Consumed: %d\n",Global.CONSUMED);
//        System.out.printf("Produced: %d\n",Global.PRODUCED);
//        System.out.printf("Total: %d\n",Global.CONSUMED+Global.PRODUCED);
//        System.out.printf("TIME ELAPSED: %fs\n", (((double) endTime) - startTime)/1000000000);
//        System.out.printf("CPU TIME : %f\n", ((double) Global.CPUTIME)/1000000000);
//    }
}


class Producer implements Runnable {
    private final Product product;

    public Producer(Product product) {
        this.product = product;
    }

    public void run() {
        while (Global.OPERATIONS>0) {

            product.produce();

//            for(int i=0;i<10;i++){
//                try {
//                    Thread.sleep(Monitor.workingTime);
//                } catch (InterruptedException ignored) { }
//                Global.setExtraTasksDone(Global.getExtraTasksDone() + 1);
//            }

        }
        Global.CPUTIME += ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
    }
}
class Consumer implements Runnable {
    private final Product product;

    public Consumer(Product product) {
        this.product = product;
    }

    public void run() {
        while (Global.OPERATIONS>0) {

            product.consume();

//            try {
//                Thread.sleep(Monitor.workingTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            Global.setExtraTasksDone(Global.getExtraTasksDone() + 1);
        }
        Global.CPUTIME += ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
    }
}


class Product {
    private volatile int amount = 0;
    private final List<String> visitors = new ArrayList<>();
    ReentrantLock productLock = new ReentrantLock(true);
    ReentrantLock producersLock = new ReentrantLock(true);
    ReentrantLock consumersLock = new ReentrantLock(true);
    Condition productCondition = productLock.newCondition();
    long workingTime;

    public Product(long workingTime){
        this.workingTime=workingTime;
    }

    public void produce() {
        try{
            producersLock.lock();

//            // ------------- Tracking -----------------
//            System.out.printf("\n-------------%s-------------\n",Thread.currentThread().getName());
//            System.out.printf("lock: %d\n",productLock.getQueueLength());
//            System.out.println("\n----------------------------------\n");
//            // ----------------------------------------

            int to_produce = Global.getAmountToProduceConsume(); //take random amount

            productLock.lock();


            while(amount+to_produce> Global.MAX_PRODUCTS && Global.OPERATIONS>0){

//                // ------------- Tracking -----------------
//                int waitingTimes = Global.producersWaitingTimes.get(Thread.currentThread().getName());
//                Global.producersWaitingTimes.replace(Thread.currentThread().getName(),waitingTimes-1);
//                System.out.printf("%s goes to firstProducer set for %d time, wanting produce %d\n",Thread.currentThread().getName(),(waitingTimes-1)*-1,to_produce);
//                // ----------------------------------------

                productCondition.await();
            }

//            // ------------- Tracking -----------------
//            Global.producersWaitingTimes.replace(Thread.currentThread().getName(),0);
////            TimeUnit.MILLISECONDS.sleep(10);
//            // ----------------------------------------
            amount+=to_produce;
            Global.PRODUCED += to_produce;
            Global.OPERATIONS -= 1;
            Thread.sleep(workingTime);

            productCondition.signal();

            productLock.unlock();

//            // ------------- Tracking -----------------
//            System.out.printf("\n%s - Just produced %d!\n",Thread.currentThread().getName(),to_produce);
//            System.out.printf("Amount of products: %s\n", this.value());
//            System.out.println("Producers Waiting Times:");
//            System.out.println(Global.producersWaitingTimes.toString());
//            // ----------------------------------------

            producersLock.unlock();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void consume() {
        try{
            consumersLock.lock();

//            // ------------- Tracking -----------------
//            System.out.printf("\n-------------%s-------------\n",Thread.currentThread().getName());
//            System.out.printf("lock: %d\n",productLock.getQueueLength());
//            System.out.println("\n----------------------------------\n");
//            // ----------------------------------------

            int to_consume = Global.getAmountToProduceConsume(); //take random amount

            productLock.lock();

            while(amount-to_consume<0 && Global.OPERATIONS>0){

//                // ------------- Tracking -----------------
//                int waitingTimes = Global.consumersWaitingTimes.get(Thread.currentThread().getName());
//                Global.consumersWaitingTimes.replace(Thread.currentThread().getName(),waitingTimes+1);
//                System.out.printf("%s goes to firstConsumer set for %d time, wanting consume %d\n",Thread.currentThread().getName(),(waitingTimes-1)*-1,to_consume);
//                // ----------------------------------------

                productCondition.await();
            }

//            // ------------- Tracking -----------------
////            TimeUnit.MILLISECONDS.sleep(10);
//            Global.consumersWaitingTimes.replace(Thread.currentThread().getName(),0);
//            // ----------------------------------------

            amount-=to_consume;
            Global.CONSUMED += to_consume;
            Global.OPERATIONS -= 1;

            productCondition.signal();

            productLock.unlock();

//            // ------------- Tracking -----------------
//            System.out.printf("%s - Just consumed %d!\n",Thread.currentThread().getName(),to_consume);
//            System.out.printf("Amount of products: %s\n", this.value());
//            System.out.println("Consumers Waiting Times:");
//            System.out.println(Global.consumersWaitingTimes.toString());
//            // ----------------------------------------

            consumersLock.unlock();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  int value() {
        return amount;
    }
}

