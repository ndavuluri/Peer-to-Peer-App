import java.io.File;
import java.io.Serializable;
import java.util.*;

public class ChunksFromOwner implements Serializable {
    int numberOfChunks;
    ArrayList<FileChunk> chunks;
    String fileType;

    public ChunksFromOwner (int numberOfChunks, ArrayList<FileChunk> chunks, String fileType) {
        this.numberOfChunks = numberOfChunks;
        this.chunks = chunks;
        this.fileType = fileType;
    }
}