import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class InputThread extends Thread {

	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> readyQueue;

	private Semaphore mutex;

	private volatile boolean isRunning;

	public InputThread(Semaphore mtx, List<ProcessImage> blockedQ, List<ProcessImage> readyQ) {
		this.mutex = mtx;
		this.blockedQueue = blockedQ;
		this.readyQueue = readyQ;
	}

	@Override
	public void run(){
		isRunning = true;
		try {
			while (isRunning) {
				mutex.acquire();
				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				mutex.release();

				if (!isBlockedQueueEmpty) {
					Scanner in = new Scanner(System.in); 
					int i = in.nextInt();
					in.close();
					
					mutex.acquire();
					ProcessImage p = blockedQueue.get(0);
					blockedQueue.remove(0);
					p.V = i;
					readyQueue.add(p);
					mutex.release();
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	public void stopThread() {
		isRunning = false;
	}
}
