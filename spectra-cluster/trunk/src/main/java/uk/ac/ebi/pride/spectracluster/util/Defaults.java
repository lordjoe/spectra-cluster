package uk.ac.ebi.pride.spectracluster.util;

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
