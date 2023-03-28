import java.util.*;

public class Genetic {

    public static Random random = new Random();

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
            Collections.shuffle(newPopulation, new Random(i));
            Individual individual = new Individual(newPopulation);
            Evaluation evaluation = Main.evaluatePriorityList(individual.priorityList, entries, config, matrix2);
            individual.evaluation = evaluation;
            population.add(individual);
        }

        for (int i = 0; i < popSize; i++) {
            Individual individual = population.get(i);
            System.out.println(individual.evaluation.cost);
            System.out.println(individual.evaluation.vehicleCount);
        }

        for (int i = 0; i < maxGen; i++) {
            List<Individual> interPop = new ArrayList<>();

            for (int j = 0; j < popSize; j++) {
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
                    child1 = parent1;
                    child2 = parent2;

                    child1.mutate();
                    child2.mutate();
                }

//                Evaluation evaluation1 = Main.evaluatePriorityList(child1.priorityList, entries, config, matrix2);
//                child1.evaluation = evaluation1;

//                Evaluation evaluation2 = Main.evaluatePriorityList(child2.priorityList, entries, config, matrix2);
//                child2.evaluation = evaluation2;

                interPop.add(child1);
                interPop.add(child2);

            }

        }


    }

    public Individual tournamentSelection(List<Individual> population){
        List<Individual> subset = new ArrayList<>();
        for (int i = 0; i < this.k; i++) {
            subset.add(population.get(random.nextInt(population.size())));
        }

        Collections.sort(subset, new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
                return Double.compare(o1.fitness(), o2.fitness());
            }
        });

        return subset.get(0);
    }

}
