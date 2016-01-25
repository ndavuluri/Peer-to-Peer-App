import java.io.*;
import java.net.*;
import java.util.*;

public class FileServer {
    
    ObjectOutputStream out;  
    // List of Chunks (id,data)
    ArrayList<FileChunk> chunks;
    int numberOfChunks;

    public static void main(String[] args){
        FileServer server = new FileServer();
        try {
            //Read server port from config file
            Scanner scanner = new Scanner(new File("config.txt"));
            int portno = 0;
            if(scanner.hasNextInt()){
               portno = scanner.nextInt();
            }
            System.out.println("port:" + portno);

            //Reading filename and extension
            String fileName = args[0];
                        System.out.println("FileName:" + fileName);
            File file = new File(fileName);
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                extension = fileName.substring(i+1);
            }
            System.out.println("extension:" + extension);
            int chunkcount = server.breakFile(file);
            System.out.println("Number of Chunks:" + chunkcount);

            server.run(portno,chunkcount,extension);
        }catch (Exception e){
            System.out.println("Failed Opening the File");
        }
    }

    public void run(int serverPort,int chunkcount, String extension){
        ServerSocket server = null;
        try {
            server = new ServerSocket(serverPort);
            System.out.println("Server's port - " + serverPort);
            //chunk division
            int residue = chunkcount%5;
            int range =  chunkcount / 5;
            int start = 0;
            int end = start + range + residue;

            boolean isRunning = true; 
            while (isRunning) {
                try {
                  
                    Socket clientSocket = server.accept();
                    System.out.println(start+",,,"+end);
                    // New Thread to handle the Client
                    Runnable r = new TransferChunks(clientSocket, chunks, numberOfChunks, start, end, extension, out);
                    start = end;
                    end = end + range;
                   // System.out.println("Incremented");
                   // System.out.println(start +",,,"+end);

                    // Starting a new Thread 
                    new Thread(r).start();

                } catch (IOException e) {
                    System.out.println("Failed to accept Client Connection");
                } catch (NullPointerException e){
                    System.out.println("Null Point Exception while accepting client connection");
                }
            }
        }catch (IOException e){
            System.out.println("Unable to start the Server");
        }
    }


     public int breakFile(File file){
        int chunkcount = 1;
        int readlen = 100 * 1024; 
        byte[] buffer;

        int fileSize = (int)file.length();
        System.out.println("File Size:" + fileSize);
        FileInputStream inputStream;
        chunks = new ArrayList<>();

        try{
            inputStream = new FileInputStream(file);
            String name = file.getName();

            int chunkSize = 0; // number of Bytes in a chunk

            while(fileSize > 0){
                if(fileSize < readlen)
                    readlen = fileSize;
                buffer = new byte[readlen];
                chunkSize = inputStream.read(buffer, 0, readlen);
                fileSize -= chunkSize;
                FileChunk chunk = new FileChunk(chunkcount, buffer);
                chunks.add(chunk);
                numberOfChunks++;
                chunkcount++;
            }
            
        }catch (IOException e){
            System.out.println("Failed to Read the File into a Stream");
        }
       return chunkcount-1;
    }
}