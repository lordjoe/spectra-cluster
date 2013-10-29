package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.datastore.*;
import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;

import javax.sql.*;
 import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.Defaults
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
public class Defaults {

    public static final int DEFAULT_NUMBER_RECLUSTERING_PASSES = 2;


    public static final Defaults INSTANCE = new Defaults();

    private int numberReclusteringPasses = DEFAULT_NUMBER_RECLUSTERING_PASSES;

    private SimilarityChecker defaultSimilarityChecker = new FrankEtAlDotProduct();

    private QualityScorer defaultQualityScorer = new SignalToNoiseChecker();

    private ClusterComparator defaultSpectrumComparator = new ClusterComparator();

    private IntensityNormalizer normalizer = new TotalIntensityNormalizer();

    private IClusteringEngineFactory defaultClusteringEngineFactory;

    private DataSource defaultDataSource;

    private ConcensusSpectrumBuilderFactory consensusFactory =  ConsensusSpectrum.FACTORY;

    private WorkingDatabaseFactory databaseFactory = WorkingClusterDatabase.FACTORY;

    private Defaults() {

    }

    public WorkingDatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public void setDatabaseFactory(WorkingDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public ConcensusSpectrumBuilderFactory getConsensusFactory() {
        return consensusFactory;
    }

    public void setConsensusFactory(final ConcensusSpectrumBuilderFactory pConsensusFactory) {
        consensusFactory = pConsensusFactory;
    }

    public int getNumberReclusteringPasses() {
        return numberReclusteringPasses;
    }

    public void setNumberReclusteringPasses(final int pNumberReclusteringPasses) {
        numberReclusteringPasses = pNumberReclusteringPasses;
    }

    public IntensityNormalizer getNormalizer() {
        return normalizer;
    }

    public synchronized DataSource getDefaultDataSource() {
        if(defaultDataSource == null) {
            defaultDataSource = SpringJDBCUtilities.buildDefaultDataSource();
        }
        return defaultDataSource;
    }

    public void setDefaultDataSource(final DataSource pDefaultDataSource) {
        defaultDataSource = pDefaultDataSource;
    }

    public IClusteringEngine getDefaultClusteringEngine() {
        SimilarityChecker similarityChecker = getDefaultSimilarityChecker();
        Comparator<ISpectralCluster> spectrumComparator = getDefaultSpectrumComparator();
        return ClusteringEngine.getClusteringEngineFactory().getClusteringEngine();

    }


    public int getDefaultNumberReclusteringPasses() {
        return numberReclusteringPasses;
    }

    public void setDefaultNumberReclusteringPasses(final int pNumberReclusteringPasses) {
        numberReclusteringPasses = pNumberReclusteringPasses;
    }

    public IntensityNormalizer getDefaultIntensityNormalizer() {
        return normalizer;
    }

    public void setNormalizer(final IntensityNormalizer pNormalizer) {
        normalizer = pNormalizer;
    }

    public SimilarityChecker getDefaultSimilarityChecker() {
        if (defaultSimilarityChecker == null) {
            defaultSimilarityChecker = new FrankEtAlDotProduct();
        }
        return defaultSimilarityChecker;
    }

    public void setDefaultSimilarityChecker(final SimilarityChecker pDefaultSimilarityChecker) {
        defaultSimilarityChecker = pDefaultSimilarityChecker;
    }

    public void setDefaultQualityScorer(final QualityScorer pDefaultQualityScorer) {
        defaultQualityScorer = pDefaultQualityScorer;
    }

    public void setDefaultSpectrumComparator(final ClusterComparator pDefaultSpectrumComparator) {
        defaultSpectrumComparator = pDefaultSpectrumComparator;
    }

    public void setDefaultClusteringEngineFactory(final IClusteringEngineFactory pDefaultClusteringEngineFactory) {
        defaultClusteringEngineFactory = pDefaultClusteringEngineFactory;
    }

    public IConsensusSpectrumBuilder getDefaultConsensusSpectrumBuilder() {
         return consensusFactory.getConsensusSpectrumBuilder();
    }

    public Comparator<ISpectralCluster> getDefaultSpectrumComparator() {
        return defaultSpectrumComparator;
    }


    public QualityScorer getDefaultQualityScorer() {
        return defaultQualityScorer;
    }

    public IClusteringEngineFactory getDefaultClusteringEngineFactory() {
        if (defaultClusteringEngineFactory == null)
            defaultClusteringEngineFactory = ClusteringEngine.getClusteringEngineFactory();
        return defaultClusteringEngineFactory;
    }
}
