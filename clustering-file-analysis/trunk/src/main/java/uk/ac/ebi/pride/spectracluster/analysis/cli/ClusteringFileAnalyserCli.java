package uk.ac.ebi.pride.spectracluster.analysis.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import uk.ac.ebi.pride.spectracluster.analysis.analyser.AnalyserFactory;
import uk.ac.ebi.pride.spectracluster.analysis.analyser.IClusteringSourceAnalyser;
import uk.ac.ebi.pride.spectracluster.analysis.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceReader;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jg on 15.07.14.
 */
public class ClusteringFileAnalyserCli {
    /**
     * @param args
     */
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(),
                    args);

            // LIST ANALYSER
            if (commandLine.hasOption(CliOptions.OPTIONS.LIST_ANALYSERS.getValue())) {
                listAnalyser();
                return;
            }

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue()) || args.length < 1) {
                printHelp();
                return;
            }

            // ANALYSER
            Set<IClusteringSourceAnalyser> analyser = new HashSet<IClusteringSourceAnalyser>();
            if (commandLine.hasOption(CliOptions.OPTIONS.ANALYSER.getValue())) {
                String[] analyserNames = commandLine.getOptionValues(CliOptions.OPTIONS.ANALYSER.getValue());

                for (String analyserName : analyserNames) {
                    IClusteringSourceAnalyser theAnalyser = AnalyserFactory.getAnalyserForString(analyserName);
                    if (theAnalyser == null) {
                        System.out.println("Unknown analyser '" + analyserName + "' - ignored.");
                    }
                    else {
                        analyser.add(theAnalyser);
                    }
                }
            }

            // ALL ANALYSERS
            if (commandLine.hasOption(CliOptions.OPTIONS.ALL_ANALYSERS.getValue())) {
                for (AnalyserFactory.ANALYSERS a : AnalyserFactory.ANALYSERS.values())
                    analyser.add(AnalyserFactory.getAnalyser(a));
            }

            // RUN THE ANALYSIS ON ALL PASSED FILES
            for (String filename : commandLine.getArgs()) {
                analyseFile(filename, analyser);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyseFile(String filename, Set<IClusteringSourceAnalyser> analyser) {
        System.out.println("Analysing '" + filename);

        // add all analysers
        Set<IClusterSourceListener> listener = new HashSet<IClusterSourceListener> (analyser);

        // open the file
        IClusterSourceReader reader = new ClusteringFileReader(new File(filename));

        try {
            reader.readClustersIteratively(listener);

            // save the results
            for (IClusteringSourceAnalyser theAnalyser : analyser) {
                // create the new filename
                String resultFilePath = filename + theAnalyser.getFileEnding();

                // get the result
                String resultString = theAnalyser.getAnalysisResultString();

                // write the result
                FileWriter writer = new FileWriter(resultFilePath);
                writer.write(resultString);
                writer.close();

                System.out.println("  Result written to " + resultFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void listAnalyser() {
        for (AnalyserFactory.ANALYSERS a : AnalyserFactory.ANALYSERS.values()) {
            System.out.println(a.getName());
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(
                        "java -jar analysis-{VERSION}.jar",
                        "Analysis .clustering files and generates basic statistical analysis.\n",
                        CliOptions.getOptions(), "\n\n", true);
    }
}