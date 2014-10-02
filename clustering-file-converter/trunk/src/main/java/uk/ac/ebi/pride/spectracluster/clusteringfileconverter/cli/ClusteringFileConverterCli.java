package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters.ConverterFactory;
import uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters.IClusterConverter;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jg on 01.08.14.
 */
public class ClusteringFileConverterCli {
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
                    commandLine.getOptionValue(CliOptions.OPTIONS.MIN_SIZE.getValue(), "0")
            );

            int maxSize = Integer.parseInt(
                    commandLine.getOptionValue(CliOptions.OPTIONS.MAX_SIZE.getValue(), Integer.toString(Integer.MAX_VALUE))
            );

            float minRatio = Float.parseFloat(
                    commandLine.getOptionValue(CliOptions.OPTIONS.MIN_RATIO.getValue(), "0")
            );

            float maxRatio = Float.parseFloat(
                    commandLine.getOptionValue(CliOptions.OPTIONS.MAX_RATIO.getValue(), "1")
            );

            String species = commandLine.getOptionValue(CliOptions.OPTIONS.SPECIES.getValue(), null);

            boolean combineResults = commandLine.hasOption(CliOptions.OPTIONS.COMBINE.getValue());

            if (!commandLine.hasOption(CliOptions.OPTIONS.OUTPUT_PATH.getValue()))
                throw new Exception("Missing required parameter " + CliOptions.OPTIONS.OUTPUT_PATH.getValue());
            String outputPath = commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue());
            System.out.println("Outputpath = " + outputPath);

            String[] formats = commandLine.getOptionValues(CliOptions.OPTIONS.FORMAT.getValue());

            // process the files
            if (combineResults) {
                convertClusteringFilesCombined(commandLine.getArgs(), outputPath, minSize, maxSize, minRatio, maxRatio, species, formats);
            }
            else {
                // process each file separately
                for (String inputFilename : commandLine.getArgs()) {
                    convertCluteringFile(inputFilename, outputPath, minSize, maxSize, minRatio, maxRatio, species, formats);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void convertClusteringFilesCombined(String[] inputFilenames, String outputPathString, int minSize, int maxSize, float minRatio, float maxRatio, String species, String[] formats) throws Exception{
        File outputPath = new File(outputPathString);

        // get all converters
        List<IClusterConverter> converters = new ArrayList<IClusterConverter>(formats.length);
        for (String format : formats) {
            IClusterConverter converter = ConverterFactory.getConverter(format);

            converter.setMinSize(minSize);
            converter.setMaxSize(maxSize);
            converter.setMinRatio(minRatio);
            converter.setMaxRatio(maxRatio);
            converter.setSpecies(species);

            converter.setOutputPath(outputPath.getPath() + "." + converter.getFiletypeExtension());
            converter.setAppend(true);

            converters.add(converter);
        }

        //  create the list of listeners
        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(converters);

        // process all files
        for (String inputFilename : inputFilenames) {
            System.out.println("Processing " + inputFilename);
            IClusterSourceReader reader = new ClusteringFileReader(new File(inputFilename));
            reader.readClustersIteratively(listeners);
        }

        // close all converters
        for (IClusterConverter c : converters) {
            c.close();
        }

        for (IClusterConverter c : converters) {
            System.out.println("Result written to " + c.getOuputPath());
        }
    }

    private static void convertCluteringFile(String inputFilename, String outputPathString, int minSize, int maxSize, float minRatio, float maxRatio, String species, String[] formats) throws Exception {
        System.out.println("Converting " + inputFilename + "\n");

        File outputPath = new File(outputPathString);

        // get all converters
        List<IClusterConverter> converters = new ArrayList<IClusterConverter>(formats.length);
        for (String format : formats) {
            IClusterConverter converter = ConverterFactory.getConverter(format);

            converter.setMinSize(minSize);
            converter.setMaxSize(maxSize);
            converter.setMinRatio(minRatio);
            converter.setMaxRatio(maxRatio);
            converter.setSpecies(species);

            converter.setOutputPath(outputPath.getPath() + "." + converter.getFiletypeExtension());

            converters.add(converter);
        }

        //  create the list of listeners
        List<IClusterSourceListener> listeners = new ArrayList<IClusterSourceListener>(converters);

        // process the file
        IClusterSourceReader reader = new ClusteringFileReader(new File(inputFilename));
        reader.readClustersIteratively(listeners);

        // close all files
        for (IClusterConverter c : converters) {
            c.close();
        }

        for (IClusterConverter c : converters) {
            System.out.println("Result written to " + c.getOuputPath());
        }
    }

    private static void printHelp() {
        StringBuilder supportedFormats = new StringBuilder();
        for (String name : ConverterFactory.CONVERTER.getAllNames())
            supportedFormats.append("\t").append(name).append("\n");

        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(
                        "java -jar {VERSION}.jar",
                        "Converts .clustering files into other formats.\nSupported formats:\n" +
                                supportedFormats.toString(),
                        CliOptions.getOptions(), "\n\n", true);
    }
}
