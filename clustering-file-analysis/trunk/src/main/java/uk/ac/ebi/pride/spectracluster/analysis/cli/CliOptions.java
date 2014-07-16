package uk.ac.ebi.pride.spectracluster.analysis.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

@SuppressWarnings("static-access")
public class CliOptions {

	public enum OPTIONS {
		LIST_ANALYSERS("list_analysers"),
        ANALYSER("analyser"),
        ALL_ANALYSERS("all_analysers"),
        OUTPUT_PATH("output_path"),
        CUMULATIVE_ANALYSIS("cumulative_analysis"),
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
        Option cumulative = OptionBuilder
                .withDescription("if set the analysis is not performed for each passed file but for all files together. In this case output_path MUST be set.")
                .create(OPTIONS.CUMULATIVE_ANALYSIS.getValue());
        options.addOption(cumulative);

        Option analyser = OptionBuilder
                .hasArg()
                .withArgName("ANALYSER")
                .withDescription("enables the defined analyser to run on the file.")
                .create(OPTIONS.ANALYSER.getValue());
        options.addOption(analyser);

        Option allAnalysers = OptionBuilder
                .withDescription("enables all available analysers.")
                .create(OPTIONS.ALL_ANALYSERS.getValue());
        options.addOption(allAnalysers);

        Option outputPath = OptionBuilder
                .hasArg()
                .withArgName("PATH")
                .withDescription("if specified the result files will be written to this path instead of the original files directory.")
                .create(OPTIONS.OUTPUT_PATH.getValue());
        options.addOption(outputPath);

        Option listAnalysers = OptionBuilder
                .withDescription("List all available analysers.")
                .create(OPTIONS.LIST_ANALYSERS.getValue());
        options.addOption(listAnalysers);

		Option help = OptionBuilder
                .withDescription("print this help.")
                .create(OPTIONS.HELP.getValue());
		options.addOption(help);
	}

	public static Options getOptions() {
		return options;
	}
}
