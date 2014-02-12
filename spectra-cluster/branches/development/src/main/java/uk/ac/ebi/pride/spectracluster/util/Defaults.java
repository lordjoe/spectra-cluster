package uk.ac.ebi.pride.spectracluster.util;

import com.lordjoe.algorithms.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.datastore.*;
import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import javax.sql.*;
import java.util.*;

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

    private static final double DEFAULT_RETAIN_THRESHOLD = 0.5;

    private static final int LARGE_BINNING_REGION = 1000;

    private static double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;

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
            IPeak.HIGHEST_USABLE_MZ,
            NARRROW_BIN_WIDTH,
            IPeak.LOWEST_USABLE_MZ,
            NARRROW_BIN_OVERLAP);


    @java.lang.SuppressWarnings("UnusedDeclaration")
    private static final IWideBinner WIDE_MZ_BINNER = new SizedWideBinner(
            IPeak.HIGHEST_USABLE_MZ,
            WIDE_BIN_WIDTH,
            IPeak.LOWEST_USABLE_MZ,
            WIDE_BIN_OVERLAP);


    public static final IWideBinner DEFAULT_WIDE_MZ_BINNER = NARROW_MZ_BINNER;


    public static final Defaults INSTANCE = new Defaults();

    private int numberReclusteringPasses = DEFAULT_NUMBER_RECLUSTERING_PASSES;

    private SimilarityChecker defaultSimilarityChecker = new FrankEtAlDotProduct();

    private QualityScorer defaultQualityScorer = new SignalToNoiseChecker();

    private ClusterComparator defaultSpectrumComparator = new ClusterComparator();

    private IntensityNormalizer normalizer = new TotalIntensityNormalizer();

    private IClusteringEngineFactory defaultClusteringEngineFactory;

    private DataSource defaultDataSource;

    private ConcensusSpectrumBuilderFactory consensusFactory = ConsensusSpectrum.FACTORY;

    private WorkingDatabaseFactory databaseFactory = WorkingClusterDatabase.FACTORY;

    private IJobBuilderFactory defaultJobBuilderFactory;

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

    public static void configureAnalysisParameters(ISetableParameterHolder application) {
        largeBinningRegion = application.getIntParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.LargeBinningRegion", LARGE_BINNING_REGION);
        numberComparedPeaks = application.getIntParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.NumberComparedPeaks", NUMBER_COMPARED_PEAKS);
        similarityMZRange = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityMZRange", DEFAULT_MZ_RANGE);
        similarityThreshold = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.RetainThreshold", DEFAULT_RETAIN_THRESHOLD);
        similarityThreshold = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProduct.SimilarityThreshold", DEFAULT_SIMILARITY_THRESHOLD);


        sameClusterMergeMZWindowSize = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.SameClustererMerger.SpectrumMergeWindow", DEFAULT_SAME_CLUSTER_MERGE_WINDOW);
        majorPeakMZWindowSize = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer.MajorPeakWindow", DEFAULT_MAJOR_PEAK_MZ_WINDOW);
        spectrumMergeMZWindowSize = application.getDoubleParameter("uk.ac.ebi.pride.spectracluster.hadoop.SpectrumMergeReducer.SpectrumMergeWindow", DEFAULT_SPECTRUM_MERGE_WINDOW);
    }

}
