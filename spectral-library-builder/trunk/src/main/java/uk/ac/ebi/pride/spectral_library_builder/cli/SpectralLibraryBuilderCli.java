package uk.ac.ebi.pride.spectral_library_builder.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrum;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectral_library_builder.converter.IConsensusSpectrumConverter;
import uk.ac.ebi.pride.spectral_library_builder.converter.MspConsensusSpectrumConverter;
import uk.ac.ebi.pride.spectracluster.filter.IPeakFilter;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectral_library_builder.util.ParameterExtractor;
import uk.ac.ebi.pride.spectral_library_builder.util.SpectrumConverter;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jg on 04.12.14.
 */
public class SpectralLibraryBuilderCli {
    private static int minSize = Integer.MIN_VALUE;
    private static float minRatio = Float.MIN_VALUE;
    private static int minNumberProjects = Integer.MIN_VALUE;
    private static int minMaximumSequenceCount = Integer.MIN_VALUE;
    private static boolean onlyCorrectlyIdentifiedSpectra = false;
    private static boolean verbose = false;

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(),
                    args);

            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printHelp();
                return;
            }

            if (!commandLine.hasOption(CliOptions.OPTIONS.OUTPUT.getValue())) {
                throw new Exception("Missing required parameter " + CliOptions.OPTIONS.OUTPUT.getValue());
            }

            String outputFilePath = commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT.getValue());

            String[] fileFormats = commandLine.getOptionValues(CliOptions.OPTIONS.FORMAT.getValue());
            if (fileFormats == null) {
                fileFormats = new String[] {"msp"};
            }

            // set the options
            minSize = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.MIN_SIZE.getValue(), "0"));
            minRatio = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.MIN_RATIO.getValue(), "0"));
            minNumberProjects = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.MIN_PROJECTS.getValue(), "0"));
            minMaximumSequenceCount = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.MIN_MAX_SEQ_COUNT.getValue(), "0"));
            onlyCorrectlyIdentifiedSpectra = commandLine.hasOption(CliOptions.OPTIONS.ONLY_CORR_ID_SPECTRA.getValue());
            verbose = commandLine.hasOption(CliOptions.OPTIONS.VERBOSE.getValue());

            // create one output file per file format
            FileWriter[] fw = new FileWriter[fileFormats.length];

            for (int i = 0; i < fileFormats.length; i++) {
                if (fileFormats[i].toLowerCase().equals("msp")) {
                    fw[i] = new FileWriter(outputFilePath + ".msp");
                }
                else {
                    fw[i] = null;
                }
            }

            // process the input files
            for (String inputFilename : commandLine.getArgs()) {
                String[] convertedSpectrum = processInputFile(inputFilename, fileFormats);

                for (int i = 0; i < fileFormats.length; i++) {
                    if (fw[i] != null) {
                        fw[i].write(convertedSpectrum[i]);
                    }
                }
            }

            // close all files
            for (FileWriter currentFw : fw) {
                if (currentFw != null)
                    currentFw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected static String[] processInputFile(String inputFilename, String[] fileFormats) throws Exception {
        MgfFile mgfFile = new MgfFile(new File(inputFilename));
        List<ISpectrum> spectra = new ArrayList<ISpectrum>(mgfFile.getMs2QueryCount());
        Iterator<Spectrum> it = mgfFile.getSpectrumIterator();

        while ( it.hasNext()) {
            Spectrum spectrum = it.next();
            spectra.add(SpectrumConverter.asClusteringSpectrum(spectrum));
        }

        // filter input spectra and build consensus spectra
        ParameterExtractor parameterExtractor = new ParameterExtractor(spectra);

        if (verbose) {
            System.out.println("Processing " + inputFilename + "...");
            System.out.println("  Spectra: " + parameterExtractor.getNumberOfSpectra());
            System.out.println("  Projects: " + parameterExtractor.getNumberOfProjects());
            System.out.println("  Assays: " + parameterExtractor.getAssays().size());
            System.out.println("  Maximum sequence: " + parameterExtractor.getMaximumSequence());
            System.out.println("  Max. Count: " + parameterExtractor.getMaximumSequenceCount());
            System.out.println("  Max. Ratio: " + parameterExtractor.getMaxPsmRatio());
        }

        if (parameterExtractor.getNumberOfSpectra() < minSize)
           return null;

        if (parameterExtractor.getMaximumSequenceCount() < minMaximumSequenceCount)
            return null;

        if (parameterExtractor.getNumberOfProjects() < minNumberProjects)
            return null;

        if ((float) parameterExtractor.getMaximumSequenceCount() / parameterExtractor.getNumberOfSpectra() < minRatio)
            return null;

        // build the list for the spectra to be included in the consensus spectrum
        IConsensusSpectrumBuilder consensusSpectrum = ConsensusSpectrum.buildFactory(IPeakFilter.NULL_FILTER).getConsensusSpectrumBuilder();

        for (ISpectrum spectrum : spectra) {
            // only use spectra from "correct" PSM
            if (onlyCorrectlyIdentifiedSpectra &&
                    !SpectrumConverter.isSpectrumIdentifiedAsSequence(spectrum, parameterExtractor.getMaximumSequence())) {
                continue;
            }

            consensusSpectrum.addSpectra(spectrum);
        }

        if (consensusSpectrum.getSpectraCount() < 1)
            return null;

        // return the consensus spectrum in the required formats
        String[] convertedSpectrum = new String[fileFormats.length];
        int index = 0;

        for (String fileFormat : fileFormats) {
            if (fileFormat.toLowerCase().equals("msp")) {
                IConsensusSpectrumConverter converter = new MspConsensusSpectrumConverter();
                convertedSpectrum[index] = converter.convertConsensusSpectrum(consensusSpectrum.getConsensusSpectrum(), parameterExtractor);
            }

            index++;
        }

        return convertedSpectrum;
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(
                        "java -jar {VERSION}.jar [OPTIONS] [MGF files]",
                        "Writes the consensus spectra generated from the mgf files into the defined output file. The " +
                        "spectra of one MGF file will be converted into one consensus spectrum.",
                        CliOptions.getOptions(), "\n\n", true);
    }
}
