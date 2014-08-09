package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters;

import uk.ac.ebi.pride.spectracluster.clusteringfileconverter.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.SequenceCount;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by jg on 01.08.14.
 */
public class ClusterMspConverter implements IClusterConverter {
    public static String FILE_EXTENSION = "msp";
    private String outputPath;
    private BufferedWriter writer;

    private int minSize = 0;
    private int maxSize = Integer.MAX_VALUE;
    private float minRatio = 0;
    private float maxRatio = 1;

    public ClusterMspConverter() {
    }

    @Override
    public String getFileHeader() {
        return "";
    }

    @Override
    public String getFiletypeExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public String convertCluster(ICluster cluster) {
        StringBuilder mspString = new StringBuilder();

        mspString.append("Name: ").append(generateClusterName(cluster)).append("\n");
        mspString.append("Comment: ").append(generateComments(cluster)).append("\n");

        mspString.append("Num peaks: ").append(cluster.getConsensusMzValues().size()).append("\n");

        for (int i = 0; i < cluster.getConsensusMzValues().size(); i++) {
            mspString.append(cluster.getConsensusMzValues().get(i)).append(" ").append(cluster.getConsensusIntensValues().get(i)).append("\n");
        }

        return mspString.toString();
    }

    private String generateComments(ICluster cluster) {
        StringBuilder commentString = new StringBuilder();
        commentString.append("Spec=Consensus");
        commentString.append(" Mods=NA"); // TODO: add modifications
        commentString.append(" Parent=").append(cluster.getAvPrecursorMz());
        commentString.append(" Nreps=").append(cluster.getSpecCount());
        commentString.append(" Naa=").append(cluster.getMaxSequence().length());
        commentString.append(" MaxRatio=").append(cluster.getMaxRatio());
        commentString.append(" PrecursorMzRange=").append(cluster.getSpectrumPrecursorMzRange());

        return commentString.toString();
    }

    private String generateClusterName(ICluster cluster) {
        String maxSeqence = "";
        int maxSequenceCount = 0;

        for (SequenceCount count : cluster.getSequenceCounts()) {
            if (maxSequenceCount < count.getCount()) {
                maxSeqence = count.getSequence();
                maxSequenceCount = count.getCount();
            }
        }

        return String.format("%s/%d", maxSeqence, ClusterUtilities.calculateClusterCharge(cluster));
    }

    @Override
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String getOuputPath() {
        return outputPath;
    }

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        if (outputPath == null)
            throw new IllegalStateException("OutputPath must be set before any clusters can be written.");

        if (newCluster.getSpecCount() < minSize)
            return;
        if (newCluster.getSpecCount() > maxSize)
            return;
        if (newCluster.getMaxRatio() < minRatio)
            return;
        if (newCluster.getMaxRatio() > maxRatio)
            return;

        String mspString = convertCluster(newCluster);

        try {
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(outputPath));
            }

            writer.write(mspString);
            writer.write("\n");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.close();
            writer = null;
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
}
