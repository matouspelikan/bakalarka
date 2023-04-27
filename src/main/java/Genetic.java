import java.util.*;
import java.util.stream.Collectors;

public class Genetic {
    public static Random random = new Random(0);
    public static Comparator<Individual> comparator;

    public Config config;
    public Double[][] matrix2;
    public Entry[][] entries;
    List<Edge> requiredEdges;
    public int kTournament = 3;
    public int maxDuplicates = 2;

    public Genetic(Config config, Double[][] matrix2, Entry[][] entries, List<Edge> requiredEdges){
        this.config = config;
        this.matrix2 = matrix2;
        this.entries = entries;
        this.requiredEdges = requiredEdges;

        comparator = new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
//                return Double.compare(o1.fitness(), o2.fitness());
                if(o1.evaluation.vehicleCount <= config.vehicles && o2.evaluation.vehicleCount <= config.vehicles){
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
                else if(o1.evaluation.vehicleCount <= config.vehicles){
                    return -1;
                }
                else if(o2.evaluation.vehicleCount <= config.vehicles){
                    return 1;
                }
                else{
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
            }
        };
    }

    public Individual createIndividual(Map<Node, Map<Node, AnalysisNode>> journal){
        List<Edge> newPriorityList = Main.deepCopy(requiredEdges);
        Collections.shuffle(newPriorityList, new Random(random.nextInt()));
        Individual individual = new Individual(newPriorityList);
        Evaluation evaluation = Main.evaluatePriorityList(individual.priorityList, config, journal, false);
        individual.evaluation = evaluation;
        return individual;
    }

    public List<Individual> createInitialPopulation(int popSize, Map<Node, Map<Node, AnalysisNode>> journal){
        Map<Evaluation, Integer> counts = new HashMap<>();

        List<Individual> population = new ArrayList<>();
        int size = 0;
        int iteration = 0;
        while(size < popSize){
            iteration++;
            if(iteration > popSize*1000){
                throw new RuntimeException();
            }
            Individual newIndividual = createIndividual(journal);
            if(counts.containsKey(newIndividual.evaluation)){
                int count = counts.get(newIndividual.evaluation);
                if(count < maxDuplicates){
                    population.add(newIndividual);
                    size++;
                    counts.put(newIndividual.evaluation, count + 1);
                }
                else{
                    //too many duplicates
                }
            }
            else{
                population.add(newIndividual);
                size++;
                counts.put(newIndividual.evaluation, 1);
            }
        }
        return population;
    }

