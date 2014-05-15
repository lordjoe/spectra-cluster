package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.algorithms.IWideBinner;
import com.lordjoe.algorithms.SizedWideBinner;
import com.lordjoe.utilities.Util;
import org.systemsbiology.hadoop.IJobBuilderFactory;
import org.systemsbiology.hadoop.IParameterHolder;
import uk.ac.ebi.pride.spectracluster.normalizer.IIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.util.comparator.DefaultClusterComparator;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.consensus.ConcensusSpectrumBuilderFactory;
import uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrum;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.datastore.SpringJDBCUtilities;
import uk.ac.ebi.pride.spectracluster.datastore.WorkingClusterDatabase;
import uk.ac.ebi.pride.spectracluster.datastore.WorkingDatabaseFactory;
import uk.ac.ebi.pride.spectracluster.engine.ClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngineFactory;
import uk.ac.ebi.pride.spectracluster.normalizer.TotalIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Comparator;

/**
 * uk.ac.ebi.pride.spectracluster.util.Defaults
 *
 * @author Steve Lewis
 * @date 20/05/13
 */
public class Defaults {

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
    // put all program constants here

    private static final int DEFAULT_NUMBER_RECLUSTERING_PASSES = 2;

    //
    // value can be set using uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer.MajorPeakWindow
    private static final double DEFAULT_MAJOR_PEAK_MZ_WINDOW = 2.0; // major peak sliding window is this

    private static final double DEFAULT_SPECTRUM_MERGE_WINDOW = 0.5;

    private static final double DEFAULT_SAME_CLUSTER_MERGE_WINDOW = DEFAULT_SPECTRUM_MERGE_WINDOW;

    private static final int NUMBER_COMPARED_PEAKS = 15;

    private static final double DEFAULT_MZ_RANGE = 0.5;

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.6;


    private static final String DEFAULT_OUTPUT_PATH = "ConsolidatedClusters";

    private static final double DEFAULT_RETAIN_THRESHOLD = 0.5;

    private static final int LARGE_BINNING_REGION = 1000;

    private static double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;

    private static String gOutputPath = DEFAULT_OUTPUT_PATH;

    public static double getSimilarityThreshold() {
        return similarityThreshold;
    }

    private static int largeBinningRegion = LARGE_BINNING_REGION;

    public static int getLargeBinningRegion() {
        return largeBinningRegion;
    }

    private static int numberComparedPeaks = NUMBER_COMPARED_PEAKS;

    public static int getNumberComparedPeaks() {
        return numberComparedPeaks;
    }

    private static double similarityMZRange = DEFAULT_MZ_RANGE;

    public static double getSimilarityMZRange() {
        return similarityMZRange;
    }

    private static double retainThreshold = DEFAULT_RETAIN_THRESHOLD;

    public static double getRetainThreshold() {
        return retainThreshold;
    }

    private static double spectrumMergeMZWindowSize = DEFAULT_SPECTRUM_MERGE_WINDOW;

    public static double getSpectrumMergeMZWindowSize() {
        return spectrumMergeMZWindowSize;
    }

    private static double majorPeakMZWindowSize = DEFAULT_MAJOR_PEAK_MZ_WINDOW;

    public static double getMajorPeakMZWindowSize() {
        return majorPeakMZWindowSize;
    }

    private static double sameClusterMergeMZWindowSize = DEFAULT_SAME_CLUSTER_MERGE_WINDOW;

    public static double getSameClusterMergeMZWindowSize() {
        return sameClusterMergeMZWindowSize;
    }


    /**
     * binning sizes
     */
    private static final double NARRROW_BIN_WIDTH = 0.6; // 0.15; //0.005; // 0.3;
    private static final double NARRROW_BIN_OVERLAP = 0.15; // 0.03; //0.002; // 0.1;

    private static final double WIDE_BIN_WIDTH = 1.0;
    private static final double WIDE_BIN_OVERLAP = 0.3;


    private static final IWideBinner NARROW_MZ_BINNER = new SizedWideBinner(
            Constants.HIGHEST_USABLE_MZ,
            NARRROW_BIN_WIDTH,
            Constants.LOWEST_USABLE_MZ,
            NARRROW_BIN_OVERLAP);


    @java.lang.SuppressWarnings("UnusedDeclaration")
    private static final IWideBinner WIDE_MZ_BINNER = new SizedWideBinner(
            Constants.HIGHEST_USABLE_MZ,
            WIDE_BIN_WIDTH,
            Constants.LOWEST_USABLE_MZ,
            WIDE_BIN_OVERLAP);


    public static final IWideBinner DEFAULT_WIDE_MZ_BINNER = NARROW_MZ_BINNER;


    public static final Defaults INSTANCE = new Defaults();

    private int numberReclusteringPasses = DEFAULT_NUMBER_RECLUSTERING_PASSES;

    private SimilarityChecker defaultSimilarityChecker = new FrankEtAlDotProduct();

