import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {

//        System.out.println(args.length);
//        if(args.length < 1){
//            System.out.println("Please provide input config file...");
//            return;
//        }
//        String configFile = args[0];
        String configFile = "config.properties";

        CARPProperties properties = CARPProperties.getInstance();
        properties.readConfigFile(configFile);

        RUN(properties);

    }

    public static String createExperimentName(CARPProperties properties){
        char version = 'a';
        String dirName = properties.resultDir + "/" + properties.dataset + "_" + properties.seed + "_" + version;

        File f;
        while((f = absolutePath(dirName).toFile()).exists()){
            dirName = dirName.substring(0, dirName.length()-1);
            dirName += (char)(++version);
        }

        return dirName;
    }
    public static Path getDirectory(String directoryName) throws IOException {
        Path jarPath = Paths.get("").toAbsolutePath();
        Path dirPathRelative = Paths.get(directoryName);

        Path dirPathAbsolute = jarPath.resolve(dirPathRelative);
        File dirFile = dirPathAbsolute.toFile();

        if(!dirFile.exists()){
            Files.createDirectories(dirPathAbsolute);
        }

        return dirPathRelative;
    }
    public static Path absolutePath(String directoryName){
        Path jarPath = Paths.get("").toAbsolutePath();
        Path dirPathRelative = Paths.get(directoryName);
        return jarPath.resolve(dirPathRelative);
    }

    public static void RUN(CARPProperties properties) throws IOException {
        String dir = createExperimentName(properties);
        System.out.println("output directory: " + dir);

        Path resultDir = getDirectory(dir);
        Path outSolution = resultDir.resolve("BSF_solution.csv");
        Path outJournal = resultDir.resolve("BSF_journal.txt");
        Path outConvergence = resultDir.resolve("convergence.csv");
        Path outConfig = resultDir.resolve("config.properties");

//        byte[] content = new FileInputStream(properties.configFileName).readAllBytes();
        File f = new File(properties.configFileName);
        byte[] content = new byte[(int)f.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        dis.readFully(content);


        FileOutputStream outputStream = new FileOutputStream(outConfig.toFile());
        outputStream.write(content);

        PrintWriter solutionWriter = new PrintWriter(new FileWriter(outSolution.toFile()));
        PrintWriter journalWriter = new PrintWriter(new FileWriter(outJournal.toFile()));
        PrintWriter convergenceWriter = new PrintWriter(new FileWriter(outConvergence.toFile()));
        convergenceWriter.println("generation,currentPopulationBestCost,currentPopulationBestVehicleCount,currentPopulationAverageCost,BSFcost,BSFvehicleCount");

        Config config = readGDB(properties.datasetGroup + "/" + properties.dataset + ".dat");
        Double[][] matrix = floydWarshall(config.nodes);
        Random random = new Random(properties.seed);  //SEED
        config.matrix = matrix;
        Route.matrix = matrix;
        Genetic.matrix = matrix;
        Genetic.config = config;
        Individual.config = config;
        Individual.random = random;
        Genetic.random = random;

        List<Edge> requiredEdges = config.edges.stream().filter(e -> e.required).collect(Collectors.toList());

        Genetic genetic = new Genetic(requiredEdges, journalWriter, convergenceWriter, properties);
        genetic.evolution(properties.popSize, properties.maxGen, properties.pCross, properties.pMutation, properties.M, properties.k, properties.N, properties.maxEpoch);

        if(true) return;

        System.out.println("best solution: " + genetic.BEST.evaluation.cost + " " + genetic.BEST.evaluation.vehicleCount + " found at generation: " + genetic.BEST.nbofGeneration);

        solutionWriter.println("bestSolutionGeneration,bestCost,bestVehicleCount");
        solutionWriter.println(genetic.BEST.nbofGeneration+","+genetic.BEST.evaluation.cost+","+genetic.BEST.evaluation.vehicleCount);
        solutionWriter.println(genetic.BEST.printRoutes());
        solutionWriter.flush();

        solutionWriter.close();
        journalWriter.close();
        convergenceWriter.close();
    }

    public static Config readGDB(String datasetPath) throws IOException {
//        FileReader fileReader = new FileReader(file);
//        BufferedReader bufferedReader = new BufferedReader(fileReader);

        InputStream is = Main.class.getClassLoader().getResourceAsStream(datasetPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

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

    public static List<Route> construct(List<Edge> priority, Config config, JournalPair journalPair,
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

            Candidate selectedCandidate = selectFromRoutes(routes, route, config.matrix, journalPair, journaling);

            if(selectedCandidate == null){
                //TODO vrat se zpet do depot, NEMUSIM RESIT
            }
            else{
                route.mergeRouteE(selectedCandidate);
            }
        }

        return new ArrayList<>(routes.stream().filter(r -> r.active).collect(Collectors.toList()));
    }

    public static Evaluation evaluatePriorityList(List<Edge> priority, Config config, JournalPair journalPair, boolean journaling){
        List<Route> routes = construct(priority, config, journalPair, journaling);
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
                    if(nodes.stream().filter(n_ -> n_.number == finalI).findFirst().get().nodes.stream().filter(n_ -> n_.hasRequired).collect(Collectors.toList()).size() > 1){
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
    public static Candidate selectFromRoutes(List<Route> routes, Route route, Double[][] matrix,
                                             JournalPair journalPair, boolean journaling){
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
            if((c = evaluateCandidate(route, outerLeft, route.tail.candidate.edge, left, r.tail.candidate.edge, matrix,
                    journalPair)) != null){
                candidates.add(c);
            }
            if((c = evaluateCandidate(route, outerLeft, route.tail.candidate.edge, right, r.head.candidate.edge, matrix,
                    journalPair)) != null){
                candidates.add(c);
            }
            if((c = evaluateCandidate(route, outerRight, route.head.candidate.edge, left, r.tail.candidate.edge, matrix,
                    journalPair)) != null){
                candidates.add(c);
            }
            if((c = evaluateCandidate(route, outerRight, route.head.candidate.edge, right, r.head.candidate.edge, matrix,
                    journalPair)) != null){
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
//                System.out.print(nodeCountsFromLeftNode.get(node) + " ");
//            }
//            System.out.println();
//
//            int countRight = 0;
//            int countDoubleRight = 0;
//            for(Node node : nodeCountsFromRightNode.keySet()){
//                countRight++;
//                if(nodeCountsFromRightNode.get(node) > 1){
//                    countDoubleRight++;
//                }
//            }
//            System.out.println("routes: " + routes.size() + " " + routes.stream().filter(r -> r.active).collect(Collectors.toList()).size());
//            System.out.println("left: " + countDoubleLeft + " " + countLeft + " right: " + countDoubleRight + " " + countRight);
//
//            System.out.println(candidates.stream().filter(c -> c.fromNode == outerLeft).collect(Collectors.toList()).size());
//            System.out.println(leftSum);
//            System.out.println("average: " + (double)leftSum/(++countLeft));
//


            Collections.sort(candidates, Comparator.comparingDouble(Candidate::getJournalEdgeEntry).thenComparingDouble(Candidate::getDistance));
        }

        //vyber prvniho nejlepsiho
        if(candidates.size() > 0){
            return candidates.get(0);
        }
        return null;
    }

    public static Candidate evaluateCandidate(Route route, Node fromNode, Edge fromEdge, Node toNode, Edge toEdge,
                                              Double[][] matrix, JournalPair journalPair){
        if(toEdge.component == route || route.capacityLeft < toEdge.component.capacityTaken){
            return null;
        }
        Candidate c = new Candidate(toEdge, toNode, fromNode, matrix[fromNode.number][toNode.number]);
        //TODO score evaluation bude pocitat s cetnosti
        c.score = c.distance;

        Map<Node, Map<Node, AnalysisNode>> journal = journalPair.journal;
        Map<Domain, Map<Domain, AnalysisNode>> journalEdge = journalPair.journalEdge;


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

        Domain domainFrom = new Domain(fromEdge, fromNode);
        if(journalEdge.containsKey(domainFrom)){
            Map<Domain, AnalysisNode> subJournalEdge = journalEdge.get(domainFrom);
            Domain domainTo = new Domain(toEdge, toNode);
            if(subJournalEdge.containsKey(domainTo)){
                AnalysisNode an = subJournalEdge.get(domainTo);
                c.journalEdgeEntry = an.sum/an.count;
            }
            else{
                c.journalEdgeEntry = Double.POSITIVE_INFINITY;
            }
        }
        else{
            c.journalEdgeEntry = Double.POSITIVE_INFINITY;
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
