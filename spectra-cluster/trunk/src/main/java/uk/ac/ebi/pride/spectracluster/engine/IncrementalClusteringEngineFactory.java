package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.comparator.DefaultClusterComparator;

/**
 * Factory for making IncrementalClusteringEngine
 * <p/>
 * The only reason to keep this factory class is for the default configuration
 *
 * @author Rui Wang
 * @version $Id$
 */
public class IncrementalClusteringEngineFactory {

    /**
     * build a new version
     *
     * @return
     */
    public IIncrementalClusteringEngine getIncrementalClusteringEngine(float windowSize) {
        final FrankEtAlDotProduct similarityChecker = new FrankEtAlDotProduct(Defaults.getSimilarityMZRange(), Defaults.getNumberComparedPeaks());
        final DefaultClusterComparator comparator = new DefaultClusterComparator();
        final double similarityThreshold = Defaults.getSimilarityThreshold();
        return new IncrementalClusteringEngine(similarityChecker, comparator, windowSize, similarityThreshold);
    }
}
