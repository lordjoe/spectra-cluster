package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.Comparator;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
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
