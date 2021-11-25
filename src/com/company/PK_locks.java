package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class Global{
    static int PRODUCERS = 5;
    static int CONSUMERS = 1;
    static int MAX_PRODUCTS = 1;
}

public class PK_locks {
    public static void main(String[] args) throws InterruptedException {
        Product products = new Product();

        List<Thread> producer_consumers = new ArrayList<>();
        for(int i = 0; i< Global.CONSUMERS; i++){
            Thread thread = new Thread(new Consumer(products));
            thread.setName(String.format( "Consumer no:%s", i));
            producer_consumers.add(thread);
        }
        for(int i = 0; i< Global.PRODUCERS; i++){
            Thread thread = new Thread(new Producer(products));
            thread.setName(String.format( "Producer no:%s", i));
            producer_consumers.add(thread);
        }
        Collections.shuffle(producer_consumers);
        for(Thread thread : producer_consumers){
            thread.start();
        }
        for(Thread thread : producer_consumers){
            thread.join();
        }

        System.out.println(products.value());
    }
}


class Producer implements Runnable {
    private final Product product;

    public Producer(Product product) {
        this.product = product;
    }

    public void run() {
        while (true) { // for(int i=0;i<10;i++){ //
            product.produce();
        }
//        product.produce();
//        this.run();
    }
}
class Consumer implements Runnable {
    private final Product product;

    public Consumer(Product product) {
        this.product = product;
    }

    public void run() {
        while (true) { //for(int i=0;i<10;i++){ //
            product.consume();
        }
//        product.consume();
//        this.run();
    }
}


class Product {
    private volatile int amount = 0;
    private final List<String> visitors = new ArrayList<>();

    public synchronized void produce() {
        while (amount>= Global.MAX_PRODUCTS) {
            try {
                if(visitors.contains(Thread.currentThread().getName()))
                    visitors.remove(Thread.currentThread().getName());
                visitors.add(Thread.currentThread().getName());
                System.out.println(visitors);
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e.getMessage());
            }
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        amount++;
        visitors.remove(Thread.currentThread().getName());
//        visitors.clear();
        System.out.printf("%s - Just produced!\n",Thread.currentThread().getName());
        System.out.printf("Amount of products: %s\n%n", this.value());
        this.notify();
    }

    public synchronized void consume() {
        while (amount<=0) {
            try {
                if(visitors.contains(Thread.currentThread().getName()))
                    visitors.remove(Thread.currentThread().getName());
                visitors.add(Thread.currentThread().getName());
                System.out.println(visitors);
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e.getMessage());
            }
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        amount--;
        visitors.remove(Thread.currentThread().getName());
//        visitors.clear();
        System.out.printf("%s - Just consumed!\n",Thread.currentThread().getName());
        System.out.printf("Amount of products: %s\n%n", this.value());
        this.notify();
    }

    public  int value() {
        return amount;
    }
}
