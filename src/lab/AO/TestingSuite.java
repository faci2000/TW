package lab.AO;

import lab.Globals.Global;
import lab.nestedLocks.Monitor;

public class TestingSuite {
    public static void main(String[] args) {
        long startTime,endTime;

        for(int i = 1;i<=30; i+=2){
            System.out.println(i);
            for(int j = 0;j<2; j+=1) {
                Monitor monitor = new Monitor(0, 0, 0, 10, i, i);
                Global.OPERATIONS = 100000;
                Global.setExtraTasksDone(0);
                startTime = System.nanoTime();
                monitor.startWorking();
                monitor.waitForWorkEnd();
                endTime = System.nanoTime();
                System.out.printf("%f %d\n",(((double) endTime) - startTime) / 1000000000, Global.getExtraTasksDone());
            }
        }
        System.out.println("-------");

        for(int i = 1;i<=30; i+=2){
            System.out.println(i);
            for(int j = 0;j<2; j+=1) {
                ActiveObject activeObject = new ActiveObject(0, 0, 0, 10, i, i);
                Global.OPERATIONS = 100000;
                Global.setExtraTasksDone(0);
                startTime = System.nanoTime();
                activeObject.startWorking();
                activeObject.waitForWorkEnd();
                endTime = System.nanoTime();
                System.out.printf("%f %d\n",(((double) endTime) - startTime) / 1000000000, Global.getExtraTasksDone());
            }
        }



    }
}
