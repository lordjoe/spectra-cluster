package uk.ac.ebi.pride.spectracluster.psmassessmentextractor.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import uk.ac.ebi.pride.spectracluster.psmassessmentextractor.assessment.DefaultReliableRequirements;

@SuppressWarnings("static-access")
public class CliOptions {

	public enum OPTIONS {
		HELP("help"),
        MIN_RATIO("min_ratio"),
        MIN_SIZE("min_size"),
        COMBINE("combine"),
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
        Option minRatio = OptionBuilder
                .withDescription("The minimum ratio required to define a PSM as reliable (default = " + DefaultReliableRequirements.MIN_RELIABLE_SEQUENCE_RATIO + ").")
                .withArgName("RATIO")
                .withType(Float.class)
                .hasArg()
                .create(OPTIONS.MIN_RATIO.getValue());
        options.addOption(minRatio);

        Option minSize = OptionBuilder
                .withDescription("The minimum cluster size to define a PSM as reliable (default = " + DefaultReliableRequirements.MIN_RELIABLE_CLUSTER_SIZE + ").")
                .hasArg()
                .withArgName("SIZE")
                .withType(Integer.class)
                .create(OPTIONS.MIN_SIZE.getValue());
        options.addOption(minSize);
        Option outputPath = OptionBuilder
                .withDescription("Path to write the resulting tsv file to.")
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
