package uk.ac.ebi.pride.spectracluster.psmassessmentextractor.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.psmassessmentextractor.assessment.DefaultReliableRequirements;
import uk.ac.ebi.pride.spectracluster.psmassessmentextractor.assessment.PsmAssessmentExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jg on 09.09.14.
 */
public class PsmAssessmentExtractorCli {
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(),
                    args);

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printHelp();
                return;
            }

            int minSize = Integer.parseInt(
                    commandLine.getOptionValue(CliOptions.OPTIONS.MIN_SIZE.getValue(), String.format("%d", DefaultReliableRequirements.MIN_RELIABLE_CLUSTER_SIZE))
            );

            float minRatio = Float.parseFloat(
                    commandLine.getOptionValue(CliOptions.OPTIONS.MIN_RATIO.getValue(), String.format("%f", DefaultReliableRequirements.MIN_RELIABLE_SEQUENCE_RATIO))
            );

            if (!commandLine.hasOption(CliOptions.OPTIONS.OUTPUT_PATH.getValue()))
                throw new Exception("Missing required parameter " + CliOptions.OPTIONS.OUTPUT_PATH.getValue());
            String outputPath = commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue());

            extractPsmAssessments(commandLine.getArgs(), minSize, minRatio, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void extractPsmAssessments(String[] inputFilenames, int minSize, float minRatio, String outputPath) throws Exception {
        System.out.println("Extracting PSM assessments (min cluster size = " + minSize + ", min sequence ratio = " + minRatio + ")");

        // create the assessment extractor
        PsmAssessmentExtractor assessmentExtractor = new PsmAssessmentExtractor();
        assessmentExtractor.setMinSequenceRatio(minRatio);
        assessmentExtractor.setMinClusterSize(minSize);
        List<IClusterSourceListener> listener = new ArrayList<IClusterSourceListener>(1);
        listener.add(assessmentExtractor);

        // process the clustering files
        for (String clusteringFilename : inputFilenames) {
            System.out.println("  Processing " + clusteringFilename + "...");

            ClusteringFileReader reader = new ClusteringFileReader(new File(clusteringFilename));
            reader.readClustersIteratively(listener);
        }

        // write the results
        Map<String, Integer> psmAssessments = assessmentExtractor.getPsmAssessments();

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        // write the header
        writer.write("psm_id\tassessment\n");

        for (String psmId : psmAssessments.keySet()) {
            writer.write(String.format("%s\t%d\n", psmId, psmAssessments.get(psmId)));
        }

        writer.close();

        System.out.println("Results written to " + outputPath + "\n");
    }

    private static void printHelp() {

        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(
                        "java -jar {VERSION}.jar",
                        "Extracts PSM assessments from the passed .clustering files. The result of all " +
                        "files is written into a single output file.\n\n" +
                        "Assessment scores are:\n  1 - ratio < MIN_RELIABLE_RATIO\n  2 - size < MIN_RELIABLE_CLUTER_SIZE\n  3 - reliable identification\n\n",
                        CliOptions.getOptions(), "\n\n", true);
    }
}
