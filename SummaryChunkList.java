import java.io.Serializable;
import java.util.HashSet;

public class SummaryChunkList implements Serializable {
    HashSet<Integer> chunkIDs;

    public SummaryChunkList(HashSet<Integer> chunkIDs) {
        this.chunkIDs = chunkIDs;
    }
}