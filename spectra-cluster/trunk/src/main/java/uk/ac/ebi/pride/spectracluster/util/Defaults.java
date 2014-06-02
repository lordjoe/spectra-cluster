package uk.ac.ebi.pride.spectracluster.util;

import uk.ac.ebi.pride.spectracluster.consensus.ConcensusSpectrumBuilderFactory;
import uk.ac.ebi.pride.spectracluster.consensus.FilteredConsensusSpectrum;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.filter.BinnedHighestNPeakFilter;
import uk.ac.ebi.pride.spectracluster.filter.IPeakFilter;
import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.comparator.ClusterComparator;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * uk.ac.ebi.pride.spectracluster.util.Defaults
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
public class Defaults {

    public static final int DEFAULT_NUMBER_RECLUSTERING_PASSES = 2;

    public static final int DEFAULT_NUMBER_COMPARED_PEAKS = 15;

    public static final double DEFAULT_MZ_RANGE = 0.5;

    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.6;

    public static final double DEFAULT_RETAIN_THRESHOLD = 0.5;

    public static final int DEFAULT_LARGE_BINNING_REGION = 1000;

    private static double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;

    private static int largeBinningRegion = DEFAULT_LARGE_BINNING_REGION;

    private static int numberComparedPeaks = DEFAULT_NUMBER_COMPARED_PEAKS;

    private static double similarityMZRange = DEFAULT_MZ_RANGE;

    private static double retainThreshold = DEFAULT_RETAIN_THRESHOLD;

    private static int numberReclusteringPasses = DEFAULT_NUMBER_RECLUSTERING_PASSES;

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

    public static int getNumberReclusteringPasses() {
        return numberReclusteringPasses;
    }

    public static void setNumberReclusteringPasses(final int pNumberReclusteringPasses) {
        numberReclusteringPasses = pNumberReclusteringPasses;
    }

    private static IConsensusSpectrumBuilder defaultConsensusSpectrumBuilder = null;

    private static ConcensusSpectrumBuilderFactory consensusFactory = FilteredConsensusSpectrum.FACTORY;

    public static ConcensusSpectrumBuilderFactory getConsensusFactory() {
        return consensusFactory;
    }

    public static void setConsensusFactory(ConcensusSpectrumBuilderFactory consensusFactory) {
        Defaults.consensusFactory = consensusFactory;
    }

    /**
     * this is the way to get a ConsensusSpectrumBuilder
     *
     * @return
     */
    public static IConsensusSpectrumBuilder getDefaultConsensusSpectrumBuilder() {
        return consensusFactory.getConsensusSpectrumBuilder();
    }


    private static ISimilarityChecker defaultSimilarityChecker = new FrankEtAlDotProduct(getSimilarityMZRange(), getNumberComparedPeaks());

    public static ISimilarityChecker getDefaultSimilarityChecker() {
        return defaultSimilarityChecker;
    }

    public static void setDefaultSimilarityChecker(ISimilarityChecker defaultSimilarityChecker) {
        Defaults.defaultSimilarityChecker = defaultSimilarityChecker;
    }

    private static IQualityScorer defaultQualityScorer = new SignalToNoiseChecker();

    public static IQualityScorer getDefaultQualityScorer() {
        return defaultQualityScorer;
    }

    public static void setDefaultQualityScorer(IQualityScorer defaultQualityScorer) {
        Defaults.defaultQualityScorer = defaultQualityScorer;
    }

    private static ClusterComparator defaultSpectrumComparator = ClusterComparator.INSTANCE;

    public static ClusterComparator getDefaultSpectrumComparator() {
        return defaultSpectrumComparator;
    }

    public static void setDefaultSpectrumComparator(ClusterComparator dc) {
        defaultSpectrumComparator = dc;
    }

    public static IPeakFilter defaultPeakFilter = BinnedHighestNPeakFilter.DEFAULT;

    public static IPeakFilter getDefaultPeakFilter() {
        return defaultPeakFilter;
    }

    public static void setDefaultPeakFilter(IPeakFilter defaultPeakFilter) {
        Defaults.defaultPeakFilter = defaultPeakFilter;
    }

    /**
     * used to write parameters in to a data sink like a clustering file
     *
     * @param out output
     */
    public static void appendAnalysisParameters(@Nonnull Appendable out) {
        try {
            out.append("largeBinningRegion=").append(String.valueOf(Defaults.getLargeBinningRegion())).append("\n");
            out.append("numberComparedPeaks=").append(String.valueOf(Defaults.getNumberComparedPeaks())).append("\n");
            out.append("similarityMZRange=").append(NumberUtilities.formatDouble(Defaults.getSimilarityMZRange(), 3)).append("\n");
            out.append("similarityThreshold=").append(NumberUtilities.formatDouble(Defaults.getSimilarityThreshold(), 3)).append("\n");
            out.append("retainThreshold=").append(NumberUtilities.formatDouble(Defaults.getRetainThreshold(), 3)).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
}
