import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsumerFolder extends Thread {
    private volatile Storage<byte[]> folderInputQueue;
    private volatile List<byte[]> folderfile;

    private Semaphore byteMutex;
    private volatile boolean isRunning;


    public ConsumerFolder(Storage<byte[]> folderInputQueue, List<byte[]> folderfile, Semaphore byteMutex) {
        this.folderInputQueue = folderInputQueue;
        this.folderfile = folderfile;
        this.byteMutex = byteMutex;
    }

    @Override
    public void run() {
        System.out.println("Consumer Folder Start");
        isRunning = true;

        while(isRunning) {
            try {
                while(folderInputQueue.isEmpty());
                byte[] bytes = folderInputQueue.removeItem();
                byteMutex.acquire();
                folderfile.add(bytes);
                byteMutex.release();
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void stopThread() {
        isRunning = false;
        this.interrupt();
    }
}
