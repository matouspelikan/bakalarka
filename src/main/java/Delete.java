import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;


public class Delete {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        System.out.println(list);
        list.subList(2, list.size()).clear();
        System.out.println(list);
    }

    public static void method(int a){
        a = a +1;
    }

}
