package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.Comparator;

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
    private final ISimilarityChecker similarityChecker;
    private final Comparator<ICluster> spectrumComparator;

    public PeakMatchClusteringEngineFactory() {
        this.similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        this.spectrumComparator = Defaults.INSTANCE.getDefaultSpectrumComparator();
    }

    public PeakMatchClusteringEngineFactory(final ISimilarityChecker pSimilarityChecker,
                                              final Comparator<ICluster> pSpectrumComparator) {
        similarityChecker = pSimilarityChecker;
        spectrumComparator = pSpectrumComparator;
    }


    /**
     * build a new version
     *
     * @return
     */
    public PeakMatchClusteringEngine getPeakMatchClusteringEngine() {
        return new PeakMatchClusteringEngine(similarityChecker, spectrumComparator);
    }
}
