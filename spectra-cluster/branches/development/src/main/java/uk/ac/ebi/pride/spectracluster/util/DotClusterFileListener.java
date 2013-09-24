package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.DotClusterFileListener
 * User: Steve
 * Date: 9/23/13
 */
public class DotClusterFileListener implements ClusterCreateListener {

    public static final String CLUSTERING_EXTENSION = ".clustering";
    public static final String CGF_EXTENSION = ".cgf";


    private final PrintWriter m_OutWriter;
    private final File m_OutFile;

    /**
     * creat with an output file
     *
     * @param inp - !null creatable file
     */
    public DotClusterFileListener(File inp) {
        try {
            m_OutFile = inp;
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            final PrintWriter printWriter = new PrintWriter(new FileWriter(inp));
            m_OutWriter = printWriter;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * creat with an output file
     *
     * @param inp - !null creatable filename
     */
    @SuppressWarnings("UnusedDeclaration")
    public DotClusterFileListener(String inp) {
        this(new File(inp));
    }

    /**
     * initialize reading - if reading happens once - sayt from
     * one file all this may happen in the constructor
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void onClusterStarted() {

        String name = m_OutFile.getName();
        if (name.endsWith(CLUSTERING_EXTENSION))
            name = name.substring(0, name.length() - CLUSTERING_EXTENSION.length());
        m_OutWriter.append("name=" + name);
        m_OutWriter.append("\n");
        Defaults defaults = Defaults.INSTANCE;
        SimilarityChecker similarityChecker = defaults.getDefaultSimilarityChecker();

        Class<? extends SimilarityChecker> scc = similarityChecker.getClass();
        m_OutWriter.append("similarity_method=" + scc.getSimpleName());
        m_OutWriter.append("\n");


        double defaultSimilarityThreshold = FrankEtAlDotProduct.DEFAULT_SIMILARITY_THRESHOLD;
        if (similarityChecker instanceof FrankEtAlDotProduct) {
            //noinspection RedundantCast
            defaultSimilarityThreshold = ((FrankEtAlDotProduct) similarityChecker).getDefaultThreshold();
        }
        m_OutWriter.append("threshold=" + defaultSimilarityThreshold);
        m_OutWriter.append("\n");
        m_OutWriter.append("fdr=0");
        m_OutWriter.append("\n");
        m_OutWriter.append("description=" + name);
        m_OutWriter.append("\n");
        m_OutWriter.append("\n");
    }

    /**
     * do something when a cluster is created or read
     *
     * @param cluster
     */
    @Override
    public void onClusterCreate(final ISpectralCluster cluster) {
        cluster.appendClustering(m_OutWriter);
    }

    /**
     * do something when a cluster when the last cluster is read -
     * this may be after a file read is finished
     */
    @Override
    public void onClusterCreateFinished() {
        m_OutWriter.close();
    }

    /**
     * create ,clustering files from CGF files
     * @param pF !null existing file or directory with cgf files
     */
    public static void creatClusteringFromCGF(final File pF) {
        if (!pF.exists())
            return;
        if (pF.isDirectory()) {
            File[] subfiles = pF.listFiles();
            if (subfiles != null) {
                for (File subfile : subfiles) {
                    creatClusteringFromCGF(subfile);
                }
            }
        }
        else {
            String name = pF.getName();
            if (name.toLowerCase().endsWith(CGF_EXTENSION)) {
                try {
                    LineNumberReader rdr = new LineNumberReader(new FileReader(pF));
                    String clusteringName = name.substring(0,name.length() -  CGF_EXTENSION.length()) + CLUSTERING_EXTENSION;
                    File outFile = new File(pF.getParent(),clusteringName);
                    DotClusterFileListener lstnr = new DotClusterFileListener(outFile);
                    ParserUtilities.readAndProcessSpectralClusters(rdr, lstnr);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);

                }

            }

        }
    }

    public static void main(String[] args) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            File f = new File(arg);
            creatClusteringFromCGF(f);
        }
    }


}
