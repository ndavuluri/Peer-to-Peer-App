import java.io.*;
import java.net.*;
import java.util.*;

public class Upload implements Runnable {
    int port;
    ObjectInputStream in;
    ObjectOutputStream out;
    HashMap<Integer,FileChunk> chunks;
    HashSet<Integer> chunkIDs;

    public Upload(int port,HashMap<Integer,FileChunk> chunks, HashSet<Integer> chunkIDs) {
        this.port = port;
        this.chunks = chunks;
        this.chunkIDs = chunkIDs;
    }

    public void run() {
        ServerSocket peerServer = null;
        try {
            peerServer = new ServerSocket(port);
            System.out.println("---> Client is Listening on Port" + port);
            while (true) {
                try {
                    Socket uploadNeighbour = peerServer.accept();
                    System.out.println("upload Neighbour Request received");
                    Object resp = null;

                    out = new ObjectOutputStream(uploadNeighbour.getOutputStream());
                    out.flush();
                    in = new ObjectInputStream(uploadNeighbour.getInputStream());
                    out.writeObject(new SummaryChunkList(chunkIDs));
                    out.flush();
                    System.out.println("Peer Sent Summary List to its upload neighbour ---> " + chunkIDs);

                    resp = in.readObject();
                    if (resp instanceof SummaryChunkList) {
                        HashSet<FileChunk> reqChunks = new HashSet<>();
                        
                        for (Integer i : ((SummaryChunkList) resp).chunkIDs) {
                            System.out.print(i + " ");
                            reqChunks.add(chunks.get(i));
                        }
                        System.out.println();
                        System.out.print("peer uploaded all requested chunks  to its upload neighbour ---> ");
                        
                        out.writeObject(new FileChunkList(reqChunks));
                        out.flush();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                } catch (ClassNotFoundException c) {
                    System.out.println(c);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}