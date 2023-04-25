import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluation that = (Evaluation) o;
        return Double.compare(that.cost, cost) == 0 && vehicleCount == that.vehicleCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, vehicleCount);
    }
}
