package uk.ac.ebi.pride.spectracluster.clustersmilarity;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityEnum
 *   Measures the similarity between two clusters
 * @author Steve Lewis
 * @date 14/11/13
 */
public enum ClusterSimilarityEnum  {
   Good,Medium,Bad,NoFit;

    public static int compare(ClusterSimilarityEnum r1, ClusterSimilarityEnum r2) {
        if(r1 == r2)
            return 0;
        switch(r1) {
            case Good:
                return -1;
            case Medium:
                if(r2 == Good)
                     return 1;
                 return -1;
            case Bad:
                if(r2 == NoFit)
                    return -1;
                return 1;
            case NoFit:
                 return 1;
         }
        throw new UnsupportedOperationException("Never get here");
    }


}
