package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.ClusterUtilities
 *
 * @author Steve Lewis
 * @date 5/10/13
 */
public class ClusterUtilities {
    private static final Map<String, ISpectralCluster> ID_TO_CLUSTER = new ConcurrentHashMap<String, ISpectralCluster>();

    public static ISpectralCluster getById(String id) {
        return ID_TO_CLUSTER.get(id);
    }


    /**
     * return a list of any peptides found with duplicates
     *
     * @return as above
     */
    public static List<String> getPeptideList(List<ISpectrum> spectra) {
        List<String> petides = new ArrayList<String>();
        for (ISpectrum spec : spectra) {
            if (spec instanceof IPeptideSpectrumMatch) {
                String peptide = ((IPeptideSpectrumMatch) spec).getPeptide();
                if (peptide == null || peptide.length() == 0)
                    continue;
                String[] items = peptide.split(";");
                petides.addAll(Arrays.asList(items));
             }

        }
        return petides;
    }

    /**
     * return the most common peptides (first if equally commmon) or ""
     * if no peptides found
     *
     * @return as above
     */
    public static String mostCommonPeptides(List<ISpectrum> spectra) {
        final List<String> peptideList = getPeptideList(spectra);
        StringBuilder sb = new StringBuilder();
        final String[] stringsByOccurance = CountedString.getStringsByOccurance(peptideList);
        for (String s : stringsByOccurance) {
            if(sb.length() > 0)
                sb.append(",");
            sb.append(s) ;
        }
        return sb.toString();
    }


    /**
     * return the peaks sorted by intensity
     *
     * @param sc
     * @return
     */
    public static List<IPeak> peaksByIntensity(ISpectrum sc) {
        List<IPeak> peaks = new ArrayList<IPeak>(sc.getPeaks());
        Collections.sort(peaks, PeakIntensityComparator.INSTANCE);
        return peaks;
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static List<ISpectrum> extractSpectra(List<ISpectralCluster> clusters) {
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectralCluster cluster : clusters) {
            holder.addAll(cluster.getClusteredSpectra());
        }
        Collections.sort(holder);
        return holder;
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double minClusterMZ(List<ISpectralCluster> clusters) {
        double ret = Double.MAX_VALUE;
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectralCluster cluster : clusters) {
            ret = Math.min(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double maxClusterMZ(List<ISpectralCluster> clusters) {
        double ret = 0;
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectralCluster cluster : clusters) {
            ret = Math.max(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }


    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double minSpectraMZ(List<ISpectrum> clusters) {
        double ret = Double.MAX_VALUE;
        for (ISpectrum cluster : clusters) {
            ret = Math.min(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static double maxSpectraMZ(List<ISpectrum> clusters) {
        double ret = 0;
        for (ISpectrum cluster : clusters) {
            ret = Math.max(cluster.getPrecursorMz(), ret);
        }
        return ret;
    }


    /**
     * return a list of all spectra in the list of spectra sorted by charge then mz
     *
     * @param spectra !null list of spectra
     * @return !null list of spectra
     */
    public static List<ISpectralCluster> asClusters(List<ISpectrum> spectra) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectrum spectrum : spectra) {
            holder.add(spectrum.asCluster());
        }
        Collections.sort(holder);
        return holder;
    }

    /**
     * use when going to a completely new cluster set - say in a reducer
     */
    public static void clearClusters() {
        ID_TO_CLUSTER.clear();
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
                        PeakIntensityComparator.getInstance());
                higheseEachBin[bin] = hightestInBin;
            }

            // add the peak
            hightestInBin.add(pk); // add - sort and keep highest is internam


        }

        // now grab all peaks in a list
        // will hold retained peaks
        List<IPeak> holder = new ArrayList<IPeak>();
        for (int i = 0; i < higheseEachBin.length; i++) {
            PriorityQueue<IPeak> hightestInBin = higheseEachBin[i];
            if (hightestInBin != null)
                holder.addAll(hightestInBin);
        }
        // back to an array
        return holder;

    }

    /**
     * make a list of mx values comma separated as a string
     *
     * @param spec !null spectrun
     * @return as above
     */
    public static String buildMZString(final ISpectrum spec) {
        StringBuilder sb = new StringBuilder();
        for (IPeak pk : spec.getPeaks()) {
           if(sb.length() > 0)
               sb.append(",");
            sb.append(String.format("%10.3f",pk.getMz()).trim());
        }
        return sb.toString();

    }

    /**
     * make a list of mx values comma separated as a string
     *
     * @param spec !null spectrun
     * @return as above
     */
    public static String buildIntensityString(final ISpectrum spec) {
        StringBuilder sb = new StringBuilder();
        for (IPeak pk : spec.getPeaks()) {
           if(sb.length() > 0)
               sb.append(",");
            sb.append(String.format("%10.5f",pk.getIntensity()).trim());
        }
        return sb.toString();

    }
}
