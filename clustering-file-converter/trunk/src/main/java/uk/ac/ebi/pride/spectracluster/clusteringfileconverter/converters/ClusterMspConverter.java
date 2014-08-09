package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.SequenceCount;

/**
 * Created by jg on 01.08.14.
 */
public class ClusterMspConverter implements IClusterConverter {
    @Override
    public String convertCluster(ICluster cluster) {
        StringBuilder mspString = new StringBuilder();

        mspString.append("Name: " + generateClusterName(cluster) + "\n");
        mspString.append("Comment: " + generateComments(cluster) + "\n");

        mspString.append("Num peaks: " + cluster.getConsensusMzValues().size() + "\n");

        for (int i = 0; i < cluster.getConsensusMzValues().size(); i++) {
            mspString.append(cluster.getConsensusMzValues().get(i) + " " + cluster.getConsensusIntensValues().get(i) + "\n");
        }

        return mspString.toString();
    }

    private String generateComments(ICluster cluster) {
        StringBuilder commentString = new StringBuilder("Spec=Consensus");
        commentString.append(" Mods=NA"); // TODO: add modifications
        commentString.append(" Parent=" + cluster.getAvPrecursorMz());
        commentString.append(" Nreps=" + cluster.getSpecCount());
        commentString.append(" Naa=" + cluster.getMaxSequence().length());
        commentString.append(" MaxRatio=" + cluster.getMaxRatio());
        commentString.append(" PrecursorMzRange=" + cluster.getSpectrumPrecursorMzRange());

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

        // calculate the average charge
        int sumCharge = 0;

        for (ISpectrumReference specRef : cluster.getSpectrumReferences()) {
            sumCharge += specRef.getCharge();
        }

        float avCharge = (float) sumCharge / (float) cluster.getSpectrumReferences().size();
        int avChargeRounded = (int) (avCharge + 0.5);

        return String.format("%s/%d", maxSeqence, avChargeRounded);
    }
}
