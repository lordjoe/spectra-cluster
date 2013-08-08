package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.IMutableSpectrumDataStore
 * Expose methods for changing the datastore - most implementations can to these
 * but we may not wish to expost the capability
 * User: Steve
 * Date: 7/15/13
 */
public interface IMutableSpectrumDataStore extends ISpectrumDataStore {

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

    public void storeSpectra(List<? extends ISpectrum> stored);


    /**
     * add a spectrum
     * @param removed  !null added
     */
    public void removeSpectrum(ISpectrum removed);


}