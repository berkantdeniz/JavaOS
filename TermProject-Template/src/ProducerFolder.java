import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Objects;

public class ProducerFolder extends Thread{

   private volatile Storage<byte[]> folderInputQueue;
   private volatile boolean isRunning;

   public ProducerFolder(Storage<byte[]> folderInputQ)
   {
       this.folderInputQueue = folderInputQ;
   }

    @Override
    public void run()
    {
        System.out.println("Producer Folder Start");

        File dir = new File(System.getProperty("user.dir"));
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith((".txt"))) {
                try {
                    System.out.println("Found " + file.getName());
                    RandomAccessFile rfile = new RandomAccessFile(file.getPath(), "r");
                    rfile.seek(0);
                    byte[] bytes = new byte[(int)rfile.length()];
                    rfile.read(bytes);
                    rfile.close();
                    folderInputQueue.insertItem(bytes);
                    String s = new String(bytes);
                    System.out.println("Mapping asm files\n" + s);
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopThread() {
        isRunning = false;
        this.interrupt();
    }
}
