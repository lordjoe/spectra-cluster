package uk.ac.ebi.pride.spectracluster.analysis.objects;

import java.util.*;

/**
 * Created by jg on 10.07.14.
 */
public class ClusteringFileCluster implements ICluster {
    private final float avPrecursorMz;
    private final float avPrecursorIntens;
    private final List<SequenceCount> sequenceCounts;
    private final List<String> specIds;
    private final float maxRatio;

    private final int specCount;
    private final Set<String> sequences;

    public ClusteringFileCluster(float avPrecursorMz, float avPrecursorIntens, List<SequenceCount> sequenceCounts, List<String> specIds) {
        this.avPrecursorMz = avPrecursorMz;
        this.avPrecursorIntens = avPrecursorIntens;
        this.sequenceCounts = sequenceCounts;
        this.specIds = specIds;

        // calculate the total specCount
        int tmpSpecCount = 0;
        sequences = new HashSet<String>(sequenceCounts.size());
        int maxSequenceCount = 0;

        for (SequenceCount c : sequenceCounts) {
            tmpSpecCount += c.getCount();
            sequences.add(c.getSequence());

            if (maxSequenceCount < c.getCount())
                maxSequenceCount = c.getCount();
        }

        specCount = tmpSpecCount;
        maxRatio = (float) maxSequenceCount / (float) specCount;
    }

    @Override
    public float getAvPrecursorMz() {
        return avPrecursorMz;
    }

    @Override
    public float getAvPrecursorIntens() {
        return avPrecursorIntens;
    }

    @Override
    public Set<String> getSequences() {
        return Collections.unmodifiableSet(sequences);
    }

    @Override
    public List<String> getSpectraIds() {
        return Collections.unmodifiableList(specIds);
    }

    @Override
    public int getSpecCount() {
        return specCount;
    }

    @Override
    public float getMaxRatio() {
        return maxRatio;
    }

    @Override
    public List<SequenceCount> getSequenceCounts() {
        return Collections.unmodifiableList(sequenceCounts);
    }
}
