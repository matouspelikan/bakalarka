import java.io.Serializable;

public class SerialJournal implements Serializable {
    public int generation;
    public JournalPair journalPair;

    public SerialJournal(int generation, JournalPair journalPair){
        this.generation = generation;
        this.journalPair = journalPair;
    }

}
