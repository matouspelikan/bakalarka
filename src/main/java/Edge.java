import java.io.Serializable;
import java.util.*;

public class Edge implements Serializable {

    public int leftNumber;
    public int rightNumber;

    public Node leftNode;
    public Node rightNode;

    List<Edge> leftEdges = new ArrayList<>();
    List<Edge> rightEdges = new ArrayList<>();

    public int cost;
    public int demand;
    public boolean required;

    public Route component = null;

    public Edge(){}

    public Edge(int leftNumber, int rightNumber, boolean required, Node leftNode, Node rightNode){
        this.leftNumber = leftNumber;
        this.rightNumber = rightNumber;
        this.required = required;
        this.leftNode = leftNode;
        this.rightNode = rightNode;

        leftNode.edges.add(this);
        rightNode.edges.add(this);
    }

    public Edge(Edge edge){
        this.leftNumber = edge.leftNumber;
        this.rightNumber = edge.rightNumber;
        this.required = edge.required;
        this.leftNode = edge.leftNode;
        this.rightNode = edge.rightNode;

        this.cost = edge.cost;
        this.demand = edge.demand;

        this.leftEdges = edge.leftEdges;
        this.rightEdges = edge.rightEdges;

//        this.component = edge.component;
    }

    public Edge(Edge edge, boolean second){
        this.leftNumber = edge.leftNumber;
        this.rightNumber = edge.rightNumber;
        this.required = edge.required;
        this.leftNode = edge.leftNode;
        this.rightNode = edge.rightNode;

        this.cost = edge.cost;
        this.demand = edge.demand;

        this.leftEdges = edge.leftEdges;
        this.rightEdges = edge.rightEdges;

        this.component = edge.component;
    }

    public void connect(List<Edge> adjacent, boolean recursive){
        for (int i = 0; i < adjacent.size(); i++) {
            Edge next = adjacent.get(i);

            if(this.leftNumber == next.leftNumber || this.leftNumber == next.rightNumber){
                this.leftEdges.add(next);
            }
            else if(this.rightNumber == next.leftNumber || this.rightNumber == next.rightNumber){
                this.rightEdges.add(next);
            }
            else{
                throw new RuntimeException("spatne adjacent vyber");
            }

            if(recursive) next.connect(new ArrayList<>(Arrays.asList(this)), false);

        }
    }

    public Node otherNode(Node other){
        if(leftNode == other){
            return rightNode;
        }
        return leftNode;
    }

    public List<Integer> numbers(){
        return Arrays.asList(leftNumber, rightNumber);
    }

    public boolean hasNode(int node){
        return node == leftNumber || node == rightNumber;
    }

    @Override
    public String toString() {
//        return "Edge{" +
//                "leftNumber=" + leftNumber +
//                ", rightNumber=" + rightNumber +
//                ", cost=" + cost +
//                ", demand=" + demand +
//                '}';
        return "(" + leftNumber + ", " + rightNumber + ") " + cost + " " + demand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return leftNumber == edge.leftNumber && rightNumber == edge.rightNumber && cost == edge.cost && demand == edge.demand;
    }

    @Override
    public int hashCode() {
        return new Random(Objects.hash(leftNumber, rightNumber, cost, demand)).nextInt();
    }

    public int hash(){
        return new Random(this.hashCode()).nextInt();
    }
}
