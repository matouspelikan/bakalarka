import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Delete {
    public static Comparator<Individual> comparator;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        comparator = new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
                if(o1.evaluation.vehicleCount <= 5 && o2.evaluation.vehicleCount <= 5){
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
                else if(o1.evaluation.vehicleCount <= 5){
                    return -1;
                }
                else if(o2.evaluation.vehicleCount <= 5){
                    return 1;
                }
                else{
                    return Double.compare(o1.evaluation.cost, o2.evaluation.cost);
                }
            }
        };

        ObjectInputStream OIS = new ObjectInputStream(
                new FileInputStream("singleExpriment/pickedGenerations.txt"));
//                new FileInputStream("singleExpriment/egl-e1-A_1_e/bestSerialized.txt"));

        PrintWriter writer = new PrintWriter(new FileWriter(new File("outRoutes.txt")));

        PrintWriter populationWriter = new PrintWriter(new FileWriter("singleExpriment/populationDelete.txt"));


        List<SerialIndividual> serialIndividuals = new ArrayList<>();
        SerialIndividual sindividual = null;
        int count = 0;
        try{
            while((sindividual = (SerialIndividual) OIS.readObject()) != null){
//                if(true) throw new EOFException("haahahah");

//                populationWriter.println("i: " + sindividual.generation + " " + sindividual.populationN);
//                populationWriter.flush();
//                Genetic.testRoutes(sindividual.populationN, sindividual.generation, writer);
                serialIndividuals.add(sindividual);
                System.out.println(count + " " + sindividual.generation);
                count++;
                if(count == 2) break;
            }
        }
        catch (EOFException e){
            System.out.println(e);
        }
        System.out.println(count);
        populationWriter.close();
        writer.close();




        ObjectInputStream OIS2 = new ObjectInputStream(
                new FileInputStream("singleExpriment/egl-e1-A_1_e/journal.txt"));


        List<SerialJournal> serialJournals = new ArrayList<>();
        SerialJournal serial;

        int count2 = 0;
        try {
            while ((serial = (SerialJournal)OIS2.readObject()) != null) {

//                System.out.println(serial.generation);
////                System.out.println(serial.journalPair.journal.entrySet().size());
//                System.out.println();
//                Genetic.printJournal(serial.journalPair);
                System.out.println("generation: " + serial.generation);
//                System.out.println(serial.journalPair.journal.get(new Node(60)));


                serialJournals.add(serial);
                count2++;

//                System.out.println(serial);

            }
        }
        catch (EOFException e){

        }
        System.out.println(count2);


//        ObjectOutputStream OOS = new ObjectOutputStream(new FileOutputStream("singleExpriment/pickedGenerations.txt"));
//
//        for (int i = 90; i < 300; i+=20) {
//            OOS.writeObject(serialIndividuals.get(i));
//        }

        Individual individual = serialIndividuals.get(1).populationN.get(0);

        System.out.println(individual);

        JournalPair journalPair = serialJournals.get(6).journalPair;

        ObjectInputStream _ois = new ObjectInputStream(new FileInputStream("konfigurace.txt"));
        Config config = (Config) _ois.readObject();
        _ois.close();

        Individual.config = config;
        Individual.random = new Random(0);
//        System.out.println(individual.priorityList);
        for (int i = 0; i < 300; i++) {
            individual.perturb(journalPair, JournalType.EDGE);
            if(individual.evaluation.cost == 3548){
                System.out.println(individual);
                System.out.println(Main.evaluateRoutes(individual.evaluation.routes, config));
                System.out.println(individual.priorityList);
                System.out.println(individual.printRoutes());

                if(i==1) break;
            }
        }




    }

    public static void method(int a){
        a = a +1;
    }

}
