package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.algorithms.CountedString;
import com.lordjoe.algorithms.LinearBinner;
import org.systemsbiology.hadoop.ISetableParameterHolder;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.PeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.hadoop.SpectrumInCluster;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.comparator.PeakIntensityComparator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.*;


/**
 * uk.ac.ebi.pride.spectracluster.util.ClusterUtilities
 * a lits of stateless static functions for manipulating clusters, lists of clusters
 * and performing other common chores
 *
 * @author Steve Lewis
 * @date 5/10/13
 */
public class ClusterUtilities {

    public static final String CLUSTERING_EXTENSION = ".clustering";
    public static final String CGF_EXTENSION = ".cgf";

    public static final String STABLE_CLUSTER_SIZE_PROPERTY = "uk.ac.ebi.pride.spectracluster.util.ClusterUtilities.StableClusterSize";
    public static final String SEMI_STABLE_CLUSTER_SIZE_PROPERTY = "uk.ac.ebi.pride.spectracluster.util.ClusterUtilities.SemiStableClusterSize";

    public static final String PLACE_SPECTRUM_IN_BEST_CLUSTER = "uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster.PlaceSpectrumInBestCluster";
    public static final String BREAK_UP_CLUSTERS_LESS_THAN = "uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster.BreakUpClustersLessThan";

    public static final int DEFAULT_STABLE_CLUSTER_SIZE = 20;
    public static final int DEFAULT_SEMI_STABLE_CLUSTER_SIZE = 10;


    public static void setStableClusterSizeFromProperties(final ISetableParameterHolder pApplication) {
        int stableClusterSize = pApplication.getIntParameter(ClusterUtilities.STABLE_CLUSTER_SIZE_PROPERTY, ClusterUtilities.DEFAULT_STABLE_CLUSTER_SIZE);
        ClusterUtilities.setStableClusterSize(stableClusterSize);
        int semiStableClusterSize = pApplication.getIntParameter(ClusterUtilities.SEMI_STABLE_CLUSTER_SIZE_PROPERTY, ClusterUtilities.DEFAULT_SEMI_STABLE_CLUSTER_SIZE);
        ClusterUtilities.setSemiStableClusterSize(semiStableClusterSize);
    }

    public static String describeDaltons(double precursorMZ) {
        return "MZ" + String.format("%05d", (int) (precursorMZ + 0.5));
    }

    public static double asDaltons(String asDaltons) {
        return Integer.parseInt(asDaltons.substring(2));
    }


    public static final String STABLE_CLUSTER_PREFIX = "SC";
    @SuppressWarnings("UnusedDeclaration")
    public static final String SEMI_STABLE_CLUSTER_PREFIX = "SSC";

    private static int stableClusterSize = DEFAULT_STABLE_CLUSTER_SIZE;
    private static int semiStableClusterSize = DEFAULT_SEMI_STABLE_CLUSTER_SIZE;

    public static int getStableClusterSize() {
        return stableClusterSize;
    }

    public static void setStableClusterSize(final int pStableClusterSize) {
        stableClusterSize = pStableClusterSize;
    }

    public static int getSemiStableClusterSize() {
        return semiStableClusterSize;
    }

    public static void setSemiStableClusterSize(final int pSemiStableClusterSize) {
        semiStableClusterSize = pSemiStableClusterSize;
    }

    public static String getStableClusterId() {
        return STABLE_CLUSTER_PREFIX + UUID.randomUUID().toString();
    }

    public static String getSemiStableClusterId() {
        return STABLE_CLUSTER_PREFIX + UUID.randomUUID().toString();
    }

