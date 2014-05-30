package uk.ac.ebi.pride.spectracluster.hadoop.datastore;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

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
     *
     * @param id !null id
     * @return possibly null cluster
     */
    public ISpectrum getSpectrumById(String id);

    /**
     * iterate over all spectra in the database
     *
     * @return
     */
    public Iterable<? extends ISpectrum> getAllSpectra();


    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    public Iterable<? extends ISpectrum> getSpectrumByMz(double minMz, double mazMz);

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    public Iterable<? extends ISpectrum> getSpectrumByMzAndCharge(double minMz, double mazMz, int charge);


    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    public Iterable<? extends ISpectrum> getByPeptide(String peptide);

    /**
     * count the spectra
     * @return
     */
    public int getSpectrumCount();
}
