package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.IClusterDataStore
 * interface implemented by an object that store clusters - it is not uncommon that
 * the underlying implementation uses a database
 * User: Steve
 * Date: 7/15/13
 */
public interface IClusterDataStore {

    /**
     * return a Cluster stored with a particular id
     * @param id !null id
     * @return   possibly null cluster
     */
    public ISpectralCluster getById(String id);

    /**
     * return a spectrum stored with a particular id
     * @param id !null id
     * @return   possibly null cluster
     */
    public ISpectrum getSpectrumById(String id);

    /**
     * iterate over all spectra in the database
     * @return
     */
    public Iterable<ISpectrum> getAllSpectra( );

    /**
     * store one spectrum in the database
     * @param stored
     */
    public void storeSpectrum(ISpectrum stored);


    /**
     *
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @return  !null iterable
     */
    public Iterable<ISpectrum> getSpectrumByMz(double minMz, double mazMz);

    /**
     *
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge  0 meaqns all charges otherwise a specific charge is called for
      * @return  !null iterable
     */
    public Iterable<ISpectrum> getSpectrumByMzAndCharge(double minMz, double mazMz, int charge);




    /**
     * store one cluster in the database
     * @param stored
     */
    public void storeCluster(ISpectralCluster stored);

    /**
     * delete one cluster in the database
     * @param stored
     */
    public void deleteCluster(ISpectralCluster stored);


    /**
     *
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @return  !null iterable
     */
    public Iterable<ISpectralCluster> getClusterByMz(double minMz, double mazMz);

    /**
     *
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge  0 meaqns all charges otherwise a specific charge is called for
      * @return  !null iterable
     */
    public Iterable<ISpectralCluster> getClusterByMzAndCharge(double minMz, double mazMz, int charge);


    /**
     * return all spectra mapped tp a specific peptide
     * @param peptide !null !empty peptide
     * @return   !null iterable
     */
    public Iterable<ISpectralCluster> getBPeptide(String peptide);


}
