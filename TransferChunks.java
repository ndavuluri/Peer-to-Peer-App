import java.io.*;
import java.net.Socket;
import java.util.*;

public class TransferChunks implements Runnable{
    Socket clientSocket;
    ArrayList<FileChunk> chunks;
    int numberOfChunks;
    String fileType;
    ObjectOutputStream out;
    int start;
    int end;

    public TransferChunks(Socket clientSocket, ArrayList<FileChunk> chunks, int numberOfChunks, int start, int end,
                      String fileType, ObjectOutputStream out) {
        this.clientSocket = clientSocket;
        this.chunks = chunks;
        this.numberOfChunks = numberOfChunks; //end - start;
        this.start = start;
        this.end = end;
        this.fileType = fileType;
        this.out = out;
    }

    public void run(){
        ArrayList<FileChunk> chunksToBeSent = new ArrayList<>();

        for (int i=start; i<end; i++) {
        
            chunksToBeSent.add(this.chunks.get(i));
        }
        ChunksFromOwner sendMessage = new ChunksFromOwner(numberOfChunks,chunksToBeSent,fileType);
        try
        {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            try {
                out.writeObject(sendMessage);
                out.flush();
            } catch(IOException e) {
                System.out.println("Cannot write to the Output Stream");
            }
        }catch(IOException i) {
            System.out.println("Cannot connect to Client OutputStream");
        }
    }
}