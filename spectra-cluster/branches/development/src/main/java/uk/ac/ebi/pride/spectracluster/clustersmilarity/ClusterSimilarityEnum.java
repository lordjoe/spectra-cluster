package uk.ac.ebi.pride.spectracluster.clustersmilarity;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityEnum
 * Measures the similarity between two clusters
 *
 * @author Steve Lewis
 * @date 14/11/13
 */
public enum ClusterSimilarityEnum {
    Identical(0), Subset(1), Good(2), Medium(3), Bad(4), NoFit(5);

    private int value;

    ClusterSimilarityEnum(final int pValue) {
        value = pValue;
    }

    public int getValue() {
        return value;
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
