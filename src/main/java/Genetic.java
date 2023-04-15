import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class Genetic {

    public static Random random = new Random(0);
    public static Comparator comparator = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return 0;
        }
    };


    public Config config;
    public Double[][] matrix2;
    public Entry[][] entries;
    List<Edge> requiredEdges;
    public int k = 3;

    public Genetic(Config config, Double[][] matrix2, Entry[][] entries, List<Edge> requiredEdges){
        this.config = config;
        this.matrix2 = matrix2;
        this.entries = entries;
        this.requiredEdges = requiredEdges;
    }

    public void evolution(int popSize, int maxGen, double probCross, double probMutation){
        List<Individual> population = new ArrayList<>();

        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
//            List<Edge> newPriorityList = new ArrayList<>(List.copyOf(this.requiredEdges));
            List<Edge> newPriorityList = Main.deepCopy(requiredEdges);
            Collections.shuffle(newPriorityList, new Random(random.nextInt()));
            Individual individual = new Individual(newPriorityList);
            Evaluation evaluation = Main.evaluatePriorityList(individual.priorityList, config);
            individual.evaluation = evaluation;
            population.add(individual);
        }


        System.out.println("\nstart\n");
//        System.out.println("Population test");
//        System.out.println(population.get(0));
//        population.get(0).mutate();
//        System.out.println(population.get(0));
//
//        System.out.println(population.get(1));
//        System.out.println(population.get(0).crossWith(population.get(1)));

//        if(true) return;

        for (int i = 0; i < popSize; i++) {
            Individual individual = population.get(i);
            System.out.println(individual.evaluation);
        }

        for (int i = 0; i < maxGen; i++) {
            if(i>10 && (i%10 == 0)){
                analyzePopulation(population, journal);
            }

            List<Individual> interPop = new ArrayList<>();

            for (int j = 0; j < popSize; j++) {
                Individual individual = population.get(j);
                individual.parent = false;
            }

            int interPopSize = 0;
            while (interPopSize < popSize) {
                Individual parent1 = tournamentSelection(population, config.vehicles);
                Individual parent2 = tournamentSelection(population, config.vehicles);

                Individual child1 = null;
                Individual child2 = null;

                if(random.nextDouble() < probCross){
                    child1 = new Individual(parent1.crossWith(parent2));
                    child2 = new Individual(parent2.crossWith(parent1));
                    if(random.nextDouble() < probMutation){
                        child1.mutate();
                        child2.mutate();
                    }
                }
                else{
                    child1 = new Individual(parent1.priorityList);
                    child2 = new Individual(parent2.priorityList);

                    child1.mutate();
                    child2.mutate();
                }

                Evaluation evaluation1 = Main.evaluatePriorityList(child1.priorityList, config);
                child1.evaluation = evaluation1;
                for(Route r : child1.evaluation.routes){
//                    r.twoOptWrap();
//                    r.singleInsertWrap();
                }


                Evaluation evaluation2 = Main.evaluatePriorityList(child2.priorityList, config);
                child2.evaluation = evaluation2;
                for (Route r : child2.evaluation.routes){
//                    r.twoOptWrap();
//                    r.singleInsertWrap();
                }

                interPop.add(child1);
                interPop.add(child2);
                interPopSize += 2;
            }

            for (int j = 0; j < popSize; j++) {
                Individual individual = population.get(j);
                individual.parent = true;
            }

            int originalSize = population.size();
            population.addAll(interPop);
            comparison1(population, config.vehicles);
            population.subList(originalSize, population.size()).clear();


//            System.out.println(population.size());

            if (originalSize != population.size()) throw new RuntimeException();

        }

        System.out.println();
        for (int i = 0; i < popSize; i++) {
            Individual individual = population.get(i);
            System.out.println(individual.evaluation);
            for(Route r : individual.evaluation.routes){
//                r.singleInsertWrap();
//                r.twoOptWrap();
            }

        }

//        Individual in = population.get(0);
//        System.out.println(in);
//
//        Individual in1 = population.get(1);
//        System.out.println(in1);
//
//        System.out.println(in.evaluation.routes.get(0).tail);
//        System.out.println(in1.evaluation.routes.get(0).tail);
//
//        System.out.println(in.evaluation.routes.get(0).tail.candidate.edge.leftNode == in1.evaluation.routes.get(0).tail.candidate.edge.leftNode);
//        System.out.println(in.evaluation.routes.get(0).tail.candidate.edge == in1.evaluation.routes.get(0).tail.candidate.edge);
//        System.out.println(in.evaluation.routes.get(0).tail.candidate.edge.equals(in1.evaluation.routes.get(0).tail.candidate.edge));



