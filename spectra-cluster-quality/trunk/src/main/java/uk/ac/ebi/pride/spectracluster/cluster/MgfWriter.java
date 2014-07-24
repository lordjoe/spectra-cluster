package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.io.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.MgfWriter
 * User: Steve
 * Date: 7/22/2014
 */
public class MgfWriter implements TypedPredicate<ICluster> {

    private final File[] files;
    private final List<TypedPredicate<ICluster>> filters = new ArrayList<TypedPredicate<ICluster>>();
    private final Set<String> writtenSpectra = new HashSet<String>();

    public MgfWriter(final File inFile) {
        files = inFile.listFiles(new FileUtilities.HasExtensionFilter("cgf"));
    }

    /**
     * @param pICluster
     * @param otherdata - implementation specific and usually blank
     * @return what the implementation does
     */
    @Override public boolean apply(@Nonnull final ICluster pICluster, final Object... otherdata) {
        for (TypedPredicate<ICluster> filter : filters) {
            if (!filter.apply(pICluster, otherdata))
                return false;
        }
        return true;
    }


    public void addClusterFilter(TypedPredicate<ICluster> added) {
        filters.add(added);
    }

    /**
     * @param f
     * @return tru if more files are needed
     */
    protected boolean handleFile(File f, PrintWriter outWriter) {
        try {
            LineNumberReader inp = new LineNumberReader(new FileReader(f));
            ICluster cluster = ParserUtilities.readSpectralCluster(inp, null);
            while (cluster != null) {
                int clusterSize = cluster.getClusteredSpectraCount();
                if (apply(cluster)) {
                    writeCluster(cluster, outWriter);
                }
                cluster = ParserUtilities.readSpectralCluster(inp, null);
            }

            return true;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    protected void writeCluster(final ICluster pCluster, PrintWriter outWriter) {
        for (ISpectrum spc : pCluster.getClusteredSpectra()) {
            String id = spc.getId();
            if (!writtenSpectra.contains(id)) {
                writtenSpectra.add(id);
                MGFSpectrumAppender.INSTANCE.appendSpectrum(outWriter, spc);
            }
        }
    }

    public void manageClusters(PrintWriter outWriter) {
        for (File file : files) {
            if (!handleFile(file, outWriter))
                break;
        }
        outWriter.close();
    }

    protected static void writeMGFSpectra(final String[] args) throws IOException {
        File directoryWithCGF = new File(args[0]);
        if (!directoryWithCGF.exists() || !directoryWithCGF.isDirectory())
            throw new IllegalArgumentException("bad input directory " + args[0]);
        File outputFile = new File(args[1]);
        PrintWriter out = new PrintWriter(new FileWriter(outputFile));
        MgfWriter actor = new MgfWriter(directoryWithCGF);

        actor.manageClusters(out);
    }

    /**
     * usage - <directoryWithCGF> <outputFile>
     * like T:/PrideClustering PrideLargeClusters.mgf
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        writeMGFSpectra(args);
    }


}
