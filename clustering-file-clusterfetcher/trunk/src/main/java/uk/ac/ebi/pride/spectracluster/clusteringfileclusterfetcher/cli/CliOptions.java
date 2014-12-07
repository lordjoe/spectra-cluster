package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

@SuppressWarnings("static-access")
public class CliOptions {

	public enum OPTIONS {
		HELP("help"),
        CLUSTER_FILE("cluster_file"),
        DISABLE_SPECTRA_RETRIEVER("disable_spectra_retriever"),
        IGNORE_INCOMPLETE_CLUSTER("ignore_incomplete_cluster"),
        MIN_SIZE("min_size"),
        MAX_SIZE("max_size"),
        MIN_RATIO("min_ratio"),
        MAX_RATIO("max_ratio"),
        OUTPUT_PATH("output_path");

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
        Option clusterFile = OptionBuilder
                .withDescription("path to a tab-separated file containing the .clustering's filename as the first column and the cluster's id that should be exported as the second column.")
                .hasArg()
                .create(OPTIONS.CLUSTER_FILE.getValue());
        options.addOption(clusterFile);

        Option outputPath = OptionBuilder
                .withDescription("path to the output file.")
                .hasArg()
                .create(OPTIONS.OUTPUT_PATH.getValue());
        options.addOption(outputPath);

        Option ignoreIncompleteClusters = OptionBuilder
                .withDescription("set to ignore incomplete clusters. Otherwise, these will be written out as well")
                .create(OPTIONS.IGNORE_INCOMPLETE_CLUSTER.getValue());
        options.addOption(ignoreIncompleteClusters);

        Option disableSpectraRetriever = OptionBuilder
                .withDescription("disables the use of the spectra-retriever. All spectra will be read from file.")
                .create(OPTIONS.DISABLE_SPECTRA_RETRIEVER.getValue());
        options.addOption(disableSpectraRetriever);

        Option minSize = OptionBuilder
                .withDescription("minimum size of a cluster to be processed")
                .hasArg()
                .withArgName("SIZE")
                .create(OPTIONS.MIN_SIZE.getValue());
        options.addOption(minSize);

        Option maxSize = OptionBuilder
                .withDescription("maximum size of a cluster to be processed")
                .hasArg()
                .withArgName("SIZE")
                .create(OPTIONS.MAX_SIZE.getValue());
        options.addOption(maxSize);

        Option minRatio = OptionBuilder
                .withDescription("minimum cluster ration of a cluster to be processed")
                .hasArg()
                .withArgName("RATIO")
                .create(OPTIONS.MIN_RATIO.getValue());
        options.addOption(minRatio);

        Option maxRatio = OptionBuilder
                .withDescription("maximum cluster ratio of a cluster to be processed")
                .hasArg()
                .withArgName("RATIO")
                .create(OPTIONS.MAX_RATIO.getValue());
        options.addOption(maxRatio);

		Option help = OptionBuilder
                .withDescription("print this help.")
                .create(OPTIONS.HELP.getValue());
		options.addOption(help);
	}

	public static Options getOptions() {
		return options;
	}
}
