package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import com.lordjoe.utilities.ElapsedTimer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.ebi.pride.spectracluster.cluster.CountBasedClusterStabilityAssessor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSimilarityChart {
    private final MostSimilarClusterSet mostSimilarClusterSet;

    public ClusterSimilarityChart(MostSimilarClusterSet mostSimilarClusterSet) throws HeadlessException {
        this.mostSimilarClusterSet = mostSimilarClusterSet;
    }


    public JPanel generateChart() {
        IClusterDistance distance = mostSimilarClusterSet.getClusterDistance();
        XYSeries xySeries = new XYSeries(distance.getClass().getSimpleName());
        for (IPeptideSpectralCluster cluster : mostSimilarClusterSet.getBaseSet().getClusters()) {
            MostSimilarClusters mostSimilarClusters = mostSimilarClusterSet.getMostSimilarClusters(cluster);
            List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();

            ClusterDistanceItem bestMatch = bestMatches.get(0);
            IPeptideSpectralCluster source = bestMatch.getSource();
            IPeptideSpectralCluster target = bestMatch.getTarget();
            double dotProduct = Defaults.INSTANCE.getDefaultSimilarityChecker().assessSimilarity(source.getConsensusSpectrum(), target.getConsensusSpectrum());
            xySeries.add(dotProduct, bestMatch.getDistance());
        }

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(xySeries);

        return ChartUtilities.createScatterPlotChart(distance.getClass().getSimpleName() + " and Dot Product", "Dot Product", distance.getClass().getSimpleName(), xySeriesCollection);
    }

    private static IClusterSet readClusterSet(SimpleSpectrumRetriever simpleSpectrumRetriever, File newFile, String saveName) {
        IClusterSet newClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(newFile, simpleSpectrumRetriever);
        if (newFile.isDirectory())
            ClusterSimilarityUtilities.saveSemiStableClusters(newClusterSet, new File(saveName));
        return newClusterSet;
    }


    public static void makeDecoyChart(ClusterComparisonMain cc) {
        ClusterSimilarityChart clusterSimilarityChart = null; // new ClusterSimilarityChart(mostSimilarClusterSet);
        JPanel charts = clusterSimilarityChart.generateChart();


        JFrame frame = new JFrame("Cluster similarity charts");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * This is an example on how to use it
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
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

        List<IPeptideSpectralCluster> stableClusters = newClusterSet.getMatchingClusters(new StableClusterPredicate(new CountBasedClusterStabilityAssessor()));
        newClusterSet = new SimpleClusterSet(stableClusters);
        System.out.println("=======New ste duplicates =======================================");
        newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


        System.out.println("==============================================================");


        List<IPeptideSpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(new SemiStableClusterPredicate(new CountBasedClusterStabilityAssessor()));
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        System.out.println("==========original set duplicates ==========================");
        originalClusterSet = SimpleClusterSet.removeDuplicates(originalClusterSet);
        System.out.println("==============================================================");

        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ClusterSpectrumOverlapDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(originalClusterSet);

        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0

        ClusterSimilarityChart clusterSimilarityChart = new ClusterSimilarityChart(mostSimilarClusterSet);

        JPanel charts = clusterSimilarityChart.generateChart();

        timer.showElapsed("Create chart");
        timer.reset(); // back to 0

        JFrame frame = new JFrame("Cluster similarity charts");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(charts, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }
}
