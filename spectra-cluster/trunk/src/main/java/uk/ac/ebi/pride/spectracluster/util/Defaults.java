package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.utilities.Util;
import org.systemsbiology.hadoop.IParameterHolder;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.consensus.ConcensusSpectrumBuilderFactory;
import uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrum;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.engine.ClusteringEngineFactory;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.normalizer.IIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.normalizer.TotalIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.comparator.DefaultClusterComparator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Comparator;

/**
 * uk.ac.ebi.pride.spectracluster.util.Defaults
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
public class Defaults {

    private static final int DEFAULT_NUMBER_RECLUSTERING_PASSES = 2;

    private static final int NUMBER_COMPARED_PEAKS = 15;

    private static final double DEFAULT_MZ_RANGE = 0.5;

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.6;

    private static final double DEFAULT_RETAIN_THRESHOLD = 0.5;

    private static final int LARGE_BINNING_REGION = 1000;

    private static double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;

    private static int largeBinningRegion = LARGE_BINNING_REGION;

    private static int numberComparedPeaks = NUMBER_COMPARED_PEAKS;

    private static double similarityMZRange = DEFAULT_MZ_RANGE;

    private static double retainThreshold = DEFAULT_RETAIN_THRESHOLD;

    public static double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public static int getLargeBinningRegion() {
        return largeBinningRegion;
    }

    public static int getNumberComparedPeaks() {
        return numberComparedPeaks;
    }

    public static double getSimilarityMZRange() {
        return similarityMZRange;
    }

    public static double getRetainThreshold() {
        return retainThreshold;
    }

    public static void setSimilarityThreshold(double similarityThreshold) {
        Defaults.similarityThreshold = similarityThreshold;
    }

    public static void setLargeBinningRegion(int largeBinningRegion) {
        Defaults.largeBinningRegion = largeBinningRegion;
    }

    public static void setNumberComparedPeaks(int numberComparedPeaks) {
        Defaults.numberComparedPeaks = numberComparedPeaks;
    }

    public static void setSimilarityMZRange(double similarityMZRange) {
        Defaults.similarityMZRange = similarityMZRange;
    }

    public static void setRetainThreshold(double retainThreshold) {
        Defaults.retainThreshold = retainThreshold;
    }

    /**
     * This must be here to make sure the variables before are initialized
     */
    public static final Defaults INSTANCE = new Defaults();

    private int numberReclusteringPasses = DEFAULT_NUMBER_RECLUSTERING_PASSES;

    private ISimilarityChecker defaultSimilarityChecker = new FrankEtAlDotProduct();

    private IQualityScorer defaultQualityScorer = new SignalToNoiseChecker();

    private DefaultClusterComparator defaultSpectrumComparator = new DefaultClusterComparator();

    private IIntensityNormalizer normalizer = new TotalIntensityNormalizer();

    private ClusteringEngineFactory defaultClusteringEngineFactory;

    private ConcensusSpectrumBuilderFactory consensusFactory = ConsensusSpectrum.FACTORY;

    private Defaults() {

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

    public IIntensityNormalizer getNormalizer() {
        return normalizer;
    }

    public IClusteringEngine getDefaultClusteringEngine() {
        ISimilarityChecker similarityChecker = getDefaultSimilarityChecker();
        Comparator<ICluster> spectrumComparator = getDefaultSpectrumComparator();
        return new ClusteringEngineFactory(similarityChecker, spectrumComparator).getClusteringEngine();

    }


    public int getDefaultNumberReclusteringPasses() {
        return numberReclusteringPasses;
    }

    public void setDefaultNumberReclusteringPasses(final int pNumberReclusteringPasses) {
        numberReclusteringPasses = pNumberReclusteringPasses;
    }

    public IIntensityNormalizer getDefaultIntensityNormalizer() {
        return normalizer;
    }

    public void setNormalizer(final IIntensityNormalizer pNormalizer) {
        normalizer = pNormalizer;
    }

    public ISimilarityChecker getDefaultSimilarityChecker() {
        if (defaultSimilarityChecker == null) {
            defaultSimilarityChecker = new FrankEtAlDotProduct();
        }
        return defaultSimilarityChecker;
    }

    public void setDefaultSimilarityChecker(final ISimilarityChecker pDefaultSimilarityChecker) {
        defaultSimilarityChecker = pDefaultSimilarityChecker;
    }

    public void setDefaultQualityScorer(final IQualityScorer pDefaultQualityScorer) {
        defaultQualityScorer = pDefaultQualityScorer;
    }

    public void setDefaultSpectrumComparator(final DefaultClusterComparator pDefaultSpectrumComparator) {
        defaultSpectrumComparator = pDefaultSpectrumComparator;
    }

    public void setDefaultClusteringEngineFactory(final ClusteringEngineFactory pDefaultClusteringEngineFactory) {
        defaultClusteringEngineFactory = pDefaultClusteringEngineFactory;
    }

    public IConsensusSpectrumBuilder getDefaultConsensusSpectrumBuilder() {
        return consensusFactory.getConsensusSpectrumBuilder();
    }

    public Comparator<ICluster> getDefaultSpectrumComparator() {
        return defaultSpectrumComparator;
    }


    public IQualityScorer getDefaultQualityScorer() {
        return defaultQualityScorer;
    }

    /**
     * this method and the one below
     *
     * @param application source of parameters
     */
    public static void configureAnalysisParameters(@Nonnull IParameterHolder application) {
        setLargeBinningRegion(application.getIntParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.LargeBinningRegion", LARGE_BINNING_REGION));
        setNumberComparedPeaks(application.getIntParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.NumberComparedPeaks", NUMBER_COMPARED_PEAKS));
        setSimilarityMZRange(application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityMZRange", DEFAULT_MZ_RANGE));
        setRetainThreshold(application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.RetainThreshold", DEFAULT_RETAIN_THRESHOLD));
        setSimilarityThreshold(application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityThreshold", DEFAULT_SIMILARITY_THRESHOLD));
    }

    /**
     * used to write parameters in to a data sink like a clustering file
     *
     * @param out output
     */
    public static void appendAnalysisParameters(@Nonnull Appendable out) {
        try {
            out.append("largeBinningRegion=").append(String.valueOf(getLargeBinningRegion())).append("\n");
            out.append("numberComparedPeaks=").append(String.valueOf(getNumberComparedPeaks())).append("\n");
            out.append("similarityMZRange=").append(Util.formatDouble(getSimilarityMZRange(), 3)).append("\n");
            out.append("similarityThreshold=").append(Util.formatDouble(getSimilarityThreshold(), 3)).append("\n");
            out.append("retainThreshold=").append(Util.formatDouble(getRetainThreshold(), 3)).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

}
