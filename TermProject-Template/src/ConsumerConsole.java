import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsumerConsole extends Thread {

    private volatile List<Integer> consoleInputQueue;
    private volatile List<ProcessImage> blockedQueue;
    private volatile List<ProcessImage> readyQueue;




    private Semaphore mutex;
    private Semaphore fullSemaphoreC;
    private Semaphore emptySemaphoreC;
    private Semaphore readyMutex;
    private Semaphore blockedMutex;
    private Semaphore consoleInputMutex;



    private volatile boolean isRunning;

    public ConsumerConsole(Semaphore consoleInputM, Semaphore emptyC , Semaphore fullC , Semaphore readyM, Semaphore blockedM, List<Integer> consoleInputQ, List<ProcessImage> readyQ, List<ProcessImage> blockQ) {
        this.consoleInputMutex = consoleInputM;
        this.fullSemaphoreC = fullC;
        this.emptySemaphoreC = emptyC;
        this.readyMutex = readyM;
        this.blockedMutex = blockedM;

        this.readyQueue = readyQ;
        this.blockedQueue = blockQ;
        this.consoleInputQueue = consoleInputQ;



    }

    @Override
    public void run(){
        isRunning = true;
        System.out.println("Consumer Console Start");
        while(isRunning)
        {   int i;

            try {
                fullSemaphoreC.acquire();
                consoleInputMutex.acquire();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!isRunning)
                break;

            i= consoleInputQueue.remove(0);
            System.out.println(i+" is removed from console input queue ");
            consoleInputMutex.release();
            emptySemaphoreC.release();


            boolean ok=false;
            while(!ok)
            {
                try {
                    blockedMutex.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
                blockedMutex.release();
                if (!isBlockedQueueEmpty) {

                    try {
                        readyMutex.acquire();
                        blockedMutex.acquire();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ProcessImage p = blockedQueue.get(0);
                    blockedQueue.remove(0);

                    p.V = i;
                    System.out.println(p.processName+" is removed from blocked queue and added to ready queue");
                    readyQueue.add(p);
                    blockedMutex.release();
                    readyMutex.release();

                    ok=true;

                }
                else
                {
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public void stopThread() {
        isRunning = false;

    }
}
