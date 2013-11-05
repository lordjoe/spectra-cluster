package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.Collection;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterDataStore implements IMutableClusterDataStore {

    @Override
    public void clearAllData() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void storeCluster(ISpectralCluster clusterToStore) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void storeClusters(Collection<ISpectralCluster> clustersToStore) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeCluster(ISpectralCluster clusterToRemove) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ISpectralCluster getById(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<? extends ISpectralCluster> getClusterByMz(double minMz, double mazMz) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<? extends ISpectralCluster> getClusterByMzAndCharge(double minMz, double mazMz, int charge) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<? extends ISpectralCluster> getClustersByPeptide(String peptide) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
