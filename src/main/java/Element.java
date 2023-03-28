public class Element {
    public Candidate candidate;
    public Element previous = null;
    public Element next = null;

    public double nextDistance = 0;
    public double previousDistance = 0;

    public Node previousLink;
    public Node nextLink;

    public Element(Candidate candidate){
        this.candidate = candidate;
    }

    public int getEdge(){
        if(nextLink == null){
            if(candidate.edge.leftNode.number == previousLink.number){
                return candidate.edge.rightNode.number;
            }
            return candidate.edge.leftNode.number;
        }
        else if(previousLink == null){
            if(candidate.edge.leftNode.number == nextLink.number){
                return candidate.edge.rightNode.number;
            }
            return candidate.edge.leftNode.number;
        }
        else{
            //element neni na kraji
            throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        return candidate.edge.toString();
    }
}
