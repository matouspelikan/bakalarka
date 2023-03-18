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
//        this.add(edge, 0, 0);
//        edge.component = this;

        edges.add(edge);
        edge.component = this;

        this.leftBorder = edge.leftNumber;
        this.rightBorder = edge.rightNumber;

    }

    public void add(Separator separator){
        edges.add(separator);
    }
    public void add(Edge edge, int connectingNode, int connectingNodeDestination){
        System.out.println("adding edge");
        System.out.println(edge);
        System.out.println(connectingNode);
        System.out.println(connectingNodeDestination);
        nodesInt.add(edge.leftNumber);
        nodesInt.add(edge.rightNumber);
        edge.component = this;
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

    public void mergeRoutes(Candidate candidate){
        Route newRoute = candidate.edge.component;
        if(newRoute.rightBorder == candidate.toNode.number){
//        if()
            if(this.rightBorder == candidate.fromNode.number){
                int size = newRoute.edges.size();
                for (int i = 0; i < size; i++) {
                    Edge e = newRoute.edges.get(size - i);
                    e.component = this;
                    this.edges.add(e);
                }
            }
            else if(this.leftBorder == candidate.fromNode.number){
                int size = newRoute.edges.size();
                for (int i = 0; i < size; i++) {
                    Edge e = newRoute.edges.get(size - i);
                    e.component = this;
                    this.edges.add(0, e);
                }
            }
            else{
                throw new RuntimeException();
            }

        }
        else if(newRoute.leftBorder == candidate.toNode.number){
            if(this.rightBorder == candidate.fromNode.number){
                int size = newRoute.edges.size();
                for (int i = 0; i < size; i++) {
                    Edge e = newRoute.edges.get(i);
                    e.component = this;
                    this.edges.add(e);
                }
            }
            else if(this.leftBorder == candidate.fromNode.number){
                int size = newRoute.edges.size();
                for (int i = 0; i < size; i++) {
                    Edge e = newRoute.edges.get(i);
                    e.component = this;
                    this.edges.add(0, e);
                }
            }
            else{
                throw new RuntimeException();
            }
        }
        else{
            System.out.println(newRoute);
            System.out.println(candidate.fromNode);
            System.out.println(candidate.toNode);
            throw new RuntimeException();
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
                "krajní edges=" + this.findOuterNodes().toString() +
                '}';
    }
}
