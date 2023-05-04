import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class CARPProperties extends Properties {
    public String datasetGroup;
    public String dataset;
    public int seed;
    public double N;
    public int M;
    public int k;
    public int maxEpoch;
    public int popSize;
    public int maxGen;

    private static CARPProperties instance;
    private CARPProperties(){}
    public static CARPProperties getInstance(){
        if(instance == null){
            instance = new CARPProperties();
        }
        return instance;
    }

    public void readConfigFile(String fileName) throws Exception{
        File inputFile = new File(fileName);
        if(!inputFile.exists()){
            throw new Exception("config file does not exist!");
        }

        FileInputStream stream = new FileInputStream(inputFile);

        this.load(stream);

        datasetGroup = this.getProperty("datasetGroup");
        dataset = this.getProperty("dataset");
        seed = Integer.parseInt(this.getProperty("seed"));
        N = Double.parseDouble(this.getProperty("N"));
        M = Integer.parseInt(this.getProperty("M"));
        k = Integer.parseInt(this.getProperty("k"));
        maxEpoch = Integer.parseInt(this.getProperty("maxEpoch"));
        popSize = Integer.parseInt(this.getProperty("popSize"));
        maxGen = Integer.parseInt(this.getProperty("maxGen"));
    }
}
