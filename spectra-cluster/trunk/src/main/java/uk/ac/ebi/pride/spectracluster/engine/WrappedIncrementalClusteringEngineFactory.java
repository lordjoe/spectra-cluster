package uk.ac.ebi.pride.spectracluster.engine;

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

    /**
     * make a copy of the clustering engine
     *
     * @return
     */
    public IClusteringEngine getClusteringEngine(Object... otherdata) {
        if (otherdata.length < 1)
            throw new IllegalArgumentException("WrappedClusteringEngine needs a Double as WindowSize"); //
        double windowSize = (Double) otherdata[0];
        final IIncrementalClusteringEngine incrementalClusteringEngine = new IncrementalClusteringEngineFactory().getIncrementalClusteringEngine(windowSize);
        return new WrappedIncrementalClusteringEngine(incrementalClusteringEngine);
    }
}
