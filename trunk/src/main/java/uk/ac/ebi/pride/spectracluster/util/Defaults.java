package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.Defaults
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
public class Defaults {

    public static final Defaults INSTANCE = new Defaults();

    private Class<? extends IntensityNormalizer> normalizerClass = TotalIntensityNormalizer.class;

    private Class<? extends  ConsensusSpectrumBuilder > consensusSpectrumBuilderClass = FrankEtAlConsensusSpectrumBuilder.class;

    private Class<? extends SimilarityChecker> similarityCheckerClass = FrankEtAlDotProduct.class;

    private Class<? extends IClusteringEngine> clusteringEngineClass = StupidClusteringEngine.class;



    private Defaults() {

    }

    public IClusteringEngine getDefaultClusteringEngine()
    {
        try {
            return getClusteringEngineClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public IntensityNormalizer getDefaultIntensityNormalizer()
     {
         try {
             return getNormalizerClass().newInstance();
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }

    public SimilarityChecker getDefaultSimilarityChecker()
    {
        try {
            return getSimilarityCheckerClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ConsensusSpectrumBuilder getDefaultConsensusSpectrumBuilder()
    {
        try {
            return getConsensusSpectrumBuilderClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<? extends IntensityNormalizer> getNormalizerClass() {
        return normalizerClass;
    }

    public void setNormalizerClass(Class<? extends IntensityNormalizer> normalizerClass) {
        this.normalizerClass = normalizerClass;
    }

    public Class<? extends ConsensusSpectrumBuilder> getConsensusSpectrumBuilderClass() {
        return consensusSpectrumBuilderClass;
    }

    public void setConsensusSpectrumBuilderClass(Class<? extends ConsensusSpectrumBuilder> consensusSpectrumBuilderClass) {
        this.consensusSpectrumBuilderClass = consensusSpectrumBuilderClass;
    }

    public Class<? extends SimilarityChecker> getSimilarityCheckerClass() {
        return similarityCheckerClass;
    }

    public void setSimilarityCheckerClass(Class<? extends SimilarityChecker> similarityCheckerClass) {
        this.similarityCheckerClass = similarityCheckerClass;
    }

    public Class<? extends IClusteringEngine> getClusteringEngineClass() {
        return clusteringEngineClass;
    }

    public void setClusteringEngineClass(Class<? extends IClusteringEngine> clusteringEngineClass) {
        this.clusteringEngineClass = clusteringEngineClass;
    }
}
