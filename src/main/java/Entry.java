import java.util.Optional;

public class Entry {
    public int toNodeNumber;
    public Node toNode;
    public double distance;

    public int fromNodeNumber;
    public Node fromNode;


    public Entry(int nodeNumber, double distance, int from, Optional<Node> toNode, Optional<Node> fromNode){
        this.toNodeNumber = nodeNumber;
        this.distance = distance;
        this.fromNodeNumber = from;
        if (distance < Double.POSITIVE_INFINITY){
            this.toNode = toNode.get();
        }
        else{
            this.toNode = null;
        }
        this.fromNode = fromNode.orElse(null);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "nodeNumber=" + toNodeNumber +
                ", distance=" + distance +
                '}';
    }
}
