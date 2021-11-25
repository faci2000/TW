package lab.AO;

public abstract class AbstractMethodRequest {
    private final Servant servant;
    private Future future;
    protected String who;

    public AbstractMethodRequest(Servant servant, String who){
        this.servant = servant;
        this.who = who;
        setFuture(new Future());
    }

    abstract boolean guard();
    abstract void call();

    public Servant getServant() {
        return servant;
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

}
