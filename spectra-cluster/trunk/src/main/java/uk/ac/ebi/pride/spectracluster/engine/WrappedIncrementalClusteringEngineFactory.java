package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.Comparator;

/**
 * Factory for making WrappedIncrementalClusteringEngine
 *
 * The only reason to keep this factory class is for the default configuration
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 *
 * todo: development since it is only used in unit tests
 */
@Deprecated
public class WrappedIncrementalClusteringEngineFactory {

    private final IncrementalClusteringEngineFactory incrementalFactory;

    public WrappedIncrementalClusteringEngineFactory() {
        incrementalFactory = new IncrementalClusteringEngineFactory(Defaults.INSTANCE.getDefaultSimilarityChecker(), Defaults.INSTANCE.getDefaultSpectrumComparator());
    }

    public WrappedIncrementalClusteringEngineFactory(final ISimilarityChecker pSimilarityChecker, final Comparator<ICluster> pSpectrumComparator) {
        incrementalFactory = new IncrementalClusteringEngineFactory(pSimilarityChecker, pSpectrumComparator);
    }

    /**
     * make a copy of the clustering engine
     *
     * @return
     */
    public IClusteringEngine getClusteringEngine(Object... otherdata) {
        if (otherdata.length < 1)
            throw new IllegalArgumentException("WrappedClusteringEngine needs a Double as WindowSize"); //
        double windowSize = (Double) otherdata[0];
        return new WrappedIncrementalClusteringEngine(incrementalFactory.getIncrementalClusteringEngine(windowSize));
    }
}
