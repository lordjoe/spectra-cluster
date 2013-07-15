package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.ISpectrumDataStore
 * interface implemented by an object that store spectra - it is not uncommon that
 * the underlying implementation uses a database
 * User: Steve
 * Date: 7/15/13
 */
public interface ISpectrumDataStore {

    /**
     * return a spectrum stored with a particular id
     * @param id !null id
     * @return   possibly null spectrum
     */
    public ISpectrum getById(String id);


    /**
     *
     * @param minMz  >0 <= maxMz  spectra returned have mz >= this
     * @param mazMz  >= minMZ spectras returned have mz < this unless it == minMz
     * @return  !null iterable
     */
    public Iterable<ISpectrum> getByMz(double minMz,double mazMz);

    /**
     *
     * @param minMz  >0 <= maxMz  spectra returned have mz >= this
     * @param mazMz  >= minMZ spectras returned have mz < this unless it == minMz
     * @param charge  0 meaqns all charges otherwise a specific charge is called for
      * @return  !null iterable
     */
    public Iterable<ISpectrum> getByMzAndCharge(double minMz,double mazMz,int charge);


    /**
     * return all spectra mapped tp a specific peptide
     * @param peptide !null !empty peptide
     * @return   !null iterable
     */
    public Iterable<ISpectrum> getBPeptide(String peptide);


}
