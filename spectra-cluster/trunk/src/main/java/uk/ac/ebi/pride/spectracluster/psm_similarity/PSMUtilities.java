package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import org.systemsbiology.common.IFileSystem;
import org.systemsbiology.hadoop.HDFSAccessor;
import org.systemsbiology.remotecontrol.LocalMachineFileSystem;
import org.systemsbiology.remotecontrol.RemoteUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusteringHeader;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IClusterSet;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleClusterSet;
import uk.ac.ebi.pride.spectracluster.io.DotClusterClusterAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.psm_similarity.PSMUtilities
 * User: Steve
 * Date: 2/3/14
 */
public class PSMUtilities {

    public static final TypedPredicate<PSMSpectrum> IS_PSM_DECOY = new TypedPredicate<PSMSpectrum>() {
        @Override
        public boolean apply(@Nonnull final PSMSpectrum pPSM, final Object... otherdata) {
            return pPSM.isDecoy();
        }
    };

    public static final int MAX_PROCESSED_FILES_DEBUG_ONLY = Integer.MAX_VALUE; // 4;

    public static void appendFileClusters(Appendable out, String clusterDirectory, IFileSystem fs, TypedPredicate<String>... tests) {
        TypedVisitor<IPeptideSpectralCluster> visitor = new AppendClusterings(out);
        int numberProcessed = 0;
        if (fs.isDirectory(clusterDirectory)) {
            final String[] files = fs.ls(clusterDirectory);
            for (int i = 0; i < files.length; i++) {
                String file = files[i];


                boolean use = true;
                for (int j = 0; j < tests.length; j++) {
                    use &= tests[j].apply(file);
                }
                if (use) {
                    SimpleClusterSet cst = new SimpleClusterSet();
                    cst.setName(clusterDirectory);
                    readClusterSet(clusterDirectory + "/" + file, fs, cst);
                    System.out.println("Read File " + file);
                    if (numberProcessed++ > MAX_PROCESSED_FILES_DEBUG_ONLY)
                        break;
                    cst.visitClusters(visitor);
                }
            }
        } else {
            SimpleClusterSet cst = new SimpleClusterSet();
            cst.setName(clusterDirectory);
            readClusterSet(clusterDirectory, fs, cst);
            cst.visitClusters(visitor);
        }
    }

    public static class AppendClusterings implements TypedVisitor<IPeptideSpectralCluster> {
        private final Appendable out;

        public AppendClusterings(final Appendable pOut) {
            out = pOut;
        }

