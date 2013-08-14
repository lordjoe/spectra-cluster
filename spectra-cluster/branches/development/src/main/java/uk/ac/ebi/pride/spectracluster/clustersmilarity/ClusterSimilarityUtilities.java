package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityUtilities
 * User: Steve
 * Date: 6/27/13
 */
public class ClusterSimilarityUtilities {

    /**
     * return all clusters with more than one spectrum
     * @param clusters !null list of clusters
     * @return  !null list of clusters
     */
    public static List<ISpectralCluster>  nonSingleClusters( List<ISpectralCluster> clusters)     {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
          for (ISpectralCluster cluster : clusters) {
            if(cluster.getClusteredSpectraCount() > 1)
                holder.add(cluster);
        }
        return holder;
    }

    /**
      * return all clusters with only  one spectrum
      * @param clusters !null list of clusters
      * @return  !null list of clusters
      */
     public static List<ISpectralCluster>  singleClusters( List<ISpectralCluster> clusters)     {
         List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
           for (ISpectralCluster cluster : clusters) {
             if(cluster.getClusteredSpectraCount() == 1)
                 holder.add(cluster);
         }
         return holder;
     }

}
