import java.util.*;

public class Route {

    public static Random random = new Random(0);
    public static Double[][] matrix;

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

    public boolean twoopted = false;


    public Route(){
    }

    public Route(Edge edge, int capacity){
//        this.add(edge, 0, 0);
//        edge.component = this;
        this.capacityLeft = capacity - edge.demand;
        this.capacityTaken = edge.demand;

        edges.add(edge);
        edge.component = this;
        edge.component2 = this;

        this.leftBorder = edge.leftNumber;
        this.rightBorder = edge.rightNumber;

        Candidate base = new Candidate(edge, null, null, 0);
        candidates.add(base);


        Element element = new Element(base);
        head = element;
        tail = element;

        element.nextLink = element.candidate.edge.rightNode;
        element.previousLink = element.candidate.edge.leftNode;

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
            System.out.println("This Route");
            System.out.println(this.tail);
            System.out.println(this.head);
            System.out.println(candidate);
            System.out.println();
            System.out.println(candidate.edge.component.tail);
            System.out.println(candidate.edge.component.tail.next);
            System.out.println(candidate.edge.component.head);
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
        this.capacityTaken += candidate.edge.component.capacityTaken;
        this.capacityLeft -= (candidate.edge.component.capacityTaken);

        if(this.capacityLeft < 0) throw new RuntimeException();
        candidate.edge.component.active = false;

        Element element = getEnd(candidate);

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

        if(head.candidate.edge.hasNode(candidate.fromNode.number) && (head.nextLink.number == candidate.fromNode.number)){
            head.next = element;
            head.nextDistance = element.candidate.distance;
            head.nextLink = element.candidate.fromNode;

            Element HEAD = head;

            Element elementIterLast = head;
            Element elementIter = element;
            Element elementIterNext = null;
            while (elementIter != null) {
                head = elementIter;
                elementIter.candidate.edge.component = this;
                if(forward){
                    //TODO asi nemusim delat vubec nic
                    elementIterNext = elementIter.next;
                    if(elementIterNext != null){

                    }
                }
                else{
                    elementIterNext = elementIter.previous;
                    elementIter.previous = elementIterLast;

                    double previousDistance = elementIter.previousDistance;
                    elementIter.previousDistance = elementIterLast.nextDistance;

                    Node previousLink = elementIter.previousLink;
                    elementIter.previousLink = elementIter.candidate.edge.otherNode(elementIter.previousLink);
                    elementIter.previousLink = elementIter.nextLink;

                    if(elementIterNext != null){
                        elementIter.next = elementIterNext;
//                        elementIter.nextDistance = elementIter.previousDistance; //TODO konflikt
                        elementIter.nextDistance = previousDistance;
//                        elementIter.nextLink = elementIter.previousLink;
                        elementIter.nextLink = previousLink;
                    }
                    else{
                        elementIter.next = null;
                        elementIter.nextDistance = 0;
                        elementIter.nextLink = previousLink; //TODO tohle se mi vyresi samo na konci
                    }
                }

                elementIterLast = elementIter;
//                elementIter.next = elementIterNext;
                elementIter = elementIterNext;
            }

            element.previous = HEAD;
            element.previousDistance = element.candidate.distance;
            element.previousLink = element.candidate.toNode;
            head.nextLink = head.candidate.edge.otherNode(head.previousLink);
        }
        else if(tail.candidate.edge.hasNode(candidate.fromNode.number) && tail.previousLink.number == candidate.fromNode.number){
//            if (true) throw new RuntimeException();
            tail.previous = element;
            tail.previousDistance = element.candidate.distance;
            tail.previousLink = element.candidate.fromNode;

            Element TAIL = tail;

            Element elementIterLast = tail;
            Element elementIter = element;
            Element elementIterNext = null;
            while (elementIter != null) {
                tail = elementIter;
                elementIter.candidate.edge.component = this;
                if(forward){
                    elementIterNext = elementIter.next;
                    elementIter.next = elementIterLast;

                    double nextDistance = elementIter.nextDistance;
                    elementIter.nextDistance = elementIterLast.previousDistance;

                    Node nextLink = elementIter.nextLink;
                    elementIter.nextLink = elementIter.previousLink;

                    if(elementIterNext != null){
                        elementIter.previous = elementIterNext;
                        elementIter.previousDistance = nextDistance;
                        elementIter.previousLink = nextLink;
                    }
                    else{
                        elementIter.previous = null;
                        elementIter.previousDistance = 0;
                        elementIter.previousLink = nextLink;
                    }
                }
                else{
                    elementIterNext = elementIter.previous;//samo se vyresi
                }
                elementIterLast = elementIter;
                elementIter = elementIterNext;
            }

            element.next = TAIL;
            element.nextDistance = element.candidate.distance;
            element.nextLink = element.candidate.toNode;
            tail.previousLink = tail.candidate.edge.otherNode(tail.nextLink);
        }
        else{
            throw new RuntimeException();
        }
    }

