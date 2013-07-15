package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;

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

    private ClusterComparator defaultSpectrumComparator = new ClusterComparator(defaultQualityScorer);

    private IntensityNormalizer normalizer = new TotalIntensityNormalizer();

    private ConsensusSpectrumBuilder defaultConsensusSpectrumBuilder;

    private IClusteringEngineFactory defaultClusteringEngineFactory;

    private Defaults() {

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

    public void setDefaultConsensusSpectrumBuilder(final ConsensusSpectrumBuilder pDefaultConsensusSpectrumBuilder) {
         defaultConsensusSpectrumBuilder = pDefaultConsensusSpectrumBuilder;
    }

    public ConsensusSpectrumBuilder getDefaultConsensusSpectrumBuilder() {
        if (defaultConsensusSpectrumBuilder == null) {
            defaultConsensusSpectrumBuilder = new FrankEtAlConsensusSpectrumBuilder();
        }
         return defaultConsensusSpectrumBuilder;
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
