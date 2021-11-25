package lab.AO;

public class ProduceRequest extends AbstractMethodRequest {
    private final int toProduce;

    public  ProduceRequest(Servant servant, int toProduce){
        super(servant,Thread.currentThread().getName());
        this.toProduce = toProduce;
    }


    @Override
    public boolean guard() {
        return getServant().getBufferMaxSize() >= getServant().getCurrentUsage() + this.toProduce;
    }

    @Override
    public void call() {
        getServant().increaseUsage(toProduce);

        this.getFuture().setReady(true);
//        System.out.println(who + " produced " + toProduce + " | buffer: " + getServant().getCurrentUsage() );
    }

    @Override
    public String toString() {
        return "ProduceRequest{" +
                "who=" + who +
                " toProduce=" + toProduce +
                '}';
    }
}
