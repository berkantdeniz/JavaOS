import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ProducerConsole  extends Thread{

    private volatile List<Integer> consoleInputQueue;

    private Semaphore consoleInputMutex;
    private Semaphore fullSemaphoreC;
    private Semaphore emptySemaphoreC;



    private volatile boolean isRunning;

    public ProducerConsole(Semaphore consoleInputMutex, Semaphore emptyC, Semaphore fullC,  List<Integer> consoleInputQ) {

        this.consoleInputMutex=consoleInputMutex;
        this.fullSemaphoreC=fullC;
        this.emptySemaphoreC=emptyC;
        this.consoleInputQueue = consoleInputQ;
    }

    @Override
    public void run(){
        isRunning = true;
        System.out.println("Producer Console Start");
        Scanner in = new Scanner(System.in);
        while(isRunning)
        {
            try {
                while(System.in.available() == 0)
                    sleep(200);
                emptySemaphoreC.acquire();
                consoleInputMutex.acquire();

                if(!isRunning)
                    break;

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            int i = in.nextInt();
            System.out.println("Console input added to console input queue");
            consoleInputQueue.add(i);

            consoleInputMutex.release();
            fullSemaphoreC.release();

        }

    }
    public void stopThread() {
        isRunning = false;
    }

}
