import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Route {

    public List<Edge> edges = new ArrayList<>();
    public List<Candidate> candidates = new ArrayList<>();
    public List<Integer> nodesInt = new ArrayList<>();

    public int leftBorder;
    public int rightBorder;

    public int capacityLeft;
    public int capacityTaken;

    public Element head = null;
    public Element tail = null;

    public boolean active = true;

    public Route(){
    }

    public Route(Edge edge, int capacity){
//        this.add(edge, 0, 0);
//        edge.component = this;
        this.capacityLeft = capacity;
        this.capacityTaken = 0;

        edges.add(edge);
        edge.component = this;
        edge.component2 = this;

        this.leftBorder = edge.leftNumber;
        this.rightBorder = edge.rightNumber;

        Candidate base = new Candidate(edge, null, null, -1);
        candidates.add(base);

        Element element = new Element(base);
        head = element;
        tail = element;

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
//                int size = newRoute.edges.size();
//                for (int i = 0; i < size; i++) {
//                    Edge e = newRoute.edges.get(size - i);
//                    e.component = this;
//                    this.edges.add(e);
//                }
            }
            else if(this.leftBorder == candidate.fromNode.number){
//                int size = newRoute.edges.size();
//                for (int i = 0; i < size; i++) {
//                    Edge e = newRoute.edges.get(size - i);
//                    e.component = this;
//                    this.edges.add(0, e);
//                }
            }
            else{
                throw new RuntimeException();
            }

        }
        else if(newRoute.leftBorder == candidate.toNode.number){
            if(this.rightBorder == candidate.fromNode.number){
//                int size = newRoute.edges.size();
//                for (int i = 0; i < size; i++) {
//                    Edge e = newRoute.edges.get(i);
//                    e.component = this;
//                    this.edges.add(e);
//                }
            }
            else if(this.leftBorder == candidate.fromNode.number){
//                int size = newRoute.edges.size();
//                for (int i = 0; i < size; i++) {
//                    Edge e = newRoute.edges.get(i);
//                    e.component = this;
//                    this.edges.add(0, e);
//                }
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

    public void mergeRoutesC(Candidate candidate){
        Route newRoute = candidate.edge.component;
        if(newRoute.rightBorder == candidate.toNode.number){
//        if()
            if(this.rightBorder == candidate.fromNode.number){
                int size = newRoute.edges.size();
                for (int i = 0; i < size; i++) {
                    Edge e = newRoute.edges.get(size - i);
                    e.component = this;
                    this.edges.add(e);

                    Candidate c = newRoute.candidates.get(size - i);
                    this.candidates.add(c);
                }
            }
            else if(this.leftBorder == candidate.fromNode.number){
                int size = newRoute.edges.size();
                for (int i = 0; i < size; i++) {
                    Edge e = newRoute.edges.get(size - i);
                    e.component = this;
                    this.edges.add(0, e);

                    Candidate c = newRoute.candidates.get(size - i);
                    this.candidates.add(0, c);
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

    public Element getEnd(Candidate candidate){

        if(candidate.edge.component.tail.candidate.edge == candidate.edge){
            candidate.edge.component.tail.candidate = candidate;
        }
        else if(candidate.edge.component.head.candidate.edge == candidate.edge){
            candidate.edge.component.head.candidate = candidate;
        }
        else{
            throw new RuntimeException();
        }


        if(candidate.edge == candidate.edge.component.tail.candidate.edge){
            return candidate.edge.component.tail;
        }
        else if(candidate.edge == candidate.edge.component.head.candidate.edge){
            return candidate.edge.component.head;
        }
        else{
            throw new RuntimeException();
        }
    }

    public void mergeRouteE(Candidate candidate){
        this.capacityLeft -= candidate.edge.demand;

        if(this.capacityLeft < 0) throw new RuntimeException();
        candidate.edge.component.active = false;

        Element element = new Element(candidate);
        element = getEnd(candidate);

        System.out.println(candidate);
        System.out.println("tail/head");
        System.out.println(element.candidate);
        System.out.println(element);
        System.out.println(element.previous);
        System.out.println(element.next);

        boolean forward = true;
        if(element.next == null){
            forward = false;
        }
        else if(element.previous == null){
            forward = true;
        }
        else{
            throw new RuntimeException();
        }

        System.out.println("forward: ");
        System.out.println(forward);

        if(head.candidate.edge.hasNode(candidate.fromNode.number)){
            System.out.println("head ji ma");
            head.next = element;
            head.nextDistance = element.candidate.distance;
            head.nextLink = element.candidate.fromNode;

            Element HEAD = head;

            Element elementIter = element;
            Element elementIterNext = null;
            while (elementIter != null) {
                System.out.println(elementIter);
                System.out.println(elementIter.previous);
                System.out.println(elementIter.next);
//                System.out.println(elementIter.candidate);
                head = elementIter;
                elementIter.candidate.edge.component = this;
                if(forward){
                    elementIterNext = elementIter.next;

                }
                else
                    elementIterNext = elementIter.previous;

                elementIter.next = elementIterNext;
                elementIter = elementIterNext;
            }

            element.previous = HEAD;
            element.previousDistance = element.candidate.distance;
            element.previousLink = element.candidate.toNode;
        }
        else if(tail.candidate.edge.hasNode(candidate.fromNode.number)){
            if (true) throw new RuntimeException();
            tail.previous = element;
            tail.previousDistance = element.candidate.distance;
            tail.previousLink = element.candidate.fromNode;

            element.next = tail;
            element.nextDistance = element.candidate.distance;
            element.nextLink = element.candidate.toNode;
            while (element != null) {
                tail = element;
                element.candidate.edge.component = this;
                if(forward)
                    element = element.next;
                else
                    element = element.previous;
            }
        }
        else{
            System.out.println(head.candidate.edge);
            System.out.println(tail.candidate.edge);
            System.out.println(candidate.fromNode);
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
//        return Arrays.asList(leftBorder, rightBorder);

        System.out.println("find outer nodes");
        System.out.println(tail);
        System.out.println(head);

        if(head == tail){
            return Arrays.asList(leftBorder, rightBorder);
        }

        if(tail.candidate.edge.leftNode.number == tail.nextLink.number){
            leftBorder = tail.candidate.edge.rightNode.number;
        }
        else {
            leftBorder = tail.candidate.edge.leftNode.number;
        }

        System.out.println(tail.previous);
        System.out.println(tail.next);
        System.out.println(tail.next.next);
        if(tail.next.next != null)
            System.out.println(tail.next.next.next);
//        System.out.println(tail.next.next.next);

        System.out.println();
        System.out.println(head.previous);
        System.out.println(head.next);
        if(head.candidate.edge.leftNode.number == head.previousLink.number){
            rightBorder = head.candidate.edge.rightNode.number;
        }
        else{
            rightBorder = head.candidate.edge.leftNode.number;
        }

        return Arrays.asList(leftBorder, rightBorder);

    }

    @Override
    public String toString() {
        return "Route{" +
                "krajní edges=" + this.findOuterNodes().toString() +
                '}';
    }
}
