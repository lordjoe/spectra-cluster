package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.comparator.DefaultClusterComparator;

import java.util.Comparator;

/**
 * Factory for making ClusteringEngine
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineFactory implements IClusteringEngineFactory {
    private final ISimilarityChecker similarityChecker;
    private final Comparator<ICluster> spectrumComparator;

    public ClusteringEngineFactory() {
        this.similarityChecker = new FrankEtAlDotProduct();
        this.spectrumComparator = DefaultClusterComparator.INSTANCE;
    }

    public ClusteringEngineFactory(final ISimilarityChecker pSimilarityChecker, final Comparator<ICluster> pSpectrumComparator) {
        similarityChecker = pSimilarityChecker;
        spectrumComparator = pSpectrumComparator;
    }

    /**
     * make a copy of the clustering engine
     *
     * @return
     */
    @Override
    public IClusteringEngine getClusteringEngine(Object... other) {
        return new ClusteringEngine(similarityChecker, spectrumComparator);
    }
}
