import me.tongfei.progressbar.ProgressBar;

import java.io.PrintWriter;
import java.util.*;

public class Genetic {
    public static Random random;
    public static Comparator<Individual> comparator;

    public static Config config;
    public static Double[][] matrix;  //matice vzdáleností
    List<Edge> requiredEdges; //seznam pozadovanych hran, jeho permutovanim se vytvareji nahodne chromosomy
    public int kTournament = 3;
    public int maxDuplicates = 2; //maximalni pocet jedincu v populace se stejnym costem

    public PrintWriter pw;

    public Genetic(List<Edge> requiredEdges, PrintWriter pw){
        this.pw = pw;
        this.requiredEdges = requiredEdges;

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

    public void evolution(int popSize, int maxGen, double probCross, double probMutation, int M, int k, double N, int maxEpoch){
        maxDuplicates = popSize;

        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>(); //struktura, ve ktere se uchovavaji vysledky analyzy
        boolean journaling = false; //priznak, podle ktereho se prepina vybirani sousedu podle vzdalenosti/analyzy

        List<Individual> population;
        population = createInitialPopulation(popSize, journal);
        sortPopulation(population);
//        printPopulation(population);

        Map<Node, Map<Node, AnalysisNode>> bestSoFarJournal = null;
        Individual bestSoFarIndividual = new Individual(); //fitness nastaveno na infinity


        int nbOfJournaling = 0;
        int nbOfEpoch = 0;

        ProgressBar pb = new ProgressBar("k=" + M, maxGen);


        for (int i = 0; i < maxGen; i++) {
            pb.step();
            if(comparator.compare(population.get(0), BEST) < 0){ //prvni jedinec v populaci je lepsi nez bestSoFar
                BEST = new Individual(population.get(0), i);
                pw.println(i + ": " + BEST.evaluation);
                pw.flush();
            }

            boolean reevaluate = false;

            if((i == M) ||                              // v M-te generaci se poprve spusti analyza
                    (i > M && (nbOfJournaling % k == 0)))    // kazdou k-tou iteraci se analyza prepocita
            {
                if(i == M){
                    bestSoFarIndividual = new Individual(population.get(0), i);
                    bestSoFarJournal = analyzePopulation(population, N);
                }

                if(comparator.compare(population.get(0), bestSoFarIndividual) < 0){ //prvni jedinec v populaci je lepsi nez bestSoFar
                    bestSoFarIndividual = new Individual(population.get(0), i);
                    journal = analyzePopulation(population, N);
                    bestSoFarJournal = journal;
                    nbOfEpoch = 1; //dokud se nejlepsi jedinec zlepsuje, zustava epocha na zacatku
                }
                else if(nbOfEpoch < maxEpoch){
                    nbOfEpoch++;
                    journal = analyzePopulation(population, N);
                }
                else{
                    nbOfEpoch = 1;
                    journal = bestSoFarJournal;
                }

                journaling = true;
                nbOfJournaling = 0;
                reevaluate = true;
//                journal = analyzePopulation(population, N);
            }

            if (reevaluate){
                for (Individual ind: population){
                    ind.perturb(journal, true);
                }
            }

            nbOfJournaling++;

//            System.out.print(i + ": " + " epoch: " + nbOfEpoch + "  ");
//            printPopulation(population);
//            System.out.println("BestSoFar: " + bestSoFarIndividual.evaluation);

            List<Individual> interPop = new ArrayList<>();
            int interPopSize = 0;
            while (interPopSize < popSize) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);

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

                child1.evaluate(journal, journaling);
                child2.evaluate(journal, journaling);

                child1.localOptimisation(journal);
                child2.localOptimisation(journal);

                interPop.add(child1);
                interPopSize++;

                interPop.add(child2);
                interPopSize++;
            }

            int originalSize = population.size();
            population.addAll(interPop);
            sortPopulation(population);
            List<Individual> nonDuplicatedPopulation = deleteDuplicates(population);
//            population.subList(originalSize, population.size()).clear();
            nonDuplicatedPopulation.subList(originalSize, nonDuplicatedPopulation.size()).clear();
            population = nonDuplicatedPopulation;

            if (originalSize != population.size()) throw new RuntimeException();

        }
        pb.close();

//        System.out.println();
//        printPopulation(population);

//        System.out.println(journal);
    }

    public Individual createIndividual(Map<Node, Map<Node, AnalysisNode>> journal){
        List<Edge> newPriorityList = Main.deepCopy(requiredEdges);
        Collections.shuffle(newPriorityList, new Random(random.nextInt()));
        Individual individual = new Individual(newPriorityList);
        individual.evaluate(journal, false);
        return individual;
    }

    public List<Individual> createInitialPopulation(int popSize, Map<Node, Map<Node, AnalysisNode>> journal){
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
            Individual newIndividual = createIndividual(journal);
            int newIndividualHash = newIndividual.hashCustom();

            if(!hashes.contains(newIndividualHash)){
                population.add(newIndividual);
                size++;
                hashes.add(newIndividualHash);
            }

            if(true) continue;
            //obsolete way of checking for duplicates

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

    public List<Individual> deleteDuplicates(List<Individual> population){
        Set<Integer> hashes = new HashSet<>();

        List<Individual> newPopulation = new ArrayList<>(); //without duplicates
        Map<Evaluation, Integer> counts = new HashMap<>();
        for(Individual individual : population){
            int hash = individual.hashCustom();
            if(!hashes.contains(hash)){
                hashes.add(hash);
                newPopulation.add(individual);
            }

            if(true) continue;
            //obsolete way of checking for duplicates

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

    public void sortPopulation(List<Individual> population) {
        Collections.sort(population, comparator);
    }

    public Individual tournamentSelection(List<Individual> population){
        List<Individual> subset = new ArrayList<>();
        for (int i = 0; i < this.kTournament; i++) {
            subset.add(population.get(random.nextInt(population.size())));
        }

        sortPopulation(subset);

        return subset.get(0);
    }

    public void printPopulation(List<Individual> population){
        for(Individual individual : population){
            System.out.print(individual.evaluation);
            System.out.print(" ");
        }
        System.out.println();

        double sum = 0;
        int count = 0;
        for (int j = 0; j < population.size(); j++) {
            sum += population.get(j).evaluation.cost;
            count++;
        }
        System.out.println("population average: " + sum/count + " best: " + population.get(0).evaluation.cost);

//        population.get(0).printRoutes();
    }

    public Map<Node, Map<Node, AnalysisNode>> analyzePopulation(List<Individual> population, double N){
        Map<Node, Map<Node, AnalysisNode>> journal = new HashMap<>();
        int sizeWorthy = (int)(population.size()*N);
        List<Individual> populationWorthy = new ArrayList<>(population.stream().limit(sizeWorthy).toList());
        for (Individual individual : populationWorthy) {
            analyzeIndividual(individual, journal);
        }
        return journal;
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

    public void analyzeElementNew(Element element, Evaluation evaluation, Map<Edge, Map<Edge, AnalysisNode>> journal){
        if(element.previous != null){
            Map<Edge, AnalysisNode> subJournal;
            if(journal.containsKey(element.candidate.edge)){
                subJournal = journal.get(element.candidate.edge);
            }
            else{
                subJournal = new HashMap<>();
                journal.put(element.candidate.edge, subJournal);
            }
            if(subJournal.containsKey(element.previous.candidate.edge)){
                AnalysisNode analysisNode = subJournal.get(element.previous.candidate.edge);
                analysisNode.count += 1;
                analysisNode.sum += evaluation.cost * evaluation.vehicleCount;
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
                subJournal.put(element.previous.candidate.edge, analysisNode);
            }
        }
        if(element.next != null){
            Map<Edge, AnalysisNode> subJournal;
            if(journal.containsKey(element.candidate.edge)){
                subJournal = journal.get(element.candidate.edge);
            }
            else{
                subJournal = new HashMap<>();
                journal.put(element.candidate.edge, subJournal);
            }
            if(subJournal.containsKey(element.next.candidate.edge)){
                AnalysisNode analysisNode = subJournal.get(element.next.candidate.edge);
                analysisNode.count += 1;
                analysisNode.sum += evaluation.cost * evaluation.vehicleCount;
            }
            else{
                AnalysisNode analysisNode = new AnalysisNode(evaluation.cost);
                subJournal.put(element.next.candidate.edge, analysisNode);
            }
        }
    }

}
