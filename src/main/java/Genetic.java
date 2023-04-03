import java.util.*;

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

        for (int i = 0; i < popSize; i++) {
            List<Edge> newPopulation = new ArrayList<>(List.copyOf(this.requiredEdges));
            Collections.shuffle(newPopulation, new Random(random.nextInt()));
            Individual individual = new Individual(newPopulation);
            Evaluation evaluation = Main.evaluatePriorityList(individual.priorityList, entries, config, matrix2);
            individual.evaluation = evaluation;
            population.add(individual);
        }

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
            List<Individual> interPop = new ArrayList<>();

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

                Evaluation evaluation1 = Main.evaluatePriorityList(child1.priorityList, entries, config, matrix2);
                child1.evaluation = evaluation1;

                Evaluation evaluation2 = Main.evaluatePriorityList(child2.priorityList, entries, config, matrix2);
                child2.evaluation = evaluation2;

                interPop.add(child1);
                interPop.add(child2);
                interPopSize += 2;
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
//            System.out.println(individual.evaluation.cost);
//            System.out.println(individual.evaluation.vehicleCount);
            System.out.println(individual.evaluation);
        }

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

}
