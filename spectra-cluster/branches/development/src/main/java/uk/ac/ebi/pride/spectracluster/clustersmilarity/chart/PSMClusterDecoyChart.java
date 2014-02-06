package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import com.lordjoe.utilities.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.psm_similarity.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PSMClusterDecoyChart {
    private final PSMComparisonMain mostSimilarClusterSet;

    public PSMClusterDecoyChart(PSMComparisonMain cm) throws HeadlessException {
        this.mostSimilarClusterSet = cm;
    }

    public PSMComparisonMain getSet() {
        return mostSimilarClusterSet;
    }

    public static final int MAX_CLUSTER_SIZE = 32;
    public static final int MIN_CLUSTER_SIZE = 4;
    public static final int CLUSTER_SIZE_MULTIPLIER = 2;


    public JPanel generateChartX(IClusterSet cs) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();


        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            addDecoyAndTargetData(dataset, cs, minimumSize);

            final JFreeChart chart = createChart("Target and Decoy " + minimumSize, dataset, minimumSize, set.getPSMCount());

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }


    public JPanel generateChart(IClusterSet cs) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();


        final XYSeriesCollection dataset = new XYSeriesCollection();
        final JFreeChart chart = createChart(cs.getName(), dataset, 0, set.getPSMCount());
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            addFDRData(dataset, cs, minimumSize);


        }

        final ChartPanel chartPanel = new ChartPanel(chart);
        ret.add(chartPanel);


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }


    public JPanel generateFractionalChart(boolean normalizeTotal) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            for (IClusterSet cs : set.getClusterings()) {
                addFractionalData(dataset, cs, minimumSize,normalizeTotal);
            }


            final JFreeChart chart = createChart("FractionalData " + minimumSize, dataset, minimumSize, set.getPSMCount());

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }

    public JPanel generateFDRChart() {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            for (IClusterSet cs : set.getClusterings()) {
                addFDRData(dataset, cs, minimumSize);
            }


            final JFreeChart chart = createChart("FDR " + minimumSize, dataset, minimumSize, set.getPSMCount());

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }


    public JPanel generateCummulativeTotalChart() {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            List<IClusterSet> clusterings = set.getClusterings();
            for (IClusterSet cs : clusterings) {
                addCummulativeData(dataset, cs, minimumSize);
            }


            final JFreeChart chart = createChart("Total PSMS " + minimumSize, dataset, minimumSize, set.getPSMCount());

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }

    public XYSeries buildCummulativeTotalSeries(String name, List<ClusterPeptideFraction> values, double number_spectra) {
        final XYSeries series1 = new XYSeries(name);
        int index = 0;
        int cutindex = 1;

        int number_total = 0;
        int number_targets = 0;
        double number_values = values.size();

        Collections.reverse(values);


        double cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
        series1.add(cutPoint, 1.0);
        cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
        for (ClusterPeptideFraction pp : values) {

            double purity = pp.getPurity();
            while (purity < cutPoint) {
                series1.add(cutPoint, 1.0 - number_total / number_values);
                if (cutindex >= CummulativeFDR.reverse_cummulativePurityPoints.length)
                    return series1;
                cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
            }
            number_total++;
        }
        return series1;
    }

    public static int countDecoys( List<ClusterPeptideFraction> values)  {
        int ret = 0;
        for (ClusterPeptideFraction value : values) {
              if(value.isDecoy())
                  ret++;
        }
        return ret;
    }
    public static int countTargets( List<ClusterPeptideFraction> values)  {
        int ret = 0;
        for (ClusterPeptideFraction value : values) {
              if(!value.isDecoy())
                  ret++;
        }
        return ret;
    }


    public XYSeries buildFractionalTotalSeries(String name, List<ClusterPeptideFraction> values,  ClusterDataType type,  boolean normalizeTotal) {
        final XYSeries series1 = new XYSeries(name + " " + type);
        int index = 0;
        int cutindex = 0;

        int number_total = 0;
        int number_cummulative = 0;
        double number_values = values.size();

        if (!normalizeTotal && type == ClusterDataType.Decoy)
            number_values = countDecoys(values);


          series1.add(0, 0.0);
        double cutPoint = CummulativeFDR.cummulativePurityPoints[cutindex++];
        for (ClusterPeptideFraction pp : values) {
            boolean use = !pp.isDecoy();
            if (type == ClusterDataType.Decoy)
                use = !use;
            if (type == ClusterDataType.All)
                use = true;
            if (!use)
                continue;
            double purity = pp.getPurity();
            if (purity > cutPoint) {
                while (purity > cutPoint) {
                    series1.add(cutPoint, number_cummulative / number_values);
                    if (cutindex >= CummulativeFDR.cummulativePurityPoints.length) {
                        series1.add(1.0,  number_cummulative / number_values);
                        return series1;
                    }
                    cutPoint = CummulativeFDR.cummulativePurityPoints[cutindex++];
                }
                number_cummulative = 0;
            }
            else {
                number_cummulative++;
             }
            number_total++;
        }
        series1.add(cutPoint,   number_cummulative / number_values);
        return series1;
    }


    public XYSeries buildFDRSeries(String name, List<ClusterPeptideFraction> values, double number_spectra) {
        final XYSeries series1 = new XYSeries(name);
        int index = 0;
        int cutindex = 1;

        int number_total = 0;
        int number_targets = 0;
        int twice_number_decoys = 0;

        Collections.reverse(values);


        double cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
        for (ClusterPeptideFraction pp : values) {

            double purity = pp.getPurity();
            while (purity < cutPoint) {
                series1.add(cutPoint, (double) twice_number_decoys / number_total);
                if (cutindex >= CummulativeFDR.reverse_cummulativePurityPoints.length)
                    return series1;
                cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
            }
            number_total++;
            if (pp.isDecoy())
                twice_number_decoys += 2;

        }
        return series1;
    }


    public XYSeries buildCummulativeSeries(String name, List<ClusterPeptideFraction> values, double number_spectra) {
        final XYSeries series1 = new XYSeries(name);
        int index = 0;
        int cutindex = 0;
        double numberPeptides = values.size();

        double cutPoint = CummulativeFDR.cummulativePurityPoints[cutindex++];
        for (ClusterPeptideFraction decoy : values) {

            double purity = decoy.getPurity();
            while (purity > cutPoint) {
                series1.add(cutPoint, index / number_spectra);
                if (cutindex >= CummulativeFDR.cummulativePurityPoints.length)
                    return series1;
                cutPoint = CummulativeFDR.cummulativePurityPoints[cutindex++];
            }
            index++;
        }
        while (cutindex < CummulativeFDR.cummulativePurityPoints.length) {
            series1.add(cutPoint, index / number_spectra);
            cutindex++;
            if (cutindex >= CummulativeFDR.cummulativePurityPoints.length)
                break;
            cutPoint = CummulativeFDR.cummulativePurityPoints[cutindex];
        }
        return series1;
    }


    public XYSeries reverseBuildCummulativeSeries(String name, List<ClusterPeptideFraction> values, double number_spectra) {
        final XYSeries series1 = new XYSeries(name);
        int index = 0;
        int cutindex = 0;
        double numberPeptides = values.size();

        List<ClusterPeptideFraction> reverseValues = new ArrayList<ClusterPeptideFraction>(values);
        Collections.reverse(reverseValues);
        double cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];

        double y = (index) / number_spectra;
        for (ClusterPeptideFraction decoy : reverseValues) {

            double purity = decoy.getPurity();
            while (purity < cutPoint) {
                series1.add(cutPoint, y);
                cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
            }
            index++;
            y = (index) / number_spectra;
        }
        while (cutindex < CummulativeFDR.reverse_cummulativePurityPoints.length) {
            double x = cutPoint;
            series1.add(x, y);
            y = (index) / number_spectra;
            cutindex++;
            if (cutindex > CummulativeFDR.reverse_cummulativePurityPoints.length)
                break;
            cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex];
        }
        return series1;
    }


    public static final boolean USE_REVERSE = true;

    protected void addDecoyAndTargetData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize) {
        PSMComparisonMain cm = getSet();
        List<ClusterPeptideFraction> decoys = ClusterComparisonMain.getCumulativeDecoyData(cs, cm, minimumClusterSize);
        XYSeries series;
        if (USE_REVERSE)
            series = reverseBuildCummulativeSeries("Decoys", decoys, cm.getPSMCount());
        else
            series = buildCummulativeSeries("Decoys", decoys, cm.getPSMCount());

        dataset.addSeries(series);

        List<ClusterPeptideFraction> targets = ClusterComparisonMain.getCumulativeTargetData(cs, cm, minimumClusterSize);
        if (USE_REVERSE)
            series = reverseBuildCummulativeSeries("Targets", targets, cm.getPSMCount());
        else
            series = buildCummulativeSeries("Targets", targets, cm.getPSMCount());


        dataset.addSeries(series);

    }

    protected void addFDRData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize) {
        PSMComparisonMain cm = getSet();
        List<ClusterPeptideFraction> decoys = ClusterComparisonMain.getCumulativeData(cs, cm, minimumClusterSize);
        XYSeries series;
        series = buildFDRSeries(cs.getName(), decoys, cm.getPSMCount());

        dataset.addSeries(series);

    }

    protected void addCummulativeData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize) {
        PSMComparisonMain cm = getSet();
        List<ClusterPeptideFraction> decoys = ClusterComparisonMain.getCumulativeData(cs, cm, minimumClusterSize);
        XYSeries series;
        series = buildCummulativeTotalSeries(cs.getName(), decoys, cm.getPSMCount());

        dataset.addSeries(series);

    }

    protected void addFractionalData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize, boolean normalizeTotal) {
        PSMComparisonMain cm = getSet();
        List<ClusterPeptideFraction> decoys = ClusterComparisonMain.getCumulativeData(cs, cm, minimumClusterSize);
        XYSeries series;
        series = buildFractionalTotalSeries(cs.getName(), decoys,  ClusterDataType.All,normalizeTotal);

        dataset.addSeries(series);

        series = buildFractionalTotalSeries(cs.getName(), decoys,   ClusterDataType.Decoy,normalizeTotal);

        dataset.addSeries(series);
    }

    /**
     * Creates a chart.
     *
     * @param dataset the data for the chart.
     * @return a chart.
     */
    private JFreeChart createChart(String title, final XYDataset dataset, int minSize, int numberSPectra) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                "Cummulative Fraction",                      // x axis label
                "Number",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        //        final StandardLegend legend = (StandardLegend) chart.getLegend();
        //      legend.setDisplaySeriesShapes(true);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        //  rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //    rangeAxis.setRange(0,numberSPectra);
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;

    }

    private static IClusterSet readClusterSet(SimpleSpectrumRetriever simpleSpectrumRetriever, File newFile, String saveName) {
        IClusterSet newClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(newFile, simpleSpectrumRetriever);
        if (newFile.isDirectory())
            ClusterSimilarityUtilities.saveSemiStableClusters(newClusterSet, new File(saveName));
        return newClusterSet;
    }


    public static void makeDecoyChart(PSMComparisonMain cc, final IClusterSet pCs, String name) {
        PSMClusterDecoyChart clusterSimilarityChart = new PSMClusterDecoyChart(cc);
        JPanel charts = clusterSimilarityChart.generateChart(pCs);


        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }


    public static void makeFDRChart(String name, final PSMComparisonMain cc) {
        PSMClusterDecoyChart clusterSimilarityChart = new PSMClusterDecoyChart(cc);
        JPanel charts = clusterSimilarityChart.generateFDRChart();


        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }


    public static void makeFractionalChart(String name, final PSMComparisonMain cc,boolean normalizeTotal) {
        PSMClusterDecoyChart clusterSimilarityChart = new PSMClusterDecoyChart(cc);
        JPanel charts = clusterSimilarityChart.generateFractionalChart( normalizeTotal);


        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }


    public static void makeCummulativeTotalChart(String name, final PSMComparisonMain cc) {
        PSMClusterDecoyChart clusterSimilarityChart = new PSMClusterDecoyChart(cc);
        JPanel charts = clusterSimilarityChart.generateCummulativeTotalChart();


        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }


    public static void usage() {
        System.out.println("Usage <tsv defining spectra> <decoy list> <clustering file or directory>");
        System.out.println("For example> <tsv defining spectra> <decoy list> <clustering file or directory>");
    }

    /**
     * This is an example on how to use it
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            usage();
            return;
        }
        SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();

        ElapsedTimer timer = new ElapsedTimer();

        File tsvFile = new File(args[0]);
        ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, simpleSpectrumRetriever);
        timer.showElapsed("Read TSV");
        timer.reset(); // back to 0

        File originalFile = new File(args[1]);

        IClusterSet originalClusterSet = readClusterSet(simpleSpectrumRetriever, originalFile, "SemiStableOriginal.clustering");
        timer.showElapsed("Read Original set");
        timer.reset(); // back to 0

        File newFile = new File(args[2]);
        IClusterSet newClusterSet = readClusterSet(simpleSpectrumRetriever, newFile, "StableNew.clustering");
        timer.showElapsed("Read New set");
        timer.reset(); // back to 0

        List<ISpectralCluster> stableClusters = newClusterSet.getMatchingClusters(ISpectralCluster.STABLE_PREDICATE);
        newClusterSet = new SimpleClusterSet(stableClusters);
        System.out.println("=======New ste duplicates =======================================");
        newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


        System.out.println("==============================================================");


        List<ISpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.SEMI_STABLE_PREDICATE);
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        System.out.println("==========original set duplicates ==========================");
        originalClusterSet = SimpleClusterSet.removeDuplicates(originalClusterSet);
        System.out.println("==============================================================");

        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ClusterSpectrumOverlapDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(originalClusterSet);

        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0

        PSMClusterDecoyChart clusterSimilarityChart = null; // new ClusterDecoyChart(mostSimilarClusterSet);

        JPanel charts = clusterSimilarityChart.generateChart(null);    // todo fix

        timer.showElapsed("Create chart");
        timer.reset(); // back to 0

        JFrame frame = new JFrame("Cluster similarity charts");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }

}
