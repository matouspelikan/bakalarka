import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    public static void main(String[] args) throws IOException, CsvValidationException {
        String directory = "resultfinalag/exp_edge_M100_k20_g300/egl-e1-A";


        Path jarPath = Paths.get("").toAbsolutePath();
        Path dirPathRelative = Paths.get(directory);

        Path dirPathAbsolute = jarPath.resolve(dirPathRelative);
        File dirFile = dirPathAbsolute.toFile();

        System.out.println(dirFile.exists());


        System.out.println(Arrays.asList(dirFile.list()));

        int seedCount = Arrays.asList(dirFile.listFiles(File::isDirectory)).size();

        List[] bsfs = new ArrayList[seedCount];
        List[] bsps = new ArrayList[seedCount];

        int bsfi = 0;
        for(File f : Arrays.asList(dirFile.listFiles(File::isDirectory))){
            File convergence = f.toPath().resolve("convergence.csv").toFile();

            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(convergence)));
            String[] line;

            List<Double> bsf = new ArrayList<>();
            bsfs[bsfi] = bsf;

            List<Double> bsp = new ArrayList<>();
            bsps[bsfi] = bsp;


            int i = 0;
            while((line = reader.readNext()) != null){
//                System.out.println(Arrays.asList(line));
                if(i > 0){
                    bsf.add(Double.parseDouble(line[4]));
                    bsp.add(Double.parseDouble(line[1]));
                }
                i++;
            }


            bsfi++;
        }

        List<Double> means = new ArrayList<>();
        List<Double> meansp = new ArrayList<>();

        for (int i = 0; i < bsfs[0].size(); i++) {
            List<Double> intermediate = new ArrayList<>();
            List<Double> intermediatep = new ArrayList<>();
            for (int j = 0; j < bsfs.length; j++) {
                intermediate.add((Double) bsfs[j].get(i));
                intermediatep.add((Double) bsps[j].get(i));
            }

            Collections.sort(intermediate);
            means.add(intermediate.get(intermediate.size()/2));

            Collections.sort(intermediatep);
            meansp.add(intermediatep.get(intermediatep.size()/2));

        }

        System.out.println(means);
        System.out.println(means.size());

        File out = dirPathAbsolute.resolve("out.csv").toFile();
        PrintWriter pw = new PrintWriter(new FileWriter(out));
        int size = means.size();
        for (int i = 0; i < size; i++) {
            pw.print(means.get(i));
            if(i < size-1){
                pw.print(",");
            }
        }
        pw.println();

        for (int i = 0; i < size; i++) {
            pw.print(meansp.get(i));
            if(i < size-1){
                pw.print(",");
            }
        }
        pw.println();

        pw.close();

    }




}
