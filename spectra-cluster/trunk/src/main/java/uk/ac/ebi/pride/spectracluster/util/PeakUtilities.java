package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.algorithms.LinearBinner;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.Peak;
import uk.ac.ebi.pride.spectracluster.util.comparator.PeakIntensityComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeakUtilities {
    /**
     * after this many peaks we can drop the rest
     */
    public static final int MAX_PEAKS_TO_KEEP = 100;


    /**
     * operations that merge peaks in lists had better preserve the total count
     * useful in testing and debuggine
     *
     * @param lst !null list of peaks
     * @return total count from all peaks
     */
    public static int getTotalCount(List<IPeak> lst) {
        int total = 0;
        for (IPeak pk : lst) {
            total += pk.getCount();
        }
        return total;
    }

    /**
     * binn the mz range finding the highest peaks in each bin
     *
     * @param peaks     !null original peaks
     * @param minMZ     minmum mz to bin
     * @param maxMZ     maximum mz to bin
     * @param binSize   size of each bin
     * @param maxPerBin maximim peaks to retain
     * @return
     */
    public static List<IPeak> getHighestInBins(List<IPeak> peaks,
                                               double minMZ,
                                               double maxMZ,
                                               double binSize,
                                               int maxPerBin) {
        // handles the binning as am object
        LinearBinner binner = new LinearBinner(maxMZ, binSize, minMZ);
        /// these will keep the highest values
        // unchecked cast
        PriorityQueue<IPeak>[] higheseEachBin = Cast.it(new PriorityQueue[binner.getNumberBins()]);


        // for all peaks
        for (IPeak pk : peaks) {
            // bin mz
            int bin = binner.asBin(pk.getMz());

            // get the queue creating as needed
            PriorityQueue<IPeak> hightestInBin = higheseEachBin[bin];
            // lazily build only as needed
            if (hightestInBin == null) {
                // saves the highest intensity peaks up to  maxPerBin
                // because the comparison is on intensity
                hightestInBin = new PriorityQueue<IPeak>(maxPerBin,
                        PeakIntensityComparator.INSTANCE);
                higheseEachBin[bin] = hightestInBin;
            }

            // add the peak
            hightestInBin.add(pk); // add - sort and keep highest is internam


        }

        // now grab all peaks in a list
        // will hold retained peaks
        List<IPeak> holder = new ArrayList<IPeak>();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < higheseEachBin.length; i++) {
            PriorityQueue<IPeak> hightestInBin = higheseEachBin[i];
            if (hightestInBin != null)
                holder.addAll(hightestInBin);
        }
        // back to an array
        return holder;

    }

    @SuppressWarnings("UnusedDeclaration")
    public static List<IPeak> buildPeaks(String commaDelimitecMZ, String commaDelimitedIntensity) {
        float[] mzValues = parseCommaDelimitedFloats(commaDelimitecMZ);
        float[] intensityValues = parseCommaDelimitedFloats(commaDelimitedIntensity);
        if (mzValues.length != intensityValues.length)
            throw new IllegalArgumentException("Unequal mz and intensity lists");
        List<IPeak> holder = new ArrayList<IPeak>();
        for (int i = 0; i < intensityValues.length; i++) {
            holder.add(new Peak(mzValues[i], intensityValues[i]));
        }
        Collections.sort(holder);  // sort peaks by mz
        return holder;
    }

    public static float[] parseCommaDelimitedFloats(String commaDelimitedFloats) {
        String[] items = commaDelimitedFloats.trim().split(",");
        float[] ret = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            ret[i] = Float.parseFloat(item);
        }
        return ret;
    }


    /**
     * get all peaks from the cluster
     *
     * @param cluster !null cluster
     * @return !null list of peaks
     */
    @SuppressWarnings("UnusedDeclaration")
    public static List<IPeak> getAllPeaks(IPeptideSpectralCluster cluster) {
        List<IPeak> holder = new ArrayList<IPeak>();
        for (ISpectrum spec : cluster.getClusteredSpectra()) {
            final List<IPeak> peaks = spec.getPeaks();
            holder.addAll(peaks);
        }
        return holder;
    }


    /**
     * return the peaks sorted by intensity
     *
     * @param sc
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public static List<IPeak> peaksByIntensity(ISpectrum sc) {
        List<IPeak> peaks = new ArrayList<IPeak>(sc.getPeaks());
        Collections.sort(peaks, PeakIntensityComparator.INSTANCE);
        return peaks;
    }
}
