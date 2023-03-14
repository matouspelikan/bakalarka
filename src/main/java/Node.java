import java.util.*;

public class Node {

    public int number;
    public List<Integer> neighbors;
    public boolean hasRequired = false;
    public List<Node> nodes = new ArrayList<>();
    public Set<Edge> edges = new HashSet<>();
    Map<Node, Integer> costs = new HashMap<>();
    Map<Node, Integer> demands = new HashMap<>();

    Map<Node, Boolean> required = new HashMap<>();

    public Node(int number, int neighbor){
        this.number = number;
        this.neighbors = new ArrayList<>();
        this.neighbors.add(neighbor);
    }

    public Node(int number){
        this.number = number;
        this.neighbors = new ArrayList<>();
    }

    public void add(int neighbor){
        this.neighbors.add(neighbor);
    }

    public void addNode(Node node, int cost, int demand, boolean required){
        this.nodes.add(node);
        this.costs.put(node, cost);
        this.demands.put(node, demand);
        this.required.put(node, required);
        if(required){
            this.hasRequired = true;
        }
    }


    @Override
    public String toString() {
        return "Node{" +
                "number=" + number +
                ", nodes=" + nodes.size() +
                '}';
    }
}
