import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Route {

    public List<Edge> edges = new ArrayList<>();
    public List<Integer> nodesInt = new ArrayList<>();

    public int leftBorder;
    public int rightBorder;

    public Route(){
    }

    public Route(Edge edge){
        this.add(edge, 0, 0);
    }

    public void add(Edge edge, int connectingNode, int connectingNodeDestination){
        nodesInt.add(edge.leftNumber);
        nodesInt.add(edge.rightNumber);
        if(edges.size() == 0) {
            edges.add(edge);

            leftBorder = edge.leftNumber;
            rightBorder = edge.rightNumber;
        }
        else{
            Edge beginning = edges.get(0);
            Edge end = edges.get(edges.size()-1);

            if(leftBorder == connectingNode){
                leftBorder = edge.numbers().stream().filter(n -> n != connectingNodeDestination).findFirst().get();
            }
            else if (rightBorder == connectingNode){
                rightBorder = edge.numbers().stream().filter(n -> n != connectingNodeDestination).findFirst().get();
            }
            else{
                throw new RuntimeException();
            }

            if(beginning.leftNumber == connectingNode || beginning.rightNumber == connectingNode){
                edges.add(0, edge);
            }
            else if(end.leftNumber == connectingNode || end.rightNumber == connectingNode){
                edges.add(edge);
            }
            else{
                throw new RuntimeException();
            }
        }
    }

    public boolean containsNodeI(int node){
        return this.nodesInt.contains(node);
    }

    public List<Integer> findOuterNodes(){
//        if(edges.size() == 1){
//            Edge e = edges.get(0);
//            return Arrays.asList(e.leftNumber, e.rightNumber);
//        }
//        else if(edges.size() == 0){
//            throw new RuntimeException();
//        }
//        else{
//            Edge start = edges.get(0);
//            Edge start2 = edges.get(1);
//
//            Edge end = edges.get(edges.size()-1);
//            Edge end2 = edges.get(edges.size()-2);
//
//
//
//        }
//        return null;
        return Arrays.asList(leftBorder, rightBorder);

    }

    @Override
    public String toString() {
        return "Route{" +
                "edges=" + edges +
                '}';
    }
}
