package uk.ac.ebi.pride.spectracluster.io;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.ClusterCreateListener;
import uk.ac.ebi.pride.spectracluster.util.Constants;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.io.DotClusterFileListener
 * User: Steve
 * Date: 9/23/13
 */
@Deprecated
public class DotClusterFileListener implements ClusterCreateListener {


    private final PrintWriter outWriter;
    private final File outFile;
    private final DotClusterClusterAppender appender = new DotClusterClusterAppender();

    /**
     * creat with an output file
     *
     * @param inp - !null creatable file
     */
    public DotClusterFileListener(File inp) {
        try {
            outFile = inp;
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            final PrintWriter printWriter = new PrintWriter(new FileWriter(inp));
            outWriter = printWriter;
        } catch (IOException e) {
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
    public void onClusterStarted(Object... otherData) {

        appender.appendDotClusterHeader(outWriter, outFile.getName());
    }


    /**
     * do something when a cluster is created or read
     *
     * @param cluster
     */
    @Override
    public void onClusterCreate(final IPeptideSpectralCluster cluster, Object... otherData) {
        appender.appendCluster(outWriter, cluster);
    }

    /**
     * do something when a cluster when the last cluster is read -
     * this may be after a file read is finished
     */
    @Override
    public void onClusterCreateFinished(Object... otherData) {
        outWriter.close();
    }

    /**
     * create ,clustering files from CGF files
     *
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
        } else {
            String name = pF.getName();
            if (name.toLowerCase().endsWith(Constants.CGF_EXTENSION)) {
                try {
                    LineNumberReader rdr = new LineNumberReader(new FileReader(pF));
                    String clusteringName = name.substring(0, name.length() - Constants.CGF_EXTENSION.length()) + Constants.CLUSTERING_EXTENSION;
                    File outFile = new File(pF.getParent(), clusteringName);
                    DotClusterFileListener lstnr = new DotClusterFileListener(outFile);
                    ParserUtilities.readAndProcessSpectralClusters(rdr, lstnr);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);

                }

            }

        }
    }


    protected static void usage() {
        System.out.println("usage <file1.cgf> <file2.cgf> ... or <directory holding cgf files>");
    }

    /**
     * create .cluster files for cgf files in arge or diectroried of cgf files
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            File f = new File(arg);
            creatClusteringFromCGF(f);
        }
    }


}
