/**
 *  Vnitrní reprezentace cest pomocí spojového seznamu
 */
public class Element {
    public Candidate candidate;
    public Element previous = null; //odkaz na dalsi prvek ve spojovem seznamu
    public Element next = null;

    public double nextDistance = 0; //vzdalenost do this.previous
    public double previousDistance = 0; //vzdalenost do this.next

    public Node previousLink; //Vrchol skrze ktery je hrana spojena se svym predchudcem (this.previous)
    public Node nextLink; //Vrchol skrze ktery je hrana spojena se svym nasledovnikem (this.next)

    public Element(Candidate candidate){
        this.candidate = candidate;
    }

    @Override
    public String toString() {
        return "|" + previousLink + " " + nextLink + "|";
    }
}
