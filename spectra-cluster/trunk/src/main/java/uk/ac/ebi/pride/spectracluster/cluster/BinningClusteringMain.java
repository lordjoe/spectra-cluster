package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.BinningClusteringMain
 * main to use BinningClusteringEngine - this is really a test main
 * User: Steve
 * Date: 7/5/13
 */
public class BinningClusteringMain {

    protected static void processFile(final File inputFile) {
        long start = System.currentTimeMillis();
        List<ISpectralCluster> clusters = ParserUtilities.readMGFClusters(inputFile);

        long end = System.currentTimeMillis();
        double seconds = ((end - start) / 1000);
        double min = seconds / 60;
        System.out.println("read " + inputFile + " with " + clusters.size() + " spectra in " + String.format("%10.3f", seconds).trim());

        if (clusters.size() == 0)     // nothing there
            return;
        if (clusters.size() == 1)    // no clustering to do
            return;
        start = System.currentTimeMillis();
        double minMZ = ClusterUtilities.minClusterMZ(clusters);
        double maxMZ = ClusterUtilities.maxClusterMZ(clusters);
        if (minMZ >= maxMZ) {    // all the same mz
            minMZ = ClusterUtilities.minClusterMZ(clusters);
            maxMZ = ClusterUtilities.maxClusterMZ(clusters);
            return;
        }
        IWideBinner binner = new LinearWideBinner((int) (maxMZ + 0.5), 1, (int) minMZ, true);
        IClusteringEngine binningEngine = new BinningClusteringEngine(binner);

        for (ISpectralCluster sc : clusters) {
            binningEngine.addClusters(sc);
        }
        SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();

        for (int i = 0; i < Defaults.INSTANCE.getDefaultNumberReclusteringPasses(); i++) {
            if (!binningEngine.processClusters()) {
                break;
            }
        }


        end = System.currentTimeMillis();
        seconds = ((end - start) / 1000);
        min = seconds / 60;
        System.out.println("clustered " + inputFile + " with " + clusters.size() + " spectra in " + String.format("%10.3f", seconds).trim());
    }


    protected static void processDirectory(final File pF) {
        final File[] files = pF.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile())
                    processFile(file); // todo better directory handling
            }
        }
    }


    protected static void usage() {
        System.out.println("Usage <mgf file or directory> ...");
    }


    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            File f = new File(arg);
            if (!f.exists())
                throw new IllegalArgumentException("File " + arg + " does not exist");
            if (f.isDirectory())
                processDirectory(f);
            else
                processFile(f);

        }
        long end = System.currentTimeMillis();
        int seconds = (int) ((end - start) / 1000);
        double min = seconds / 60;
        System.out.println("Processed in " + String.format("%10.2f", min) + " min");
    }


}
