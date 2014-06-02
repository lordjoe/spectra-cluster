package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.comparator.ClusterComparator;

/**
 * Factory for making ClusteringEngine
 * <p/>
 * The only reason to keep this factory class is for the default configuration
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineFactory {

    /**
     * make a copy of the clustering engine
     *
     * @return
     */
    public IClusteringEngine getClusteringEngine() {
        final ISimilarityChecker similarityChecker = Defaults.getDefaultSimilarityChecker();
        final ClusterComparator comparator = Defaults.getDefaultSpectrumComparator();
        final double similarityThreshold = Defaults.getSimilarityThreshold();
        return new ClusteringEngine(similarityChecker, comparator, similarityThreshold);
    }

}
