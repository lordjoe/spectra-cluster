package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters;

import uk.ac.ebi.pride.spectracluster.clusteringfileconverter.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by jg on 09.08.14.
 */
public class ClusterMgfConverter implements IClusterConverter {
    public static String FILE_EXTENSION = "mgf";
    private String outputPath;
    private BufferedWriter writer;

    private int minSize = 0;
    private int maxSize = Integer.MAX_VALUE;
    private float minRatio = 0;
    private float maxRatio = 1;

    private int clusterCounter = 0;

    @Override
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String getOuputPath() {
        return outputPath;
    }

    @Override
    public String getFileHeader() {
        return "COM=Converted spectral library\n";
    }

    @Override
    public String getFiletypeExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public String convertCluster(ICluster cluster) {
        StringBuilder stringBuilder = new StringBuilder("BEGIN IONS\n");

        stringBuilder.append(String.format("TITLE=consensus spectrum %d\n", clusterCounter));
        int charge = ClusterUtilities.calculateClusterCharge(cluster);
        stringBuilder.append(String.format("PEPMASS=%.3f\n", cluster.getAvPrecursorMz()));
        stringBuilder.append(String.format("CHARGE=%d%c\n", Math.abs(charge), (charge > 0 ? '+' : '-')));

        // add the peak list
        for (int i = 0; i < cluster.getConsensusMzValues().size(); i++) {
            stringBuilder.append(cluster.getConsensusMzValues().get(i)).append(" ").append(cluster.getConsensusIntensValues().get(i)).append("\n");
        }

        stringBuilder.append("END IONS\n");

        return stringBuilder.toString();
    }

    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.close();
            writer = null;
            clusterCounter = 0;
        }
    }

    @Override
    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    @Override
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void setMinRatio(float minRatio) {
        this.minRatio = minRatio;
    }

    @Override
    public void setMaxRatio(float maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public int getMinSize() {
        return minSize;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public float getMinRatio() {
        return minRatio;
    }

    @Override
    public float getMaxRatio() {
        return maxRatio;
    }

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        if (outputPath == null)
            throw new IllegalStateException("OutputPath must be set before clusters can be written.");

        if (newCluster.getSpecCount() < minSize)
            return;
        if (newCluster.getSpecCount() > maxSize)
            return;
        if (newCluster.getMaxRatio() < minRatio)
            return;
        if (newCluster.getMaxRatio() > maxRatio)
            return;

        try {
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(outputPath));
                writer.write(getFileHeader());
            }

            writer.write(convertCluster(newCluster));
            writer.write("\n");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