    private IQualityScorer defaultQualityScorer = new SignalToNoiseChecker();

    private DefaultClusterComparator defaultSpectrumComparator = (DefaultClusterComparator)DefaultClusterComparator.INSTANCE;

    private IIntensityNormalizer normalizer = new TotalIntensityNormalizer();

    private IClusteringEngineFactory defaultClusteringEngineFactory;

    private Appendable debugOutput;

    /**
     * code might append results we want to look at
     *
     * @return
     */
    public Appendable getDebugOutput() {
        return debugOutput;
    }

    public void setDebugOutput(final Appendable pDebugOutput) {
        debugOutput = pDebugOutput;
    }

    private DataSource defaultDataSource;

    private ConcensusSpectrumBuilderFactory consensusFactory = ConsensusSpectrum.FACTORY;

    private WorkingDatabaseFactory databaseFactory = WorkingClusterDatabase.FACTORY;

    private IJobBuilderFactory defaultJobBuilderFactory;

    private Defaults() {

    }

    public static String getOutputPath() {
        return gOutputPath;
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

    public IIntensityNormalizer getNormalizer() {
        return normalizer;
    }

    public synchronized DataSource getDefaultDataSource() {
        if (defaultDataSource == null) {
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

    public IIntensityNormalizer getDefaultIntensityNormalizer() {
        return normalizer;
    }

    public void setNormalizer(final IIntensityNormalizer pNormalizer) {
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

    public void setDefaultQualityScorer(final IQualityScorer pDefaultQualityScorer) {
        defaultQualityScorer = pDefaultQualityScorer;
    }

    public void setDefaultSpectrumComparator(final DefaultClusterComparator pDefaultSpectrumComparator) {
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


    public IQualityScorer getDefaultQualityScorer() {
        return defaultQualityScorer;
    }

    /**
     * make a constructor for jobs for Clustering engine and other Hadoop jobs
     *
     * @return default is null allowing you to plug in your own factory
     */
    public IClusteringEngineFactory getDefaultClusteringEngineFactory() {
        if (defaultClusteringEngineFactory == null)
            defaultClusteringEngineFactory = ClusteringEngine.getClusteringEngineFactory();
        return defaultClusteringEngineFactory;
    }

    public IJobBuilderFactory getDefaultJobBuilderFactory() {
        return defaultJobBuilderFactory;
    }

    public void setDefaultJobBuilderFactory(IJobBuilderFactory defaultJobBuilderFactory) {
        this.defaultJobBuilderFactory = defaultJobBuilderFactory;
    }

    /**
     * this method and the one below
     *
     * @param application source of parameters
     */
    public static void configureAnalysisParameters(@Nonnull IParameterHolder application) {
        largeBinningRegion = application.getIntParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.LargeBinningRegion", LARGE_BINNING_REGION);
        numberComparedPeaks = application.getIntParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.NumberComparedPeaks", NUMBER_COMPARED_PEAKS);
        similarityMZRange = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityMZRange", DEFAULT_MZ_RANGE);
        retainThreshold = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.RetainThreshold", DEFAULT_RETAIN_THRESHOLD);
        similarityThreshold = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityThreshold", DEFAULT_SIMILARITY_THRESHOLD);


        sameClusterMergeMZWindowSize = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.SameClustererMerger.SpectrumMergeWindow", DEFAULT_SAME_CLUSTER_MERGE_WINDOW);
        majorPeakMZWindowSize = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer.MajorPeakWindow", DEFAULT_MAJOR_PEAK_MZ_WINDOW);
        spectrumMergeMZWindowSize = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.SpectrumMergeReducer.SpectrumMergeWindow", DEFAULT_SPECTRUM_MERGE_WINDOW);

        gOutputPath = application.getParameter("uk.ac.ebi.pride.spectracluster.hadoop.OutputPath", DEFAULT_OUTPUT_PATH);
    }

    /**
     * used to write parameters in to a data sink like a clustering file
     *
     * @param out output
     */
    public static void appendAnalysisParameters(@Nonnull Appendable out) {
        try {
            out.append("largeBinningRegion=" + largeBinningRegion + "\n");
            out.append("numberComparedPeaks=" + numberComparedPeaks + "\n");
            out.append("similarityMZRange=" + Util.formatDouble(similarityMZRange, 3) + "\n");
            out.append("similarityThreshold=" + Util.formatDouble(similarityThreshold, 3) + "\n");
            out.append("retainThreshold=" + Util.formatDouble(retainThreshold, 3) + "\n");
            out.append("sameClusterMergeMZWindowSize=" + Util.formatDouble(sameClusterMergeMZWindowSize, 3) + "\n");
            out.append("majorPeakMZWindowSize=" + Util.formatDouble(majorPeakMZWindowSize, 3) + "\n");
            out.append("spectrumMergeMZWindowSize=" + Util.formatDouble(spectrumMergeMZWindowSize, 3) + "\n");
            out.append("outputPath=" + getOutputPath() + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

}
