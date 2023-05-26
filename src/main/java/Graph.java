import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import me.tongfei.progressbar.wrapped.ProgressBarWrappedInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    public static void main(String[] args) throws IOException, CsvValidationException {

        PrintWriter outpw = new PrintWriter(new FileWriter("outall.csv"));

//        analyzeDirectory("resultfinalag", "exp_node_M100_k100_g1000", outpw);
//        analyzeDirectory("resultfinalag", "exp_basic_M200_k200_g1000", outpw);
//        analyzeDirectory("resultfinalag", "exp_node_M100_k20_g300", outpw);
//        analyzeDirectory("resultfinalag", "exp_node_M200_k200_g1000", outpw);

//        analyzeDirectory("resultfinalag", "exp_edge_M100_k20_g300", outpw);




//        analyzeDirectory("resultfinalag", "exp_basic_M100_k20_g300", outpw);
//        analyzeDirectory("resultfinalag", "exp_node_M100_k20_g300", outpw);
        analyzeDirectory("resultfinalag", "exp_edge_M100_k20_g300", outpw);
        analyzeDirectory("resultfinalag", "exp_basic_M200_k200_g1000", outpw);
        analyzeDirectory("resultfinalag", "exp_node_M200_k200_g1000", outpw);


//        analyzeDirectory("resultfinalag", "exp_vanilla_M100_k100_g1000", outpw);
//        analyzeDirectory("resultfinalag", "exp_basic_M100_k100_g1000", outpw);
//        analyzeDirectory("resultfinalag", "exp_node_M100_k100_g1000", outpw);

//        analyzeDirectory("resultfinalag", "exp_edge_M100_k20_g300", outpw);

        outpw.close();

        if(true){
            return;
        }

        String directory = "resultfinalagok/exp_edge_M100_k20_g300/egl-e1-A";
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

        System.out.println(meansp);
        System.out.println(meansp.size());

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

    public static void analyzeDirectory(String directory, String configuration, PrintWriter pw) throws IOException, CsvValidationException {

        Path jarPath = Paths.get("").toAbsolutePath();
        Path dirPathRelative = Paths.get(directory);
        Path confPathRelative = Paths.get(configuration);

        Path dirPathAbsolute = jarPath.resolve(dirPathRelative).resolve(confPathRelative);
        File dirFile = dirPathAbsolute.toFile();

        System.out.println(dirFile);
        System.out.println(dirFile.exists());

        List<File> files = new ArrayList<>(Arrays.stream(dirFile.listFiles(File::isDirectory)).sorted().collect(Collectors.toList()));

        List[] bsfs = new List[30];
        List[] bsps = new List[30];
        int bsfi = 0;

        boolean next = true;
        String previous = "";

        double BEST = Double.POSITIVE_INFINITY;
        int CEST = Integer.MAX_VALUE;

        System.out.println(files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            String[] s = file.getPath().split("\\\\");
            String dataset = s[s.length-1].split("_")[0];

            String last = s[s.length-1];
            String c = last.substring(last.length()-1, last.length());
//            System.out.println(c);



            System.out.println(dataset + Arrays.asList(s));

            if(next){
                if(dataset.equals(previous)){
                    continue;
                }

                previous = dataset;
                next = false;
            }

            if(c.equals("b")){
                continue;
            }

            previous = dataset;


            File convergence = file.toPath().resolve("convergence.csv").toFile();
            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(convergence)));

            String[] line;

            List<Double> bsf = new ArrayList<>();
            bsfs[bsfi] = bsf;

            List<Double> bsp = new ArrayList<>();
            bsps[bsfi] = bsp;

            int vehicleC = 0;

            int in = 0;
            while((line = reader.readNext()) != null){
                if(in > 0){
                    bsf.add(Double.parseDouble(line[4]));
                    bsp.add(Double.parseDouble(line[1]));
                    vehicleC = Integer.parseInt(line[5]);
                    if(vehicleC < CEST)
                        CEST = vehicleC;
                }
                in++;
            }
            System.out.println(in);
            double B = bsf.get(bsf.size()-1);
            if(B < BEST){
                BEST = B;
            }


            reader.close();

            bsfi++;
            if(bsfi >= 30){
                next = true;
                //together

                List<Double> means = new ArrayList<>();
                List<Double> meansp = new ArrayList<>();


                for (int k = 0; k < 1000; k++) {
//                    if(k >= in-1){
//                        double _last = means.get(means.size()-1);
//                        means.add(_last);
//                        double _last2 = meansp.get(meansp.size()-1);
//                        meansp.add(_last2);
//                        continue;
//                    }

                    List<Double> intermediate = new ArrayList<>();
                    List<Double> intermediatep = new ArrayList<>();
                    for (int j = 0; j < 30; j++) {
//                        System.out.println(j + " " + k);
                        intermediate.add((Double) bsfs[j].get(k));
                        intermediatep.add((Double) bsps[j].get(k));
                    }

                    File outstat = jarPath.resolve(dirPathRelative).resolve(dataset + ".csv").toFile();
                    PrintWriter appendstat = new PrintWriter(new FileWriter(outstat, true));


                    if(k == in - 2){
                        System.out.println(intermediate.size());
                        System.out.println(dataset);
                        System.out.println(intermediate);
                        for (int j = 0; j < intermediate.size(); j++) {
                            appendstat.print(intermediate.get(j));
                            System.out.println(intermediate.get(j));
                            if(j < intermediate.size() - 1)
                                appendstat.print(",");
                        }
                        appendstat.println();
                        appendstat.flush();
                        appendstat.close();
                        break;
                    }

//                    if(k == 299){
//                        System.out.println(intermediate.size());
//                        System.out.println(dataset);
//                        System.out.println(intermediate);
//                        for (int j = 0; j < intermediate.size(); j++) {
//                            appendstat.print(intermediate.get(j));
//                            System.out.println(intermediate.get(j));
//                            if(j < intermediate.size() - 1)
//                                appendstat.print(",");
//                        }
//                        appendstat.println();
//                    }



                    Collections.sort(intermediate);
                    means.add(intermediate.get(intermediate.size()/2));

                    Collections.sort(intermediatep);
                    meansp.add(intermediatep.get(intermediatep.size()/2));
                }

//                System.out.println(means);
//                System.out.println(means.size());
//
//                System.out.println(meansp);
//                System.out.println(meansp.size());

                File outt = jarPath.resolve(dirPathRelative).resolve(dataset + ".csdvv").toFile();
//                System.out.println(outt);

                PrintWriter appendPw = new PrintWriter(new FileWriter(outt, true));


                File outt2 = jarPath.resolve(dirPathRelative).resolve(dataset + ".txt").toFile();
                PrintWriter appendPw2 = new PrintWriter(new FileWriter(outt2, true));
                appendPw2.println(means.get(means.size()-1) + "," + vehicleC+","+BEST);




                for (int j = 0; j < means.size(); j++) {
                    appendPw.print(means.get(j));
                    if(j < means.size() - 1){
                        appendPw.print(",");
                    }
                }
                appendPw.println();

                for (int j = 0; j < meansp.size(); j++) {
                    appendPw.print(meansp.get(j));
                    if(j < meansp.size() - 1){
                        appendPw.print(",");
                    }
                }
                appendPw.println();

                appendPw.close();
                appendPw2.close();

                bsfs = new List[30];
                bsps = new List[30];
                bsfi = 0;
                BEST = Double.POSITIVE_INFINITY;
                CEST = Integer.MAX_VALUE;
            }
        }

    }

    public static void analyzeDataset(String dataset){

    }




}
