public class Candidate {
    public Edge edge;
    public Node toNode;
    public Node fromNode;
    public double distance;

    public Candidate(Edge edge, Node toNode, Node fromNode, double distance) {
        this.edge = edge;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return edge.toString() + " fromNode: " + this.fromNode.number + " toNode: " + this.toNode.number +
                " distance: " + distance;
    }
}
