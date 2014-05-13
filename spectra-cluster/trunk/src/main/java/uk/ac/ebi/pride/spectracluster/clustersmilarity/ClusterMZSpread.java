package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nonnull;

/**
 * class representing the mean ans standard deviation of
 * a cluster - sorts low to high standard deviation
 */
public class ClusterMZSpread implements Comparable<ClusterMZSpread> {

    @SuppressWarnings("UnusedDeclaration")
    public Comparable<ClusterMZSpread> BY_STANDARD_DEVIATION = new Comparable<ClusterMZSpread>() {
        @Override
        public int compareTo(@Nonnull final ClusterMZSpread o) {
            int ret = CompareTo.compare(getStandardDeviation(), o.getStandardDeviation());
            if (ret == 0)
                return CompareTo.compare(getMean(), o.getMean());
            return -ret;
        }
    };
    @SuppressWarnings("UnusedDeclaration")
    public Comparable<ClusterMZSpread> BY_RANGE = new Comparable<ClusterMZSpread>() {
        @Override
        public int compareTo(@Nonnull final ClusterMZSpread o) {
            int ret = CompareTo.compare(getRange(), o.getRange());
            if (ret == 0)
                return CompareTo.compare(getMean(), o.getMean());
            return -ret;
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public Comparable<ClusterMZSpread> TOP_FIRST = new Comparable<ClusterMZSpread>() {
        @Override
        public int compareTo(@Nonnull final ClusterMZSpread o) {
            int ret = CompareTo.compare(getStandardDeviation(), o.getStandardDeviation());
            if (ret == 0)
                return CompareTo.compare(getMean(), o.getMean());
            return -ret;
        }
    };

    private final double standardDeviation;
    private final double mean;
    private final double range;
    private final double maxMz;
    private final double minMz;
    private final int clusterSize;

    public static int number_pure_decoy = 0;

    public ClusterMZSpread(ISpectralCluster cluster) {
        double maxmz = Double.MIN_VALUE;
        double minmz = Double.MAX_VALUE;
        ;
        double sum = 0;
        double sumsq = 0;
        int n = 0;
        clusterSize = cluster.getClusteredSpectraCount();
        for (ISpectrum o : cluster.getClusteredSpectra()) {
            double mz = o.getPrecursorMz();
            sum += mz;
            sumsq += mz * mz;
            maxmz = Math.max(maxmz, mz);
            minmz = Math.min(minmz, mz);
            n++;
        }


        switch (n) {
            case 0:
                throw new UnsupportedOperationException("should never happen");
            case 1:
                mean = sum;
                standardDeviation = 0;
                break;
            default:
                mean = sum / n;
                double variance = sumsq / n - mean * mean;
                standardDeviation = Math.sqrt(variance);
        }
        maxMz = maxmz;
        minMz = minmz;
        range = maxMz - minMz;

        // show wide stable clusters
//        if(n >= 32 && standardDeviation > 2) {
//            cluster.appendClustering(System.out);
//        }

        if (n > 10 && range < 0.00001)
            sum = 0; // break here
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }


    public double getRange() {
        return range;
    }

    public double getMaxMz() {
        return maxMz;
    }

    public double getMinMz() {
        return minMz;
    }

    public double getMean() {
        return mean;
    }

    public int getClusterSize() {
        return clusterSize;
    }

    /**
     * sort least pure first
     *
     * @param o other
     * @return
     */
    @Override
    public int compareTo(@Nonnull ClusterMZSpread o) {
        int ret = CompareTo.compare(getRange(), o.getRange());
        if (ret != 0)
            return ret;

        return CompareTo.compare(getMean(), o.getMean());
    }

    @Override
    public String toString() {
        //noinspection ImplicitArrayToString
        return String.format("%8.1f", getMean()).trim() + ":" + String.format("%6.3f", getStandardDeviation()).trim() + ":" + getClusterSize();
    }
}
