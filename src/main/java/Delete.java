import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Delete {
    public static void main(String[] args) {
        Candidate c1 = new Candidate(null, null, null, 0);
        Candidate c2 = new Candidate(null, null, null, 0);
        Candidate c3 = new Candidate(null, null, null, 0);



        c1.journalEntry = 1.0;
        c2.journalEntry = 10.0;
        c3.journalEntry = 5.0;

        List<Candidate> list = new ArrayList<>(Arrays.asList(c1, c2, c3));

        Collections.sort(list, Comparator.comparingDouble(Candidate::getJournalEntry));

        System.out.println(list);

    }

    public static void method(int a){
        a = a +1;
    }

}
