import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class Download implements Runnable {
    int downloadNeighbourPort;
    HashMap<Integer,FileChunk> chunks;
    HashSet<Integer> chunkIDs;
    ObjectInputStream in;
    ObjectOutputStream out;
    int numberOfChunks;
    int clientPort;
    String fileType;
    int rvdChunks;
    int counter; //Given in argument for Client , used for filename

    // Constructor
    public Download(int downloadNeighbourPort,HashMap<Integer,FileChunk> chunks,HashSet<Integer> chunkIDs,
                             int numberOfChunks, int clientPort,
                             String fileType, int rvdChunks, int counter)
    {
        this.downloadNeighbourPort = downloadNeighbourPort;
        this.chunks = chunks;
        this.chunkIDs = chunkIDs;
        this.numberOfChunks = numberOfChunks;
        this.clientPort = clientPort;
        this.fileType = fileType;
        this.rvdChunks = rvdChunks;
        this.counter = counter;
    }

    public void run(){
     
        boolean unfinished = true;
        while(unfinished) {
         
            try{
                Thread.sleep(4000);
            }catch (InterruptedException e){
                System.out.println("Error with the Thread");
            }

            boolean finished = false;
            int track = 0;

            try {
                Socket downloadNeighbourSocket = new Socket("localhost", downloadNeighbourPort);
                System.out.println("---> Peer is connected to its Downloading Neighbour " + downloadNeighbourPort);

                out = new ObjectOutputStream(downloadNeighbourSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(downloadNeighbourSocket.getInputStream());

                int counter = 6000;
                while(!finished) {
                    counter--;
                    if(counter == 0){
                        finished = true;
                    }

                    Object resp = null;
                    HashSet<Integer> rvdIDs = null;
                    resp = in.readObject();
                    if (track == 0 && resp instanceof SummaryChunkList) {
                        rvdIDs = ((SummaryChunkList) resp).chunkIDs;
                        System.out.println("Summary List from neighbour<--- " + rvdIDs);
                    
                        if (rvdIDs != null) {
                    
                            rvdIDs.removeAll(chunkIDs);
                            if (!rvdIDs.isEmpty()) {
                                SummaryChunkList wantedIDs = new SummaryChunkList(rvdIDs);
                                try {
                                    out.writeObject(wantedIDs);
                                    out.flush();
                                    System.out.println(rvdIDs + " requested for missing ID's");
                    
                                } catch (IOException e) {
                                    System.out.println("Failed to  Send missing ID Request");
                                }
                            }
                        }
                        track = 1;
                    } else if (track == 1 && resp instanceof FileChunkList) {
                        System.out.print("Received missing IDS <--- ");
                        
                        for (FileChunk c : ((FileChunkList) resp).chunks) {
                            chunkIDs.add(c.chunkID);
                            chunks.put(c.chunkID, c);
                            rvdChunks++;
                            System.out.print(c.chunkID + " ");
                        }
                        System.out.println();
                        System.out.println("List" + chunkIDs);
                        System.out.println(rvdChunks);
                        finished = true;
                        in.close();
                        out.close();
                        downloadNeighbourSocket.close();
                    }
                }
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    System.out.println("Failed to Pause the Thread");
                }
            } catch (ClassNotFoundException c) {
                System.out.println(c);
            }
            System.out.println("CurrentChunks - TotalChunks:"+ chunkIDs.size() + "---" + numberOfChunks);
            
            if (chunkIDs.size() == numberOfChunks) {
                unfinished = false;
                System.out.println("Peer Recieved all the Chunks");
                System.out.println(chunkIDs);
                try {
                    joinFiles();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    public void joinFiles() throws IOException{
        String fileName = counter +"-joined" + "." +  fileType;
        FileOutputStream fos = new FileOutputStream(fileName);
        try(BufferedOutputStream joinStream = new BufferedOutputStream(fos)){
            for(FileChunk c : chunks.values()){
                joinStream.write(c.bytes);
            }
        
            joinStream.close();
            System.out.println("Joined all chunks and formed : " + fileName);
        }
    }

}