import java.util.Optional;

public class Entry {
    public int nodeNumber;
    public Node node;
    public double distance;

    public int from;

    public Entry(int nodeNumber, double distance, int from, Optional<Node> node){
        this.nodeNumber = nodeNumber;
        this.distance = distance;
        this.from = from;
        if (distance < Double.POSITIVE_INFINITY){
            this.node = node.get();
        }
        else{
            this.node = null;
        }
    }

    @Override
    public String toString() {
        return "Entry{" +
                "nodeNumber=" + nodeNumber +
                ", distance=" + distance +
                '}';
    }
}
