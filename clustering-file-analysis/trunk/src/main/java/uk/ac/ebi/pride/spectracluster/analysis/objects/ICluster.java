package uk.ac.ebi.pride.spectracluster.analysis.objects;

import java.util.List;
import java.util.Set;

/**
 * Created by jg on 10.07.14.
 */
public interface ICluster {
    public float getAvPrecursorMz();

    public float getAvPrecursorIntens();

    public Set<String> getSequences();

    public List<String> getSpectraIds();

    public int getSpecCount();

    public List<SequenceCount> getSequenceCounts();

    public float getMaxRatio();
}
