package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.IMutableClusterDataStore
 * Expose methods for changing the datastore - most implementations can to these
 * but we may not wish to expost the capability
 * User: Steve
 * Date: 7/15/13
 */
public interface IMutableClusterDataStore extends IClusterDataStore {

    /**
     * delete ALL data - use with caution
     */
    public void clearAllData();


    /**
     * add a spectrum
     * @param added  !null added
     */
    public void addCluster(ISpectralCluster added);


    /**
     * add a spectrum
     * @param removed  !null added
     */
    public void removeCluster(ISpectralCluster removed);

}
