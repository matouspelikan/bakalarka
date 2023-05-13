import java.io.Serializable;
import java.util.Map;

public class JournalPair implements Serializable {
    public Map<Node, Map<Node, AnalysisNode>> journal;
    public Map<Domain, Map<Domain, AnalysisNode>> journalEdge;
    public boolean journaling;

    public JournalPair(Map<Node, Map<Node, AnalysisNode>> journal, Map<Domain, Map<Domain, AnalysisNode>> journalEdge,
                       boolean journaling) {
        this.journal = journal;
        this.journalEdge = journalEdge;
        this.journaling = journaling;
    }

}
