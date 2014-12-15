package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;

/**
 * Created by jg on 02.12.14.
 */
public class ClusteringFileClusterFetcherCli {
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(),
                    args);

            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printHelp();
                return;
            }

            String clusterListPath = commandLine.getOptionValue(CliOptions.OPTIONS.CLUSTER_FILE.getValue(), "");

            String outputPath = commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue(), "");
            if (outputPath.equals("")) {
                throw new Exception("Missing required parameter '" + CliOptions.OPTIONS.OUTPUT_PATH.getValue() + "'");
            }

            String clusterIdFilePath = commandLine.getOptionValue(CliOptions.OPTIONS.CLUTER_ID_FILE.getValue());

            boolean useOnlyFileFetcher = commandLine.hasOption(CliOptions.OPTIONS.DISABLE_SPECTRA_RETRIEVER.getValue());
            boolean ignoreIncompleteClusters = commandLine.hasOption(CliOptions.OPTIONS.IGNORE_INCOMPLETE_CLUSTER.getValue());
            boolean ignoreExisting = commandLine.hasOption(CliOptions.OPTIONS.IGNORE_EXISSTING.getValue());

            int minSize = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.MIN_SIZE.getValue(), "0"));
            int maxSize = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.MAX_SIZE.getValue(), new Integer(Integer.MAX_VALUE).toString()));

            float minRatio = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.MIN_RATIO.getValue(), "0"));
            float maxRatio = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.MAX_RATIO.getValue(), new Float(Float.MAX_VALUE).toString()));

            IClusterProcessor clusterProcessor;

            if (clusterListPath != "") {
                clusterProcessor = new ClusterFilelistProcessor(clusterListPath);
            }
            else {
                clusterProcessor = new ClusteringFilesProcessor(commandLine.getArgs(), minSize, maxSize, minRatio, maxRatio, clusterIdFilePath);
            }

            clusterProcessor.setOutputPath(outputPath);
            clusterProcessor.setDisableSpectraFetcher(useOnlyFileFetcher);
            clusterProcessor.setIgnoreIncompleteClusters(ignoreIncompleteClusters);
            clusterProcessor.setIgnoreExisting(ignoreExisting);

            clusterProcessor.processClusters();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }







    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(
                        "java -jar {VERSION}.jar",
                        "Writes the spectra of a specified cluster to an MGF file.",
                        CliOptions.getOptions(), "\n\n", true);
    }
}
