import java.io.Serializable;

public class AnalysisNode implements Serializable {
    public double sum;
    public int count;

    public AnalysisNode(){
        sum = 0;
        count = 0;
    }

    public AnalysisNode(double sum){
        this.sum = sum;
        count = 1;
    }

    @Override
    public String toString() {
//        return "AnalysisNode{" +
//                "sum=" + sum +
//                ", count=" + count +
//                '}';
        return "AN{" + sum/count + "}";
    }
}
