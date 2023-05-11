import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Genetic {
    public static Random random;
    public static Comparator<Individual> comparator;

    public static Config config;
    public static Double[][] matrix;  //matice vzdáleností
    List<Edge> requiredEdges; //seznam pozadovanych hran, jeho permutovanim se vytvareji nahodne chromosomy

    public int maxDuplicates; //maximalni pocet jedincu v populace se stejnym costem

    public PrintWriter journalWriter;
    public PrintWriter convergenceWriter;
    public ObjectOutputStream journalObjectStream;
    public ObjectOutputStream bestObjectStream;

    public CARPProperties properties;

    public Genetic(List<Edge> requiredEdges, PrintWriter journalWriter, PrintWriter convergenceWriter,
                   ObjectOutputStream journalObjectStream, ObjectOutputStream bestObjectStream,
                   CARPProperties properties){
        this.bestObjectStream = bestObjectStream;
        this.journalObjectStream = journalObjectStream;
        this.journalWriter = journalWriter;
        this.convergenceWriter = convergenceWriter;
        this.requiredEdges = requiredEdges;

        this.properties = properties;

        this.maxDuplicates = properties.duplicates;

        Config finalConfig = config;
        //pravidlo pro razeni populace
        comparator = new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
                if(o1.evaluation.vehicleCount <= finalConfig.vehicles && o2.evaluation.vehicleCount <= finalConfig.vehicles){
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
                else if(o1.evaluation.vehicleCount <= finalConfig.vehicles){
                    return -1;
                }
                else if(o2.evaluation.vehicleCount <= finalConfig.vehicles){
                    return 1;
                }
                else{
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
            }
        };
    }

    public Individual BEST = new Individual();

    public void evolution(int popSize, int maxGen, double probCross, double probMutation, int M, int k, int N, int maxEpoch) throws IOException {
        System.out.println(properties);

        JournalType journalType = properties.journalType;

//        maxDuplicates = popSize;

//        Map<Domain, Map<Domain, AnalysisNode>> journalEdge = new HashMap<>();
//
//        Edge e = config.edges.get(0);
//        System.out.println(e);
//        Node n = config.nodes.get(0);
//        System.out.println(n);
//
//        Domain d = new Domain(e, n);
//        Domain d2 = new Domain(new Edge(e), new Node(n));
//
//        System.out.println(d.edge.equals(d2.edge));
//        System.out.println(d.node.equals(d2.node));
//
//        System.out.println(d == d2);
//        System.out.println(d.equals(d2));
//
//        Map<Domain, AnalysisNode> subJournalEdge = new HashMap<>();
//        subJournalEdge.put(d, new AnalysisNode(1));
//        System.out.println(subJournalEdge.containsKey(d2));
//        System.out.println(subJournalEdge.get(d2));
//
//        System.out.println(Objects.hash(n));
//        System.out.println(n.hashCode());
//
//
//        if(true) return;


        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>(); //struktura, ve ktere se uchovavaji vysledky analyzy
        boolean journaling = false; //priznak, podle ktereho se prepina vybirani sousedu podle vzdalenosti/analyzy
        Map<Domain, Map<Domain, AnalysisNode>> journalEdge = new HashMap<>();
        JournalPair journalPair = new JournalPair(journal, journalEdge);

        List<Individual> population;
        population = createInitialPopulation(popSize, journalPair, journalType);
        sortPopulation(population);
//        printPopulation(population);

        Map<Node, Map<Node, AnalysisNode>> bestSoFarJournal = null;
        Map<Domain, Map<Domain, AnalysisNode>> bestSoFarJournalEdge = null;
        Individual bestSoFarIndividual = new Individual(); //fitness nastaveno na infinity
        JournalPair bestSoFarJournalPair = analyzePopulation(population, N, -1, true, journalType);

        int nbOfJournaling = 0;
        int nbOfEpoch = 0;

        ProgressBarBuilder pbb = new ProgressBarBuilder();
        pbb.setStyle(ProgressBarStyle.ASCII);
        pbb.setInitialMax(maxGen);
        pbb.setTaskName(properties.dataset);

        ProgressBar pb = pbb.build();

        journalWriter.println("best neighbors before analysis = distances");
        for(Node node : config.nodes.stream().filter(_n -> _n.hasRequired).sorted(Comparator.comparingInt(Node::getNumber)).collect(Collectors.toList())) {
            printBestNeighbors(node, null);
        }

        for (int i = 0; i < maxGen; i++) {
            pb.setExtraMessage("| BSF cost: " + BEST.evaluation.cost);
            journalWriter.flush();
            convergenceWriter.flush();
//            pb.step();

            System.out.println(i);
            serializeBest(population, i);
            System.out.println(population);

            boolean reevaluatePopRestart = false;

            if((i == M) ||                              // v M-te generaci se poprve spusti analyza
                    (i > M && (nbOfJournaling % k == 0)))    // kazdou k-tou iteraci se analyza prepocita
            {
                if(comparator.compare(population.get(0), bestSoFarIndividual) < 0){ //prvni jedinec v populaci je lepsi nez bestSoFar
                    bestSoFarIndividual = new Individual(population.get(0), i);
                    journalPair = analyzePopulation(population, N, i, false, journalType);
                    bestSoFarJournalPair = journalPair;
//                    journal = analyzePopulation(population, N, i, false);
//                    bestSoFarJournal = journal;
                    nbOfEpoch = 0; //dokud se nejlepsi jedinec zlepsuje, zustava epocha na zacatku
                }
                else if(nbOfEpoch < maxEpoch){
                    nbOfEpoch++;
                    journalPair = analyzePopulation(population, N, i, false, journalType);
//                    journal = analyzePopulation(population, N, i, false);
                }
                else{
                    nbOfEpoch = 0;
//                    journal = bestSoFarJournal;
                    journalPair = bestSoFarJournalPair;
                    reevaluatePopRestart = true; //pouze pro ucely population restart
                }

                journaling = true;
                nbOfJournaling = 0;
//                reevaluatePopRestart = true;

                //pokud je journalovací verze, prepocita se vzdy, jinak pouze kdyz dojdou epochy
                if(journalType == JournalType.EDGE || journalType == JournalType.NODE){
                    for (Individual ind: population){
                        ind.perturb(journalPair, true, journalType);
                    }
//                    sortPopulation(population);
                }
                else if(reevaluatePopRestart){ //pro populationRestart variantu chceme zanechat prvnich N individuí a zbytek permutovat
//                    System.out.println("perturbing basic");
                    for (int j = 0; j < population.size(); j++) {
                        if(j < N) continue; //prvnich N jedincu nechavame beze zmeny
                        population.get(j).perturb(journalPair,true, journalType);
                    }
//                    sortPopulation(population);
                }
            }
            nbOfJournaling++;



            //new generation
            List<Individual> interPop = new ArrayList<>();
            int interPopSize = 0;
            while (interPopSize < popSize) {
                Individual parent1 = tournamentSelection(population, properties.tournament1);
                Individual parent2 = tournamentSelection(population, properties.tournament2);

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

                child1.evaluate(journalPair, journaling, journalType);
                child2.evaluate(journalPair, journaling, journalType);

                child1.localOptimisation(journalPair, journalType);
                child2.localOptimisation(journalPair, journalType);

                interPop.add(child1);
                interPopSize++;

                interPop.add(child2);
                interPopSize++;
            }

            int originalSize = population.size();
            population.addAll(interPop);
            sortPopulation(population);

//            System.out.print(population.size() + "  i: " + i + " ");
//            for (int j = 0; j < population.size(); j++) {
//                System.out.print(population.get(j).evaluation + " ");
//            }
//            System.out.println();


            List<Individual> nonDuplicatedPopulation = deleteDuplicates(population);
//            population.subList(originalSize, population.size()).clear();
            int nonDSize = nonDuplicatedPopulation.size();
            if(nonDSize<originalSize){
                for (int j = 0; j < population.size() && nonDSize < originalSize; j++) {
                    Individual individual = population.get(j);
                    if(!nonDuplicatedPopulation.contains(individual)){
                        nonDuplicatedPopulation.add(individual);
                        nonDSize++;
                    }
                }
            }
            else{
                nonDuplicatedPopulation.subList(originalSize, nonDuplicatedPopulation.size()).clear();
            }
            population = nonDuplicatedPopulation;

            if (originalSize != population.size()) throw new RuntimeException();


            if(comparator.compare(population.get(0), BEST) < 0){ //prvni jedinec v populaci je lepsi nez bestSoFar
                BEST = new Individual(population.get(0), i);
                if(i < M) bestSoFarIndividual = BEST;
            }

            printPopulation(population, i, BEST);

        }


        System.out.println("end");
        System.out.println(population);


        pb.close();

    }

    public Individual createIndividual(JournalPair journalPair, JournalType journalType){
        List<Edge> newPriorityList = Main.deepCopy(requiredEdges);
        Collections.shuffle(newPriorityList, new Random(random.nextInt()));
        Individual individual = new Individual(newPriorityList);
        individual.evaluate(journalPair, false, journalType);
        return individual;
    }

    public List<Individual> createInitialPopulation(int popSize, JournalPair journalPair, JournalType journalType){
        Set<Integer> hashes = new HashSet<>();

        Map<Evaluation, Integer> counts = new HashMap<>();

        List<Individual> population = new ArrayList<>();
        int size = 0;
        int iteration = 0;
        while(size < popSize){
            iteration++;
            if(iteration > popSize*1000){ //assurance iteration overflow
                throw new RuntimeException();
            }
            Individual newIndividual = createIndividual(journalPair, journalType);
            int newIndividualHash = newIndividual.hashCustom();

            if(!hashes.contains(newIndividualHash)){
                population.add(newIndividual);
                size++;
                hashes.add(newIndividualHash);
            }

//            if(true) continue;
//            //obsolete way of checking for duplicates
//
//            if(counts.containsKey(newIndividual.evaluation)){
//                int count = counts.get(newIndividual.evaluation);
//                if(count < maxDuplicates){
//                    population.add(newIndividual);
//                    size++;
//                    counts.put(newIndividual.evaluation, count + 1);
//                }
//                else{
//                    //too many duplicates
//                }
//            }
//            else{
//                population.add(newIndividual);
//                size++;
//                counts.put(newIndividual.evaluation, 1);
//            }
        }
        return population;
    }

    public List<Individual> deleteDuplicates(List<Individual> population){
        Set<Integer> hashes = new HashSet<>();

        List<Individual> newPopulation = new ArrayList<>(); //without duplicates
        Map<Evaluation, Integer> counts = new HashMap<>();
        Map<Integer, Integer> hashCounts = new HashMap<>();
        int count;
        for(Individual individual : population){
            int hash = individual.hashCustom();

            if(hashCounts.containsKey(hash)){
                count = hashCounts.get(hash);
                if(count < maxDuplicates){
                    hashCounts.put(hash, count+1);
                    newPopulation.add(individual);
                }
                else{
                    //too many duplicates
                }
            }
            else{
                hashCounts.put(hash, 1);
                newPopulation.add(individual);
            }

            //obsolete latest
//            if(!hashes.contains(hash) ){
//                hashes.add(hash);
//                newPopulation.add(individual);
//            }

            //obsolete way of checking for duplicates
//            if(counts.containsKey(individual.evaluation)){
//                int count = counts.get(individual.evaluation);
//                if(count < maxDuplicates){
//                    newPopulation.add(individual);
//                    counts.put(individual.evaluation, count+1);
//                }
//                else{
//                    //too many of same evaluation
//                }
//            }
//            else{
//                newPopulation.add(individual);
//                counts.put(individual.evaluation, 1);
//            }
        }
        return newPopulation;
    }

    public void sortPopulation(List<Individual> population) {
        Collections.sort(population, comparator);
    }

    public Individual tournamentSelection(List<Individual> population, int limit){
        List<Individual> subset = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            subset.add(population.get(random.nextInt(population.size())));
        }

        sortPopulation(subset);

        return subset.get(0);
    }

    public void printPopulation(List<Individual> population, int generation, Individual BEST){
        double sum = 0;
        int count = 0;
        for (int j = 0; j < population.size(); j++) {
            sum += population.get(j).evaluation.cost;
            count++;
        }
//        convergenceWriter.println("population average: " + sum/count + " best: " + population.get(0).evaluation.cost);
        Individual best = population.get(0);
        convergenceWriter.println(generation + "," + best.evaluation.cost + "," + best.evaluation.vehicleCount + ","
                + sum/count + "," + BEST.evaluation.cost + "," + BEST.evaluation.vehicleCount);
        convergenceWriter.flush();
//        population.get(0).printRoutes();
    }

    public JournalPair analyzePopulation(List<Individual> population, int N, int generation, boolean distances,
                                         JournalType journalType) throws IOException {
        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>();
        Map<Domain, Map<Domain, AnalysisNode>> journalEdge = new HashMap<>();

        int sizeWorthy = N;
        List<Individual> populationWorthy = new ArrayList<>(population.stream().limit(sizeWorthy).collect(Collectors.toList()));
        for (Individual individual : populationWorthy) {
            analyzeIndividual(individual, journal, distances, journalEdge);
        }

        journalWriter.println("new analysis generation: " + generation);

        JournalPair journalPair = new JournalPair(journal, journalEdge);

        for(Node node : config.nodes.stream().filter(n -> n.hasRequired).sorted(Comparator.comparingInt(Node::getNumber)).collect(Collectors.toList())){
            printBestNeighbors(node, journal);
        }
//        System.out.println(generation);
        printBestNeighbors2(journalPair, journalType, generation);

        return journalPair;
    }

    public void printBestNeighbors(Node node, Map<Node, Map<Node, AnalysisNode>> journal){
        int limit = 5;
        if(journal == null){
            List<Node> otherNodes = new ArrayList<>(config.nodes.stream().filter(n -> n.hasRequired).collect(Collectors.toList()));
            otherNodes.sort(new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return Double.compare(matrix[node.number][o1.number], matrix[node.number][o2.number]);
                }
            });
            List<Node> toPrint = otherNodes.stream().limit(limit).collect(Collectors.toList());
            journalWriter.println(node.number + ": " + toPrint + " " + otherNodes.size());

            return;
        }

        Map<Node, AnalysisNode> subJournal = journal.get(node);
        if(subJournal == null){
            journalWriter.println(node.number + ": ");
            return;
        }
        List<Foo> foos = new ArrayList<>();
        for(Node n : subJournal.keySet()){
            foos.add(new Foo(n, subJournal.get(n)));
        }
        foos.sort(Comparator.comparingDouble(Foo::getAverage));
        List<Foo> toPrint = foos.stream().limit(5).collect(Collectors.toList());
        journalWriter.println(node.number + ": " + toPrint + " " + foos.size() );
    }

    public void printBestNeighbors2(JournalPair journalPair, JournalType journalType, int generation) throws IOException {
//        for (Node node : config.nodes.stream().filter(n -> n.hasRequired).sorted(Comparator.comparingInt(Node::getNumber)).collect(Collectors.toList())){
//            journalWriter.print("node: " + node.number);
//            Map<Node, Integer> subJournal = journal.get(node);
//        }

        if(journalType == JournalType.EDGE){
            journalObjectStream.writeObject(new SerialJournal(generation, journalPair));
        }
        else if(journalType == JournalType.NODE){
            journalObjectStream.writeObject(new SerialJournal(generation, journalPair));
        }
        else{
            journalObjectStream.writeObject(new SerialJournal(generation, journalPair));
        }
        journalObjectStream.flush();
    }

    public void serializeBest(List<Individual> population, int generation) throws IOException {
        System.out.println(population);
        bestObjectStream.writeObject(new SerialIndividual(generation,
                new ArrayList<>(population)));

//        for (int i = 0; i < properties.N; i++) {
//            Individual individual = population.get(i);
////            bestObjectStream.writeObject(individual);
//            bestObjectStream.writeObject(individual);
//        }

        bestObjectStream.flush();
    }

    class Foo{
        public Node node;
        public double average;
        public Foo(Node node, AnalysisNode analysisNode){
            this.node = node;
            this.average = analysisNode.sum/analysisNode.count;
        }
        public double getAverage(){
            return average;
        }

        @Override
        public String toString() {
            return node.number + "";
        }
    }

    public void analyzeIndividual(Individual individual, Map<Node, Map<Node, AnalysisNode>> journal,
                                  boolean distances, Map<Domain, Map<Domain, AnalysisNode>> journalEdge){
        List<Route> routes = individual.evaluation.routes;
        for (Route route : routes) {
            Element element = route.tail;
            while(element != null){
                analyzeElement(element, individual.evaluation, journal, distances);
                analyzeElement2(element, individual.evaluation, journalEdge, distances);
                element = element.next;
            }
        }
    }

    public void analyzeElement(Element element, Evaluation evaluation, Map<Node, Map<Node, AnalysisNode>> journal, boolean distances){
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
                if(distances){
                    analysisNode.sum += matrix[element.previousLink.number][element.previous.nextLink.number];
                }
                else{
                    analysisNode.sum += evaluation.cost;
                }
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
                if(distances){
                    analysisNode.sum += matrix[element.nextLink.number][element.next.previousLink.number];
                }
                else{
                    analysisNode.sum += evaluation.cost;
                }
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
                subJournal.put(element.next.previousLink, analysisNode);
            }
        }
    }

    public void analyzeElement2(Element element, Evaluation evaluation, Map<Domain, Map<Domain, AnalysisNode>> journalEdge, boolean distances){
        if(element.previous != null){
            Map<Domain, AnalysisNode> subJournalEdge;

            Domain domainFrom = new Domain(element.candidate.edge, element.previousLink);

            if(journalEdge.containsKey(domainFrom)){
                subJournalEdge = journalEdge.get(domainFrom);
            }
            else{
                subJournalEdge = new HashMap<>();
                journalEdge.put(domainFrom, subJournalEdge);
            }

            Domain domainTo = new Domain(element.previous.candidate.edge, element.previous.nextLink);

            if(subJournalEdge.containsKey(domainTo)){
                AnalysisNode analysisNode = subJournalEdge.get(domainTo);
                analysisNode.count += 1;
                if(distances){
                    analysisNode.sum += matrix[element.previousLink.number][element.previous.nextLink.number];
                }
                else{
                    analysisNode.sum += evaluation.cost;
                }
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
                subJournalEdge.put(domainTo, analysisNode);
            }
        }
        if(element.next != null){
            Map<Domain, AnalysisNode> subJournalEdge;

            Domain domainFrom = new Domain(element.candidate.edge, element.nextLink);

            if(journalEdge.containsKey(domainFrom)){
                subJournalEdge = journalEdge.get(domainFrom);
            }
            else{
                subJournalEdge = new HashMap<>();
                journalEdge.put(domainFrom, subJournalEdge);
            }

            Domain domainTo = new Domain(element.next.candidate.edge, element.next.previousLink);

            if(subJournalEdge.containsKey(domainTo)){
                AnalysisNode analysisNode = subJournalEdge.get(domainTo);
                analysisNode.count += 1;
                if(distances){
                    analysisNode.sum += matrix[element.nextLink.number][element.next.previousLink.number];
                }
                else{
                    analysisNode.sum += evaluation.cost;
                }
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
                subJournalEdge.put(domainTo, analysisNode);
            }
        }
    }

}
