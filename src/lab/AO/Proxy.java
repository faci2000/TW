package lab.AO;

public class Proxy {
    Scheduler scheduler;
    Thread schedulerThread;
    Servant servant;

    public Proxy(Servant servant){
        scheduler = new Scheduler();
        this.servant = servant;
        schedulerThread = new Thread(scheduler);
//        schedulerThread.setName("Scheduler");
        schedulerThread.start();
    }

    Future produce(int toProduce){                                              // proxy dodaje do głównej kolejki Schedulera, która jest synchronizowana
        ProduceRequest methodRequest = new ProduceRequest(servant,toProduce);
        scheduler.enqueue(methodRequest);
//        System.out.println("Enqueued: " + methodRequest);
        return methodRequest.getFuture();
    }

    Future consume(int toConsume){                                              // proxy dodaje do głównej kolejki Schedulera, która jest synchronizowana
        ConsumeRequest methodRequest = new ConsumeRequest(servant,toConsume);
        scheduler.enqueue(methodRequest);
//        System.out.println("Enqueued: " + methodRequest);
        return methodRequest.getFuture();
    }

}
