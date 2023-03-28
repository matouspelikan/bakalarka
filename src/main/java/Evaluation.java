public class Evaluation {

    public double cost;
    public int vehicleCount;

    public Evaluation(double cost, int vehicleCount){
        this.cost = cost;
        this.vehicleCount = vehicleCount;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "cost=" + cost +
                ", vehicleCount=" + vehicleCount +
                '}';
    }
}