        public Appendable getOut() {
            return out;
        }

        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final IPeptideSpectralCluster sc) {
            new DotClusterClusterAppender().appendCluster(out, sc);
        }
    }

    private static AppendClusterings gClusterSaver;

    public static AppendClusterings getClusterSaver() {
        return gClusterSaver;
    }

    public static void startClusterSaver(final Appendable pOut) {
        gClusterSaver = new AppendClusterings(pOut);
    }

    public static void closeClusterSaver() {
        if (getClusterSaver() != null) {
            Appendable out = getClusterSaver().getOut();
            if (out instanceof PrintWriter)
                ((PrintWriter) out).close();
        }
    }


    public static void readClusterSet(String clusterDirectory, IFileSystem fs, SimpleClusterSet simpleClusterSet) {

        if (fs.isDirectory(clusterDirectory)) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
        } else {
            String contents = fs.readFromFileSystem(clusterDirectory);
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(contents));

            ClusteringHeader header = simpleClusterSet.getHeader();
            if (header == null) {
                header = ParserUtilities.readClusterHeader(lineNumberReader);
                simpleClusterSet.setHeader(header);
            }

            IPeptideSpectralCluster[] clusters = readClustersFromClusteringFile(lineNumberReader);
            simpleClusterSet.addClusters(Arrays.asList(clusters));

        }
    }


    public static IClusterSet readClusterSet(File file, String name) {
        SimpleClusterSet simpleClusterSet = new SimpleClusterSet();
        simpleClusterSet.setName(name);
        SpectrumToCluster stc = new SpectrumToCluster();

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return null;
            for (File file1 : files) {
                IClusterSet clusterSet = readClusterSet(file1, name);
                if (simpleClusterSet.getHeader() == null)
                    simpleClusterSet.setHeader(clusterSet.getHeader());

                simpleClusterSet.addClusters(clusterSet.getClusters());
            }
        } else if (file.getName().endsWith(".clustering")) {
            try {
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));

                ClusteringHeader header = simpleClusterSet.getHeader();
                if (header == null) {
                    header = ParserUtilities.readClusterHeader(lineNumberReader);
                    simpleClusterSet.setHeader(header);
                }

                IPeptideSpectralCluster[] clusters = readClustersFromClusteringFile(lineNumberReader);
                List<IPeptideSpectralCluster> clusters1 = Arrays.asList(clusters);
                stc.addClusters(clusters1);
                simpleClusterSet.addClusters(clusters1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (false) {
            int[] duplicatedOfSize = stc.getDuplicatedOfSize();
            for (int i = 1; i < duplicatedOfSize.length; i++) {
                int dups = duplicatedOfSize[i];
                //           System.out.println("duplicates " + i + " " + dups);
            }
            StringBuilder sb = new StringBuilder();
            stc.appendDuplicatedSpectra(sb);
        }

        return simpleClusterSet;
    }


    /**
     * Read clustering file into a set of clusters
     *
     * @param inp
     * @return
     */
    public static IPeptideSpectralCluster[] readClustersFromClusteringFile(LineNumberReader inp) {
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();

        try {
            String line = inp.readLine();
            while (line != null && !line.equals(ParserUtilities.BEGIN_CLUSTERING)) {
                line = inp.readLine();
            }

            List<String> clusterContent = new ArrayList<String>();
            while (line != null) {
                if (line.startsWith(ParserUtilities.BEGIN_CLUSTERING)) {
                    if (!clusterContent.isEmpty()) {
                        IPeptideSpectralCluster cluster = processIntoCluster(clusterContent);
                        if (cluster != null) {
                            holder.add(cluster);
                        }
                    }
                    clusterContent.clear();
                } else {
                    clusterContent.add(line);
                }

                line = inp.readLine();
            }

            if (!clusterContent.isEmpty()) {
                IPeptideSpectralCluster cluster = processIntoCluster(clusterContent);
                if (cluster != null) {
                    holder.add(cluster);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to read ", ioe);
        }

        IPeptideSpectralCluster[] ret = new IPeptideSpectralCluster[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected static IPeptideSpectralCluster processIntoCluster(List<String> clusterLines) {

        PSMSpectralCluster cluster = new PSMSpectralCluster();
        PSMSpectrum spectrum = null;
        String consensusMzLine = null;
        String consensusIntensityLine = null;
        for (String clusterLine : clusterLines) {
            if (clusterLine.length() == 0)
                break;
            if (clusterLine.startsWith("name=")) {
                break; // start of a new file
            }
            if (clusterLine.startsWith(ParserUtilities.AVERAGE_PRECURSOR_MZ)) {
                float precursorMz = Float.parseFloat(clusterLine.replace(ParserUtilities.AVERAGE_PRECURSOR_MZ, ""));
                cluster.setPrecursorMz(precursorMz);
            } else if (clusterLine.startsWith(ParserUtilities.CONSENSUS_MZ)) {
                consensusMzLine = clusterLine.replace(ParserUtilities.CONSENSUS_MZ, "");
            } else if (clusterLine.startsWith(ParserUtilities.CONSENSUS_INTENSITY)) {
                consensusIntensityLine = clusterLine.replace(ParserUtilities.CONSENSUS_INTENSITY, "");
            } else if (clusterLine.startsWith(ParserUtilities.PEPTIDE_SEQUENCE)) {
                String peptideSequence = clusterLine.replace(ParserUtilities.PEPTIDE_SEQUENCE, "");
                peptideSequence = peptideSequence.replace("[", "").replace("]", "");
                cluster.addPeptides(peptideSequence);
            } else if (clusterLine.startsWith(ParserUtilities.SPECTRUM_ID)) {
                String[] parts = clusterLine.split("\t");
                spectrum = PSMSpectrum.getSpectrum(parts[1]);
                cluster.addSpectra(spectrum);
            } else //noinspection StatementWithEmptyBody
                if (clusterLine.startsWith(ParserUtilities.AVERAGE_PRECURSOR_INTENSITY)) {
                    // do nothing here
                } else {
                    if (clusterLine.length() > 0) {
                        throw new IllegalArgumentException("cannot process line " + clusterLine);
                    }
                }
        }

        if (consensusIntensityLine == null)
            return null;

        List<IPeak> peaks = ParserUtilities.buildPeaks(consensusMzLine, consensusIntensityLine);
        if (peaks == null)
            return null;
        PeptideSpectrumMatch consensusSpectrum = new PeptideSpectrumMatch(null, null, 0, cluster.getPrecursorMz(), peaks);
        cluster.setConsensusSpectrum(consensusSpectrum);

        return cluster;
    }


    public static void usage() {
        System.out.println("Usage   <ClusteringFile or Directory> <outfile>");
    }


    private static void mergeWithHDFS(String[] args) throws IOException {

        String NAME_NODE = RemoteUtilities.getHost();
        int HDFS_PORT = RemoteUtilities.getPort();

        IFileSystem fs = HDFSAccessor.getFileSystem(NAME_NODE, HDFS_PORT);
        String inDir = args[0];
        if (!fs.exists(inDir) || !fs.isDirectory(inDir))
            throw new IllegalStateException("input directory does not exist " + inDir);

        String outFileStr = args[1];
        PrintWriter out = new PrintWriter(new FileWriter(outFileStr));
        TypedPredicate<String> test = new TypedPredicate<String>() {
            @Override
            public boolean apply(@Nonnull final String s, final Object... otherdata) {
                if (!s.endsWith(".clustering"))
                    return false;
                if (!s.startsWith("ClusteringBin"))
                    return false;
                return true;
            }
        };
        try {
            appendFileClusters(out, args[0], fs, test);
        } finally {
            out.close();
        }
    }

    private static void mergeWithFileSystem(String[] args) throws IOException {
        File inFiles = new File(args[0]);
        if (!inFiles.exists() || !inFiles.isDirectory())
            throw new IllegalStateException("problem"); // ToDo change

        IFileSystem fs = new LocalMachineFileSystem(inFiles);
        String outFileStr = args[1];
        PrintWriter out = new PrintWriter(new FileWriter(outFileStr));
        TypedPredicate<String> test = new TypedPredicate<String>() {
            @Override
            public boolean apply(@Nonnull final String s, final Object... otherdata) {
                if (!s.endsWith(".clustering"))
                    return false;
                if (!s.startsWith("ClusteringBin"))
                    return false;
                return true;
            }
        };
        try {
            appendFileClusters(out, args[0], fs, test);
        } finally {
            out.close();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }
        //      mergeWithFileSystem(args);
        mergeWithHDFS(args);

    }


}
