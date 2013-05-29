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
    private static final Map<String,ISpectralCluster> ID_TO_CLUSTER = new ConcurrentHashMap<String, ISpectralCluster>() ;

    public ISpectralCluster getById(String id)
    {
        return ID_TO_CLUSTER.get(id);
    }

    /**
      * return a list of all spectra in the list of clusters sorted by charge then mz
      * @param clusters !null list of clusters
      * @return   !null list of spectra
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
       * return a list of all spectra in the list of spectra sorted by charge then mz
       * @param spectra !null list of spectra
       * @return   !null list of spectra
       */
      public static List<ISpectralCluster> asClusters(List<IPeptideSpectrumMatch> spectra) {
          List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
          for (IPeptideSpectrumMatch spectrum : spectra) {
              holder.add(spectrum.asCluster());
          }
          Collections.sort(holder);
          return holder;
      }
    /**
     * use when going to a completely new cluster set - say in a reducer
     */
    public static void clearClusters()
    {
        ID_TO_CLUSTER.clear();
    }
    /**
     * get a cluster given an id building it as needed
     *
     * @param id !null or empty id
     * @return !null cluster
     */
//    public static ISpectralCluster getCluster(String id,int charge) {
//        ISpectralCluster ret = ID_TO_CLUSTER.get(id);
//        if(ret != null)   {    // already exists
//            if(ret.getPrecursorCharge() != charge)
//                throw new IllegalStateException("cannot change charge on a cluster");
//            return ret;
//        }
//        ret = new SpectralCluster(id, charge);
//        ID_TO_CLUSTER.put(id, ret) ; // remember this custer so id not reused
//        return ret;
//    }

    /**
     * get a cluster given an id building it as needed
     *
     * @param id !null or empty id
     * @return !null cluster
     */
//    public static ISpectralCluster getCLuster(String id, double mz, int charge, IPeak[] peaks) {
//        ISpectralCluster ret = ID_TO_CLUSTER.get(id);
//          if(ret != null)   {    // already exists
//              if(ret.getCharge() != charge)
//                  throw new IllegalStateException("cannot change charge on a cluster");
//              throw new UnsupportedOperationException("Is this a legal case??"); // ToDo better check spectra ...
//             // return ret;
//          }
//         ret = new SingleSpectrum(id, mz, charge, peaks).asCluster();
//         ID_TO_CLUSTER.put(id, ret) ; // remember this custer so id not reused
//         return ret;
//    }
//
    /**
     * binn the mz range finding the highest peaks in each bin
     * @param peaks   !null original peaks
     * @param minMZ minmum mz to bin
     * @param maxMZ maximum mz to bin
     * @param binSize size of each bin
     * @param maxPerBin maximim peaks to retain
     * @return
     */
    public static List<IPeak> getHighestInBins(List<IPeak> peaks,
                                                      double minMZ,
                                                      double maxMZ,
                                                      double binSize,
                                                      int maxPerBin)
    {
        // handles the binning as am object
        LinearBinner binner = new LinearBinner(maxMZ,binSize,minMZ);
        /// these will keep the highest values
        PriorityQueue<IPeak>[]  higheseEachBin =
                (PriorityQueue<IPeak>[])new PriorityQueue[binner.getNumberBins()];
        // for all peaks
        for (IPeak pk : peaks) {
            // bin mz
            int bin = binner.asBin(pk.getMz()) ;

            // get the queue creating as needed
            PriorityQueue<IPeak> hightestInBin = higheseEachBin[bin];
            // lazily build only as needed
            if(hightestInBin == null)  {
                // saves the highest intensity peaks up to  maxPerBin
                // because the comparison is on intensity
                  hightestInBin =new PriorityQueue<IPeak>(maxPerBin,
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
            if(hightestInBin != null)
                 holder.addAll(hightestInBin);
        }
        // back to an array
         return holder;

    }
}
