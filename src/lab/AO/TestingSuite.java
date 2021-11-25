package lab.AO;

import lab.Globals.Global;
import lab.nestedLocks.Monitor;

public class TestingSuite {
    public static void main(String[] args) {
        long startTime,endTime;

        for(int i = 91;i<=101; i+=1){
            System.out.println(i);
            for(int j = 0;j<10; j+=1) {
                Global.OPERATIONS = 100000;
                Monitor monitor = new Monitor(0, 0, 0, 10, 102-i, i);
                Global.setExtraTasksDone(0);
                startTime = System.nanoTime();
                monitor.startWorking();
                monitor.waitForWorkEnd();
                endTime = System.nanoTime();
                System.out.printf("%f\n",(((double) endTime) - startTime) / 1000000000);
            }
        }
        System.out.println("-------");

        for(int i = 91;i<=101; i+=1){
            System.out.println(i);
            for(int j = 0;j<10; j+=1) {
                Global.OPERATIONS = 100000;
                ActiveObject activeObject = new ActiveObject(5, 0, 0,10, 102-i, i);
                Global.setExtraTasksDone(0);
                startTime = System.nanoTime();
                activeObject.startWorking();
                activeObject.waitForWorkEnd();
                endTime = System.nanoTime();
                System.out.printf("%f\n", (((double) endTime) - startTime) / 1000000000);
            }
        }



    }
}
