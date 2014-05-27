package uk.ac.ebi.pride.spectracluster.clustersmilarity;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityEnum
 * Measures the similarity between two clusters
 *
 * @author Steve Lewis
 * @date 14/11/13
 */
public enum ClusterSimilarityEnum {
    Identical(0), Subset(1), CloseSubset(2), Good(3), Medium(4), Bad(5), NoFit(6);

    private int value;

    ClusterSimilarityEnum(final int pValue) {
        value = pValue;
    }

    public int getValue() {
        return value;
    }

    /**
     * true if the relationship is a true or close subset and not equality
     *
     * @return
     */
    public boolean isSubset() {
        switch (this) {
            case Subset:
            case CloseSubset:
                return true;
            default:
                return false;
        }
    }

    public static int compare(ClusterSimilarityEnum r1, ClusterSimilarityEnum r2) {
        if (r1 == r2)
            return 0;
        if (r1.value < r2.value)
            return -1;
        else
            return 1;
    }


}