    public void evolution(int popSize, int maxGen, double probCross, double probMutation, int M, int k, double N){
        List<Individual> population = new ArrayList<>();

        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>();

//        for (int i = 0; i < popSize; i++) {
////            List<Edge> newPriorityList = new ArrayList<>(List.copyOf(this.requiredEdges));
//
//            population.add(createIndividual(journal));
//        }
        population = createInitialPopulation(popSize, journal);


        System.out.println("\nstart\n");

        comparison1(population, config.vehicles);
        printPopulation(population);

        boolean journaling = false;

        for (int i = 0; i < maxGen; i++) {
            journaling = false;
            if(i == M){
                System.out.println("first journal");
                journaling = true;
                analyzePopulation(population, journal, N);
            }

            if(i > M && (i % k == 0)){
                journaling = true;
                System.out.println("journaling");
                journal = new HashMap<>();
                analyzePopulation(population, journal, N);
            }
            journaling = false;
            for (int j = 0; j < population.size(); j++) {
                for (int l = 0; l < 10; l++) {
                    for (int m = 1; m < config.vehicles; m++) {
//                        pathScanningWrap(population.get(j), journal, m, true);
                    }
                }
            }

            System.out.print(i + ": ");
            printPopulation(population);

            double sum = 0;
            int count = 0;
            for (int j = 0; j < population.size(); j++) {
                sum += population.get(j).evaluation.cost;
                count++;
            }
            System.out.println("population average: " + sum/count + " best: " + population.get(0).evaluation.cost);

            List<Individual> interPop = new ArrayList<>();

            for (int j = 0; j < popSize; j++) {
                Individual individual = population.get(j);
                individual.parent = false;
            }

            int interPopSize = 0;
            int iteration = 0;
            while (interPopSize < popSize) {
                iteration++;
//                if(iteration > popSize*4) break;

                Individual parent1 = tournamentSelection(population, config.vehicles);
                Individual parent2 = tournamentSelection(population, config.vehicles);

                Individual child1 = null;
                Individual child2 = null;

                if(random.nextDouble() < probCross){
                    child1 = new Individual(parent1.crossWith2(parent2));
                    child2 = new Individual(parent2.crossWith2(parent1));
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

                Evaluation evaluation1 = Main.evaluatePriorityList(child1.priorityList, config, journal, journaling);
                child1.evaluation = evaluation1;
                for(Route r : child1.evaluation.routes){
//                    System.out.println("evall " + evaluation1);
                    r.twoOptWrap();
                    r.singleInsertWrap();
//                    r.singleReverseWrap();

                }

                Evaluation evaluation2 = Main.evaluatePriorityList(child2.priorityList, config, journal, journaling);
                child2.evaluation = evaluation2;
                for (Route r : child2.evaluation.routes){
//                    System.out.println("evall " + evaluation2);
                    r.twoOptWrap();
                    r.singleInsertWrap();
//                    r.singleReverseWrap();

                }

                for (int j = 0; j < 10; j++) {
                    for (int l = 2; l < 4; l++) {
//                        pathScanningWrap(child1, journal, l, false);
                    }
                }
                for (int j = 0; j < 10; j++) {
                    for (int l = 2; l < 4; l++) {
//                        pathScanningWrap(child2, journal, l, false);
                    }
                }
//                pathScanningWrap(child1, journal, 3, false);
//                pathScanningWrap(child2, journal, 3, false);

                if(true || comparator.compare(child1, parent1) < 0){
                    interPop.add(child1);
                    interPopSize++;
                }
                if(true || comparator.compare(child2, parent2) < 0){
                    interPop.add(child2);
                    interPopSize++;
                }
            }

            for (int j = 0; j < popSize; j++) {
                Individual individual = population.get(j);
                individual.parent = true;
            }

            int originalSize = population.size();
            population.addAll(interPop);
            comparison1(population, config.vehicles);
            List<Individual> nonDuplicatedPopulation = deleteDuplicates(population);
//            population.subList(originalSize, population.size()).clear();
            nonDuplicatedPopulation.subList(originalSize, nonDuplicatedPopulation.size()).clear();
            population = nonDuplicatedPopulation;

//            System.out.println(population.size());

            if (originalSize != population.size()) throw new RuntimeException();

        }

        System.out.println();
        printPopulation(population);


//        for (int i = 0; i < popSize; i++) {
//            Individual individual = population.get(i);
//            for(Route r : individual.evaluation.routes){
//                r.singleInsertWrap();
//                r.twoOptWrap();
//            }
//
//        }
        System.out.println(journal);

//        Individual in = population.get(0);
//        System.out.println(in);
//        System.out.println("all routes");
//        for(Route route: in.evaluation.routes) {
//            System.out.println(route);
//        }
//        System.out.println("\n\n");
//
//        for(Route route: in.evaluation.routes){
//            System.out.println("new route");
//            System.out.println(route);
//            Element element = route.tail;
//            while(element != null){
//                System.out.println("\t\tnew element");
//                System.out.println(element.previousLink + " " + element.previousDistance);
//                System.out.println(element.candidate.edge);
//                System.out.println(element.nextLink + " " + element.nextDistance);
//                element = element.next;
//            }
//        }
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

//        for (Individual inn: population){
//            System.out.println(inn);
//            for (int i = 0; i < 100; i++) {
//                pathScanningWrap(inn, journal);
//            }
//            for(Route r : inn.evaluation.routes){
//                r.singleInsertWrap();
//                r.twoOptWrap();
//            }
//        }

//        for(Route r : in.evaluation.routes){
//            System.out.println(r);
//            System.out.println(Main.evaluateRoute(r, config.matrix));
//            System.out.println(r.length());
////            r.twoOpt();
//            r.twoOptWrap();
//        }
    }

    public List<Individual> deleteDuplicates(List<Individual> population){
        List<Individual> newPopulation = new ArrayList<>(); //without duplicates
        Map<Evaluation, Integer> counts = new HashMap<>();
        for(Individual individual : population){
            if(counts.containsKey(individual.evaluation)){
                int count = counts.get(individual.evaluation);
                if(count < maxDuplicates){
                    newPopulation.add(individual);
                    counts.put(individual.evaluation, count+1);
                }
                else{
                    //too many of same evaluation
                }
            }
            else{
                newPopulation.add(individual);
                counts.put(individual.evaluation, 1);
            }
        }
        return newPopulation;
    }

    private void comparison1(List<Individual> population, int maxVehicles) {
        Collections.sort(population, comparator);
    }

    public Individual tournamentSelection(List<Individual> population, int maxVehicles){
//        if(true)
//            return population.get(random.nextInt(population.size()));
        List<Individual> subset = new ArrayList<>();
        for (int i = 0; i < this.kTournament; i++) {
            subset.add(population.get(random.nextInt(population.size())));
        }

        comparison1(subset, maxVehicles);

        return subset.get(0);
    }

    public void printPopulation(List<Individual> population){
        for(Individual individual : population){
            System.out.print(individual.evaluation);
            System.out.print(" ");
        }
        System.out.println();
    }

    public void pathScanningWrap(Individual individual,Map<Node, Map<Node, AnalysisNode>> journal, int limit, boolean elite){
        List<Route> routes = individual.evaluation.routes;
        Collections.shuffle(routes, new Random(0));
        List<Route> subRoutes = routes.stream().limit(limit).collect(Collectors.toList());
        double pre = Main.evaluateRoutes(subRoutes, config);
        Evaluation evaluation = pathScanning(subRoutes, journal);
        if(evaluation.cost < pre && evaluation.vehicleCount <= individual.evaluation.vehicleCount){
//            if(elite)
//                System.out.println("improvement pathscanning");
//            System.out.println(pre);
//            System.out.println(evaluation.cost);
            int rpre = Main.evaluateRoutes(routes, config);
            for(Route r : subRoutes){
                routes.remove(r);
            }
            routes.addAll(evaluation.routes);
            int rpost = Main.evaluateRoutes(routes, config);

            individual.evaluation.cost = rpost;
            individual.evaluation.vehicleCount = routes.size();

            if(pre - evaluation.cost != rpre - rpost){
                throw new RuntimeException();
            }
        }
//        pathScanning(routes.stream().limit(3).collect(Collectors.toList()));
    }

    public Evaluation pathScanning(List<Route> routes, Map<Node, Map<Node, AnalysisNode>> journal){
        List<Edge> allEdges = new ArrayList<>();
        for (Route r : routes){
            Element element = r.tail;
            while(element != null){
                allEdges.add(new Edge(element.candidate.edge));
                element = element.next;
            }
        }
        Collections.shuffle(allEdges, new Random(0));
//        System.out.println("difference");
//        System.out.println(routes.size());
//        System.out.println(Main.evaluateRoutes(routes, config));
//        System.out.println(Main.evaluatePriorityList(allEdges, config));
        return Main.evaluatePriorityList(allEdges, config, journal, false);
    }

    public void analyzePopulation(List<Individual> population, Map<Node, Map<Node, AnalysisNode>> journal, double N){
//        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>();
        int sizeWorthy = (int)(population.size()*N);
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
                analyzeElement(element, individual.evaluation, journal);
                element = element.next;
            }
        }
    }

    public void analyzeElement(Element element, Evaluation evaluation, Map<Node, Map<Node, AnalysisNode>> journal){
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
                analysisNode.sum += evaluation.cost * evaluation.vehicleCount;
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
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
                analysisNode.sum += evaluation.cost * evaluation.vehicleCount;
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
                subJournal.put(element.next.previousLink, analysisNode);
            }
        }
    }

}
