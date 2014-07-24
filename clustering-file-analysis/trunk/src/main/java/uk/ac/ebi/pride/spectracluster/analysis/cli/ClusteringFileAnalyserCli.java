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

            // CUMULATIVE
            boolean cumulativeAnalysis = commandLine.hasOption(CliOptions.OPTIONS.CUMULATIVE_ANALYSIS.getValue());

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

            // OUTPUT PATH
            String outputPath = null;
            if (commandLine.hasOption(CliOptions.OPTIONS.OUTPUT_PATH.getValue())) {
                outputPath = commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue());
            }

            // RUN THE ANALYSIS ON ALL PASSED FILES
            if (cumulativeAnalysis) {
                analyseFilesCummulatively(commandLine.getArgs(), analyser, outputPath);
            }
            else {
                for (String filename : commandLine.getArgs()) {
                    analyseFile(filename, analyser, outputPath);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyseFilesCummulatively(String[] filenames, Set<IClusteringSourceAnalyser> analyser, String outputPath) throws Exception {
        if (outputPath == null)
            throw new Exception("output_path must be set.");

        File outputFilename = new File(outputPath);

        Set<IClusterSourceListener> listener = new HashSet<IClusterSourceListener>(analyser);

        for (String filename : filenames) {
            System.out.println("Processing " + filename);
            IClusterSourceReader reader = new ClusteringFileReader(new File(filename));
            reader.readClustersIteratively(listener);
        }

        // save the results
        for (IClusteringSourceAnalyser theAnalyser : analyser) {
            // get the result
            String resultString = theAnalyser.getAnalysisResultString();

            // write the result
            FileWriter writer = new FileWriter(outputFilename + theAnalyser.getFileEnding());
            writer.write(resultString);
            writer.close();

            System.out.println("  Result written to " + outputFilename + theAnalyser.getFileEnding());
        }
    }

    private static void analyseFile(String filename, Set<IClusteringSourceAnalyser> analyser, String outputPath) {
        try {
            System.out.println("Analysing '" + filename);
            File fileToAnalyse = new File(filename);

            String resultFilePath = filename;
            if (outputPath != null && outputPath.length() > 0) {
                File outputDirectory = new File(outputPath);
                if (outputDirectory.isDirectory())
                    resultFilePath = outputDirectory.getPath() + File.separator + fileToAnalyse.getName();
                else
                    throw new IllegalStateException(outputPath + " is not a directory.");
            }

            // add all analysers
            Set<IClusterSourceListener> listener = new HashSet<IClusterSourceListener> (analyser);

            // open the file
            IClusterSourceReader reader = new ClusteringFileReader(fileToAnalyse);

            reader.readClustersIteratively(listener);

            // save the results
            for (IClusteringSourceAnalyser theAnalyser : analyser) {
                // get the result
                String resultString = theAnalyser.getAnalysisResultString();

                // write the result
                FileWriter writer = new FileWriter(resultFilePath + theAnalyser.getFileEnding());
                writer.write(resultString);
                writer.close();

                System.out.println("  Result written to " + resultFilePath + theAnalyser.getFileEnding());
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