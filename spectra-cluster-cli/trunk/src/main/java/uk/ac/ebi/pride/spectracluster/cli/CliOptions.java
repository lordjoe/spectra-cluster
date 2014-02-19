package uk.ac.ebi.pride.spectracluster.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 9/15/13
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class CliOptions {
    public enum OPTIONS {
        // VARIABLES
        CLUSTERING_ENGINE("clustering_engine"),
        CLUSTERING_ROUNDS("clustering_rounds"),
        // ACTIONS
        CLUSTER_FILE("cluster_file"),
        LIST_CLUSTERING_ENGINE("list_clustering_engines"),
        HELP("help");

        private String value;

        OPTIONS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final Options options = new Options();

    static {
        // VARIABLES
        Option clusteringEngine = OptionBuilder
                .hasArg()
                .withArgName("clustering engine")
                .withDescription("the clustering engine to use.")
                .create(OPTIONS.CLUSTERING_ENGINE.getValue());
        options.addOption(clusteringEngine);

        Option clusteringRounds = OptionBuilder
                .hasArg()
                .withArgName("clustering rounds")
                .withDescription("number of time the 'processClusters()' method is called.")
                .create(OPTIONS.CLUSTERING_ROUNDS.getValue());
        options.addOption(clusteringRounds);

        // ACTIONS
        Option clusterFile = OptionBuilder
                .hasArg()
                .withArgName("filename")
                .withDescription("clusters the specified MGF file using the selected clustering engine.")
                .create(OPTIONS.CLUSTER_FILE.getValue());
        options.addOption(clusterFile);

        Option listClusteringEngines = OptionBuilder
                .withDescription("list all available clustering engines.")
                .create(OPTIONS.LIST_CLUSTERING_ENGINE.getValue());
        options.addOption(listClusteringEngines);

        Option help = new Option(
                OPTIONS.HELP.toString(),
                "print this message.");
        options.addOption(help);

    }

    public static Options getOptions() {
        return options;
    }
}
