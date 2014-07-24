package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.io.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ClusterFilterer
 * User: Steve
 * Date: 7/22/2014
 */
 public class ClusterFilterer implements TypedPredicate<ICluster> {

    private final File[] files;
    private int numberClusters;
    private int maxClusters = Integer.MAX_VALUE;
    private final List<TypedPredicate<ICluster>> filters = new ArrayList<TypedPredicate<ICluster>>();

    public ClusterFilterer(final File inFile) {
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

    public int getNumberClusters() {
        return numberClusters;
    }
     public void incrementNumberClusters() {
        numberClusters++;
    }

    public int getMaxClusters() {
        return maxClusters;
    }

     public void setMaxClusters(final int pMaxClusters) {
        maxClusters = pMaxClusters;
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
                if(clusterSize > 10)     {
                    if (apply(cluster)) {
                        writeCluster(cluster, outWriter);
                    }
                    if (getMaxClusters() <= getNumberClusters())
                        return false;

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
        CGFClusterAppender.INSTANCE.appendCluster(outWriter, pCluster);
        incrementNumberClusters();
    }

    public void manageClusters(PrintWriter outWriter) {
        for (File file : files) {
            if (!handleFile(file, outWriter))
                break;
        }
        outWriter.close();
    }

    public static final int MINIMUM_INTERESTING_SIZE = 100;
  //  public static final int MINIMUM_INTERESTING_SIZE = 500;
     public static final int MAXIMUM_INTERESTING_CLUSTERS = 8000;

    protected static void writeLargeClusters(final String[] args) throws IOException {
        File directoryWithCGF = new File(args[0]);
        if (!directoryWithCGF.exists() || !directoryWithCGF.isDirectory())
            throw new IllegalArgumentException("bad input directory " + args[0]);
        File outputFile = new File(args[1]);
        PrintWriter out = new PrintWriter(new FileWriter(outputFile));
        ClusterFilterer actor = new ClusterFilterer(directoryWithCGF);

        actor.addClusterFilter(new TypedPredicate<ICluster>() {
            @Override public boolean apply(@Nonnull final ICluster pICluster, final Object... otherdata) {
                return pICluster.getClusteredSpectraCount() > MINIMUM_INTERESTING_SIZE;
            }
        });

        actor.setMaxClusters(MAXIMUM_INTERESTING_CLUSTERS);

        actor.manageClusters(out);
    }

    /**
       * usage - <directoryWithCGF> <outputFile>
       *    like T:/PrideClustering PrideLargeClusters.cgf
       * @param args
       */
      public static void main(String[] args) throws Exception {
          writeLargeClusters(args);
      }


}
