package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
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

    private final ISimilarityChecker similarity = Defaults.getDefaultSimilarityChecker();

    private ConsensusSimilarityDistance() {
    }

    public double distance(IPeptideSpectralCluster c1, IPeptideSpectralCluster c2) {
        ISpectrum sp1 = c1.getConsensusSpectrum();
        ISpectrum sp2 = c2.getConsensusSpectrum();
        double d11 = similarity.assessSimilarity(sp1, sp1);
        double d22 = similarity.assessSimilarity(sp2, sp2);
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
