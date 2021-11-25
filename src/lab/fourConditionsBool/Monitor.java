package lab.fourConditionsBool;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Global{
    static Map<String,Integer> producersWaitingTimes = new HashMap<>();
    static Map<String,Integer> consumersWaitingTimes = new HashMap<>();
    static Random RAND = new Random(1);
    static int PRODUCERS = 10;
    static int CONSUMERS = 10;
    static int MAX_PRODUCTS = 100;
    static int OPERATIONS = 100000;
    static long PRODUCED = 0;
    static long CONSUMED = 0;
    static long CPUTIME = 0;

    static int getAmountToProduceConsume(){
        return RAND.nextInt(MAX_PRODUCTS/2)+1;
    }
}

public class Monitor {
    public static void main(String[] args) throws InterruptedException {
        Product products = new Product();

        List<Thread> producer_consumers = new ArrayList<>();
        for( int i=0; i<Global.CONSUMERS; i++){
            Thread thread = new Thread(new Consumer(products));
            thread.setName(String.format( "Consumer no:%s", i));
            producer_consumers.add(thread);
            Global.consumersWaitingTimes.put(thread.getName(),0);
        }
        for( int i=0; i<Global.PRODUCERS; i++){
            Thread thread = new Thread(new Producer(products));
            thread.setName(String.format( "Producer no:%s", i));
            producer_consumers.add(thread);
            Global.producersWaitingTimes.put(thread.getName(),0);
        }
        Collections.shuffle(producer_consumers);
        long startTime = System.nanoTime();
        for(Thread thread : producer_consumers){
            thread.start();
        }
        for(Thread thread : producer_consumers){
            thread.join();
        }
        long endTime = System.nanoTime();
        System.out.printf("Consumed: %d\n",Global.CONSUMED);
        System.out.printf("Produced: %d\n",Global.PRODUCED);
        System.out.printf("Total: %d\n",Global.CONSUMED+Global.PRODUCED);
        System.out.printf("TIME ELAPSED: %fs\n", (((double) endTime) - startTime)/1000000000);
        System.out.printf("CPU TIME : %f\n", ((double) Global.CPUTIME)/1000000000);
    }
}


class Producer implements Runnable {
    private final Product product;

    public Producer(Product product) {
        this.product = product;
    }

    public void run() {
        while (Global.OPERATIONS>0) {
            product.produce();
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
        }
        Global.CPUTIME += ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
    }
}


class Product {
    private volatile int amount = 0;
    private final List<String> visitors = new ArrayList<>();
    boolean firstProducerHasWaiter = false;
    boolean firstConsumerHasWaiter = false;
    ReentrantLock productLock = new ReentrantLock(true);
    Condition otherConsumers = productLock.newCondition();
    Condition firstProducer = productLock.newCondition();
    Condition otherProducers = productLock.newCondition();
    Condition firstConsumer = productLock.newCondition();


    public void produce() {
        try{
            productLock.lock();

            // ------------- Tracking -----------------
            System.out.printf("\n-------------%s-------------\n",Thread.currentThread().getName());
            System.out.printf("lock: %d\n",productLock.getQueueLength());
            System.out.printf("otherProducers: %d\n",productLock.getWaitQueueLength(otherProducers));
            System.out.printf("firstProducer: %d\n",productLock.getWaitQueueLength(firstProducer));
            System.out.printf("otherConsumers: %d\n",productLock.getWaitQueueLength(otherConsumers));
            System.out.printf("firstConsumer: %d\n",productLock.getWaitQueueLength(firstConsumer));
            System.out.println("\n----------------------------------\n");
            // ----------------------------------------

            int to_produce = Global.getAmountToProduceConsume(); //take random amount
//            while (productLock.hasWaiters(firstProducer) ) {
            while (firstProducerHasWaiter && Global.OPERATIONS>0){ // zastosowanie wspomnianego booleana zamiast hasWaiters()

                // ------------- Tracking -----------------
                int waitingTimes = Global.producersWaitingTimes.get(Thread.currentThread().getName());
                Global.producersWaitingTimes.replace(Thread.currentThread().getName(),waitingTimes+1);
                System.out.printf("%s goes to otherProducers set for %d time, wanting produce %d\n",Thread.currentThread().getName(),(waitingTimes+1),to_produce);
                // ----------------------------------------

                otherProducers.await();
            }

            // ------------- Tracking -----------------
            Global.producersWaitingTimes.replace(Thread.currentThread().getName(),0);
            // ----------------------------------------

            firstProducerHasWaiter = true;
            while(amount+to_produce>Global.MAX_PRODUCTS && Global.OPERATIONS>0){

                // ------------- Tracking -----------------
                int waitingTimes = Global.producersWaitingTimes.get(Thread.currentThread().getName());
                Global.producersWaitingTimes.replace(Thread.currentThread().getName(),waitingTimes-1);
                System.out.printf("%s goes to firstProducer set for %d time, wanting produce %d\n",Thread.currentThread().getName(),(waitingTimes-1)*-1,to_produce);
                // ----------------------------------------

                firstProducer.await();
            }
            firstProducerHasWaiter = false;

            // ------------- Tracking -----------------
            Global.producersWaitingTimes.replace(Thread.currentThread().getName(),0);
//            TimeUnit.MILLISECONDS.sleep(10);
            // ----------------------------------------

            amount+=to_produce;
            Global.PRODUCED += to_produce;
            Global.OPERATIONS -= 1;

            otherProducers.signal();
            firstConsumer.signal();

            // ------------- Tracking -----------------
            System.out.printf("\n%s - Just produced %d!\n",Thread.currentThread().getName(),to_produce);
            System.out.printf("Amount of products: %s\n", this.value());
            System.out.println("Producers Waiting Times:");
            System.out.println(Global.producersWaitingTimes.toString());
            // ----------------------------------------

        } catch (InterruptedException e) {
            e.printStackTrace();
            productLock.unlock();
        }finally {
            productLock.unlock();
        }
    }

