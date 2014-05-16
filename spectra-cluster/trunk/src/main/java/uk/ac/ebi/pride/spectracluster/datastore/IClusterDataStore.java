package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;


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
     *
     * @param id !null id
     * @return possibly null cluster
     */
    public IPeptideSpectrumCluster getById(String id);

    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    public Iterable<? extends IPeptideSpectrumCluster> getClusterByMz(double minMz, double mazMz);

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    public Iterable<? extends IPeptideSpectrumCluster> getClusterByMzAndCharge(double minMz, double mazMz, int charge);


    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    public Iterable<? extends IPeptideSpectrumCluster> getClustersByPeptide(String peptide);

}
