package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * uk.ac.ebi.pride.spectracluster.ClusterUtilities
 *
 * @author Steve Lewis
 * @date 5/10/13
 */
public class ClusterUtilities {
    public static ClusterUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ClusterUtilities.class;

    private static final Map<String,ISpectralCluster> gIdTpCluster = new ConcurrentHashMap<String, ISpectralCluster>() ;

    /**
     * use when going to a completely new cluster set - say in a reducer
     */
    public static void clearClusters()
    {
        gIdTpCluster.clear();
    }
    /**
     * get a cluster given an id building it as needed
     *
     * @param id !null or empty id
     * @return !null cluster
     */
    public static ISpectralCluster getCluster(String id,int charge) {
        ISpectralCluster ret = gIdTpCluster.get(id);
        if(ret != null)   {    // already exists
            if(ret.getCharge() != charge)
                throw new IllegalStateException("cannot change charge on a cluster");
            return ret;
        }
        ret = new MultiSpectrumCluster(id, charge);
        gIdTpCluster.put(id,ret) ; // remember this custer so id not reused
        return ret;
    }

    /**
     * get a cluster given an id building it as needed
     *
     * @param id !null or empty id
     * @return !null cluster
     */
    public static ISpectralCluster getCLuster(String id, double mz, int charge, ISpecClusterPeak[] peaks) {
        ISpectralCluster ret = gIdTpCluster.get(id);
          if(ret != null)   {    // already exists
              if(ret.getCharge() != charge)
                  throw new IllegalStateException("cannot change charge on a cluster");
              throw new UnsupportedOperationException("Is this a legal case??"); // ToDo better check spectra ...
             // return ret;
          }
         ret = new SingleSpectrum(id, mz, charge, peaks).asCluster();
         gIdTpCluster.put(id,ret) ; // remember this custer so id not reused
         return ret;
    }

    /**
     * binn the mz range finding the highest peaks in each bin
     * @param peaks   !null original peaks
     * @param minMZ minmum mz to bin
     * @param maxMZ maximum mz to bin
     * @param binSize size of each bin
     * @param maxPerBin maximim peaks to retain
     * @return
     */
    public static ISpecClusterPeak[] getHighestInBins(ISpecClusterPeak[] peaks,
                                                      double minMZ,
                                                      double maxMZ,
                                                      double binSize,
                                                      int maxPerBin)
    {
        // handles the binning as am object
        LinearBinner binner = new LinearBinner(maxMZ,minMZ,binSize);
        /// these will keep the highest values
        PriorityQueue<ISpecClusterPeak>[]  higheseEachBin =
                (PriorityQueue<ISpecClusterPeak>[])new   PriorityQueue[binner.getNumberBins()];
        // for all peaks
        for (int i = 0; i < peaks.length; i++) {
            ISpecClusterPeak pk = peaks[i];
            // bin mz
            int bin = binner.asBin(pk.getMassChargeRatio()) ;

            // get the queue creating as needed
            PriorityQueue<ISpecClusterPeak> hightestInBin = higheseEachBin[bin];
            // lazily build only as needed
            if(hightestInBin == null)  {
                // saves the highest intensity peaks up to  maxPerBin
                // because the comparison is on intensity
                  hightestInBin =new PriorityQueue<ISpecClusterPeak>(maxPerBin,
                                        ISpecClusterPeak.BY_INTENSITY);
                higheseEachBin[bin] = hightestInBin;
            }

            // add the peak
            hightestInBin.add(pk); // add - sort and keep highest is internam


        }

        // now grab all peaks in a list
       // will hold retained peaks
        List<ISpecClusterPeak> holder = new ArrayList<ISpecClusterPeak>();
        for (int i = 0; i < higheseEachBin.length; i++) {
            PriorityQueue<ISpecClusterPeak> hightestInBin = higheseEachBin[i];
            if(hightestInBin != null)
                 holder.addAll(hightestInBin);
        }
        // back to an array
        ISpecClusterPeak[] ret = new ISpecClusterPeak[holder.size()];
        holder.toArray(ret);
        return ret;

    }
}
