package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.Comparator;

/**
 * Factory for making IncrementalClusteringEngine
 *
 * The only reason to keep this factory class is for the default configuration
 *
 * @author Rui Wang
 * @version $Id$
 */
public class IncrementalClusteringEngineFactory {

    private final ISimilarityChecker similarityChecker;
    private final Comparator<ICluster> spectrumComparator;

    public IncrementalClusteringEngineFactory() {
        this.similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        this.spectrumComparator = Defaults.INSTANCE.getDefaultSpectrumComparator();
    }

    public IncrementalClusteringEngineFactory(final ISimilarityChecker pSimilarityChecker,
                                              final Comparator<ICluster> pSpectrumComparator) {
        similarityChecker = pSimilarityChecker;
        spectrumComparator = pSpectrumComparator;
    }


    /**
     * build a new version
     *
     * @return
     */
    public IIncrementalClusteringEngine getIncrementalClusteringEngine(double windowSize) {
        return new IncrementalClusteringEngine(similarityChecker, spectrumComparator, windowSize);
    }
}
