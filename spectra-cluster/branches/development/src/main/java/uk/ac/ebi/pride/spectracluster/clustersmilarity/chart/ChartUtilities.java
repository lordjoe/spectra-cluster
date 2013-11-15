package uk.ac.ebi.pride.spectracluster.clustersmilarity.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ChartUtilities {

    public static JPanel createScatterPlotChart(String title,
                                                String xAxisLabel,
                                                String yAxisLabel,
                                                XYDataset dataset) {

        JFreeChart chart = ChartFactory.createScatterPlot(title,
                xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, true, false);

        XYPlot xyPlot = (XYPlot) chart.getPlot();
        XYDotRenderer xyDotRenderer = new XYDotRenderer();
        xyDotRenderer.setDotWidth(2);
        xyDotRenderer.setDotHeight(2);
        xyPlot.setRenderer(xyDotRenderer);

        NumberAxis domainAxis = (NumberAxis)xyPlot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);

        return createChartPanel(chart);
    }

    private static JPanel createChartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }
}
