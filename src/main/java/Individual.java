import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Individual {

    public static Random random = new Random(0);

    public List<Edge> priorityList;
    public Evaluation evaluation;

    public Individual(List<Edge> priorityList){
        this.priorityList = Main.deepCopy(priorityList);
    }

    public double fitness(){
        return evaluation.cost;
    }

    public List<Edge> crossWith(Individual other){
        List<Edge> newPriorityList = new ArrayList<>();
        int half = priorityList.size() / 2;
        for (int i = 0; i < half; i++) {
            newPriorityList.add(priorityList.get(i));
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

    public List<Edge> crossWith2(Individual other){
        int midsize = this.priorityList.size()/2;
        int residuum = priorityList.size() - midsize;
        int presize = residuum/2;
        int postsize = residuum - presize;

        if(presize + midsize + postsize != priorityList.size()) throw new RuntimeException();

        List<Edge> newPriorityList = new ArrayList<>();
        for (int i = presize; i < presize + midsize; i++) {
            newPriorityList.add(priorityList.get(i));
        }

        for (int i = 0; i < presize; i++) {
            for (int j = 0; j < other.priorityList.size(); j++) {
                if(!newPriorityList.contains(other.priorityList.get(j))){
                    newPriorityList.add(0, other.priorityList.get(j));
                    break;
                }
            }
        }
        for (int i = 0; i < postsize; i++) {
            for (int j = 0; j < other.priorityList.size(); j++) {
                if(!newPriorityList.contains(other.priorityList.get(j))){
                    newPriorityList.add(other.priorityList.get(j));
                    break;
                }
            }
        }

        if(newPriorityList.size() != priorityList.size()){
            throw new RuntimeException();
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