    public void mergeRouteF(Candidate candidate, Element element){
//        this.capacityTaken += candidate.edge.component.capacityTaken;
//        this.capacityLeft -= (candidate.edge.component.capacityTaken);
//
//        if(this.capacityLeft < 0) throw new RuntimeException();
//        candidate.edge.component.active = false;

//        Element element = getEnd(candidate);

        element.candidate = candidate;

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

        if(head.candidate.edge.hasNode(candidate.fromNode.number) && (head.nextLink.number == candidate.fromNode.number)){
            head.next = element;
            head.nextDistance = element.candidate.distance;
            head.nextLink = element.candidate.fromNode;

            Element HEAD = head;

            Element elementIterLast = head;
            Element elementIter = element;
            Element elementIterNext = null;
            while (elementIter != null) {
                head = elementIter;
                elementIter.candidate.edge.component = this;
                if(forward){
                    //TODO asi nemusim delat vubec nic
                    elementIterNext = elementIter.next;
                    if(elementIterNext != null){

                    }
                }
                else{
                    elementIterNext = elementIter.previous;
                    elementIter.previous = elementIterLast;

                    double previousDistance = elementIter.previousDistance;
                    elementIter.previousDistance = elementIterLast.nextDistance;

                    Node previousLink = elementIter.previousLink;
                    elementIter.previousLink = elementIter.candidate.edge.otherNode(elementIter.previousLink);
                    elementIter.previousLink = elementIter.nextLink;

                    if(elementIterNext != null){
                        elementIter.next = elementIterNext;
//                        elementIter.nextDistance = elementIter.previousDistance; //TODO konflikt
                        elementIter.nextDistance = previousDistance;
//                        elementIter.nextLink = elementIter.previousLink;
                        elementIter.nextLink = previousLink;
                    }
                    else{
                        elementIter.next = null;
                        elementIter.nextDistance = 0;
                        elementIter.nextLink = previousLink; //TODO tohle se mi vyresi samo na konci
                    }
                }

                elementIterLast = elementIter;
//                elementIter.next = elementIterNext;
                elementIter = elementIterNext;
            }

            element.previous = HEAD;
            element.previousDistance = element.candidate.distance;
            element.previousLink = element.candidate.toNode;
            head.nextLink = head.candidate.edge.otherNode(head.previousLink);
        }
        else if(tail.candidate.edge.hasNode(candidate.fromNode.number) && tail.previousLink.number == candidate.fromNode.number){
//            if (true) throw new RuntimeException();
            tail.previous = element;
            tail.previousDistance = element.candidate.distance;
            tail.previousLink = element.candidate.fromNode;

            Element TAIL = tail;

            Element elementIterLast = tail;
            Element elementIter = element;
            Element elementIterNext = null;
            while (elementIter != null) {
                tail = elementIter;
                elementIter.candidate.edge.component = this;
                if(forward){
                    elementIterNext = elementIter.next;
                    elementIter.next = elementIterLast;

                    double nextDistance = elementIter.nextDistance;
                    elementIter.nextDistance = elementIterLast.previousDistance;

                    Node nextLink = elementIter.nextLink;
                    elementIter.nextLink = elementIter.previousLink;

                    if(elementIterNext != null){
                        elementIter.previous = elementIterNext;
                        elementIter.previousDistance = nextDistance;
                        elementIter.previousLink = nextLink;
                    }
                    else{
                        elementIter.previous = null;
                        elementIter.previousDistance = 0;
                        elementIter.previousLink = nextLink;
                    }
                }
                else{
                    elementIterNext = elementIter.previous;//samo se vyresi
                }
                elementIterLast = elementIter;
                elementIter = elementIterNext;
            }

            element.next = TAIL;
            element.nextDistance = element.candidate.distance;
            element.nextLink = element.candidate.toNode;
            tail.previousLink = tail.candidate.edge.otherNode(tail.nextLink);
        }
        else{
            throw new RuntimeException();
        }
    }



