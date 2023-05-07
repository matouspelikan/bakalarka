import java.util.Objects;

public class Candidate {
    public Edge edge;
    public Node toNode;
    public Node fromNode;
    public double distance;

    public double score;
    public double journalEntry;
    public double journalEdgeEntry;

    public Candidate(Edge edge, Node toNode, Node fromNode, double distance) {
        this.edge = edge;
        this.toNode = toNode;
        this.fromNode = fromNode;
        this.distance = distance;
    }

    public Double getDistance(){
        return distance;
    }

    public Double getJournalEntry(){
        return journalEntry;
    }

    public Double getJournalEdgeEntry(){
        return journalEdgeEntry;
    }

    @Override
    public String toString() {
        return edge.toString() + " fromNode: " + this.fromNode.number + " toNode: " + this.toNode.number +
                " distance: " + distance;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Candidate candidate = (Candidate) o;
//        return Double.compare(candidate.distance, distance) == 0 && Double.compare(candidate.score, score) == 0 && Objects.equals(edge, candidate.edge) && Objects.equals(toNode, candidate.toNode) && Objects.equals(fromNode, candidate.fromNode);
//    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, toNode, fromNode, distance, score);
    }
}
