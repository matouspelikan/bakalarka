import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Delete {
    public static Comparator<Individual> comparator;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        DataOutputStream dos = new DataOutputStream(new FileOutputStream("fff.txt"));

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
                new FileInputStream("testRoutes/egl-e1-A_1_p/journal.txt"));

//        PrintWriter writer = new PrintWriter(new FileWriter(new File("outRoutes.txt")));
//
//
//        SerialIndividual sindividual = null;
//        int count = 0;
//        try{
//            while((sindividual = (SerialIndividual) OIS.readObject()) != null){
//                count++;
//
////                System.out.println("i: " + sindividual.generation + " " + sindividual.populationN.get(0).printRoutes());
//                Genetic.testRoutes(sindividual.populationN, sindividual.generation, writer);
//
//
//            }
//        }
//        catch (EOFException e){
//
//        }
//        System.out.println(count);
//        writer.close();

        SerialJournal serial;
//        Object serial;

        int count = 0;
        try {
            while ((serial = (SerialJournal)OIS.readObject()) != null) {

//                System.out.println(serial.generation);
////                System.out.println(serial.journalPair.journal.entrySet().size());
//                System.out.println();
//                Genetic.printJournal(serial.journalPair);
                System.out.println("generation: " + serial.generation);
                System.out.println(serial.journalPair.journal.get(new Node(60)));

                count++;
//                System.out.println(serial);

            }
        }
        catch (EOFException e){

        }
        System.out.println(count);



    }

    public static void method(int a){
        a = a +1;
    }

}
