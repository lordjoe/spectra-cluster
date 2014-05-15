package uk.ac.ebi.pride.spectracluster.cluster;


import uk.ac.ebi.pride.spectracluster.engine.PeakMatchClusteringEngine;
import uk.ac.ebi.pride.spectracluster.io.CGFClusterAppender;
import uk.ac.ebi.pride.spectracluster.io.DotClusterClusterAppender;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.BinningClusteringMain
 * main to use BinningClusteringEngine - this is really a test main
 * User: Steve
 * Date: 7/5/13
 */
public class ClusteringEngineMain {

    private static String name = "PeakMatchClustering";
    private static String description = "Describe Me";


    private long readTimeMillisec;

    public ClusteringEngineMain() {
    }

    public long getReadTimeMillisec() {
        return readTimeMillisec;
    }

    public void addReadTimeMillisec(final long pReadTimeMillisec) {
        readTimeMillisec += pReadTimeMillisec;
    }

    /**
     * do the work of clustering one MGF file
     *
     * @param inputFile !null existing readable mgf file
     */
    protected void processFile(final File inputFile) {
        if (!inputFile.getName().toLowerCase().endsWith(".mgf"))
            return; // not an mgf

        long start = System.currentTimeMillis();
        List<ISpectralCluster> clusters = ParserUtilities.readMGFClusters(inputFile);

        /**
         * Add your favorite clustering engine here
         */
        PeakMatchClusteringEngine engine = new PeakMatchClusteringEngine();
        //     IClusteringEngine engine = new PRideC();
        long end = System.currentTimeMillis();
        final long readTIme = end - start;
        addReadTimeMillisec(readTIme);
        double seconds = (readTIme / 1000);
        double min = seconds / 60;
        System.out.println("read " + inputFile + " with " + clusters.size() + " spectra in " + String.format("%10.3f", seconds).trim());

        if (clusters.size() == 0)     // nothing there
            return;
        if (clusters.size() == 1)    // no clustering to do
            return;
        start = System.currentTimeMillis();

        for (ISpectralCluster sc : clusters) {
            engine.addClusters(sc);
        }
        SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();

        engine.processClusters(); // force pass 1 - then we can recluster
        List<ISpectralCluster> pass1 = engine.getClusters();     // look at results
        List<ISpectralCluster> pass2 = null;
        for (int i = 0; i < Defaults.INSTANCE.getDefaultNumberReclusteringPasses(); i++) {
            if (!engine.processClusters()) {
                break;
            }
            pass2 = engine.getClusters();  // look at results
        }

        List<ISpectralCluster> clusters1 = engine.getClusters();     // get results


        saveClusters(clusters1, inputFile);
        saveClustersAsClusterings(clusters1, inputFile);


        end = System.currentTimeMillis();
        seconds = ((end - start) / 1000);
        min = seconds / 60;
        System.out.println("clustered " + inputFile + " with " + clusters.size() + " spectra in " + String.format("%10.3f sec", seconds).trim());
    }

    /**
     * write clusters to a file in the default directory with the extension .cgf
     *
     * @param pClusters1 !null list of clusters
     * @param pInputFile !null input file
     */
    protected void saveClusters(final List<ISpectralCluster> pClusters1, final File pInputFile) {

        if (pClusters1.size() == 0)
            return;
        String outName = pInputFile.getName().replace(".mgf", "") + ".cgf";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(outName));
            final CGFClusterAppender clusterAppender = new CGFClusterAppender(new MGFSpectrumAppender());
            for (ISpectralCluster iSpectralCluster : pClusters1) {
                clusterAppender.appendCluster(out, iSpectralCluster);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * write clusters to a file in the default directory with the extension .cgf
     *
     * @param pClusters1 !null list of clusters
     * @param pInputFile !null input file
     */
    protected void saveClustersAsClusterings(final List<ISpectralCluster> pClusters1, final File pInputFile) {

        if (pClusters1.size() == 0)
            return;
        String outName = pInputFile.getName() + ".clustering";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(outName));

            appendClusteringHeaders(out);

            final DotClusterClusterAppender clusterAppender = new DotClusterClusterAppender();
            for (ISpectralCluster iSpectralCluster : pClusters1) {
                clusterAppender.appendCluster(out, iSpectralCluster);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * add the headers needed by a clustering file
     *
     * @param pOut !null output
     */
    protected void appendClusteringHeaders(final PrintWriter pOut) {
        pOut.append("name=" + name + "\n");
        final SimilarityChecker defaultSimilarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        pOut.append("similarity_method=" + defaultSimilarityChecker.getClass().getSimpleName() + "\n");
        pOut.append("threshold=" + defaultSimilarityChecker.getDefaultThreshold() + "\n");
        pOut.append("fdr=" + "-1" + "\n"); // todo what is this?
        pOut.append("description=" + description + "\n");
        Defaults.appendAnalysisParameters(pOut);
        pOut.append("\n");
    }

    /**
     * process every file in a directory containing mgf files
     *
     * @param pF !null existing directory
     */
    protected void processDirectory(final File pF) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        double min;
        final File[] files = pF.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile())
                    processFile(file); // todo better directory handling
                end = System.currentTimeMillis();
                int seconds = (int) ((end - start) / 1000);
                min = seconds / 60;
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


        ClusteringEngineMain mainClusterer = new ClusteringEngineMain();
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        double min = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            File f = new File(arg);
            if (!f.exists())
                throw new IllegalArgumentException("File " + arg + " does not exist");
            if (f.isDirectory())
                mainClusterer.processDirectory(f);
            else
                mainClusterer.processFile(f);

            end = System.currentTimeMillis();
            int seconds = (int) ((end - start) / 1000);
            min = seconds / 60;
        }
        double readMin = mainClusterer.getReadTimeMillisec() / (60 * 1000);
        System.out.println("read in " + String.format("%10.2f", readMin) + " min");
        System.out.println("Processed in " + String.format("%10.2f", min - readMin) + " min");
        System.out.println("Total " + String.format("%10.2f", min) + " min");
    }


}
