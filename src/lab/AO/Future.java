package lab.AO;

public class Future {
    private boolean isReady;

    public Future(){
        this.setReady(false);
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}
