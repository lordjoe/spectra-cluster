package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.comparator.DefaultClusterComparator;

/**
 * Factory for making PeakMatchClusteringEngine
 *
 * The only reason to keep this factory class is for the default configuration
 *
 * @author Rui Wang
 * @version $Id$
 *
 * todo: development
 */
@Deprecated
public class PeakMatchClusteringEngineFactory {

    /**
     * build a new version
     *
     * @return
     */
    public PeakMatchClusteringEngine getPeakMatchClusteringEngine() {
        final FrankEtAlDotProduct similarityChecker = new FrankEtAlDotProduct(Defaults.getSimilarityMZRange(), Defaults.getNumberComparedPeaks());
        final DefaultClusterComparator comparator = new DefaultClusterComparator();
        final ClusteringEngine clusteringEngine = new ClusteringEngine(similarityChecker, comparator);
        return new PeakMatchClusteringEngine(similarityChecker, comparator, clusteringEngine);
    }
}
