import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ProducerFile extends Thread {

    private volatile List<ProcessImage> fileInputQueue;
    private volatile List<byte[]> folderfile;


    private Semaphore fileInputMutex;
    private Semaphore fullSemaphore;
    private Semaphore emptySemaphore;
    private Semaphore byteMutex;

    private volatile boolean isRunning;

    public ProducerFile(Semaphore fileInputM, Semaphore emptyS , Semaphore fullS , List<ProcessImage> fileInputQ , List<byte[]> folderfile, Semaphore byteMutex) {

        this.fileInputMutex = fileInputM;
        this.fullSemaphore = fullS;
        this.emptySemaphore = emptyS;
        this.fileInputQueue = fileInputQ;

        this.folderfile = folderfile;
        this.byteMutex = byteMutex;
    }

    public void loadProcess(InputStream inputfile, Assembler assembler) throws IOException, InterruptedException {
        System.out.println("Loading Processes...");
        BufferedReader in = new BufferedReader(new InputStreamReader((inputfile)));
        String st = "";
        String name;
        String time;
        while ((st = in.readLine()) != null) {
            String[] tokens = st.split(" ");
            if (tokens.length != 2) {
                throw new IllegalArgumentException();
            }
            name = tokens[0];
            time = tokens[1];
            System.out.println("Creating binary file for " + name + "...");
            String name2 = "";

            if (name.endsWith(".asm")) {
                name2 = name.substring(0, name.length() - 4);
            }

            int instructionSize = assembler.createBinaryFile(name, name2 + ".bin");
            ProcessImage pImage = new ProcessImage(name, instructionSize);
            //

            emptySemaphore.acquire();
            fileInputMutex.acquire();

            System.out.println(name + "  added File Input Queue...");
            fileInputQueue.add(pImage);
            fileInputMutex.release();
            fullSemaphore.release();

            int sleepTime = Integer.parseInt(time);
            sleep(sleepTime);
        }


    }


    @Override
    public void run() {
        isRunning = true;
        System.out.println("Producer File Start");
        while(isRunning)
        {
            boolean empty = true;
            try {
                byteMutex.acquire();
                empty = folderfile.isEmpty();
                byteMutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!empty)
            {
            byte[] bytes = {};

                try {
                    byteMutex.acquire();
                    bytes = folderfile.remove(0);
                    byteMutex.release();
                    InputStream targetStream = new ByteArrayInputStream(bytes);
                    Assembler assembler = new Assembler();
                    loadProcess(targetStream, assembler);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            } else
            {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        /*
        System.out.println("Producer File start");
       // FileInputStream fil/= null;
        try {


            file = new FileInputStream("inputSequence.txt");
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
        Assembler assembler = new Assembler();
        try {
            loadProcess(file, assembler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        while (isRunning  ) {

        }*/
    }

    public void stopThread() {
        isRunning = false;
    }
}