import java.io.Serializable;
import java.util.List;

public class SerialIndividual implements Serializable {
    int generation;
    List<Individual> populationN;

    public SerialIndividual(int generation, List<Individual> populationN){
        this.generation = generation;
        this.populationN = populationN;
    }

    @Override
    public String toString() {
        return generation + " " + populationN.size();
    }
}
