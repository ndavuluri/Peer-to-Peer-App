import java.io.*;
import java.net.*;
import java.util.*;

public class FileClient {
    
    ObjectOutputStream  out;           
    ObjectInputStream   in;            
    int clientPort;
    int serverPort;
    int downloadNeighbourPort;         
    int numberOfChunks;                
    HashMap<Integer,FileChunk> chunks;            
    HashSet<Integer> chunkIDs;          //Summary List of the Chunk ID's
    String fileType;
    int rvdChunks;


    public static void main(String[] args) {
        //Read ports info from command line 
        int portno = 0;
        int counter = Integer.parseInt(args[0]);
        FileClient client = new FileClient();
        //Added try block - Scanner 
        try{
          Scanner scanner = new Scanner(new File("config.txt"));
          
          int skip = counter-1;

             if(scanner.hasNextInt()){
               portno = scanner.nextInt();
            }
            scanner.nextLine(); //To skip \n
            for(int i = skip ; i> 0 ; i--)
                scanner.nextLine();

            if(scanner.hasNext()){
           client.clientPort = scanner.nextInt();
           client.downloadNeighbourPort = scanner.nextInt();
        }
             System.out.println("Serverport:" + portno);
             System.out.println("ClientPort" + client.clientPort);
             System.out.println("Download" + client.downloadNeighbourPort);
        }
        catch(Exception e ){
            System.out.println("Exception in reading Config file and assigning port numbers");
            System.out.println(e);

        }
        
        //client.clientPort = Integer.parseInt(args[0]);
        //client.downloadNeighbourPort = Integer.parseInt(args[1]);
        client.serverPort = portno;
        client.run(counter);
    }

    void run(int counter){
        Socket ServerSocket = null;
        try {
            ServerSocket = new Socket("localhost", serverPort);
            try {
                out = new ObjectOutputStream(ServerSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(ServerSocket.getInputStream());
                Object object = null;
                try {
                    System.out.println("Received chunks from the Server");
                    object = in.readObject();
                    if (object instanceof ChunksFromOwner) {
                        numberOfChunks = ((ChunksFromOwner) object).numberOfChunks;
                        chunks = new HashMap<>();
                        rvdChunks = ((ChunksFromOwner) object).chunks.size();
                        for( FileChunk c : ((ChunksFromOwner) object).chunks){
                            chunks.put(c.chunkID, c);
                        }
                        fileType = ((ChunksFromOwner) object).fileType;
                    }

                    // Summary File and updating
                    chunkIDs = new HashSet<>();
                    addtoList();
                    
                    Runnable r1 = new Upload(clientPort,chunks,chunkIDs); // Create a Thread and Listen on ClientPort
                    new Thread(r1).start();

                    Runnable r2 = new Download(downloadNeighbourPort, chunks, chunkIDs,numberOfChunks,
                            clientPort,fileType, rvdChunks,counter);
                    new Thread(r2).start();


                } catch (IOException e) {
                    System.out.println("Failed to Read object");
                } catch (ClassNotFoundException e) {
                    System.out.println("Got some Unrecognized Object on the Stream");
                }
            }catch (IOException e){
                System.out.println("couldnot connect to ServerPort");
            }

        }catch (IOException e){
            System.out.println("Unable to Connect to the Server!");
        }
        finally {
            try {
                in.close();
                out.close();
                ServerSocket.close();
            }
            catch(IOException e) {
                System.out.println("failed to Close the Connection");
            }
        }
    }

    public void addtoList(){
        if(!chunks.isEmpty()) {
            for (FileChunk c : chunks.values()) {
                chunkIDs.add(c.chunkID);
                System.out.print(c.chunkID + ",");
            }
            System.out.println("");
        }
    }
}