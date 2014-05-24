package uk.ac.ebi.pride.spectracluster.hadoop;

import org.systemsbiology.hadoop.IParameterHolder;
import org.systemsbiology.hadoop.ISetableParameterHolder;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.NumberUtilities;
import uk.ac.ebi.pride.spectracluster.util.StableClusterUtilities;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 *
 * Note: Rui has moved all the properties used into this class
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ConfigurableProperties {

    public static final String LARGE_BINNING_REGION_PROPERTY = "uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.LargeBinningRegion";
    public static final String NUMBER_COMPARED_PEAKS_PROPERTY = "uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.NumberComparedPeaks";
    public static final String SIMILARITY_MZ_RANGE_PROPERTY = "uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityMZRange";
    public static final String RETAIN_THRESHOLD_PROPERTY = "uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.RetainThreshold";
    public static final String SIMILARITY_THRESHOLD_PROPERTY = "uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityThreshold";
    public static final String STABLE_CLUSTER_SIZE_PROPERTY = "uk.ac.ebi.pride.spectracluster.util.ClusterUtilities.StableClusterSize";
    public static final String SEMI_STABLE_CLUSTER_SIZE_PROPERTY = "uk.ac.ebi.pride.spectracluster.util.ClusterUtilities.SemiStableClusterSize";

    public static void setStableClusterSizeFromProperties(final ISetableParameterHolder pApplication) {
        int stableClusterSize = pApplication.getIntParameter(STABLE_CLUSTER_SIZE_PROPERTY, StableClusterUtilities.DEFAULT_STABLE_CLUSTER_SIZE);
        StableClusterUtilities.setStableClusterSize(stableClusterSize);
        int semiStableClusterSize = pApplication.getIntParameter(SEMI_STABLE_CLUSTER_SIZE_PROPERTY, StableClusterUtilities.DEFAULT_SEMI_STABLE_CLUSTER_SIZE);
        StableClusterUtilities.setSemiStableClusterSize(semiStableClusterSize);
    }

    /**
     * this method and the one below
     *
     * @param application source of parameters
     */
    public static void configureAnalysisParameters(@Nonnull IParameterHolder application) {
        Defaults.setLargeBinningRegion(application.getIntParameter(LARGE_BINNING_REGION_PROPERTY, Defaults.DEFAULT_LARGE_BINNING_REGION));
        Defaults.setNumberComparedPeaks(application.getIntParameter(NUMBER_COMPARED_PEAKS_PROPERTY, Defaults.DEFAULT_NUMBER_COMPARED_PEAKS));
        Defaults.setSimilarityMZRange(application.getDoubleParameter(SIMILARITY_MZ_RANGE_PROPERTY, Defaults.DEFAULT_MZ_RANGE));
        Defaults.setRetainThreshold(application.getDoubleParameter(RETAIN_THRESHOLD_PROPERTY, Defaults.DEFAULT_RETAIN_THRESHOLD));
        Defaults.setSimilarityThreshold(application.getDoubleParameter(SIMILARITY_THRESHOLD_PROPERTY, Defaults.DEFAULT_SIMILARITY_THRESHOLD));
    }



}
