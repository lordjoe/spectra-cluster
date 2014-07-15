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
        Option listAnalysers = OptionBuilder
                .withDescription("List all available analysers.")
                .create(OPTIONS.LIST_ANALYSERS.getValue());
        options.addOption(listAnalysers);

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

		// ACTIONS
		Option help = OptionBuilder
                .withDescription("print this help.")
                .create(OPTIONS.HELP.getValue());
		options.addOption(help);
	}

	public static Options getOptions() {
		return options;
	}
}