    public void consume() {
        try{
            productLock.lock();

            // ------------- Tracking -----------------
            System.out.printf("\n-------------%s-------------",Thread.currentThread().getName());
            System.out.printf("lock: %d\n",productLock.getQueueLength());
            System.out.printf("otherProducers: %d\n",productLock.getWaitQueueLength(otherProducers));
            System.out.printf("firstProducer: %d\n",productLock.getWaitQueueLength(firstProducer));
            System.out.printf("otherConsumers: %d\n",productLock.getWaitQueueLength(otherConsumers));
            System.out.printf("firstConsumer: %d\n",productLock.getWaitQueueLength(firstConsumer));
            System.out.println("\n----------------------------------\n");
            // ----------------------------------------

            int to_consume = Global.getAmountToProduceConsume(); //take random amount
//            while (productLock.hasWaiters(firstConsumer)){
            while (firstConsumerHasWaiter && Global.OPERATIONS>0) { // zastosowanie wspomnianego booleana zamiast hasWaiters()

                // ------------- Tracking -----------------
                int waitingTimes = Global.consumersWaitingTimes.get(Thread.currentThread().getName());
                Global.consumersWaitingTimes.replace(Thread.currentThread().getName(),waitingTimes+1);
                System.out.printf("%s goes to otherConsumers set for %d time, wanting consume %d\n",Thread.currentThread().getName(),(waitingTimes+1),to_consume);
                // ----------------------------------------

                otherConsumers.await();
            }

            // ------------- Tracking -----------------
            Global.consumersWaitingTimes.replace(Thread.currentThread().getName(),0);
            // ----------------------------------------

            firstConsumerHasWaiter = true;
            while(amount-to_consume<0 && Global.OPERATIONS>0){

                // ------------- Tracking -----------------
                int waitingTimes = Global.consumersWaitingTimes.get(Thread.currentThread().getName());
                Global.consumersWaitingTimes.replace(Thread.currentThread().getName(),waitingTimes-1);
                System.out.printf("%s goes to firstConsumer set for %d time, wanting consume %d\n",Thread.currentThread().getName(),(waitingTimes-1)*-1,to_consume);
                // ----------------------------------------

                firstConsumer.await();
            }
            firstConsumerHasWaiter = false;

            // ------------- Tracking -----------------
//            TimeUnit.MILLISECONDS.sleep(10);
            Global.consumersWaitingTimes.replace(Thread.currentThread().getName(),0);
            // ----------------------------------------

            amount-=to_consume;
            Global.CONSUMED += to_consume;
            Global.OPERATIONS -= 1;
            otherConsumers.signal();
            firstProducer.signal();

            // ------------- Tracking -----------------
            System.out.printf("%s - Just consumed %d!\n",Thread.currentThread().getName(),to_consume);
            System.out.printf("Amount of products: %s\n", this.value());
            System.out.println("Consumers Waiting Times:");
            System.out.println(Global.consumersWaitingTimes.toString());
            // ----------------------------------------

        } catch (InterruptedException e) {
            e.printStackTrace();
            productLock.unlock();
        }finally {
            productLock.unlock();
        }

    }

    public  int value() {
        return amount;
    }
}

// unikanie zagłodzenia można osiągnąć poprzez wykorzystanie 4 kolejek condition, z czego dwie z nich wskazują pierwszego producenta/konsumenta,
//  który ma pierwszeństwo przed reszta w dostepie do zasobów


// Objaśnienie zakleszczenia w przypadku użycia hasWaiters()
// dwóch konsumentów: c1,c2, jeden producent: p1, bufor: 10, max ilość jednostek do skonsumowania/wyprodukowania: 5
// 0) ... program działa ..
// 1) consumer c1 z kolejki firstConsumer zostaje wzbudzony, czeka w locku razem z producerem p1 i consumerem c2 | bufor:3
// 2) zostaje wzbudzony c2(4), kolejka otherConsumers jest pusta, jednak jest za mało produktów, więc trafia do kolejki firstConsumer | bufor:3
// 3) zostaje wzbudzony c1(4), sprawdza while z firstConsumer, jednak wciąz jest za mało produktów, żeby skonsumować, trafia z powrotem do firstConsumer -> dwa wątki w jednej kolejce condition | bufor:3
// 4) zostaje wzbudzony p1(5), produkuje i wzbudza kogoś z kolejki firstConsumer | bufor: 8
// 4) np c1 zostje wzbudzony, konsumuje 4 jednostki, trafia ponownie na locka | bufor: 4
// 3) c1 ponownie wchodzi do monitora, sprawdza czy firstConsumer jest pusty, nie jest, gdyż wciąż c2 tam przebywa, zatem wstawia się do otherConsumers niewywołując nikogo, pozostawiajac locka pustego | bufor: 4
// 5) ZAKLESZCZENIE

// rozwiązaniem powyższego problemu jest zastosowanie zmiennych typu bool, zamiast wykorzystywanie narzędzia do monitorowania kolejek condition hasWaiters()

// Dodatkowo moje rozwiązanie posiada pełne i działające aśledzenie wątków