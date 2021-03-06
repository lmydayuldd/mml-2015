package application.experiments.results;

import experiment.Experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import static com.google.common.math.DoubleMath.mean;
import static java.util.Collections.max;
import static java.util.Collections.min;

/**
 * Created by alex on 5/5/15.
 */
public class Reporting {
    public static void writeTestResults(Experiment experiment, String experimentName, int iteration) {
        String path = "../experiments/output/most_recent/" + experimentName;
        File experimentDirectory = new File(path);
        if(!experimentDirectory.exists()){
            experimentDirectory.mkdirs();
        }

        experiment.writeRocCurves(path + "/roc_curves_iter_" + iteration);

        try(PrintWriter writer = new PrintWriter(path + "/" + experimentName + "iter-" + iteration)) {
            List<Double> errorRates = experiment.test();
            System.out.println(String.format("Mean: %f Std.dev.: %f", mean(errorRates), std(errorRates)));
            writer.println(mean(errorRates));
            writer.println(std(errorRates));
            writer.println(max(errorRates));
            writer.println(min(errorRates));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to write experimental results!", e);
        }
    }


    private static double std(List<Double> test) {
        double mean = mean(test);
        double std = test.stream().mapToDouble(error -> Math.pow(error - mean, 2)).average().getAsDouble();
        std = Math.sqrt(std);

        return std;
    }

}
