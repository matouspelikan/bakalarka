import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class CARPProperties extends Properties {

    public int seed;
    public double N;

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

        seed = Integer.parseInt(this.getProperty("seed"));
        N = Double.parseDouble(this.getProperty("N"));
    }
}
