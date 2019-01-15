import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class OS extends Thread {

	private final int QUANTUM = 5;

	private CPU cpu;
	private Memory memory;
	private volatile List<ProcessImage> readyQueue;
	private volatile List<ProcessImage> blockedQueue;
	/********/
	private volatile  List<ProcessImage> fileInputQueue;
	private volatile  List<Integer> consoleInputQueue;
	/****/
	private volatile Storage<byte[]> folderInputQueue;
	private volatile List<byte[]> folderfile;
	private Semaphore byteMutex = new Semaphore(1);

	private Semaphore fullQueueF = new Semaphore(0);
	private Semaphore emptyQueueF = new Semaphore(5);

	private Semaphore fullQueueC = new Semaphore(0);
	private Semaphore emptyQueueC = new Semaphore(5);

	private Semaphore fileInputMutex = new Semaphore(1);
	private Semaphore consoleInputMutex = new Semaphore(1);
	private Semaphore memoryMutex = new Semaphore(1);
	private Semaphore readyQueueMutex = new Semaphore(1);
	private Semaphore blockedQueueMutex = new Semaphore(1);


	private ProducerFile fileProducerThread;
	private ConsumerFile fileConsumerThread;
	private ProducerConsole consoleProducerThread;
	private ConsumerConsole consoleConsumerThread;
	private ProducerFolder folderProducerThread;
	private ConsumerFolder folderConsumerThread;
	/********/


	public OS(int size) {
		this.memory = new Memory(size);
		this.cpu = new CPU(memory);


		this.readyQueue = new ArrayList<ProcessImage>();
		this.blockedQueue = new ArrayList<ProcessImage>();

		/********/
		this.fileInputQueue = new ArrayList<ProcessImage>();
		this.consoleInputQueue = new ArrayList<Integer>();
		this.folderInputQueue = new Storage<byte[]>();
		this.folderfile = new ArrayList<byte[]>();


		/********/

		this.fileProducerThread=new ProducerFile(fileInputMutex,emptyQueueF,fullQueueF,fileInputQueue,folderfile,byteMutex);
		this.fileConsumerThread = new ConsumerFile(fileInputMutex,emptyQueueF,fullQueueF,readyQueueMutex,memoryMutex,fileInputQueue,readyQueue,memory);

		this.consoleProducerThread = new ProducerConsole(consoleInputMutex,emptyQueueC, fullQueueC, consoleInputQueue);
		this.consoleConsumerThread = new ConsumerConsole(consoleInputMutex,emptyQueueC,fullQueueC,readyQueueMutex,blockedQueueMutex,consoleInputQueue,readyQueue,blockedQueue);

		this.folderProducerThread = new ProducerFolder(folderInputQueue);
		this.folderConsumerThread = new ConsumerFolder(folderInputQueue,folderfile,byteMutex);

		// start consumer and producer
		fileProducerThread.start();
		fileConsumerThread.start();
		consoleProducerThread.start();
		consoleConsumerThread.start();
		folderProducerThread.start();
		folderConsumerThread.start();
		/********/
	}



	@Override
	public void run() {
		try {
			sleep(5000);
			while (true) {
				blockedQueueMutex.acquire();
				readyQueueMutex.acquire();



				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				boolean isReadyQueueEmpty = readyQueue.isEmpty();
				readyQueueMutex.release();
				blockedQueueMutex.release();



				if(isBlockedQueueEmpty && isReadyQueueEmpty) {
					break;
				}


				if (!isReadyQueueEmpty) {
					System.out.println("Executing " + (readyQueue.get(0)).processName);

					readyQueueMutex.acquire();
					cpu.transferFromImage(readyQueue.get(0));
					readyQueueMutex.release();
					for (int i = 0; i < QUANTUM; i++) {
						if (cpu.getPC() < cpu.getLR()) {
							cpu.fetch(); 
							int returnCode = cpu.decodeExecute();

							if (returnCode == 0)  {
								readyQueueMutex.acquire();
								System.out.println("Process " + readyQueue.get(0).processName + " made a system call for ");
								readyQueueMutex.release();
								if (cpu.getV() == 0) {
									System.out.println( "Input, transfering to blocked queue and waiting for input...");
									ProcessImage p=new ProcessImage();
									this.cpu.transferToImage(p);


									readyQueueMutex.acquire();
									blockedQueueMutex.acquire();
									readyQueue.remove(0);
									blockedQueue.add(p);
									blockedQueueMutex.release();
									readyQueueMutex.release();

								} 
								else { //syscall for output
									System.out.print("Output Value: ");
									ProcessImage p=new ProcessImage();
									cpu.transferToImage(p);
									readyQueueMutex.acquire();
									readyQueue.remove(0);
									System.out.println( p.V +"\n");
									readyQueue.add(p);
									readyQueueMutex.release();
								}
								//Process blocked, need to end quantum prematurely
								break;
							}
						}
						else {
							readyQueueMutex.acquire();
							System.out.println("Process " + readyQueue.get(0).processName +" has been finished! Removing from the queue...\n" );
							readyQueueMutex.release();
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);
							p.writeToDumpFile();

							readyQueueMutex.acquire();
							readyQueue.remove(0);
							readyQueueMutex.release();

							memoryMutex.acquire();
							memory.removeInst(p.LR,p.BR);
							memoryMutex.release();
							break;
						}

						if (i == QUANTUM - 1) {
							//quantum finished put the process at the end of readyQ
							System.out.println ("Context Switch! Allocated quantum have been reached, switching to next process...\n");
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);  

							readyQueueMutex.acquire();
							readyQueue.remove(0);
							readyQueue.add(p);
							readyQueueMutex.release();
						}
					}
				}
			}



			consoleConsumerThread.stopThread();
			consoleProducerThread.stopThread();
			fileConsumerThread.stopThread();
			fileProducerThread.stopThread();
			folderConsumerThread.stopThread();
			folderProducerThread.stopThread();


			System.out.println("Execution of all processes has finished!");
			this.interrupt();

			//System.out.println(this.isAlive());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
