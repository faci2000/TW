package lab.AO;

import lab.Globals.Global;

public class Servant {
    private final int bufferMaxSize;
    private int currentUsage;
    private final long workTime;

    public Servant(int bufferMaxSize, long workTime){
        this.bufferMaxSize = bufferMaxSize;
        this.workTime = workTime;
        this.setCurrentUsage(0);
    }

    public void increaseUsage(int produced){
//        try {
//            Thread.sleep(workTime);
//        }catch (Exception ignored){}

        setCurrentUsage(getCurrentUsage() + produced);
        Global.OPERATIONS--;
    }

    public void decreaseUsage(int consumed){
//        try {
////            Thread.sleep(workTime);
//        }catch (Exception ignored){}

        setCurrentUsage(getCurrentUsage()-consumed);
        Global.OPERATIONS--;
    }

    public int getBufferMaxSize() {
        return bufferMaxSize;
    }

    public int getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(int currentUsage) {
        this.currentUsage = currentUsage;
    }
}
