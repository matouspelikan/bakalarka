import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        Config config = null;
        try {
            config = readGDB();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        Double[][] matrix = floydWarshall(config.nodes);
        List<Object> matrices = floydWarshall(config.nodes);
        Double[][] matrix = (Double[][]) matrices.get(0);
        Double[][] matrix2 = (Double[][]) matrices.get(1);

        System.out.println("matrix2 row: ");
        System.out.println(Arrays.asList(matrix2[3]));

        Entry[][] entries = createEntries(matrix2, config.nodes);
        System.out.println(Arrays.asList(entries[3]));

        Map<Integer, List<Edge>> edgeMap = config.edgeMap;
        System.out.println("Edge map:");
        System.out.println(edgeMap.get(7));

        List<Edge> requiredEdges = config.edges.stream().filter(e -> e.required).collect(Collectors.toList());
        Collections.shuffle(requiredEdges, new Random(1));
        System.out.println("Required edges: ");
//        requiredEdges.remove(0);
        System.out.println(requiredEdges);

        construct(requiredEdges, matrix, matrix2, entries, config);
    }

    public static void construct(List<Edge> priority, Double[][] matrix, Double[][] matrix2, Entry[][] entries, Config config){
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < priority.size(); i++) {
            routes.add(new Route(priority.get(i), config.capacity));
        }

        int currentVehicle = 0;

        for (int i = 0; i < priority.size(); i++) {
            System.out.println("Iteration " + i);
            Edge edge = priority.get(i);
            //identify component (Route)
//            Route route = routes.stream().filter(r -> r.edges.contains(edge)).findFirst().get();
            Route route = edge.component;
            System.out.println(route.tail);
            System.out.println(route.head);
            System.out.println(route.tail.next);
            System.out.println(route.tail.nextLink);

            List<Integer> ends = route.findOuterNodes();
            int left = ends.get(0);
            int right = ends.get(1);

            //TODO efektivneji vybrat entries aby se nemuselo filtrovat cele pokazde, nejak to mergnout
//            List<Entry> closestLeftCandidates = Arrays.stream(entries[left]).filter(entry -> nodeToComponent(entry.nodeNumber, routes) != route && entry.distance < Double.POSITIVE_INFINITY).toList();
//            List<Entry> closestRightCandidates = Arrays.stream(entries[right]).filter(entry -> nodeToComponent(entry.nodeNumber, routes) != route && entry.distance < Double.POSITIVE_INFINITY).toList();
            List<Entry> closestLeftCandidates = Arrays.stream(entries[left]).filter(entry -> entryToComponent(entry, route) && entry.distance < Double.POSITIVE_INFINITY).toList();
            List<Entry> closestRightCandidates = Arrays.stream(entries[right]).filter(entry -> entryToComponent(entry, route) && entry.distance < Double.POSITIVE_INFINITY).toList();
            List<Entry> closestLRCandidates;
            //            List<Entry> closestLRCandidates = mergeEntryLists(closestLeftCandidates, closestRightCandidates);
            closestLRCandidates = new ArrayList<>(Stream.concat(closestLeftCandidates.stream(), closestRightCandidates.stream()).toList());
            closestLRCandidates.sort(new Comparator<Entry>() {
                @Override
                public int compare(Entry o1, Entry o2) {
                    return Double.compare(o1.distance, o2.distance); }
            });

            System.out.println("merged candidates");
            System.out.println(closestLRCandidates);


            Entry minEntry = closestLRCandidates.get(0);
            int connectingNode = minEntry.fromNodeNumber;

            Entry finalMinEntry = minEntry;
            //doneTODO optimize
//            List<Edge> candidates = edges.stream().filter(e -> e.hasNode(finalMinEntry.nodeNumber) && e.required && edgeToComponent(e, routes) != route).toList();
//            List<Edge> candidates = config.edgeMap.get(minEntry.nodeNumber).stream().filter(e -> e.required && edgeToComponent(e, routes) != route).toList();
            List<Candidate> edgeCandidates = getCandidatesFromMultipleNodes(closestLRCandidates, config.edgeMap, route, routes);

            System.out.println(route.findOuterNodes());
            System.out.println("Candidates through  " + route.findOuterNodes());
            System.out.println(edgeCandidates);
            assert edgeCandidates.size() > 0;

            System.out.println();
            System.out.println(connectingNode);
            System.out.println(route.findOuterNodes());
            Candidate selectedCandidate = selectViableCandidate(route, edgeCandidates);
            System.out.println("selected candidate");
            System.out.println(selectedCandidate);
            if(selectedCandidate == null){
                //TODO vrat se zpet do depot
                System.out.println("je null");
                route.add(new Separator());
            }
            else{
                route.mergeRouteE(selectedCandidate);
//                route.mergeRoutes(selectedCandidate);
//                route.add(selectedCandidate.edge, connectingNode, selectedCandidate.toNode.number);
            }

            Element ocas = route.tail;
            System.out.println("looping");
            while (ocas != null) {
                System.out.println(ocas);
//                System.out.println(ocas.candidate.edge.component);
                ocas = ocas.next;
            }
            System.out.println(route.active);
//            System.out.println(route.findOuterNodes());

            if (i == 2) break;
        }

        for (Route r : routes) {
            System.out.println();
//            System.out.println(r.findOuterNodes());
            System.out.println(r.active);

            Element el = r.tail;
            System.out.println("tail: ");
            while(el != null){
                System.out.println(el.candidate.edge);
                System.out.println(el.previousLink);
                System.out.println(el.previousDistance);
                System.out.println(el.nextLink);
                System.out.println(el.nextDistance);
                System.out.println();
                el = el.next;
            }
            System.out.println("end");
        }

    }

    /**
     * Component = Route which and edge is part of
     */
    public static Route nodeToComponent(int node, List<Route> routes){
        int componentCount = 0;
        Route onlyRoute = null;
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
//            for (int j = 0; j < route.edges.size(); j++) {
//                Edge edge = route.edges.get(j);
//                if(edge.leftNumber == node || edge.rightNumber == node){
//                    return route;
//                }
//            }
            if(route.containsNodeI(node)){
                componentCount++;
                onlyRoute = route;
            }
        }

        if(componentCount > 1){
            return null;
        }

        return onlyRoute;
    }

    public static boolean entryToComponent(Entry entry, Route route){
        if(entry.toNode == null){
            return false;
        }
        for (Edge e : entry.toNode.edges){
            if(e.component != route){
                return true;
            }
        }
        return false;
    }

    public static Route edgeToComponent(Edge edge, List<Route> routes){
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            if(route.edges.contains(edge)){
                return route;
            }
        }
        return null;
    }

    public static Entry[][] createEntries(Double[][] matrix2, List<Node> nodes){
        Entry[][] entries = new Entry[matrix2.length][];
        for (int i = 1; i < matrix2.length; i++) {
            Double[] row = matrix2[i];
            Entry[] entryRow = new Entry[matrix2[i].length];
            for (int j = 0; j < matrix2[i].length; j++) {
                int finalJ = j;
                Optional<Node> toNode = nodes.stream().filter(n -> n.number == finalJ).findFirst();

                int finalI = i;
                Optional<Node> fromNode = nodes.stream().filter(n -> n.number == finalI).findFirst();

                entryRow[j] = new Entry(j, matrix2[i][j], i, toNode, fromNode);
            }
            Arrays.sort(entryRow, new Comparator<Entry>() {
                @Override
                public int compare(Entry o1, Entry o2) {
                    return Double.compare(o1.distance, o2.distance);
                }
            });
            entries[i] = entryRow;
        }
        return entries;
    }

    public static List<Object> floydWarshall(List<Node> nodes){
        Node[] nodeArray = getNodeArray(nodes);

//        Integer[][] matrix = new Integer[nodes.size()+1][nodes.size()+1];
        Double[][] matrix = new Double[nodes.size()+1][nodes.size()+1];

        System.out.println(matrix.length);

        for (int i = 1; i < matrix.length; i++) {
            for (int j = 1; j < matrix.length; j++) {
                if(i == j){
                    matrix[i][j] = 0.0;
//                    matrix[i][j] = Double.POSITIVE_INFINITY;
                    continue;
                }

                Node node = getNode(nodes, i);
                int finalJ = j;
                if(node.nodes.stream().anyMatch(n -> n.number == finalJ)){
//                    matrix[i][j] =
                }

                Optional<Node> onode;
                if((onode = node.nodes.stream().filter(n -> n.number == finalJ).findFirst()).isPresent()){
                    matrix[i][j] = (double)node.costs.get(onode.get());
                }
                else{
//                    matrix[i][j] = Integer.MAX_VALUE;
                    matrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        for (int k = 1; k < matrix.length; k++) {
            for (int i = 1; i < matrix.length; i++) {
                for (int j = 1; j < matrix.length; j++) {
                    if (matrix[i][j] > matrix[i][k] + matrix[k][j]){
                        matrix[i][j] = matrix[i][k] + matrix[k][j];
                    }
                }
            }
        }

//        for (int i = 0; i < matrix.length; i++) {
//            for (int j = 0; j < matrix.length; j++) {
//                System.out.print(matrix[i][j]);
//                System.out.print(" ");
//            }
//            System.out.println();
//        }c


        Double[][] matrix2 = new Double[matrix.length][];

        Node n = null;

        for (int i = 1; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (j==0){
//                    matrix2[i] = new Double[matrix.length];
                    matrix2[i] = matrix[i].clone();
                    matrix2[i][j] = Double.POSITIVE_INFINITY;
                    matrix[i][j] = Double.POSITIVE_INFINITY;
                    continue;
                }
                if (i == j) {
//                    matrix[i][j] = 0.0;
                    matrix[i][j] = Double.POSITIVE_INFINITY;
                    matrix2[i][j] = Double.POSITIVE_INFINITY;

                    //potrebuji zjistit jestli se vrchol nachazi ve vice pozadovanych hranach
                    int finalI = i;
                    if(nodes.stream().filter(n_ -> n_.number == finalI).findFirst().get().nodes.stream().filter(n_ -> n_.hasRequired).toList().size() > 1){
                        matrix2[i][j] = 0.0;
                    }
//                    continue;
                }
                else {
                    int finalJ = j;
                    if(!(n = nodes.stream().filter(o -> o.number == finalJ).findFirst().get()).hasRequired){
                        matrix2[i][j] = Double.POSITIVE_INFINITY;
                    }
                }
            }
        }

//        return matrix;
        return Arrays.asList(matrix, matrix2);
    }

    public static int argMinArray(Double[] array){
        Double min = Double.POSITIVE_INFINITY;
        int arg = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i]<min){
                min = array[i];
                arg = i;
            }
        }
        return arg;
    }

    public static Node[] getNodeArray(List<Node> nodes){
        Node[] nodesArray = new Node[nodes.size() + 1];
        for (int i = 1; i < nodes.size(); i++) {
            nodesArray[i] = getNode(nodes, i);
        }
        return nodesArray;
    }

    public static List<Entry> filterEntries(int left, int right, List<Route> routes, Route route){
        return null;
    }

    //TODO nemusí fungovat, protoze zadny z tech minimal nemusi byt vyhovujici pro
    public static List<Entry> mergeEntryLists(List<Entry> e1, List<Entry> e2){
        double minimal1 = e1.get(0).distance;
        double minimal2 = e2.get(0).distance;
        List<Entry> result = new ArrayList<>();
        double minimal = minimal2;
        if (minimal1 < minimal2){
            minimal = minimal1;
        }
        for (Entry e : e1) {
            if(e.distance > minimal)
                break;
            result.add(e);
        }
        for (Entry e : e2){
            if (e.distance > minimal)
                break;
            result.add(e);
        }
        return result;
    }

    public static Node getNode(List<Node> nodes, int number){
        return nodes.stream().filter(n -> n.number == number).findFirst().get();
    }

    public static Config readGDB() throws IOException {
        FileReader fileReader = new FileReader("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\test.dat");
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        Pattern patternDigit = Pattern.compile("\\d+");

        //NOMBRE
        line = bufferedReader.readLine();

        //COMENTARIO
        line = bufferedReader.readLine();

        //VERTICES
        line = bufferedReader.readLine();

        //ARISTAS_REQ
        line = bufferedReader.readLine();
        Matcher requiredCountMatch = patternDigit.matcher(line);
        if(!requiredCountMatch.find()) throw new RuntimeException("required edges regex failed");
        int requiredCount = Integer.parseInt(requiredCountMatch.group());
        System.out.println(requiredCount);

        //ARISTAS_NOREQ
        line = bufferedReader.readLine();
        Matcher optionalCountMatch = patternDigit.matcher(line);
        if(!optionalCountMatch.find()) throw new RuntimeException("optional edges regex failed");
        int optionalCount = Integer.parseInt(optionalCountMatch.group());
        System.out.println(optionalCount);

        //VEHICULOS
        line = bufferedReader.readLine();
        Matcher vehiclesMatch = patternDigit.matcher(line);
        if(!vehiclesMatch.find()) throw new RuntimeException();
        int vehicles = Integer.parseInt(vehiclesMatch.group());
        System.out.println(vehicles);

        //CAPACIDAD
        line = bufferedReader.readLine();
        Matcher capacityMatch = patternDigit.matcher(line);
        if(!capacityMatch.find()) throw  new RuntimeException();
        int capacity = Integer.parseInt(capacityMatch.group());
        System.out.println(capacity);

        //TIPO_COSTES_ARISTAS
        line = bufferedReader.readLine();

        //COSTE_TOTAL_REQ
        line = bufferedReader.readLine();

        //LISTA_ARISTAS_REQ
        line = bufferedReader.readLine();

        Pattern patternRequired = Pattern.compile("\\( (\\d+), (\\d+)\\)\\s+coste\\s+(\\d+)\\s+demanda\\s+(\\d+)");
        Pattern patternOptional = Pattern.compile("\\( (\\d+), (\\d+)\\)\\s+coste\\s+(\\d+)");

        List<Node> nodes = new ArrayList<>();

//        while((line = bufferedReader.readLine()) != null){
        for (int i = 0; i < requiredCount; i++) {
            line = bufferedReader.readLine();
            processEdge(line, patternRequired, nodes, true);
        }

        if (optionalCount > 0){
            bufferedReader.readLine();
            for (int i = 0; i < optionalCount; i++) {
                line = bufferedReader.readLine();
                processEdge(line, patternOptional, nodes, false);
            }
        }

        line = bufferedReader.readLine();
        Matcher depotMatcher = patternDigit.matcher(line);
        if(!depotMatcher.find()) throw new RuntimeException("invalid input, depot number not found at the end");
        int depot = Integer.parseInt(depotMatcher.group());

        List<Edge> edges = makeEdges(nodes);

        return new Config(nodes, edges, depot, vehicles, capacity);
    }

    public static List<Edge> makeEdges(List<Node> nodes){
        Set<Node> visited = new HashSet<>();
        List<Edge> edges = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            for (int j = 0; j < node.nodes.size(); j++) {
                Node next = node.nodes.get(j);
                if (visited.contains(next)) continue;

                Edge newEdge = new Edge(node.number, next.number, node.demands.get(next) != 0, node, next);
                newEdge.cost = node.costs.get(next);
                newEdge.demand = node.demands.get(next);

                List<Edge> adjacent = edges.stream().filter(e -> e.leftNumber == node.number || e.rightNumber == node.number
                        || e.leftNumber == next.number || e.rightNumber == next.number).collect(Collectors.toList());

                newEdge.connect(adjacent, true);

                edges.add(newEdge);
            }

            visited.add(node);
        }

        return edges;
    }

    /**
        get candidates more effectively
     */
    public static List<Candidate> getCandidatesFromMultipleNodes(List<Entry> entries, Map<Integer, List<Edge>> edgeMap, Route route, List<Route> routes){

        List<Candidate> candidates = new ArrayList<>();
        for (Entry entry : entries){
//            edges.addAll(edgeMap.get(entry.nodeNumber).stream().filter(e -> e.required && e.component != route).toList());
            for(Edge e : edgeMap.get(entry.toNodeNumber).stream().filter(_e -> _e.required && _e.component != route).collect(Collectors.toList())){
                candidates.add(new Candidate(e, entry.toNode, entry.fromNode, entry.distance));
            }
        }
        return candidates;
    }

    /**
     * selects candidates based on current remaining vehicle capacity
     * @return
     */
    public static Candidate selectViableCandidate(Route route, List<Candidate> candidates){
        for (int i = 0; i < candidates.size(); i++) {
            Candidate candidate = candidates.get(i);
            //TODO musím zajistit aby vybrany node byl na kraji svoji komponenty
            if(!candidate.edge.component.findOuterNodes().contains(candidate.toNode.number)){
                continue;
            }

            //greedy
            if(candidate.edge.demand <= route.capacityLeft){
//            if(candidate.edge.component2.capacityTaken <= route.capacityLeft){
                return candidate;
            }
        }
        return null;
    }

    public static void processEdge(String line, Pattern pattern, List<Node> nodes, boolean required){
        Matcher matcher = pattern.matcher(line);

        if(!matcher.find())
            throw new RuntimeException(pattern.toString() + " REGEX failed on line: " + line);

        int from = Integer.parseInt(matcher.group(1));
        int to = Integer.parseInt(matcher.group(2));
        int cost = Integer.parseInt(matcher.group(3));
        int demand = 0;
        if(required)
            demand = Integer.parseInt(matcher.group(4));

        Node fromNode = findNode(from, nodes);
        Node toNode = findNode(to, nodes);

        fromNode.addNode(toNode, cost, demand, required);
        toNode.addNode(fromNode, cost, demand, required);
    }

    public static Node findNode(int number, List<Node> nodes){
        if(nodes.stream().anyMatch(n -> n.number == number)){
            return nodes.stream().filter(n -> n.number == number).findFirst().get();
        }
        Node newNode =  new Node(number);
        nodes.add(newNode);
        return newNode;
    }



}
