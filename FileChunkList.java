import java.io.Serializable;
import java.util.HashSet;

public class FileChunkList implements Serializable{
    HashSet<FileChunk> chunks;

    public FileChunkList(HashSet<FileChunk> chunks) {
        this.chunks = chunks;
    }
}