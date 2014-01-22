package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import com.lordjoe.utilities.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterDecoyChart {
    private final ClusterComparisonMain mostSimilarClusterSet;

    public ClusterDecoyChart(ClusterComparisonMain cm) throws HeadlessException {
        this.mostSimilarClusterSet = cm;
    }

    public ClusterComparisonMain getSet() {
        return mostSimilarClusterSet;
    }

    public JPanel generateChart(IClusterSet cs) {
        JPanel ret = new JPanel();
        ret.setLayout(new GridLayout(0, 2));

        ClusterComparisonMain set = getSet();


        for (int minimumSize = 4; minimumSize < 62; minimumSize *= 2) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            addDecoyAndTargetData(dataset,cs, minimumSize);

            final JFreeChart chart = createChart(dataset, minimumSize,set.getNumberSpectra());

            final ChartPanel chartPanel = new ChartPanel(chart);
            ret.add(chartPanel);

        }


        ret.setPreferredSize(new java.awt.Dimension(800, 600));
        return ret;

        //        IClusterDistance distance = mostSimilarClusterSet.getClusterDistance();
//        XYSeries xySeries = new XYSeries(distance.getClass().getSimpleName());
//        for (ISpectralCluster cluster : mostSimilarClusterSet.getBaseSet().getClusters()) {
//            MostSimilarClusters mostSimilarClusters = mostSimilarClusterSet.getMostSimilarClusters(cluster);
//            List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();
//
//            ClusterDistanceItem bestMatch = bestMatches.get(0);
//            ISpectralCluster source = bestMatch.getSource();
//            ISpectralCluster target = bestMatch.getTarget();
//            double dotProduct = Defaults.INSTANCE.getDefaultSimilarityChecker().assessSimilarity(source.getConsensusSpectrum(), target.getConsensusSpectrum());
//            xySeries.add(dotProduct, bestMatch.getDistance());
//        }
//
//        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
//        xySeriesCollection.addSeries(xySeries);
//
//        return ChartUtilities.createScatterPlotChart(distance.getClass().getSimpleName() + " and Dot Product", "Dot Product", distance.getClass().getSimpleName(), xySeriesCollection);
    }

    public static final double cummulativePurityPoints[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99, 1.0};

    public XYSeries buildCummulativeSeries(String name, List<ClusterPeptideFraction> values) {
        final XYSeries series1 = new XYSeries(name);
        int index = 0;
        int cutindex = 0;
        double cutPoint = cummulativePurityPoints[cutindex++];
        for (ClusterPeptideFraction decoy : values) {

            double purity = decoy.getPurity();
            while (purity > cutPoint) {
                series1.add(cutPoint, index);
                cutPoint = cummulativePurityPoints[cutindex++];
            }
            index++;
        }
        while(cutindex < cummulativePurityPoints.length)  {
            series1.add(cutPoint, index);
            cutindex++;
            if(cutindex >= cummulativePurityPoints.length)
                break;
            cutPoint = cummulativePurityPoints[cutindex];
        }
         return series1;
    }


    protected void addDecoyAndTargetData(final XYSeriesCollection dataset,IClusterSet cs, int minimumClusterSize) {
        ClusterComparisonMain cm = getSet();
        List<ClusterPeptideFraction> decoys = cm.getCumulativeDecoyData(cs,minimumClusterSize);
        XYSeries series = buildCummulativeSeries("Decoys", decoys);
        dataset.addSeries(series);

        List<ClusterPeptideFraction> targets = cm.getCumulativeTargetData(cs,minimumClusterSize);
        series = buildCummulativeSeries("Targets", targets);
        dataset.addSeries(series);

    }

    /**
     * Creates a chart.
     *
     * @param dataset the data for the chart.
     * @return a chart.
     */
    private JFreeChart createChart(final XYDataset dataset, int minSize,int numberSPectra) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Target and Decoy " + minSize,      // chart title
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
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
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


    public static void makeDecoyChart(ClusterComparisonMain cc,  final IClusterSet pCs,String name) {
        ClusterDecoyChart clusterSimilarityChart = new ClusterDecoyChart(cc);
        JPanel charts = clusterSimilarityChart.generateChart(pCs);


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

        ClusterDecoyChart clusterSimilarityChart = null; // new ClusterDecoyChart(mostSimilarClusterSet);

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
