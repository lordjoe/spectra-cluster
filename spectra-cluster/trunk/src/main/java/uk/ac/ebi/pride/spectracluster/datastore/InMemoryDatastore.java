package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.InMemoryDatastore
 * Stupid version of ClusterDataStore where all spectra and clusters are in memory
 * User: Steve
 * Date: 7/15/13
 */
public class InMemoryDatastore implements IMutableClusterDataStore, IMutableSpectrumDataStore {

    private final Map<String, ISpectrum> idToSpectrum = new HashMap<String, ISpectrum>();
    private final Map<String, IPeptideSpectralCluster> idToCluster = new HashMap<String, IPeptideSpectralCluster>();

    public InMemoryDatastore() {
    }

    /**
     * return a Cluster stored with a particular id
     *
     * @param id !null id
     * @return possibly null cluster
     */
    @Override
    public IPeptideSpectralCluster getById(final String id) {
        return idToCluster.get(id);
    }

    /**
     * return a spectrum stored with a particular id
     *
     * @param id !null id
     * @return possibly null cluster
     */
    @Override
    public ISpectrum getSpectrumById(final String id) {
        return idToSpectrum.get(id);
    }

    /**
     * iterate over all spectra in the database
     *
     * @return
     */
    @Override
    public Iterable<ISpectrum> getAllSpectra() {
        List<ISpectrum> ret = new ArrayList<ISpectrum>(idToSpectrum.values());
        Collections.sort(ret);  // sort by mz
        return ret;
    }

    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    @Override
    public Iterable<? extends ISpectrum> getSpectrumByMz(final double minMz, final double mazMz) {
        List<ISpectrum> ret = new ArrayList<ISpectrum>(idToSpectrum.values());
        Collections.sort(ret);  // sort by mz
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectrum sc : ret) {
            float mz = sc.getPrecursorMz();
            if (mz < minMz || ((minMz == mazMz) && mz > mazMz) || (mz >= mazMz))
                continue;
            holder.add(sc);
        }


        return holder;
    }

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    @Override
    public Iterable<? extends ISpectrum> getSpectrumByMzAndCharge(final double minMz, final double mazMz, final int charge) {
        List<ISpectrum> ret = new ArrayList<ISpectrum>(idToSpectrum.values());
        Collections.sort(ret);  // sort by mz
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectrum sc : ret) {
            if (charge != 0 && charge != sc.getPrecursorCharge())
                continue;
            float mz = sc.getPrecursorMz();
            if (mz < minMz || ((minMz == mazMz) && mz > mazMz) || (mz >= mazMz))
                continue;
            holder.add(sc);
        }
        return holder;
    }

    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    @Override
    public Iterable<? extends IPeptideSpectralCluster> getClusterByMz(final double minMz, final double mazMz) {
        List<IPeptideSpectralCluster> ret = new ArrayList<IPeptideSpectralCluster>(idToCluster.values());
        Collections.sort(ret);  // sort by mz
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster sc : ret) {
            float mz = sc.getPrecursorMz();
            if (mz < minMz || ((minMz == mazMz) && mz > mazMz) || (mz >= mazMz))
                continue;
            holder.add(sc);
        }
        return holder;
    }

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    @Override
    public Iterable<? extends IPeptideSpectralCluster> getClusterByMzAndCharge(final double minMz, final double mazMz, final int charge) {
        List<IPeptideSpectralCluster> ret = new ArrayList<IPeptideSpectralCluster>(idToCluster.values());
        Collections.sort(ret);  // sort by mz
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster sc : ret) {
            if (charge != 0 && charge != sc.getPrecursorCharge())
                continue;
            float mz = sc.getPrecursorMz();
            if (mz < minMz || ((minMz == mazMz) && mz > mazMz) || (mz >= mazMz))
                continue;
            holder.add(sc);
        }
        return holder;
    }

    @Override
    public void storeClusters(Collection<IPeptideSpectralCluster> clustersToStore) {
        for (IPeptideSpectralCluster cluster : clustersToStore) {
            storeCluster(cluster);
        }
    }


    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    @Override
    public Iterable<? extends ISpectrum> getByPeptide(final String peptide) {
        List<ISpectrum> ret = new ArrayList<ISpectrum>(idToSpectrum.values());
        Collections.sort(ret);  // sort by mz
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectrum sc : ret) {
            if (sc instanceof IPeptideSpectrumMatch) {
                if (peptide.equals(((IPeptideSpectrumMatch) sc).getPeptide()))
                    holder.add(sc);
            }
        }
        return holder;
    }


    /**
     * delete ALL data - use with caution
     */
    @Override
    public void clearAllData() {
        idToCluster.clear();
        idToSpectrum.clear();
    }

    /**
     * add a spectrum
     *
     * @param added !null added
     */
    @Override
    public void storeSpectrum(final ISpectrum added) {
        final String key = added.getId();
        if (idToSpectrum.containsKey(key))
            throw new IllegalStateException("cannot add an existing spectrum");
        idToSpectrum.put(key, added);
    }

    /**
     * store a collection of spectra
     *
     * @param stored !null collection
     */
    @Override
    public void storeSpectra(final List<? extends ISpectrum> stored) {
        for (ISpectrum added : stored) {
            storeSpectrum(added);
        }
    }

    /**
     * add a spectrum
     *
     * @param added !null added
     */
    @Override
    public void storeCluster(final IPeptideSpectralCluster added) {
        final String key = added.getId();
        if (idToCluster.containsKey(key))
            throw new IllegalStateException("cannot add an existing spectrum");
        idToCluster.put(key, added);

    }

    /**
     * add a spectrum
     *
     * @param removed !null added
     */
    @Override
    public void removeSpectrum(final ISpectrum removed) {
        final String key = removed.getId();
        idToSpectrum.remove(key);
    }

    /**
     * add a spectrum
     *
     * @param removed !null added
     */
    @Override
    public void removeCluster(final IPeptideSpectralCluster removed) {
        final String key = removed.getId();
        idToCluster.remove(key);

    }

    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    @Override
    public Iterable<IPeptideSpectralCluster> getClustersByPeptide(final String peptide) {
        List<IPeptideSpectralCluster> ret = new ArrayList<IPeptideSpectralCluster>(idToCluster.values());
        Collections.sort(ret);  // sort by mz
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster sc : ret) {
            String peptides = ClusterUtilities.mostCommonPeptides(sc);
            if (peptides.contains(",")) {
                String[] items = peptides.split(",");
                for (int i = 0; i < items.length; i++) {
                    String item = items[i];
                    if (peptide.equals(item)) {
                        holder.add(sc);
                        break; // done
                    }
                }
            } else {
                if (peptide.equals(peptides))  // only one peptide
                    holder.add(sc);
            }
        }
        return holder;
    }
}
