package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

@SuppressWarnings("static-access")
public class CliOptions {

	public enum OPTIONS {
		HELP("help"),
        CLUSTER_FILE("cluster_file"),
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

		Option help = OptionBuilder
                .withDescription("print this help.")
                .create(OPTIONS.HELP.getValue());
		options.addOption(help);
	}

	public static Options getOptions() {
		return options;
	}
}
