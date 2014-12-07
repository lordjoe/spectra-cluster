package uk.ac.ebi.pride.spectral_library_builder.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

@SuppressWarnings("static-access")
public class CliOptions {

	public enum OPTIONS {
		HELP("help"),
        MIN_SIZE("min_size"),
        MIN_RATIO("min_ratio"),
        MIN_PROJECTS("min_projects"),
        MIN_MAX_SEQ_COUNT("min_max_sequence_count"),
        ONLY_CORR_ID_SPECTRA("only_correctly_identified_spectra"),
        VERBOSE("verbose"),
        OUTPUT("output"),
        FORMAT("format");

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
        Option output = OptionBuilder
                .hasArg()
                .withDescription("path of the outputfile.")
                .create(OPTIONS.OUTPUT.getValue());
        options.addOption(output);

        Option format = OptionBuilder
                .hasArg()
                .withArgName("file format")
                .withDescription("The output format to write the final spectra to. Multiple formats may be supplied simultaneously.")
                .create(OPTIONS.FORMAT.getValue());
        options.addOption(format);

        Option minSize = OptionBuilder
                .hasArg()
                .withDescription("minimum required cluster size.")
                .create(OPTIONS.MIN_SIZE.getValue());
        options.addOption(minSize);

        Option minProjects = OptionBuilder
                .hasArg()
                .withDescription("minimum required number of projects.")
                .create(OPTIONS.MIN_PROJECTS.getValue());
        options.addOption(minProjects);

        Option onlyCorrIdSpectra = OptionBuilder
                .withDescription("only use correctly identified spectra for consensus spectrum")
                .create(OPTIONS.ONLY_CORR_ID_SPECTRA.getValue());
        options.addOption(onlyCorrIdSpectra);

        Option minMaxSequCount = OptionBuilder
                .hasArg()
                .withDescription("minimum required maximum sequence count.")
                .create(OPTIONS.MIN_MAX_SEQ_COUNT.getValue());
        options.addOption(minMaxSequCount);

        Option minRatio = OptionBuilder
                .hasArg()
                .withDescription("minimum required ratio.")
                .create(OPTIONS.MIN_RATIO.getValue());
        options.addOption(minRatio);

        Option verbose = OptionBuilder
                .withDescription("print out more information.")
                .create(OPTIONS.VERBOSE.getValue());
        options.addOption(verbose);

		Option help = OptionBuilder
                .withDescription("print this help.")
                .create(OPTIONS.HELP.getValue());
		options.addOption(help);
	}

	public static Options getOptions() {
		return options;
	}
}
