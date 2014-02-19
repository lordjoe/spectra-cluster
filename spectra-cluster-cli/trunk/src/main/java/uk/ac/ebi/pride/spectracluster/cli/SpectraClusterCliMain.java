package uk.ac.ebi.pride.spectracluster.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import uk.ac.ebi.pride.spectracluster.cluster.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 9/15/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpectraClusterCliMain {
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(), args);

            /**
             * VARIABLES
             */

            // CLUSTERING ENGINE
            CLUSTERING_ENGINES clusteringEngine = null;
            if (commandLine.hasOption(CliOptions.OPTIONS.CLUSTERING_ENGINE.getValue())) {
                String clusteringEngineName = commandLine.getOptionValue(CliOptions.OPTIONS.CLUSTERING_ENGINE.getValue());
                clusteringEngine = CLUSTERING_ENGINES.getClusteringEngineFromName(clusteringEngineName);
            }

            // CLUSTERING ROUNDS
            int clusteringRounds = 1;
            if (commandLine.hasOption(CliOptions.OPTIONS.CLUSTERING_ROUNDS.getValue())) {
                clusteringRounds = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.CLUSTERING_ROUNDS.getValue()));
            }

            /**
             * ACTIONS
             */

            // CLUSTER FILE
            if (commandLine.hasOption(CliOptions.OPTIONS.CLUSTER_FILE.getValue())) {
                String filename = commandLine.getOptionValue(CliOptions.OPTIONS.CLUSTER_FILE.getValue());
                clusterFile(filename, clusteringEngine, clusteringRounds);
            }

            // LIST CLUSTERING ENGINES
            if (commandLine.hasOption(CliOptions.OPTIONS.LIST_CLUSTERING_ENGINE.getValue())) {
                listAvailableClusteringEngines();
                return;
            }

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printUsage();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());

            System.exit(1);
        }
    }

    private static void clusterFile(String filename, CLUSTERING_ENGINES clusteringEngine, int clusteringRounds) throws Exception {
        if (clusteringEngine == null)
            throw new Exception("Missing required parameter clustering_engine");

        if (clusteringRounds < 1)
            throw new Exception("clustering_rounds must be greater or equal to 1");

        IClusteringEngine theClusteringEngine = clusteringEngine.getClusteringEngine();
        System.out.println("Clustering engine: " + clusteringEngine.getName());

        // load the MGF file
        System.out.print("Loading spectra from file...");
        File mgfFile = new File(filename);

        if (!mgfFile.exists())
            throw new Exception(filename + " does not exist.");
        if (!mgfFile.canRead())
            throw new Exception("Cannot read " + mgfFile);

        IPeptideSpectrumMatch[] mgfSpectra = ParserUtilities.readMGFScans(mgfFile);
        System.out.print("Done (" + mgfSpectra.length + " spectra loaded).\n");

        // cluster the file
        System.out.print("Adding clusters to clustering engine...");
        List<ISpectralCluster> spectraAsCluster = new ArrayList<ISpectralCluster>(mgfSpectra.length);
        for (IPeptideSpectrumMatch psm : mgfSpectra)
            spectraAsCluster.add(psm.asCluster());

        theClusteringEngine.addClusters(spectraAsCluster.toArray(new ISpectralCluster[spectraAsCluster.size()]));

        System.out.print("Done.\n");


        System.out.println("Processing clusters (" + clusteringRounds + " rounds)...");
        long start = System.currentTimeMillis();
        for (int currentRound = 0; currentRound < clusteringRounds; currentRound++) {
            theClusteringEngine.processClusters();
        }
        long duration = System.currentTimeMillis() - start;
        System.out.printf("Clustering done (%d msec).\n", duration);

        // write out the results
        saveClustersAsClusterings(theClusteringEngine.getClusters(), mgfFile, clusteringEngine);
    }

    /**
     * write clusters to a file in the default directory with the extension .cgf
     *
     * @param pClusters1 !null list of clusters
     * @param pInputFile !null input file
     */
    protected static void saveClustersAsClusterings(final List<ISpectralCluster> pClusters1, final File pInputFile, final CLUSTERING_ENGINES clusteringEngine) {

        if (pClusters1.size() == 0)
            return;
        String outName = pInputFile.getAbsolutePath() + ".clustering";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(outName));

            appendClusteringHeaders(out, clusteringEngine);

            for (ISpectralCluster iSpectralCluster : pClusters1) {
                appendClusterAsClustering(out, iSpectralCluster);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (out != null)
                out.close();
        }
    }

    public static void appendClusterAsClustering(Appendable out, ISpectralCluster cluster) {
        int indent = 0;

        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", cluster.getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");


            out.append("sequence=[" + ClusterUtilities.mostCommonPeptides(cluster.getClusteredSpectra()) + "]");
            out.append("\n");

            out.append("consensus_mz=" + ClusterUtilities.buildMZString(cluster.getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(cluster.getConsensusSpectrum()));
            out.append("\n");

            for (ISpectrum spec : cluster.getClusteredSpectra()) {
                out.append("SPEC\t");
                out.append(spec.getId());
                out.append("\ttrue\n");

            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * add the headers needed by a clustering file
     *
     * @param pOut !null output
     */
    protected static void appendClusteringHeaders(final PrintWriter pOut, CLUSTERING_ENGINES engine) {
        pOut.append("name=" + engine.getName() + "\n");
        final SimilarityChecker defaultSimilarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        pOut.append("similarity_method=" + defaultSimilarityChecker.getClass().getSimpleName() + "\n");
        pOut.append("threshold=" + defaultSimilarityChecker.getDefaultThreshold() + "\n");
        pOut.append("fdr=" + "0" + "\n"); // todo what is this?
        pOut.append("description=\n"); // todo fix output description
        pOut.append("\n");
    }

    private static void listAvailableClusteringEngines() {
        System.out.println("-- Available Clustering engines --");
        for (CLUSTERING_ENGINES availableEngine : CLUSTERING_ENGINES.values()) {
            System.out.println(availableEngine.getName() + "\t\t" + availableEngine.getDescription() + "\n");
        }
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Spectra Cluster - Clusterer",
                "Clusters the spectra found in an MGF file and writes the results in a text-based file.\n",
                CliOptions.getOptions(), "\n\n", true);
    }


}
