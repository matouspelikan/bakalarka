public class Entry {
    public int nodeNumber;
    public double distance;

    public int from;

    public Entry(int nodeNumber, double distance, int from){
        this.nodeNumber = nodeNumber;
        this.distance = distance;
        this.from = from;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "nodeNumber=" + nodeNumber +
                ", distance=" + distance +
                '}';
    }
}