    /**
     * allow nonfitting spectra to leave and retuen a list of clusters to write out
     *
     * @param cluster
     * @return !null List<ISpectralCluster
     */
    @Nonnull
    public static List<IPeptideSpectralCluster> removeNonFittingSpectra(@Nonnull IPeptideSpectralCluster cluster, @Nonnull IClusteringEngine engine) {
        final List<IPeptideSpectralCluster> allClusters = engine.findNoneFittingSpectra(cluster);
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        if (!allClusters.isEmpty()) {
            for (IPeptideSpectralCluster removedCluster : allClusters) {

                // drop all spectra
                final List<ISpectrum> clusteredSpectra = removedCluster.getClusteredSpectra();
                ISpectrum[] allRemoved = clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]);
                cluster.removeSpectra(allRemoved);

                // and save as stand alone
                holder.add(removedCluster);
            }

        }
        if (cluster.getClusteredSpectraCount() > 0)
            holder.add(cluster);
        return holder;
    }


    public static SpectrumInCluster readSpectrumInCluster(String str) {
        LineNumberReader rdr = new LineNumberReader(new StringReader(str));
        return readSpectrumInCluster(rdr);
    }


    public static SpectrumInCluster readSpectrumInCluster(LineNumberReader rdr) {
        try {
            String line = rdr.readLine();
            SpectrumInCluster ret = new SpectrumInCluster();
            while (line != null) {
                if ("=SpectrumInCluster=".equals(line.trim())) {
                    line = rdr.readLine();
                    break;
                }
            }

            if (!line.startsWith("removeFromCluster="))
                throw new IllegalStateException("badSpectrumInCluster");
            ret.setRemoveFromCluster(Boolean.parseBoolean(line.substring("removeFromCluster=".length())));
            line = rdr.readLine();

            if (!line.startsWith("distance="))
                throw new IllegalStateException("badSpectrumInCluster");
            double distance = Double.parseDouble(line.substring("distance=".length()));
            if (distance >= 0)    // todo fix later
                ret.setDistance(distance);

            IPeptideSpectrumMatch spec = ParserUtilities.readMGFScan(rdr, line);

            ret.setSpectrum(spec);

            IPeptideSpectralCluster[] clusters = ParserUtilities.readClustersFromClusteringFile(rdr, null);
            if (clusters.length != 1)
                throw new IllegalStateException("badSpectrumInCluster");

            ret.setCluster(clusters[0]);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * find the highest quality spectrum in a list of clusters
     *
     * @param copied should be non-empty array
     * @return !null spectrum unless copied is empty
     */
    public static ISpectrum getHighestQualitySpectrum(IPeptideSpectralCluster... copied) {
        if (copied.length == 0)
            return null;
        ISpectrum ret = copied[0].getHighestQualitySpectrum();
        for (int i = 1; i < copied.length; i++) {
            ISpectrum highestQualitySpectrum = copied[i].getHighestQualitySpectrum();
            if (!ret.equivalent(highestQualitySpectrum))
                throw new IllegalStateException("AlternativeSpectralClusters MUST have the same highest quality spectrum");
        }

        return ret;
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


//    private static final Map<String, ISpectralCluster> ID_TO_CLUSTER = new ConcurrentHashMap<String, ISpectralCluster>();
//
//    public static ISpectralCluster getById(String id) {
//        return ID_TO_CLUSTER.get(id);
//    }
//
//    /**
//      * use when going to a completely new cluster set - say in a reducer
//      */
//     public static void clearClusters() {
//         ID_TO_CLUSTER.clear();
//     }

    /**
     * hard code a call to this to
     */
    public static void breakHere() {
        //noinspection UnusedDeclaration
        int i = 0;
        //noinspection UnusedAssignment
        i++; // Break here - something interesting happened in teh outer code
    }


    public static final int MZ_RESOLUTION = 1000; // we care about differences of 0.01 dalton

    /**
     * convert am int into an mz for east comparison
     *
     * @param mz input
     * @return MZ_RESOLUTION * mz as int
     */
    public static int mzToInt(double mz) {
        return (int) ((MZ_RESOLUTION * mz) + 0.5);
    }


    /**
     * purely for debugging to allow handling of one bad case to be traces
     *
     * @param pk
     * @return
     */

    public static final float MININUM_INTERESTING_MZ = 474.09F;
    public static final float MAXINUM_INTERESTING_MZ = 475.4F;

    @SuppressWarnings("UnusedDeclaration")
    public static boolean isPeakInteresting(IPeak pk) {
        float mz = pk.getMz();
        return isMZInteresting(mz);
    }

    /**
     * purely for debugging to allow handling of one bad case to be traces
     *
     * @param pMz
     * @return
     */
    public static boolean isMZInteresting(final float pMz) {
        //noinspection RedundantIfStatement
        if (pMz < MININUM_INTERESTING_MZ)
            return false;
        //noinspection RedundantIfStatement
        if (pMz > MAXINUM_INTERESTING_MZ)
            return false;

        return true; // break here
    }

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
     * take out all clusters consisting of a single spectrum and return them as a list
     * - engines will do this as a step in reclustering
     *
     * @param clusters !null   a list of clusters - this WILL be modified
     * @return !null list of clusters containing a single spectrum
     */
    public static List<IPeptideSpectralCluster> removeSingleSpectrumClusters(List<IPeptideSpectralCluster> clusters) {
        return removeSingleSpectrumClustersSizedLessThan(clusters, 1);
    }

    /**
     * take out all clusters sized les shtne size and return them as a list of single spectrum clusters
     * - engines will do this as a step in reclustering
     *
     * @param pClusters !null   a list of clusters - this WILL be modified
     * @param size      - clusters sized less than or equal to this will be removed and returned as single spectrum clusters
     * @return !null list of clusters containing a single spectrum
     */
    public static List<IPeptideSpectralCluster> removeSingleSpectrumClustersSizedLessThan(final List<IPeptideSpectralCluster> pClusters, final int size) {
        List<IPeptideSpectralCluster> retained = new ArrayList<IPeptideSpectralCluster>();
        List<IPeptideSpectralCluster> asSingleSpectra = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster cluster : pClusters) {
            if (cluster.getClusteredSpectraCount() <= size) {
                final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
                for (ISpectrum spectrum : clusteredSpectra) {
                    asSingleSpectra.add(ClusterUtilities.asCluster(spectrum)); // di not retain but return as one spectrum clusters
                }
            } else {
                retained.add(cluster); // large enough keep it
            }
        }
        pClusters.clear();
        pClusters.addAll(retained);

        return asSingleSpectra;
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
     * return a comma delimited set of most common peptides
     *
     * @return as above
     */
    public static String mostCommonPeptides(IPeptideSpectralCluster cluster) {
        final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
        //noinspection UnnecessaryLocalVariable
        String petides = mostCommonPeptides(clusteredSpectra);
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
        CountedString[] countedStrings = CountedString.getCountedStrings(peptideList);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < countedStrings.length; i++) {
            CountedString countedString = countedStrings[i];
            if (sb.length() > 0)
                sb.append(",");
            sb.append(countedString);

        }
//        final String[] stringsByOccurance = CountedString.getStringsByOccurance(peptideList);
//          for (String s : stringsByOccurance) {
//            if (sb.length() > 0)
//                sb.append(",");
//            sb.append(s);
//        }
        return sb.toString();
    }

    public static String[] getMostCommonPeptides(IPeptideSpectralCluster cluster) {
        final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
        //noinspection UnnecessaryLocalVariable
        final List<String> peptideList = getPeptideList(clusteredSpectra);
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        final String[] stringsByOccurance = CountedString.getStringsByOccurance(peptideList);

        return stringsByOccurance;
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

    /**
     * return a list of all spectra in the list of clusters sorted by charge then mz
     *
     * @param clusters !null list of clusters
     * @return !null list of spectra
     */
    public static List<ISpectrum> extractSpectra(List<IPeptideSpectralCluster> clusters) {
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (IPeptideSpectralCluster cluster : clusters) {
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
    public static double minClusterMZ(List<IPeptideSpectralCluster> clusters) {
        double ret = Double.MAX_VALUE;
        for (IPeptideSpectralCluster cluster : clusters) {
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
    public static double maxClusterMZ(List<IPeptideSpectralCluster> clusters) {
        double ret = 0;
        for (IPeptideSpectralCluster cluster : clusters) {
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
     * convert a spectrum into cluster
     * @param spectrum  given spectrum
     * @return  a spectral cluster
     */
    public static IPeptideSpectralCluster asCluster(ISpectrum spectrum) {
        PeptideSpectralCluster ret = new PeptideSpectralCluster();
        ret.addSpectra(spectrum);
        return ret;
    }


    /**
     * return a list of all spectra in the list of spectra sorted by charge then mz
     *
     * @param spectra !null list of spectra
     * @return !null list of spectra
     */
    public static List<IPeptideSpectralCluster> asClusters(List<ISpectrum> spectra) {
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (ISpectrum spectrum : spectra) {
            holder.add(asCluster(spectrum));
        }
        Collections.sort(holder);
        return holder;
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

    /**
     * make a list of mx values comma separated as a string
     *
     * @param spec !null spectrun
     * @return as above
     */
    public static String buildMZString(final ISpectrum spec) {
        StringBuilder sb = new StringBuilder();
        for (IPeak pk : spec.getPeaks()) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(String.format("%10.3f", pk.getMz()).trim());
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
            if (sb.length() > 0)
                sb.append(",");
            sb.append(String.format("%10.2f", pk.getIntensity()).trim());
        }
        return sb.toString();

    }


    /**
     * merge clusters among themselves returning a list of surviving clusters which will replace mergable
     *
     * @param mergable          !null list of clusters
     * @param similarityChecker !null simil;atity checker
     * @param maxMZDIfference   max difference to consider merging
     * @return !null list of new clusters
     */
    public static List<IPeptideSpectralCluster> mergeClusters(List<IPeptideSpectralCluster> mergable, SimilarityChecker similarityChecker, double maxMZDIfference) {
        List<IPeptideSpectralCluster> retained = new ArrayList<IPeptideSpectralCluster>();
        // clusters need to be compared with all clusters below them
        for (IPeptideSpectralCluster cluster : mergable) {
            double currentMZ = cluster.getPrecursorMz();
            double minimimMergableMZ = currentMZ - maxMZDIfference;
            IPeptideSpectralCluster mergeWith = null;
            if (retained.isEmpty()) {   // start with the first cluster
                retained.add(cluster);
                continue;
            }

            // start at the top mz cluster
            for (int index = retained.size() - 1; index >= 0; index--) {
                IPeptideSpectralCluster test = retained.get(index);
                if (test.getPrecursorMz() < minimimMergableMZ)
                    break; // no more to consider
                final ISpectrum cs1 = test.getConsensusSpectrum();
                double similarity = similarityChecker.assessSimilarity(cs1, cluster.getConsensusSpectrum());
                if (similarity >= similarityChecker.getDefaultThreshold()) {
                    mergeWith = test;
                    break; // found who to merge with
                }

            }
            if (mergeWith == null) {
                retained.add(cluster); // nothing to merge with so keep the cluster
            } else {  // merge with a close enough cluster
                final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
                mergeWith.addSpectra(clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]));
            }
        }
        // make sure the retained are still in mz order
        Collections.sort(retained);
        return retained;
    }


    /**
     * take a collection of clusters - presumably with clustered and another group - maybe
     * single spectrun clusters - that fact is not important but the groups sould be distinct
     * merge where possible and return the list of not merged spectra
     *
     * @param mergable          !null list of clusters - the clusters will change but not change in number
     * @param singles           !null list of single spectra
     * @param similarityChecker !null similarity
     * @param maxMZDIfference   ! maxMZ difference to look at for merging
     * @return !null list of non-merged clusters from singles
     */
    public static List<IPeptideSpectralCluster> mergeClustersWithSingleSpectra(List<IPeptideSpectralCluster> mergable, List<IPeptideSpectralCluster> singles, SimilarityChecker similarityChecker, double maxMZDIfference) {
        List<IPeptideSpectralCluster> retainedSingleSpectra = new ArrayList<IPeptideSpectralCluster>();
        int startIndex = 0;
        // clusters need to be compared with all clusters below them
        for (IPeptideSpectralCluster cluster : singles) {
            double currentMZ = cluster.getPrecursorMz();
            double minimimMergableMZ = currentMZ - maxMZDIfference;
            IPeptideSpectralCluster mergeWith = null;
            // start at the top mz cluster
            for (int index = startIndex; index > mergable.size(); index++) {
                IPeptideSpectralCluster test = mergable.get(index);
                if (index == startIndex) {
                    if (test.getPrecursorMz() < minimimMergableMZ) {
                        startIndex++;
                        continue; // try again
                    }
                }
                double distance = similarityChecker.assessSimilarity(test.getConsensusSpectrum(), cluster.getConsensusSpectrum());
                if (distance <= similarityChecker.getDefaultThreshold()) {
                    mergeWith = test;
                    break; // found who to merge with
                }

            }
            if (mergeWith == null) {
                retainedSingleSpectra.add(cluster); // nothing to merge with so keep single
            } else {  // merge with a close enough cluster
                // note this may disturb the order of the cluster list but should not stop
                // the algorithm from working
                final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
                mergeWith.addSpectra(clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]));
            }
        }


        // make sure the mergable are still in mz order
        Collections.sort(mergable);


        // make sure the retainedSingleSpectra are still in mz order
        Collections.sort(retainedSingleSpectra);
        return retainedSingleSpectra;
    }


    /**
     * Rounding factor to use. 1000 means 3 positions after the comma.
     */
    public final static int MZ_PRECISSION = 1000; // using a precision of 1000 reduces memory usages but leads to different results.

    /**
     * Round to certain number of decimals
     *
     * @param f
     * @return
     */
    public static double round(double f) {
        return round(f, MZ_PRECISSION);
    }

    /**
     * Round to certain number of decimals
     *
     * @param f
     * @param decimalPlace
     * @return
     */
    public static double round(double f, int decimalPlace) {
        //        BigDecimal bd = new BigDecimal(Float.toString(d));
        //        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        //        return bd.floatValue();
        int i = (int) ((f * decimalPlace) + 0.5);
        return i / (double) decimalPlace;
    }


    /**
     * write the header of a .clustering file
     *
     * @param out
     * @param name
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void appendDotClusterHeader(Appendable out, String name) {

        if (name.endsWith(CLUSTERING_EXTENSION))
            name = name.substring(0, name.length() - CLUSTERING_EXTENSION.length());
        try {
            out.append("name=" + name);
            out.append("\n");
            Defaults defaults = Defaults.INSTANCE;
            SimilarityChecker similarityChecker = defaults.getDefaultSimilarityChecker();

            Class<? extends SimilarityChecker> scc = similarityChecker.getClass();
            out.append("similarity_method=" + scc.getSimpleName());
            out.append("\n");


            double defaultSimilarityThreshold = Defaults.getSimilarityThreshold();
            if (similarityChecker instanceof FrankEtAlDotProduct) {
                //noinspection RedundantCast
                defaultSimilarityThreshold = ((FrankEtAlDotProduct) similarityChecker).getDefaultThreshold();
            }
            out.append("threshold=" + defaultSimilarityThreshold);
            out.append("\n");
            out.append("fdr=0");
            out.append("\n");
            out.append("description=" + name);
            out.append("\n");
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * null safe equals function
     *
     * @param o1
     * @param o2
     * @return
     */
    public static boolean equalObject(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 == null || o2 == null)
            return false;
        return o1.equals(o2);
    }

}
