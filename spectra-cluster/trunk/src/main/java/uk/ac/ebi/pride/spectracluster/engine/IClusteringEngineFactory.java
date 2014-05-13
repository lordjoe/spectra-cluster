package uk.ac.ebi.pride.spectracluster.engine;

/**
 * uk.ac.ebi.pride.spectracluster.engine.IClusteringEngineFactory
 * a good way to produce clustering engines
 * User: Steve
 * Date: 7/5/13
 */
public interface IClusteringEngineFactory {
    /**
     * make a copy of the clustering engine
     *
     * @return
     */
    public IClusteringEngine getClusteringEngine(Object... other);

}
