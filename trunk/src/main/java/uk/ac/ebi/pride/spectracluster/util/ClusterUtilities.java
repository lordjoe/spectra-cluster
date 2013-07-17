package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;


/**
 * uk.ac.ebi.pride.spectracluster.util.ClusterUtilities
 *    a lits of stateless static functions for manipulating clusters, lists of clusters
 *    and performing other common chores
 * @author Steve Lewis
 * @date 5/10/13
 */
public class ClusterUtilities {
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
     *  take out all clusters consisting of a single spectrum and return them as a list
     *  - engines will do this as a step in reclustering
     * @param clusters !null   a list of clusters - this WILL be modified
     * @return !null list of clusters containing a single spectrum
     */
     public static List<ISpectralCluster> removeSingleSpectrumClusters(List<ISpectralCluster> clusters) {
         return removeSingleSpectrumClustersSizedLessThan( clusters,1);
     }

    /**
     *  take out all clusters sized les shtne size and return them as a list of single spectrum clusters
     *  - engines will do this as a step in reclustering
     * @param pClusters  !null   a list of clusters - this WILL be modified
     * @param size - clusters sized less than or equal to this will be removed and returned as single spectrum clusters
     * @return !null list of clusters containing a single spectrum
     */
    public static List<ISpectralCluster> removeSingleSpectrumClustersSizedLessThan(final List<ISpectralCluster> pClusters, final int size) {
        List<ISpectralCluster> retained = new ArrayList<ISpectralCluster>();
        List<ISpectralCluster> asSingleSpectra = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : pClusters) {
            if(cluster.getClusteredSpectraCount() <= size)   {
                final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
                for (ISpectrum spectrum : clusteredSpectra) {
                    asSingleSpectra.add(spectrum.asCluster()); // di not retain but return as one spectrum clusters
                }
            }
            else {
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
     * @return as above
     */
    public static String mostCommonPeptides(ISpectralCluster cluster) {
        final List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
        String petides =  mostCommonPeptides(clusteredSpectra) ;
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


    /**
     * merge clusters among themselves returning a list of surviving clusters which will replace mergable
     * @param mergable  !null list of clusters
     * @param similarityChecker !null simil;atity checker
     * @param maxMZDIfference  max difference to consider merging
     * @return !null list of new clusters
     */
     public static  List<ISpectralCluster> mergeClusters(List<ISpectralCluster> mergable,SimilarityChecker similarityChecker,double maxMZDIfference) {
         List<ISpectralCluster> retained = new ArrayList<ISpectralCluster>();
         // clusters need to be compared with all clusters below them
         for (ISpectralCluster cluster : mergable) {
             double currentMZ = cluster.getPrecursorMz();
             double minimimMergableMZ = currentMZ - maxMZDIfference;
             ISpectralCluster mergeWith = null;
             if(retained.isEmpty())  {   // start with the first cluster
                 retained.add(cluster);
                 continue;
             }

             // start at the top mz cluster
             for (int index = retained.size() - 1; index >= 0; index--) {
                 ISpectralCluster test = retained.get(index);
                 if (test.getPrecursorMz() < minimimMergableMZ)
                     break; // no more to consider
                 final ISpectrum cs1 = test.getConsensusSpectrum();
                 double similarity =  similarityChecker.assessSimilarity(cs1, cluster.getConsensusSpectrum());
                if(similarity >= similarityChecker.getDefaultThreshold())   {
                    mergeWith = test;
                    break; // found who to merge with
                }

             }
             if(mergeWith == null) {
                 retained.add(cluster); // nothing to merge with so keep the cluster
             }
             else {  // merge with a close enough cluster
                 mergeWith.addSpectra(cluster.getClusteredSpectra());
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
     * @param mergable !null list of clusters - the clusters will change but not change in number
     * @param singles  !null list of single spectra
     * @param similarityChecker  !null similarity
     * @param maxMZDIfference  ! maxMZ difference to look at for merging
     * @return   !null list of non-merged clusters from singles
     */
     public static  List<ISpectralCluster> mergeClustersWithSingleSpectra(List<ISpectralCluster> mergable,List<ISpectralCluster> singles,SimilarityChecker similarityChecker,double maxMZDIfference) {
         List<ISpectralCluster> retainedSingleSpectra = new ArrayList<ISpectralCluster>();
         int startIndex = 0;
         // clusters need to be compared with all clusters below them
         for (ISpectralCluster cluster : singles) {
             double currentMZ = cluster.getPrecursorMz();
             double minimimMergableMZ = currentMZ - maxMZDIfference;
             ISpectralCluster mergeWith = null;
             // start at the top mz cluster
             for (int index = startIndex; index > mergable.size(); index++) {
                 ISpectralCluster test = mergable.get(index);
                 if(index == startIndex)  {
                     if (test.getPrecursorMz() < minimimMergableMZ)   {
                         startIndex++;
                         continue; // try again
                     }
                 }
                double distance =  similarityChecker.assessSimilarity(test.getConsensusSpectrum(),cluster.getConsensusSpectrum());
                if(distance <= similarityChecker.getDefaultThreshold())   {
                    mergeWith = test;
                    break; // found who to merge with
                }

             }
             if(mergeWith == null) {
                 retainedSingleSpectra.add(cluster); // nothing to merge with so keep single
             }
             else {  // merge with a close enough cluster
                 // note this may disturb the order of the cluster list but should not stop
                 // the algorithm from working
                 mergeWith.addSpectra(cluster.getClusteredSpectra());
             }
         }


          // make sure the mergable are still in mz order
         Collections.sort(mergable);


          // make sure the retainedSingleSpectra are still in mz order
         Collections.sort(retainedSingleSpectra);
         return retainedSingleSpectra;
     }


}
