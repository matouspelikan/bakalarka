import java.util.Map;

public class JournalPair {
    public Map<Node, Map<Node, AnalysisNode>> journal;
    public Map<Domain, Map<Domain, AnalysisNode>> journalEdge;

    public JournalPair(Map<Node, Map<Node, AnalysisNode>> journal, Map<Domain, Map<Domain, AnalysisNode>> journalEdge) {
        this.journal = journal;
        this.journalEdge = journalEdge;
    }

}
