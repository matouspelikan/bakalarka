import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Individual {

    public static Random random = new Random(0);

    public List<Edge> priorityList;
    public Evaluation evaluation;

    public Individual(List<Edge> priorityList){
        this.priorityList = priorityList;
    }

    public double fitness(){
        return evaluation.cost;
    }

    public List<Edge> crossWith(Individual other){
        List<Edge> newPriorityList = new ArrayList<>();
        int half = priorityList.size() / 2;
        for (int i = 0; i < priorityList.size(); i++) {
            if (i < half){
                newPriorityList.add(priorityList.get(i));
                continue;
            }
            break;
        }

        for (int i = newPriorityList.size(); i < priorityList.size(); i++) {
            for (int j = 0; j < other.priorityList.size(); j++) {
                if(!newPriorityList.contains(other.priorityList.get(j))){
                    newPriorityList.add(other.priorityList.get(j));
                    break;
                }
            }
        }

        return newPriorityList;
    }

    public void mutate(){
        int indexFrom = random.nextInt(priorityList.size());
        int indexTo = random.nextInt(priorityList.size());
        Edge temp = priorityList.get(indexFrom);

        priorityList.set(indexFrom, priorityList.get(indexTo));
        priorityList.set(indexTo, temp);

//        System.out.println(indexFrom + "  " + indexTo);
    }

    @Override
    public String toString() {
        return "Individual{" +
                "priorityList=" + priorityList +
                '}';
    }
}
