package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

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
    public void addSpectrum(ISpectrum added);

    /**
     * store one spectrum in the database
     *
     * @param stored
     */

    public void storeSpectra(List<ISpectrum> stored);


    /**
     * add a spectrum
     * @param added  !null added
     */
    public void addCluster(ISpectralCluster added);


    /**
     * add a spectrum
     * @param removed  !null added
     */
    public void removeSpectrum(ISpectrum removed);

    /**
     * add a spectrum
     * @param removed  !null added
     */
    public void removeCluster(ISpectralCluster removed);

}
