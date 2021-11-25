package lab.AO;

public class ConsumeRequest extends AbstractMethodRequest {
    private final int toConsume;

    public ConsumeRequest(Servant servant, int toConsume){
        super(servant,Thread.currentThread().getName());
        this.toConsume = toConsume;
    }


    @Override
    public boolean guard() {
        return 0 <= getServant().getCurrentUsage() - this.toConsume;
    }

    @Override
    public void call() {
        getServant().decreaseUsage(toConsume);          // wykonujemy zadanie, gdyż Scheduler przydzielił nam dostęp do zasobów współdzielonych

        this.getFuture().setReady(true);                // przestawiamy wartość Future na gotową do odczytu
//        System.out.println(who + " consumed " + toConsume + " | buffer: " + getServant().getCurrentUsage() );
    }

    @Override
    public String toString() {
        return "ConsumeRequest{" +
                "who=" + who +
                " toConsume=" + toConsume +
                '}';
    }
}
