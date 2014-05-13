package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ConsensusSilimarityDistance
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ConsensusSimilarityDistance implements IClusterDistance {

    public static final String VERSION = "0.1";
    public static final ConsensusSimilarityDistance INSTANCE = new ConsensusSimilarityDistance();

    private final SimilarityChecker similarity = Defaults.INSTANCE.getDefaultSimilarityChecker();

    private ConsensusSimilarityDistance() {
    }

    public double distance(ISpectralCluster c1, ISpectralCluster c2) {
        ISpectrum sp1 = c1.getConsensusSpectrum();
        ISpectrum sp2 = c2.getConsensusSpectrum();
        double d11 = sp1.getSelfDotProduct();
        double d22 = sp2.getSelfDotProduct();
        double dist = similarity.assessSimilarity(sp1, sp2);

        return 1.0 - (dist * dist) / (d11 * d22);
    }

    /**
     * return a name which should not change
     *
     * @return !null name
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * return a version number - this may be updated over time
     *
     * @return !null version
     */
    @Override
    public String getCurrentVersion() {
        return VERSION;
    }

    /**
     * if distance is further than the return value we consider the distance is
     * not worth considering
     *
     * @return
     */
    @Override
    public double getMinimalMatchDistance() {
        return DEFAULT_MINIMAL_DISTANCE;
    }


}
