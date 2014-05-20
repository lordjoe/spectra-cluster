package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.Comparator;

/**
 * Factory for making ClusteringEngine
 *
 * The only reason to keep this factory class is for the default configuration
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineFactory {
    private final ISimilarityChecker similarityChecker;
    private final Comparator<ICluster> spectrumComparator;

    public ClusteringEngineFactory() {
        this.similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        this.spectrumComparator = Defaults.INSTANCE.getDefaultSpectrumComparator();
    }

    public ClusteringEngineFactory(final ISimilarityChecker pSimilarityChecker,
                                   final Comparator<ICluster> pSpectrumComparator) {
        similarityChecker = pSimilarityChecker;
        spectrumComparator = pSpectrumComparator;
    }

    /**
     * make a copy of the clustering engine
     *
     * @return
     */
    public IClusteringEngine getClusteringEngine(Object... other) {
        return new ClusteringEngine(similarityChecker, spectrumComparator);
    }
}
