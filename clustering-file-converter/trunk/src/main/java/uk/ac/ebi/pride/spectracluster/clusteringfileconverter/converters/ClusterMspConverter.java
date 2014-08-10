package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters;

import uk.ac.ebi.pride.spectracluster.clusteringfileconverter.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.SequenceCount;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by jg on 01.08.14.
 */
public class ClusterMspConverter extends AbstractClusterConverter {
    public static String FILE_EXTENSION = "msp";

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
    public void onNewClusterRead(ICluster newCluster) {
        if (!shouldClusterBeExported(newCluster))
            return;

        String mspString = convertCluster(newCluster);

        writeStringToFile(mspString + "\n", getFileHeader());
    }
}