//        List<Route> _routes = in.evaluation.routes;
//        Collections.sort(_routes, new Comparator<Route>() {
//            @Override
//            public int compare(Route o1, Route o2) {
//                return Integer.compare(o1.length(), o2.length());
//            }
//        });
//        for(Route r: _routes){
//            System.out.println(r.length() + " cost: " + Main.evaluateRoute(r, config.matrix) + " taken: " + r.capacityTaken + " left: " + r.capacityLeft);
//        }

//        for (int i = 0; i < 100; i++) {
////            pathScanningWrap(in);
//        }
//        for(Route r : in.evaluation.routes){
////                r.singleInsertWrap();
////                r.twoOptWrap();
//        }
//        for(Route r : in.evaluation.routes){
//            System.out.println(r);
//            System.out.println(Main.evaluateRoute(r, config.matrix));
//            System.out.println(r.length());
////            r.twoOpt();
//            r.twoOptWrap();
//        }
    }

    private void comparison1(List<Individual> population, int maxVehicles) {
        Collections.sort(population, new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
//                return Double.compare(o1.fitness(), o2.fitness());
                if(o1.evaluation.vehicleCount <= maxVehicles && o2.evaluation.vehicleCount <= maxVehicles){
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
                else if(o1.evaluation.vehicleCount <= maxVehicles){
                    return -1;
                }
                else if(o2.evaluation.vehicleCount <= maxVehicles){
                    return 1;
                }
                else{
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
            }
        });
    }

    public Individual tournamentSelection(List<Individual> population, int maxVehicles){
        List<Individual> subset = new ArrayList<>();
        for (int i = 0; i < this.k; i++) {
            subset.add(population.get(random.nextInt(population.size())));
        }

        comparison1(subset, maxVehicles);

        return subset.get(0);
    }

    public void pathScanningWrap(Individual individual){
        List<Route> routes = individual.evaluation.routes;
        Collections.shuffle(routes, new Random());
        List<Route> subRoutes = routes.stream().limit(3).collect(Collectors.toList());
        double pre = Main.evaluateRoutes(subRoutes, config);
        Evaluation evaluation = pathScanning(subRoutes);
        if(evaluation.cost < pre){
            System.out.println("improvement pathscanning");
            System.out.println(pre);
            System.out.println(evaluation.cost);
        }
//        pathScanning(routes.stream().limit(3).collect(Collectors.toList()));
    }

    public Evaluation pathScanning(List<Route> routes){
        List<Edge> allEdges = new ArrayList<>();
        for (Route r : routes){
            Element element = r.tail;
            while(element != null){
                allEdges.add(new Edge(element.candidate.edge));
                element = element.next;
            }
        }
        Collections.shuffle(allEdges);
//        System.out.println("difference");
//        System.out.println(routes.size());
//        System.out.println(Main.evaluateRoutes(routes, config));
//        System.out.println(Main.evaluatePriorityList(allEdges, config));
        return Main.evaluatePriorityList(allEdges, config);
    }

    public void analyzePopulation(List<Individual> population, Map<Node, Map<Node, AnalysisNode>> journal){
//        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>();

        int sizeWorthy = population.size()/4;
        List<Individual> populationWorthy = new ArrayList<>(population.stream().limit(sizeWorthy).toList());
        for (Individual individual : populationWorthy) {
            analyzeIndividual(individual, journal);
        }
    }

    public void analyzeIndividual(Individual individual, Map<Node, Map<Node, AnalysisNode>> journal){
        List<Route> routes = individual.evaluation.routes;
        for (Route route : routes) {
            Element element = route.tail;
            while(element != null){
                analyzeElement(element, journal);
                element = element.next;
            }
        }
    }

    public void analyzeElement(Element element, Map<Node, Map<Node, AnalysisNode>> journal){
        if(element.previous != null){
            Map<Node, AnalysisNode> subJournal;
            if(journal.containsKey(element.previousLink)){
                subJournal = journal.get(element.previousLink);
            }
            else{
                subJournal = new HashMap<>();
                journal.put(element.previousLink, subJournal);
            }
            if(subJournal.containsKey(element.previous.nextLink)){
                AnalysisNode analysisNode = subJournal.get(element.previous.nextLink);
                analysisNode.count += 1;
                analysisNode.sum += element.previousDistance;
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(element.previousDistance);
                subJournal.put(element.previous.nextLink, analysisNode);
            }
        }
        if(element.next != null){
            Map<Node, AnalysisNode> subJournal;
            if(journal.containsKey(element.nextLink)){
                subJournal = journal.get(element.nextLink);
            }
            else{
                subJournal = new HashMap<>();
                journal.put(element.nextLink, subJournal);
            }
            if(subJournal.containsKey(element.next.previousLink)){
                AnalysisNode analysisNode = subJournal.get(element.next.previousLink);
                analysisNode.count += 1;
                analysisNode.sum += element.nextDistance;
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(element.nextDistance);
                subJournal.put(element.next.previousLink, analysisNode);
            }
        }
    }

}
