import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsumerFile extends Thread {

    private volatile List<ProcessImage> fileInputQueue;
    private volatile List<ProcessImage> readyQueue;

    private Semaphore mutex;
    private Semaphore fullSemaphore;
    private Semaphore emptySemaphore;
    private Semaphore readyMutex;
    private Semaphore memoryMutex;
    private Semaphore fileInputMutex;

    private Assembler assembler;

    private Memory memory;

    private volatile boolean isRunning;


    public ConsumerFile(Semaphore fileInputM, Semaphore emptyS, Semaphore fullS, Semaphore readyM, Semaphore memoryM, List<ProcessImage> fileInputQ, List<ProcessImage> readyQ, Memory memory ) {

        this.fileInputMutex = fileInputM;
        this.fullSemaphore = fullS;
        this.emptySemaphore = emptyS;
        this.readyMutex = readyM;
        this.memoryMutex = memoryM;


        this.readyQueue = readyQ;
        this.fileInputQueue = fileInputQ;

        this.assembler = new Assembler();
        this.memory = memory;


    }

    @Override
    public void run() {
        isRunning = true;
        System.out.println("Consumer File Start");
        while (isRunning) {

            try {
                fullSemaphore.acquire();
                fileInputMutex.acquire();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            ProcessImage pImage=fileInputQueue.remove(0);
            System.out.println((pImage.processName) + " is removeds from file input queue...");
            fileInputMutex.release();
            emptySemaphore.release();



            try {
                memoryMutex.acquire(); // change it to memory mutex
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean free=memory.hasFreeSpace(pImage.LR);
            memoryMutex.release(); // change it to memory mutexs

            while(!free)
            {
                try {
                    System.out.println("There is no enough size for "+ (pImage.processName) +" in memory will check again in 2000ms");
                    sleep(2000);
                    memoryMutex.acquire();
                    if(memory.hasFreeSpace(pImage.LR))
                        free = true;
                    memoryMutex.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                memoryMutex.acquire(); // memory mutex
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String name2 = pImage.processName.substring(0, pImage.processName.length() - 4);
            pImage.BR=memory.addInstructions2(assembler.readBinaryFile(pImage.LR,name2+".bin"),pImage.LR );

            memoryMutex.release(); // memory mutex

            try {
                readyMutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readyQueue.add(pImage);
            System.out.println(pImage.processName+" added ready queue...");
            readyMutex.release();
        }
    }


    public void stopThread() {
        isRunning = false;
    }
}
