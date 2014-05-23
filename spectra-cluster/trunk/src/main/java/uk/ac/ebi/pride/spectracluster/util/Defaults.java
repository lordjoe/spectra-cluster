package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.utilities.Util;
import org.systemsbiology.hadoop.IParameterHolder;

import javax.annotation.Nonnull;
import java.io.IOException;

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
