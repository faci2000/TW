package lab.AO;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

public class Scheduler implements Runnable{

    private final SynchronousQueue<AbstractMethodRequest> activationQueue;
    private final Queue<AbstractMethodRequest> secondaryQueue;
    private boolean work;

    public Scheduler(){
        this.activationQueue = new SynchronousQueue<>();
        this.secondaryQueue = new LinkedList<>();
        this.work = true;
    }

    @Override
    public void run() {                                                         // cały mechanizm obsługi zadań przez Scheduler
        while (work){
//            System.out.println("\n>>Secondary queue:");
//            System.out.println(secondaryQueue);
            AbstractMethodRequest methodRequest = secondaryQueue.peek();
            if (methodRequest!=null){                                           // sprawdzamy, czy nie ma jakiegoś zadania do wykonania w drugiej kolejce
               if (methodRequest.guard()){                                      // jeśli możliwe jest jego wykonanie
                   methodRequest.call();                                        // wykonujemy go i usuwamy z kolejki
//                   System.out.println("Served: " + secondaryQueue.remove());
                   secondaryQueue.remove();
                   continue;                                                    // przechodzimy do kolejnego zadania z tej kolejki dopóki to możliwe
               }                                                                // kolejka ta ma wyższy priorytet niż główna kolejka, pownieważ chcemy
            }                                                                   // możliwie zachować kolejność przychodzących zadań

//            System.out.println(">>Primary queue:");
//            System.out.println(activationQueue);
            try {
                methodRequest = activationQueue.take();                         // jeśli druga kolejka jest pusta, bądź zadania z niej nie da się wykonać
//                System.out.println(">>Took from primary queue: " + methodRequest);
            } catch (InterruptedException e) {                                  // próbujemy wyciągnąć zadanie z głównej kolejki, jeśli nie ma żadnego,
                continue;                                                       // kolejka synchronizowana pozwala zawiesić Scheduler do momentu  nadejścia zadań
            }
            if(methodRequest.guard()){                                          // sprawdzamy możliwość wykonania zadania
                methodRequest.call();                                           // wykonujemy jeśli to możliwe
//                System.out.println("Served: " + methodRequest);
            }else {                                                             // natomiast jeśli nie jest to możliwe w tym momencie, wrzucamy zadanie do drugiej kolejki
                secondaryQueue.add(methodRequest);
//                System.out.println("Thrown to secondary queue: " + methodRequest);
            }

        }
    }

    public void enqueue(AbstractMethodRequest methodRequest) {
        try {
            this.activationQueue.put(methodRequest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        this.work = false;
    }
}
