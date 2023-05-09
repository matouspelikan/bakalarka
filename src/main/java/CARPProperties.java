import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class CARPProperties extends Properties {
    public String datasetGroup;
    public String dataset;
    public int seed;
    public int N;
    public int M;
    public int k;
    public int maxEpoch;
    public int popSize;
    public int maxGen;
//    public int tournament;
    public int duplicates;
    public double pMutation;
    public double pCross;
    public int tournament1;
    public int tournament2;
    public String resultDir;
    public boolean populationRestart;


    public String configFileName;


    private static CARPProperties instance;
    private CARPProperties(){}
    public static CARPProperties getInstance(){
        if(instance == null){
            instance = new CARPProperties();
        }
        return instance;
    }

    public void readConfigFile(String configFileName) throws Exception {
        this.configFileName = configFileName;
        File inputFile = new File(configFileName);
        if (!inputFile.exists()) {
            throw new Exception("config file does not exist!");
        }

        FileInputStream stream = new FileInputStream(inputFile);

        this.load(stream);

        datasetGroup = this.getProperty("datasetGroup");
        dataset = this.getProperty("dataset");
        seed = Integer.parseInt(this.getProperty("seed"));
        N = Integer.parseInt(this.getProperty("N"));
        M = Integer.parseInt(this.getProperty("M"));
        k = Integer.parseInt(this.getProperty("k"));
        maxEpoch = Integer.parseInt(this.getProperty("maxEpoch"));
        popSize = Integer.parseInt(this.getProperty("popSize"));
        maxGen = Integer.parseInt(this.getProperty("maxGen"));
//        tournament = Integer.parseInt(this.getProperty("tournament"));
        duplicates = Integer.parseInt(this.getProperty("duplicates"));
        pMutation = Double.parseDouble(this.getProperty("pMutation"));
        pCross = Double.parseDouble(this.getProperty("pCross"));
        tournament1 = Integer.parseInt(this.getProperty("tournament1"));
        tournament2 = Integer.parseInt(this.getProperty("tournament2"));
        resultDir = this.getProperty("resultsDir");
        populationRestart  = Boolean.parseBoolean(this.getProperty("populationRestart"));
    }

    @Override
    public String toString() {
        return "CARPProperties{" +
                "dataset='" + dataset + '\'' +
                ", seed=" + seed +
                ", N=" + N +
                ", M=" + M +
                ", k=" + k +
                ", maxEpoch=" + maxEpoch +
                ", popSize=" + popSize +
                ", maxGen=" + maxGen +
                ", duplicates=" + duplicates +
                ", pMutation=" + pMutation +
                ", pCross=" + pCross +
                ", tournament1=" + tournament1 +
                ", tournament2=" + tournament2 +
                '}';
    }
}
