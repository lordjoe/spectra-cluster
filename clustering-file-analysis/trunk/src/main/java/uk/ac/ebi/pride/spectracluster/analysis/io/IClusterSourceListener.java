package uk.ac.ebi.pride.spectracluster.analysis.io;

import uk.ac.ebi.pride.spectracluster.analysis.objects.ICluster;

/**
 * Created by jg on 10.07.14.
 */
public interface IClusterSourceListener {
    public void onNewClusterRead(ICluster newCluster);
}
