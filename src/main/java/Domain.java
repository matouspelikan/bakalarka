import java.io.Serializable;
import java.util.Objects;

public class Domain implements Serializable {

    public Edge edge;
    public Node node;

    public Domain(Edge edge, Node node){
        this.edge = edge;
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domain domain = (Domain) o;
        return edge.equals(domain.edge) && node.equals(domain.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, node);
    }

    @Override
    public String toString() {
        return "Domain{" +
                "edge=" + edge +
                ", node=" + node +
                '}';
    }
}