    public void twoOptWrap(){
//        System.out.println("twoopt");
        int len = length();
        double diff;
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < len - 1; j++) {
                if(i == j) continue;
                if(i > j) continue;
                if((diff = twoOpt(i, j)) < 0){
//                    System.out.println("improvement twoopt " + diff + " " + i + " " + j + " len: " + len);
                    double evalBefore = Main.evaluateRoute(this, matrix);
                    twoOptApply(i, j);
                    double evalAfter = Main.evaluateRoute(this, matrix);
                    if(evalAfter != evalBefore + diff) throw new RuntimeException();
                    this.twoopted = true;
                }
            }
        }
    }

    public double twoOpt(int first, int second){
//        int len = length();
//        int first = random.nextInt(len);
//        int second;
//        while((second = random.nextInt(len)) == first)
//            ;
        Element e1 = get(first);
        Element e2 = get(second);

        double diff = - e1.nextDistance - e2.nextDistance;
        diff += matrix[e1.nextLink.number][e2.nextLink.number];

        int e1N = elementToNumberNext(e1.next);

        int e2N = elementToNumberNext(e2.next);

        diff += matrix[e1N][e2N];

        return diff;
    }

    public void twoOptApply(int first, int second){
        Element e1 = get(first);
        Element e2 = get(second);

        Element HEAD = head;

//        Element ite = tail;
//        while(ite != null){
//            System.out.println(ite.previousLink + " " + ite.previousDistance);
//            System.out.println(ite + " " + ite.candidate.edge.cost);
//            System.out.println(ite.nextLink + " " + ite.nextDistance);
//            System.out.println();
//            ite = ite.next;
//        }

        Element e1Next = e1.next;
        Element e2Next = e2.next;

        e1.next = null;
        e2.next = null;

        Candidate candidate = new Candidate(e2.candidate.edge, e2.nextLink, e1.nextLink, matrix[e1.nextLink.number][e2.nextLink.number]);

        e1Next.previous = null;
        head = e1;
        this.mergeRouteF(candidate, e2);
//        System.out.println(e1.nextLink);

        head.next = e2Next;
        int e2N = elementToNumberNext(e2Next);
        head.nextDistance = matrix[head.nextLink.number][e2N];
        if(e2Next != null){
            e2Next.previous = head;
            e2Next.previousDistance = head.nextDistance;
        }
        head = HEAD;


//        ite = tail;
//        while(ite != null){
//            System.out.println(ite.previousLink + " " + ite.previousDistance);
//            System.out.println(ite + " " + ite.candidate.edge.cost);
//            System.out.println(ite.nextLink + " " + ite.nextDistance);
//            System.out.println();
//            ite = ite.next;
//        }

//        throw new RuntimeException();

    }

    public void singleInsertWrap(){
        int len = length();
        double diff;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if(i == j) continue;
                if(Math.abs(i - j) == 1) continue;
                if(i > j) continue;
//                System.out.println("next iter " + i + " " + j + " " + this.hashCode());
                if((diff = singleInsert(i, j)) < 0){



                    double score = Main.evaluateRoute(this, matrix);
//                    System.out.println("improvement single " + diff);
//                    System.out.println(i + " " + j + "  : " + len);
                    singleInsertApply(i, j);

                    double improved = Main.evaluateRoute(this, matrix);
                    if(improved != score + diff) throw new RuntimeException();
                    if(length() != len) throw new RuntimeException();
//                    System.out.println(this);

                }
            }
        }
    }

    public double singleInsert(int first, int second){
        Element e1 = get(first);
        Element e2 = get(second);

        double diff = - e1.previousDistance - e1.nextDistance - e2.previousDistance - e2.nextDistance;

        int e1N = elementToNumberNext(e1.next);
        int e2N = elementToNumberNext(e2.next);

        int e1P = elementToNumberPrev(e1.previous);
        int e2P = elementToNumberPrev(e2.previous);

        diff += matrix[e1P][e2.previousLink.number] + matrix[e2.nextLink.number][e1N];
        diff += matrix[e2P][e1.previousLink.number] + matrix[e1.nextLink.number][e2N];

        return diff;
    }

    public void singleInsertApply(int first, int second){
        Element e1 = get(first);
        Element e2 = get(second);

        Element e1Prev = e1.previous;
        Node e1PrevLink = e1.previousLink;
        Element e1Next = e1.next;
        Node e1NextLink = e1.nextLink;

        Element e2Prev = e2.previous;
        Node e2PrevLink = e2.previousLink;
        Element e2Next = e2.next;
        Node e2NextLink = e2.nextLink;

        if(e1Prev != null){
            e1.previous.next = e2;
            e1.previous.nextDistance = matrix[e1Prev.nextLink.number][e2PrevLink.number];
        }
        e1.previous = e2Prev;
        int e1P = elementToNumberPrev(e2Prev);
        e1.previousDistance = matrix[e1PrevLink.number][e1P];

        if(e1Next != null){
            e1.next.previous = e2;
            e1.next.previousDistance = matrix[e1Next.previousLink.number][e2NextLink.number];
        }
        e1.next = e2Next;
        int e1N = elementToNumberNext(e2Next);
        e1.nextDistance = matrix[e1NextLink.number][e1N];

        if(e2Prev != null){
            e2.previous.next = e1;
            e2.previous.nextDistance = matrix[e2Prev.nextLink.number][e1PrevLink.number];
        }
        e2.previous = e1Prev;
        int e2P = elementToNumberPrev(e1Prev);
        e2.previousDistance = matrix[e2PrevLink.number][e2P];

        if(e2Next != null){
            e2.next.previous = e1;
            e2.next.previousDistance = matrix[e2Next.previousLink.number][e1NextLink.number];
        }
        e2.next = e1Next;
        int e2N = elementToNumberNext(e1Next);
        e2.nextDistance = matrix[e2NextLink.number][e2N];

        if(tail == e1){
            tail = e2;
        }
        else if(tail == e2){
            tail = e1;
        }

        if(head == e1){
            head = e2;
        }
        else if(head == e2){
            head = e1;
        }

    }

    public void singleReverseWrap(){
        int len = length();

        double diff;
        for (int i = 0; i < len; i++) {
            if((diff = singleReverse(i)) < 0){
                System.out.println("single reverse improvement " + diff);
            }
        }

    }

    public double singleReverse(int index){
        Element e = get(index);

        double diff = - e.previousDistance - e.nextDistance;

        int eN = elementToNumberNext(e.next);
        int eP = elementToNumberPrev(e.previous);

        diff += matrix[eP][e.nextLink.number] + matrix[e.previousLink.number][eN];

        return diff;
    }

    public void singleReverseApply(int index){

    }



    public static int elementToNumberNext(Element element){
        if(element != null)
            return element.previousLink.number;
        return 1;
    }
    public static int  elementToNumberPrev(Element element){
        if(element != null)
            return element.nextLink.number;
        return 1;
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


        if(head == tail){
            return Arrays.asList(leftBorder, rightBorder);
        }

        if(tail.candidate.edge.leftNode.number == tail.nextLink.number){
            leftBorder = tail.candidate.edge.rightNode.number;
        }
        else {
            leftBorder = tail.candidate.edge.leftNode.number;
        }

        if(head.candidate.edge.leftNode.number == head.previousLink.number){
            rightBorder = head.candidate.edge.rightNode.number;
        }
        else{
            rightBorder = head.candidate.edge.leftNode.number;
        }

        Set<Integer> s1 = new HashSet<>();
        s1.add(leftBorder);
        s1.add(rightBorder);

        Set<Integer> s2 = new HashSet<>();
        s2.add(tail.previousLink.number);
        s2.add(head.nextLink.number);

        if(!s1.containsAll(s2) || !s2.containsAll(s1)){
            throw new RuntimeException("findOuterNodes not working properly");
        }

        return Arrays.asList(leftBorder, rightBorder);

    }

    public List<Node> findOuterNodesObj(){
        if(head == tail){
//            return Arrays.asList(leftBorder, rightBorder);
            return Arrays.asList(tail.candidate.edge.leftNode, tail.candidate.edge.rightNode);
        }
        Node leftNode;
        Node rightNode;

        if(tail.candidate.edge.leftNode.number == tail.nextLink.number){
            leftBorder = tail.candidate.edge.rightNode.number;
            leftNode = tail.candidate.edge.rightNode;
        }
        else {
            leftBorder = tail.candidate.edge.leftNode.number;
            leftNode = tail.candidate.edge.leftNode;
        }


        if(head.candidate.edge.leftNode.number == head.previousLink.number){
            rightBorder = head.candidate.edge.rightNode.number;
            rightNode = head.candidate.edge.rightNode;
        }
        else{
            rightBorder = head.candidate.edge.leftNode.number;
            rightNode = head.candidate.edge.leftNode;
        }

//        return Arrays.asList(leftBorder, rightBorder);
        return Arrays.asList(leftNode, rightNode);
    }

    public int length(){
        Element el = tail;
        int count = 0;
        while (el != null){
            count++;
            el = el.next;
//            System.out.println(count);
        }
        return count;
    }

    public Element get(int index){
        Element el = tail;
        while(el != null && index > 0){
            el = el.next;
            index--;
        }
        return el;
    }

    @Override
    public String toString() {
        return fullRoute();
    }

    public String fullRoute(){
        String s = "";
        Element element = tail;
        while(element != null){
            s += element.candidate.edge + " | ";
            element = element.next;
        }
        return s;
    }
}
