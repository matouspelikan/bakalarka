import java.util.List;

public class Evaluation {

    public double cost;
    public int vehicleCount;
    public List<Route> routes;

    public Evaluation(double cost, int vehicleCount, List<Route> routes){
        this.cost = cost;
        this.vehicleCount = vehicleCount;
        this.routes = routes;
    }

    @Override
    public String toString() {
        return "Ev{" +
                "cost=" + cost +
                ", vehC=" + vehicleCount +
                '}';
    }
}
