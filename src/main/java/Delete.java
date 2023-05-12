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
                new FileInputStream("serializationn/egl-e1-A_1_n/journal.txt"));

//
//        SerialIndividual sindividual = null;
//        SerialIndividual firstSindividual = null;
//        int count = 0;
//        try{
//            while((sindividual = (SerialIndividual) OIS.readObject()) != null){
//                if(count == 0){
//                    firstSindividual = sindividual;
//                }
//                count++;
//
//
//                System.out.println(sindividual.generation);
//                System.out.println(sindividual.populationN);
//
//                if(true)continue;
//
//                if(sindividual.generation == 188){
////                    Individual chosen = sindividual.populationN.get(0);
////                    System.out.println(chosen.hashCustom());
////                    System.out.println(chosen.evaluation);
////
////                    System.out.println(chosen.printRoutes());
////                    System.out.println(chosen.evaluation.routes.get(0));
////                    System.out.println(chosen.evaluation.routes.get(0).tail.candidate.edge.hash());
//
//                    System.out.println(sindividual.generation);
//                    System.out.println(sindividual.populationN);
//                    System.out.println();
//                }
//                if(sindividual.generation == 189){
////                    Individual chosen = sindividual.populationN.get(0);
////                    System.out.println(chosen.hashCustom());
////                    System.out.println(chosen.evaluation);
////
////                    System.out.println(chosen.printRoutes());
////                    System.out.println(chosen.evaluation.routes.get(0));
////                    System.out.println(chosen.evaluation.routes.get(0).tail.candidate.edge.hash());
////
////                    for(Individual ind : sindividual.populationN){
////                        System.out.println(ind.hashCustom());
////                        System.out.println(ind.evaluation);
////                    }
//
//
//                    System.out.println(sindividual.generation);
//                    System.out.println(sindividual.populationN);
//
//
//                    System.out.println();
//                }
//            }
//        }
//        catch (EOFException e){
//
//        }
//



        SerialJournal serial;
//        Object serial;

        int count = 0;
        try {
            while ((serial = (SerialJournal)OIS.readObject()) != null) {

//                System.out.println(serial.generation);
////                System.out.println(serial.journalPair.journal.entrySet().size());
//                System.out.println();
                Genetic.printJournal(serial.journalPair);


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
