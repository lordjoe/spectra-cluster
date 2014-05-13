package uk.ac.ebi.pride.spectracluster.clustersmilarity;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityProblem
 *
 * @author Steve Lewis
 * @date 14/11/13
 */
public enum ClusterSimilarityProblem {
    UnevenSize,
    BadFit,
    FewCommonSpectra,
    NoSpectra,
    CloseSecondCluster,
    DifferentBestPeptide,
    HighDotProduct
}
