package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.IWideBinner;
import com.lordjoe.algorithms.SizedWideBinner;
import com.lordjoe.utilities.Util;
import org.systemsbiology.hadoop.IJobBuilderFactory;
import org.systemsbiology.hadoop.IParameterHolder;
import uk.ac.ebi.pride.spectracluster.util.MZIntensityUtilities;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Steve Lewis
 * @version $Id$
 */
public final class HadoopDefaults {


        /*
    # properties for defaults look like
    uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.LargeBinningRegion=1000
    uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.NumberComparedPeaks=15
    uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityMZRange=0.5
     uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer.MajorPeakWindow= 2.0
      uk.ac.ebi.pride.spectracluster.hadoop.SpectrumMergeReducer.SpectrumMergeWindow=0.5
      uk.ac.ebi.pride.spectracluster.hadoop.SameClustererMerger.SpectrumMergeWindow=0.5
      uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.RetainThreshold=0.5
     uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityThreshold=0.6
     uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.RetainThreshold=0.5

     */

    //
    // value can be set using uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer.MajorPeakWindow
    public static final double DEFAULT_MAJOR_PEAK_MZ_WINDOW = 2.0; // major peak sliding window is this

    public static final double DEFAULT_SPECTRUM_MERGE_WINDOW = 0.5;

    public static final double DEFAULT_SAME_CLUSTER_MERGE_WINDOW = DEFAULT_SPECTRUM_MERGE_WINDOW;

    public static final String DEFAULT_OUTPUT_PATH = "ConsolidatedClusters";

    private static String gOutputPath = DEFAULT_OUTPUT_PATH;

    private static double spectrumMergeMZWindowSize = DEFAULT_SPECTRUM_MERGE_WINDOW;

    private static double majorPeakMZWindowSize = DEFAULT_MAJOR_PEAK_MZ_WINDOW;

    private static double sameClusterMergeMZWindowSize = DEFAULT_SAME_CLUSTER_MERGE_WINDOW;

    public static double getSpectrumMergeMZWindowSize() {
        return spectrumMergeMZWindowSize;
    }

    public static double getMajorPeakMZWindowSize() {
        return majorPeakMZWindowSize;
    }

    public static double getSameClusterMergeMZWindowSize() {
        return sameClusterMergeMZWindowSize;
    }

    public static void setOutputPath(String gOutputPath) {
        HadoopDefaults.gOutputPath = gOutputPath;
    }

    public static void setSpectrumMergeMZWindowSize(double spectrumMergeMZWindowSize) {
        HadoopDefaults.spectrumMergeMZWindowSize = spectrumMergeMZWindowSize;
    }

    public static void setMajorPeakMZWindowSize(double majorPeakMZWindowSize) {
        HadoopDefaults.majorPeakMZWindowSize = majorPeakMZWindowSize;
    }

    public static void setSameClusterMergeMZWindowSize(double sameClusterMergeMZWindowSize) {
        HadoopDefaults.sameClusterMergeMZWindowSize = sameClusterMergeMZWindowSize;
    }

    /**
     * binning sizes
     */
    private static final double NARRROW_BIN_WIDTH = 0.6; // 0.15; //0.005; // 0.3;
    private static final double NARRROW_BIN_OVERLAP = 0.15; // 0.03; //0.002; // 0.1;

    private static final double WIDE_BIN_WIDTH = 1.0;
    private static final double WIDE_BIN_OVERLAP = 0.3;


    private static final IWideBinner NARROW_MZ_BINNER = new SizedWideBinner(
            MZIntensityUtilities.HIGHEST_USABLE_MZ,
            NARRROW_BIN_WIDTH,
            MZIntensityUtilities.LOWEST_USABLE_MZ,
            NARRROW_BIN_OVERLAP);


    @java.lang.SuppressWarnings("UnusedDeclaration")
    private static final IWideBinner WIDE_MZ_BINNER = new SizedWideBinner(
            MZIntensityUtilities.HIGHEST_USABLE_MZ,
            WIDE_BIN_WIDTH,
            MZIntensityUtilities.LOWEST_USABLE_MZ,
            WIDE_BIN_OVERLAP);


    public static final IWideBinner DEFAULT_WIDE_MZ_BINNER = NARROW_MZ_BINNER;

    public static final HadoopDefaults INSTANCE = new HadoopDefaults();


    private IJobBuilderFactory defaultJobBuilderFactory;

    private HadoopDefaults() {
    }

    public IJobBuilderFactory getDefaultJobBuilderFactory() {
        return defaultJobBuilderFactory;
    }

    public void setDefaultJobBuilderFactory(IJobBuilderFactory defaultJobBuilderFactory) {
        this.defaultJobBuilderFactory = defaultJobBuilderFactory;
    }

    public static String getOutputPath() {
        return gOutputPath;
    }

    /**
     * this method and the one below
     *
     * @param application source of parameters
     */
    public static void configureAnalysisParameters(@Nonnull IParameterHolder application) {

        setSameClusterMergeMZWindowSize(application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.SameClustererMerger.SpectrumMergeWindow", DEFAULT_SAME_CLUSTER_MERGE_WINDOW));
        setMajorPeakMZWindowSize(application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer.MajorPeakWindow", DEFAULT_MAJOR_PEAK_MZ_WINDOW));
        setSpectrumMergeMZWindowSize(application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.SpectrumMergeReducer.SpectrumMergeWindow", DEFAULT_SPECTRUM_MERGE_WINDOW));

        setOutputPath(application.getParameter("uk.ac.ebi.pride.spectracluster.hadoop.OutputPath", DEFAULT_OUTPUT_PATH));
    }

    /**
     * used to write parameters in to a data sink like a clustering file
     *
     * @param out output
     */
    public static void appendAnalysisParameters(@Nonnull Appendable out) {
        try {
            out.append("sameClusterMergeMZWindowSize=").append(Util.formatDouble(getSameClusterMergeMZWindowSize(), 3)).append("\n");
            out.append("majorPeakMZWindowSize=").append(Util.formatDouble(getMajorPeakMZWindowSize(), 3)).append("\n");
            out.append("spectrumMergeMZWindowSize=").append(Util.formatDouble(getSpectrumMergeMZWindowSize(), 3)).append("\n");
            out.append("outputPath=").append(getOutputPath()).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
}
