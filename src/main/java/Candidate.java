public class Candidate {
    public Edge edge;
    public Node toNode;
    public Node fromNode;

    public Candidate(Edge edge, Node connectingEnd, Node fromNode) {
        this.edge = edge;
        this.toNode = connectingEnd;
        this.fromNode = fromNode;
    }

    @Override
    public String toString() {
        return edge.toString() + " connectingEnd: " + this.toNode.number;
    }
}
