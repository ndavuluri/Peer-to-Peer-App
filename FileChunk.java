import java.io.File;
import java.io.Serializable;

public class FileChunk implements Serializable {
    int chunkID;
    byte[] bytes;

    public FileChunk(int chunkID, byte[] bytes) {
        this.chunkID = chunkID;
        this.bytes = bytes;
    }
}