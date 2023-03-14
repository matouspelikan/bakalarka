public class Candidate {
    public Edge edge;
    public Node connectingEnd;

    public Candidate(Edge edge, Node connectingEnd) {
        this.edge = edge;
        this.connectingEnd = connectingEnd;
    }

    @Override
    public String toString() {
        return edge.toString() + " connectingEnd: " + this.connectingEnd.number;
    }
}
