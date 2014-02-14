package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.utilities.*;
import org.systemsbiology.common.*;
import org.systemsbiology.remotecontrol.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

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


    public static void appendFileClusters(Appendable out, String clusterDirectory, IFileSystem fs, TypedPredicate<String>... tests) {
        SimpleClusterSet cst = new SimpleClusterSet();
        TypedVisitor<ISpectralCluster> visitor = new AppendClusterings(out);
        cst.setName(clusterDirectory);
        if (fs.isDirectory(clusterDirectory)) {
            final String[] files = fs.ls(clusterDirectory);
            for (String file : files) {
                boolean use = true;
                for (int j = 0; j < tests.length; j++) {
                    use &= tests[j].apply(file);
                }
                if (use)
                    readClusterSet(clusterDirectory + "/" + file, fs, cst);
            }
        }
        else {
            readClusterSet(clusterDirectory, fs, cst);
        }
        cst.visitClusters(visitor);
    }

    public static class AppendClusterings implements TypedVisitor<ISpectralCluster> {
        private final Appendable out;

        public AppendClusterings(final Appendable pOut) {
            out = pOut;
        }


        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final ISpectralCluster sc) {
            sc.appendClustering(out);
        }
    }

    public static void readClusterSet(String clusterDirectory, IFileSystem fs, SimpleClusterSet simpleClusterSet) {

        if (fs.isDirectory(clusterDirectory)) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
        }
        else {
            String contents = fs.readFromFileSystem(clusterDirectory);
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(contents));

            ClusteringHeader header = simpleClusterSet.getHeader();
            if (header == null) {
                header = ParserUtilities.readClusterHeader(lineNumberReader);
                simpleClusterSet.setHeader(header);
            }

            ISpectralCluster[] clusters = readClustersFromClusteringFile(lineNumberReader);
            simpleClusterSet.addClusters(Arrays.asList(clusters));

        }
    }


    public static IClusterSet readClusterSet(File file,   String name) {
        SimpleClusterSet simpleClusterSet = new SimpleClusterSet();
        simpleClusterSet.setName(name);

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
        }
        else if (file.getName().endsWith(".clustering")) {
            try {
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));

                ClusteringHeader header = simpleClusterSet.getHeader();
                if (header == null) {
                    header = ParserUtilities.readClusterHeader(lineNumberReader);
                    simpleClusterSet.setHeader(header);
                }

                ISpectralCluster[] clusters = readClustersFromClusteringFile(lineNumberReader );
                simpleClusterSet.addClusters(Arrays.asList(clusters));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return simpleClusterSet;
    }


    /**
     * Read clustering file into a set of clusters
     *
     * @param inp
     * @return
     */
    public static ISpectralCluster[] readClustersFromClusteringFile(LineNumberReader inp) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();

        try {
            String line = inp.readLine();
            while (line != null && !line.equals(ParserUtilities.BEGIN_CLUSTERING)) {
                line = inp.readLine();
            }

            List<String> clusterContent = new ArrayList<String>();
            while (line != null) {
                if (line.startsWith(ParserUtilities.BEGIN_CLUSTERING)) {
                    if (!clusterContent.isEmpty()) {
                        ISpectralCluster cluster = processIntoCluster(clusterContent);
                        if (cluster != null) {
                            holder.add(cluster);
                        }
                    }
                    clusterContent.clear();
                }
                else {
                    clusterContent.add(line);
                }

                line = inp.readLine();
            }

            if (!clusterContent.isEmpty()) {
                ISpectralCluster cluster = processIntoCluster(clusterContent);
                if (cluster != null) {
                    holder.add(cluster);
                }
            }
        }
        catch (IOException ioe) {
            throw new IllegalStateException("Failed to read ", ioe);
        }

        ISpectralCluster[] ret = new ISpectralCluster[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected static ISpectralCluster processIntoCluster(List<String> clusterLines) {

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
            }
            else if (clusterLine.startsWith(ParserUtilities.CONSENSUS_MZ)) {
                consensusMzLine = clusterLine.replace(ParserUtilities.CONSENSUS_MZ, "");
            }
            else if (clusterLine.startsWith(ParserUtilities.CONSENSUS_INTENSITY)) {
                consensusIntensityLine = clusterLine.replace(ParserUtilities.CONSENSUS_INTENSITY, "");
            }
            else if (clusterLine.startsWith(ParserUtilities.PEPTIDE_SEQUENCE)) {
                String peptideSequence = clusterLine.replace(ParserUtilities.PEPTIDE_SEQUENCE, "");
                peptideSequence = peptideSequence.replace("[", "").replace("]", "");
                cluster.addPeptides(peptideSequence);
            }
            else if (clusterLine.startsWith(ParserUtilities.SPECTRUM_ID)) {
                String[] parts = clusterLine.split("\t");
                spectrum = PSMSpectrum.getSpectrum(parts[1]);
                cluster.addSpectra(spectrum);
            }
            else //noinspection StatementWithEmptyBody
                if (clusterLine.startsWith(ParserUtilities.AVERAGE_PRECURSOR_INTENSITY)) {
                    // do nothing here
                }
                else {
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



    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }
        File inFiles  = new File(args[0]);
        if(!inFiles.exists() || !inFiles.isDirectory())
                throw new IllegalStateException("problem"); // ToDo change

        IFileSystem fs = new LocalMachineFileSystem(inFiles);
        String outFileStr = args[1];
        PrintWriter out = new PrintWriter(new FileWriter(outFileStr));
        TypedPredicate<String>  test = new TypedPredicate<String>() {
            @Override
            public boolean apply(@Nonnull final String s, final Object... otherdata) {
                if(!s.endsWith(".clustering"))
                    return false;
                if(!s.startsWith("ClusteringBin"))
                    return false;
                return true;
            }
        } ;
        try {
            appendFileClusters(out,args[0],fs,test);
        }
        finally {
            out.close();
        }

    }
}
