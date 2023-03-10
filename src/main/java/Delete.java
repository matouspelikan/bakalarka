import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;


public class Delete {
    public static void main(String[] args) {
        Map<Integer, List<Integer>> mapa = new HashMap<>();

        List<Integer> list = new ArrayList<>();
        list.add(9);

        mapa.put(2, list);

        System.out.println(mapa);

        System.out.println(mapa.get(2));

        mapa.get(2).add(10);

        System.out.println(mapa);

        int a = 1;
        method(a);
        System.out.println(a);

    }

    public static void method(int a){
        a = a +1;
    }

}
