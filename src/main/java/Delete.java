import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Delete {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        System.out.println(list);
        System.out.println(list.stream().limit(2).collect(Collectors.toList()));
    }

    public static void method(int a){
        a = a +1;
    }

}
