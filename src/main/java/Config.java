import java.util.*;

public class Config {

    public List<Node> nodes;
    public List<Edge> edges;
    public int depot;

    public int vehicles;

    public int capacity;

    public Map<Integer, List<Edge>> edgeMap = new HashMap<>();

    public Double[][] matrix;

    public Config(List<Node> nodes, List<Edge> edges, int depot, int vehicles, int capacity){
        this.nodes = nodes;
        this.edges = edges;
        this.depot = depot;
        this.vehicles = vehicles;
        this.capacity = capacity;

        createEdgeMap();
    }

    public void createEdgeMap(){
        for (Edge e : this.edges){
            if(edgeMap.containsKey(e.leftNumber)){
                if(!edgeMap.get(e.leftNumber).contains(e))
                    edgeMap.get(e.leftNumber).add(e);
            }
            else{
//                edgeMap.put(e.leftNumber, List.of(e));
                edgeMap.put(e.leftNumber, new ArrayList<>());
                edgeMap.get(e.leftNumber).add(e);
            }

            if(edgeMap.containsKey(e.rightNumber)){
                if(!edgeMap.get(e.rightNumber).contains(e))
                    edgeMap.get(e.rightNumber).add(e);
            }
            else{
//                edgeMap.put(e.rightNumber, List.of(e));
                edgeMap.put(e.rightNumber, new ArrayList<>());
                edgeMap.get(e.rightNumber).add(e);
            }
        }
    }

}
