package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import com.lordjoe.algorithms.CountedMap;
import com.lordjoe.utilities.ElapsedTimer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterMZSpread;
import uk.ac.ebi.pride.spectracluster.cluster.ClusterPeptideFraction;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMComparisonMain;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ReliableIdentificationChart {
    private final PSMComparisonMain mostSimilarClusterSet;

    public ReliableIdentificationChart(PSMComparisonMain cm) throws HeadlessException {
        this.mostSimilarClusterSet = cm;
    }

    public PSMComparisonMain getSet() {
        return mostSimilarClusterSet;
    }

    public static final int MAX_CLUSTER_SIZE = 32;
    public static final int MIN_CLUSTER_SIZE = 10;
    public static final double MIN_FRACTION = 0.7;
    public static final int CLUSTER_SIZE_MULTIPLIER = 2;


    public JPanel generateChart(IClusterSet cs) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        PSMComparisonMain set = getSet();


        final XYSeriesCollection dataset = new XYSeriesCollection();
        final JFreeChart chart = createChart(cs.getName(), dataset, "Cummulative Fraction", "Fraction");
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            addFDRData(dataset, cs, minimumSize);


        }

        final ChartPanel chartPanel = new ChartPanel(chart);
        ret.add(chartPanel);


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }


    public JPanel generateClusterRangeChart(boolean normalizeTotal) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            for (IClusterSet cs : set.getClusterings()) {
                //           System.out.println("===" + cs.getName() + "===");
                addClusterRangeData(dataset, cs, minimumSize, normalizeTotal);
            }


            final JFreeChart chart = createLogLinear("Cluster Ranges " + minimumSize, dataset, "MZ Range", "Fraction of Clusters");

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }


    protected void addClusterRangeData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize, boolean normalizeTotal) {
        PSMComparisonMain cm = getSet();
        List<ClusterMZSpread> psms = ClusterComparisonMain.getClusterRangeData(cs, cm, minimumClusterSize);
        XYSeries series;
        if (psms.size() == 0) {
            psms = ClusterComparisonMain.getClusterRangeData(cs, cm, minimumClusterSize); // why are we here
            return;
        }

        series = buildFractionalSpread(cs.getName(), psms, normalizeTotal);
        dataset.addSeries(series);

    }


    public JPanel generateFractionalChart(boolean normalizeTotal) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();
        for (int minimumSize = MIN_CLUSTER_SIZE; minimumSize < MAX_CLUSTER_SIZE * 2; minimumSize *= CLUSTER_SIZE_MULTIPLIER) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            for (IClusterSet cs : set.getClusterings()) {
                //               System.out.println("===" + cs.getName() + "===");
                addFractionalData(dataset, cs, minimumSize, normalizeTotal);
            }


            final JFreeChart chart = createChart("FractionalData " + minimumSize, dataset, "Cummulative Fraction", "Fraction");

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }

    public JPanel generateDuplicatesChart(boolean normalizeTotal) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        PSMComparisonMain set = getSet();
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (IClusterSet cs : set.getClusterings()) {
            addDuplicatesData(dataset, cs);
        }
        final JFreeChart chart = createChart("Duplicate ", dataset, "Cummulative Fraction", "Fraction");
        final ChartPanel chartPanel = new ChartPanel(chart);
        ret.add(chartPanel);


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


            final JFreeChart chart = createChart("FDR " + minimumSize, dataset, "Cummulative Fraction", "Fraction");

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
                addCummulativeData(dataset, cs, minimumSize, true, 0.8);    // true says normalize decoy plots to total
            }


            final JFreeChart chart = createChart("Total PSMS " + minimumSize, dataset, "Cummulative Fraction", "Fraction");

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new Dimension(800, 600));
        return ret;
    }


    public XYSeries buildCummulativeTotalSeries(String name, List<ClusterPeptideFraction> values, ClusterDataType type, boolean normalizeTotal, double maxCut) {
        final XYSeries series1 = new XYSeries(name + " " + type);
        int index = 0;
        int cutindex = 0;

        int number_total = 0;
        int number_not_used = 0;
        double cummulativeFraction = 0;
        int number_cummulative = 0;
        double number_values = values.size();

        if (!normalizeTotal && type == ClusterDataType.Decoy)
            number_values = countDecoys(values);


        series1.add(0, 0.0);
        double[] fractionalPurityPoints = CummulativeFDR.fractionalPurityPoints;
        double cutPoint = fractionalPurityPoints[cutindex++];
        for (ClusterPeptideFraction pp : values) {
            boolean use = !pp.isDecoy();
            if (type == ClusterDataType.Decoy)
                use = !use;
            if (type == ClusterDataType.All)
                use = true;
            if (!use) {
                number_not_used++;
                continue;
            }
            double purity = pp.getPurity();

            if (purity > cutPoint) {
                while (purity > cutPoint) {
                    double y = number_cummulative / number_values;
                    //       System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
                    series1.add(cutPoint, y);
                    if (cutindex >= fractionalPurityPoints.length) {
                        y = number_cummulative / number_values;
                        series1.add(1.0, y);
                        //  System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
                        return series1;
                    }
                    cutPoint = fractionalPurityPoints[cutindex++];

                    //                  number_cummulative = 0;
                }
                // drop really pure clusters
                if (cutPoint > maxCut)
                    break;
                //              number_cummulative = 0;
            } else {
                number_cummulative++;
            }
            number_total++;
        }
        double y = number_cummulative / number_values;
        //    System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
        //      System.out.println("Cummulative fraction " + Util.formatDouble(y));
        if (type == ClusterDataType.Decoy) {
            double nn = number_not_used / number_values;
            //        System.out.println("Not Used fraction " + Util.formatDouble(nn));
        }
        if (cutPoint <= maxCut)
            series1.add(cutPoint, y);
        return series1;
    }


    public XYSeries buildCummulativeTotalSeries(String name, List<ClusterPeptideFraction> values, ClusterDataType type, Object normalizeTotal) {
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
            boolean use = !pp.isDecoy();
            if (type == ClusterDataType.Decoy)
                use = !use;
            if (type == ClusterDataType.All)
                use = true;
            if (!use)
                continue;

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

    public static int countDecoys(List<ClusterPeptideFraction> values) {
        int ret = 0;
        for (ClusterPeptideFraction value : values) {
            if (value.isDecoy())
                ret++;
        }
        return ret;
    }

    public static int countTargets(List<ClusterPeptideFraction> values) {
        int ret = 0;
        for (ClusterPeptideFraction value : values) {
            if (!value.isDecoy())
                ret++;
        }
        return ret;
    }


    public static final double MINIMUM_RANGE_BIN = 0.05;

    public XYSeries buildFractionalSpread(String name, List<ClusterMZSpread> values, boolean normalizeTotal) {
        final XYSeries series1 = new XYSeries(name);
        int cutindex = 0;

        double cummulativeFraction = 0;
        int number_cummulative = 0;
        double number_values = values.size();
        double number_total = 0;
        double number_zero = 0;

        double maxRange = 0;
        try {
            maxRange = values.get(values.size() - 1).getRange();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
        maxRange = 3;

        double maxbin = MINIMUM_RANGE_BIN;


        for (ClusterMZSpread pp : values) {
            double range = pp.getStandardDeviation();
            if (range == 0) {
                number_zero++;
                number_cummulative++;
                continue;
            }
            if (range > maxRange)
                break;
            number_total++;
            if (range > maxbin) {
                while (range > maxbin) {
                    double y = number_cummulative / number_values;
                    cummulativeFraction += y;
                    //               System.out.println(Util.formatDouble(maxbin) + " , " + Util.formatDouble(y));
                    series1.add(maxbin, y);
                    maxbin *= 1.5;
                    number_cummulative = 0;
                }
                number_cummulative = 0;
            } else {
                number_cummulative++;
            }
        }

        // everything else
        double y = (number_values - number_total) / number_values;

        cummulativeFraction += y;
        //    System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
        //       System.out.println("Cummulative fraction " + Util.formatDouble(cummulativeFraction));
        series1.add(maxbin, y);
        return series1;
    }


    public XYSeries buildFractionalTotalSeries(String name, List<ClusterPeptideFraction> values, ClusterDataType type, boolean normalizeTotal) {
        final XYSeries series1 = new XYSeries(name + " " + type);
        int index = 0;
        int cutindex = 0;

        int number_total = 0;
        double cummulativeFraction = 0;
        int number_cummulative = 0;
        double number_values = values.size();

        if (!normalizeTotal && type == ClusterDataType.Decoy)
            number_values = countDecoys(values);


        series1.add(0, 0.0);
        double[] fractionalPurityPoints = CummulativeFDR.fractionalPurityPoints;
        double cutPoint = fractionalPurityPoints[cutindex++];
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
                    double y = number_cummulative / number_values;
                    cummulativeFraction += y;
                    //                  System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
                    series1.add(cutPoint, y);
                    if (cutindex >= fractionalPurityPoints.length) {
                        y = number_cummulative / number_values;
                        cummulativeFraction += y;
                        series1.add(1.0, y);
                        //  System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
                        return series1;
                    }
                    cutPoint = fractionalPurityPoints[cutindex++];
                    number_cummulative = 0;
                }
                number_cummulative = 0;
            } else {
                number_cummulative++;
            }
            number_total++;
        }
        double y = number_cummulative / number_values;
        cummulativeFraction += y;
        //    System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
        //  System.out.println("Cummulative fraction " + Util.formatDouble(cummulativeFraction));
        series1.add(cutPoint, y);

        while (cutindex < CummulativeFDR.fractionalPurityPoints.length) {
            series1.add(cutPoint, 0);
            cutPoint = CummulativeFDR.fractionalPurityPoints[cutindex++];
        }
        return series1;
    }


    public XYSeries buildFractionalDuplicatesSeries(String name, List<ClusterPeptideFraction> values, ClusterDataType type, boolean normalizeTotal) {
        final XYSeries series1 = new XYSeries(name + " " + type);
        int index = 0;
        int cutindex = 0;

        int number_total = 0;
        double cummulativeFraction = 0;
        int number_cummulative = 0;
        double number_values = values.size();

        if (!normalizeTotal && type == ClusterDataType.Decoy)
            number_values = countDecoys(values);


        series1.add(0, 0.0);
        double[] fractionalPurityPoints = CummulativeFDR.fractionalPurityPoints;
        double cutPoint = fractionalPurityPoints[cutindex++];
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
                    double y = number_cummulative / number_values;
                    cummulativeFraction += y;
                    //                  System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
                    series1.add(cutPoint, y);
                    if (cutindex >= fractionalPurityPoints.length) {
                        y = number_cummulative / number_values;
                        cummulativeFraction += y;
                        series1.add(1.0, y);
                        //  System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
                        return series1;
                    }
                    cutPoint = fractionalPurityPoints[cutindex++];
                    number_cummulative = 0;
                }
                number_cummulative = 0;
            } else {
                number_cummulative++;
            }
            number_total++;
        }
        double y = number_cummulative / number_values;
        cummulativeFraction += y;
        //    System.out.println(Util.formatDouble(cutPoint) + " , " + Util.formatDouble(y));
        //  System.out.println("Cummulative fraction " + Util.formatDouble(cummulativeFraction));
        series1.add(cutPoint, y);

        while (cutindex < CummulativeFDR.fractionalPurityPoints.length) {
            series1.add(cutPoint, 0);
            cutPoint = CummulativeFDR.fractionalPurityPoints[cutindex++];
        }
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
        while (cutindex < CummulativeFDR.reverse_cummulativePurityPoints.length) {
            series1.add(cutPoint, (double) twice_number_decoys / number_total);
            cutPoint = CummulativeFDR.reverse_cummulativePurityPoints[cutindex++];
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
        List<ClusterPeptideFraction> psms = ClusterComparisonMain.getCumulativeData(cs, cm, minimumClusterSize);
        XYSeries series;
        series = buildFDRSeries(cs.getName(), psms, cm.getPSMCount());

        dataset.addSeries(series);

    }

    protected void addCummulativeData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize, boolean normalizeTotal, double maxCut) {
        PSMComparisonMain cm = getSet();
        List<ClusterPeptideFraction> psms = ClusterComparisonMain.getCumulativeData(cs, cm, minimumClusterSize);
        XYSeries series;
        series = buildCummulativeTotalSeries(cs.getName(), psms, ClusterDataType.All, normalizeTotal, maxCut);

        dataset.addSeries(series);


        series = buildCummulativeTotalSeries(cs.getName(), psms, ClusterDataType.Decoy, normalizeTotal, maxCut);

        dataset.addSeries(series);

    }

    protected void addFractionalData(final XYSeriesCollection dataset, IClusterSet cs, int minimumClusterSize, boolean normalizeTotal) {
        PSMComparisonMain cm = getSet();
        List<ClusterPeptideFraction> psms = ClusterComparisonMain.getCumulativeData(cs, cm, minimumClusterSize);
        XYSeries series;
        series = buildFractionalTotalSeries(cs.getName(), psms, ClusterDataType.All, normalizeTotal);

        dataset.addSeries(series);

        series = buildFractionalTotalSeries(cs.getName(), psms, ClusterDataType.Decoy, normalizeTotal);

        dataset.addSeries(series);
    }

    protected void addDuplicatesData(final XYSeriesCollection dataset, IClusterSet cs) {
        final XYSeries series1 = new XYSeries(cs.getName());
        CountedMap<String> countedMap = ClusterSimilarityUtilities.getCountedMap(cs);
        double[] duplicateFraction = countedMap.getCountDistribution();
        for (int i = 0; i < duplicateFraction.length; i++) {
            double v = duplicateFraction[i];
            series1.add(i, 1.0 - v);

        }
        dataset.addSeries(series1);
    }

    /**
     * Creates a chart.
     *
     * @param dataset the data for the chart.
     * @return a chart.
     */
    private JFreeChart createChart(String title, final XYDataset dataset, String XAxisName, String YAxisName) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                XAxisName,                      // x axis label
                YAxisName,                      // y axis label
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

    /**
     * Creates a chart.
     *
     * @param dataset the data for the chart.
     * @return a chart.
     */
    private JFreeChart createLogLinear(String title, final XYDataset dataset, String XAxisName, String YAxisName) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                XAxisName,                      // x axis label
                YAxisName,                      // y axis label
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

        final NumberAxis domainAxis = new LogarithmicAxis(XAxisName);
        plot.setDomainAxis(domainAxis);

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
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateChart(pCs);


        packChartIntoFrame(name, charts);

    }


    public static void makeFDRChart(String name, final PSMComparisonMain cc) {
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateFDRChart();


        packChartIntoFrame(name, charts);
    }


    public static void makeReliableIdentificationChart(String name, final PSMComparisonMain cc, boolean normalizeTotal) {
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateClusterRangeChart(normalizeTotal);
        packChartIntoFrame(name, charts);
    }

    public static void makeClusterRangeChart(String name, final PSMComparisonMain cc, boolean normalizeTotal) {
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateClusterRangeChart(normalizeTotal);
        packChartIntoFrame(name, charts);
    }

    public static void makeFractionalChart(String name, final PSMComparisonMain cc, boolean normalizeTotal) {
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateFractionalChart(normalizeTotal);
        packChartIntoFrame(name, charts);
    }


    public static void makeDuplicatesChart(String name, final PSMComparisonMain cc, boolean normalizeTotal) {
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateDuplicatesChart(normalizeTotal);
        packChartIntoFrame(name, charts);
    }


    public static void makeCummulativeTotalChart(String name, final PSMComparisonMain cc) {
        ReliableIdentificationChart clusterSimilarityChart = new ReliableIdentificationChart(cc);
        JPanel charts = clusterSimilarityChart.generateCummulativeTotalChart();
        packChartIntoFrame(name, charts);
    }

    public static void packChartIntoFrame(final String name, final JPanel pCharts) {
        JFrame frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(pCharts, BorderLayout.CENTER);
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

        List<IPeptideSpectrumCluster> stableClusters = newClusterSet.getMatchingClusters(new StableClusterPredicate());
        newClusterSet = new SimpleClusterSet(stableClusters);
        System.out.println("=======New ste duplicates =======================================");
        newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


        System.out.println("==============================================================");


        List<IPeptideSpectrumCluster> semiStableClusters = originalClusterSet.getMatchingClusters(new SemiStableClusterPredicate());
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        System.out.println("==========original set duplicates ==========================");
        originalClusterSet = SimpleClusterSet.removeDuplicates(originalClusterSet);
        System.out.println("==============================================================");

        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ClusterSpectrumOverlapDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(originalClusterSet);

        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0

        ReliableIdentificationChart clusterSimilarityChart = null; // new ClusterDecoyChart(mostSimilarClusterSet);

        JPanel charts = clusterSimilarityChart.generateChart(null);    // todo fix

        timer.showElapsed("Create chart");
        timer.reset(); // back to 0

        packChartIntoFrame("Cluster similarity charts", charts);

    }

}
