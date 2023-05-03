import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\egl\\egl-e2-A.dat");
//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\egl\\egl-e3-B.dat");
//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\egl\\egl-e4-C.dat");
//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\egl\\egl-e2-B.dat");
//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\egl\\egl-e2-C.dat");

//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\gdb\\gdb5.dat");
//        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\gdb\\gdb10.dat");


        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\val\\val10A.dat");

        //        runDataset("C:\\Users\\Asus\\ownCloud\\cvut\\carp\\carpbak\\src\\main\\resources\\egl\\egl-s3-A.dat");

    }

    public static void runDataset(String file) throws IOException {
        List<String> l = Arrays.stream(file.split("[\\\\, .]")).toList();
        String output = l.get(l.size()-2);
        String ol = output;
        output = "C:\\Users\\Asus\\ownCloud\\cvut\\carp\\resultsWrap\\" + output + ".csv";

        File f = new File(output);
//        System.out.println(f.createNewFile());
        FileWriter fw = new FileWriter(f);
        PrintWriter pw = new PrintWriter(fw);


        pw.println("seed,k=50 iteration, best,k=100 iteration, best,k=Inf iteration, best");
        System.out.println(ol);
        for (int i = 0; i < 10; i++) {
            System.out.println("seed " + i);
            Individual best1 = run(file, i, 50);
            Individual best2 = run(file, i, 100);
            Individual best3 = run(file, i, Integer.MAX_VALUE);

            pw.println(i+","+best1.nbofGeneration+","+best1.evaluation.cost+","+best2.nbofGeneration+","+best2.evaluation.cost+","+best3.nbofGeneration+","+best3.evaluation.cost);
            pw.flush();
        }

        pw.flush();
        pw.close();
        fw.close();
    }

    public static Individual run(String file, int seed, int period) throws IOException {

        List<String> l = Arrays.stream(file.split("[\\\\, .]")).toList();
        String output = l.get(l.size()-2);
        output = "C:\\Users\\Asus\\ownCloud\\cvut\\carp\\results\\" + output + "-s="+seed+"-p="+period+".txt";

        File f = new File(output);
//        System.out.println(f.createNewFile());
        FileWriter fw = new FileWriter(f);
        PrintWriter pw = new PrintWriter(fw);




//        if(true) return;

        Config config = null;
        try {
            config = readGDB(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Double[][] matrix = floydWarshall(config.nodes);
        Random random = new Random(seed);  //SEED
        config.matrix = matrix;
        Route.matrix = matrix;
        Genetic.matrix = matrix;
        Genetic.config = config;
        Individual.config = config;
        Individual.random = random;
        Genetic.random = random;

        List<Edge> requiredEdges = config.edges.stream().filter(e -> e.required).collect(Collectors.toList());

        Genetic genetic = new Genetic(requiredEdges, pw);
        genetic.evolution(100, 500, 0.9, 0.5, period, period, 0.15, 3);

        pw.flush();
        pw.close();
        fw.close();

        return genetic.BEST;
    }

    public static Config readGDB(String file) throws IOException {
        FileReader fileReader = new FileReader(file);
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

        //ARISTAS_NOREQ
        line = bufferedReader.readLine();
        Matcher optionalCountMatch = patternDigit.matcher(line);
        if(!optionalCountMatch.find()) throw new RuntimeException("optional edges regex failed");
        int optionalCount = Integer.parseInt(optionalCountMatch.group());


        //VEHICULOS
        line = bufferedReader.readLine();
        Matcher vehiclesMatch = patternDigit.matcher(line);
        if(!vehiclesMatch.find()) throw new RuntimeException();
        int vehicles = Integer.parseInt(vehiclesMatch.group());


        //CAPACIDAD
        line = bufferedReader.readLine();
        Matcher capacityMatch = patternDigit.matcher(line);
        if(!capacityMatch.find()) throw  new RuntimeException();
        int capacity = Integer.parseInt(capacityMatch.group());


        //TIPO_COSTES_ARISTAS
        line = bufferedReader.readLine();

        //COSTE_TOTAL_REQ
        line = bufferedReader.readLine();

        //LISTA_ARISTAS_REQ
        line = bufferedReader.readLine();

        Pattern patternRequired = Pattern.compile("\\(\\s+(\\d+),\\s+(\\d+)\\)\\s+coste\\s+(\\d+)\\s+demanda\\s+(\\d+)");
        Pattern patternOptional = Pattern.compile("\\(\\s+(\\d+),\\s+(\\d+)\\)\\s+coste\\s+(\\d+)");

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

    public static List<Route> construct(List<Edge> priority, Config config, Map<Node, Map<Node, AnalysisNode>> journal,
                                        boolean journaling){
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < priority.size(); i++) {
            Edge edge = priority.get(i);
            routes.add(new Route(edge, config.capacity));
        }

        for (int i = 0; i < priority.size(); i++) {
//            System.out.println("Iteration " + i);
            Edge edge = priority.get(i);
            Route route = edge.component;

            Candidate selectedCandidate = selectFromRoutes(routes, route, config.matrix, journal, journaling);

            if(selectedCandidate == null){
                //TODO vrat se zpet do depot, NEMUSIM RESIT
            }
            else{
                route.mergeRouteE(selectedCandidate);
            }
        }

        return new ArrayList<>(routes.stream().filter(r -> r.active).collect(Collectors.toList()));
    }

    public static Evaluation evaluatePriorityList(List<Edge> priority, Config config, Map<Node, Map<Node,
            AnalysisNode>> journal, boolean journaling){
        List<Route> routes = construct(priority, config, journal, journaling);
        int cumulativeCost = evaluateRoutes(routes, config);
        return new Evaluation(cumulativeCost, routes.size(), routes);
    }

    public static int evaluateRoutes(List<Route> routes, Config config){
        int cumulativeCost = 0;
        for (Route r :
                routes) {
            cumulativeCost += evaluateRoute(r, config.matrix);
        }
        return cumulativeCost;
    }


    public static double evaluateRoute(Route route, Double[][] matrix){
        if(route.tail == null) return 0.0;

        Element element = route.tail;
        double cost = 0;
        while(element != null){
            cost += element.candidate.edge.cost;
            if(element.next != null)
                cost += element.nextDistance;
            element = element.next;
        }
//        for (Node krajni :
//                route.findOuterNodesObj()) {
//            cost += matrix[krajni.number][1];
//        }
//        cost += matrix[route.tail.previousLink.number][1];
//        cost += matrix[route.head.nextLink.number][1];

        route.tail.previousDistance = matrix[route.tail.previousLink.number][1];
        route.head.nextDistance = matrix[route.head.nextLink.number][1];

        cost += route.tail.previousDistance;
        cost += route.head.nextDistance;

        return cost;
    }

    public static List<Edge> deepCopy(List<Edge> priorityList){
        List<Edge> newPriorityList = new ArrayList<>();
        for (Edge e: priorityList){
            newPriorityList.add(new Edge(e));
        }
        return newPriorityList;
    }

    public static Double[][] floydWarshall(List<Node> nodes){
        Node[] nodeArray = getNodeArray(nodes);

//        Integer[][] matrix = new Integer[nodes.size()+1][nodes.size()+1];
        Double[][] matrix = new Double[nodes.size()+1][nodes.size()+1];

//        System.out.println(matrix.length);

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
//                    matrix[i][j] = Double.POSITIVE_INFINITY;
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

        return matrix;
//        return Arrays.asList(matrix, matrix2);
    }


    public static Node[] getNodeArray(List<Node> nodes){
        Node[] nodesArray = new Node[nodes.size() + 1];
        for (int i = 1; i < nodes.size(); i++) {
            nodesArray[i] = getNode(nodes, i);
        }
        return nodesArray;
    }

    public static Node getNode(List<Node> nodes, int number){
        return nodes.stream().filter(n -> n.number == number).findFirst().get();
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
     * stezejni metoda, ve ktereho dochazi k vyberu nejvhodnejsiho kandidata na prodlouzeni cesty
     */
    public static Candidate selectFromRoutes(List<Route> routes, Route route, Double[][] matrix, Map<Node, Map<Node,
            AnalysisNode>> journal, boolean journaling){
        Node outerLeft = route.tail.previousLink;
        Node outerRight = route.head.nextLink;

        List<Candidate> candidates = new ArrayList<>();

        //podivej se na vsechny ostatni rozpracovane cesty a jejich krajni body, uvaz pouze ty ktere vyhovuji kapacitnim omezenim
        for (Route r :
                routes) {
            if(!r.active || r == route){
                continue;
            }
            Node left = r.tail.previousLink;
            Node right = r.head.nextLink;

            Candidate c;
            if((c = evaluateCandidate(route, outerLeft, left, r.tail.candidate.edge, matrix, journal)) != null){
                candidates.add(c);
            }
            if((c = evaluateCandidate(route, outerLeft, right, r.head.candidate.edge, matrix, journal)) != null){
                candidates.add(c);
            }
            if((c = evaluateCandidate(route, outerRight, left, r.tail.candidate.edge, matrix, journal)) != null){
                candidates.add(c);
            }
            if((c = evaluateCandidate(route, outerRight, right, r.head.candidate.edge, matrix, journal)) != null){
                candidates.add(c);
            }
        }

//        Collections.sort(candidates, Comparator.comparingDouble(Candidate::getScore).thenComparingInt(Candidate::getToNodeNumber).thenComparingInt(Candidate::getFromNodeNumber).thenComparingInt(Candidate::hashCode));


        //vybrani nejlepsiho kandidata nejprve podle vzdalenosti, poto podle zaznamu v journal (getJournalEntry())
        if(!journaling){
            Collections.sort(candidates, Comparator.comparingDouble(Candidate::getDistance));
        }
        if(journaling){
//            Map<Node, Integer> nodeCountsFromLeftNode = new HashMap<>();
//            Map<Node, Integer> nodeCountsFromRightNode = new HashMap<>();
//
//            for(Candidate candidate : candidates){
//                if(candidate.fromNode == outerLeft){
//                    if(!nodeCountsFromLeftNode.containsKey(candidate.toNode)){
//                        nodeCountsFromLeftNode.put(candidate.toNode, 1);
//                    }
//                    else{
//                        int count = nodeCountsFromLeftNode.get(candidate.toNode);
//                        nodeCountsFromLeftNode.put(candidate.toNode, count + 1);
//                    }
//                }
//                else if(candidate.fromNode == outerRight){
//                    if(!nodeCountsFromRightNode.containsKey(candidate.toNode)){
//                        nodeCountsFromRightNode.put(candidate.toNode, 1);
//                    }
//                    else{
//                        int count = nodeCountsFromRightNode.get(candidate.toNode);
//                        nodeCountsFromRightNode.put(candidate.toNode, count + 1);
//                    }
//                }
//                else{
//                    throw new RuntimeException();
//                }
//            }
//
//            int countLeft = 0;
//            int countDoubleLeft = 0;
//            int leftSum = 0;
//            for(Node node : nodeCountsFromLeftNode.keySet()){
//                countLeft++;
//                if(nodeCountsFromLeftNode.get(node) > 1){
//                    countDoubleLeft++;
//                }
//                leftSum += nodeCountsFromLeftNode.get(node);
//            }
//
//            int countRight = 0;
//            int countDoubleRight = 0;
//            for(Node node : nodeCountsFromRightNode.keySet()){
//                countRight++;
//                if(nodeCountsFromRightNode.get(node) > 1){
//                    countDoubleRight++;
//                }
//            }
//            System.out.println("routes: " + routes.size() + " " + routes.stream().filter(r -> r.active).toList().size());
//            System.out.println("left: " + countDoubleLeft + " " + countLeft + " right: " + countDoubleRight + " " + countRight);
//
//            System.out.println(candidates.stream().filter(c -> c.fromNode == outerLeft).toList().size());
//            System.out.println(leftSum);

            Collections.sort(candidates, Comparator.comparingDouble(Candidate::getJournalEntry).thenComparingDouble(Candidate::getDistance));
        }

        //vyber prvniho nejlepsiho
        if(candidates.size() > 0){
            return candidates.get(0);
        }
        return null;
    }

    public static Candidate evaluateCandidate(Route route, Node fromNode, Node toNode, Edge toEdge, Double[][] matrix, Map<Node, Map<Node, AnalysisNode>> journal){
        if(toEdge.component == route || route.capacityLeft < toEdge.component.capacityTaken){
            return null;
        }
        Candidate c = new Candidate(toEdge, toNode, fromNode, matrix[fromNode.number][toNode.number]);
        //TODO score evaluation bude pocitat s cetnosti
        c.score = c.distance;

        if(journal.containsKey(fromNode)){
            Map<Node, AnalysisNode> subJournal = journal.get(fromNode);
            if(subJournal.containsKey(toNode)){
                AnalysisNode an = subJournal.get(toNode);
                c.journalEntry = an.sum/an.count;
            }
            else{
                c.journalEntry = Double.POSITIVE_INFINITY;
            }
        }
        else{
            c.journalEntry = Double.POSITIVE_INFINITY;
        }

        return c;
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
        Node newNode = new Node(number);
        nodes.add(newNode);
        return newNode;
    }

}
