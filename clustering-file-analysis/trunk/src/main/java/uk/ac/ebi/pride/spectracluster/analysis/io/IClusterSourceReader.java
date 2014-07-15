package uk.ac.ebi.pride.spectracluster.analysis.io;

import uk.ac.ebi.pride.spectracluster.analysis.objects.ICluster;

import java.util.Collection;

/**
 * Created by jg on 10.07.14.
 */
public interface IClusterSourceReader {
    public Collection<ICluster> readAllClusters() throws Exception;

    public boolean supportsReadAllClusters();

    public void readClustersIteratively(Collection<IClusterSourceListener> listeners) throws Exception;
}
