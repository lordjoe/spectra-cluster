package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.InMemoryDatastore
 * Stupid version of ClusterDataStore where all spectra and clusters are in memory
 * User: Steve
 * Date: 7/15/13
 */
public class InMemoryDatastore implements IClusterDataStore {

    private final Map<String, ISpectrum> idToSpectrum = new HashMap<String, ISpectrum>();
    private final Map<String, ISpectralCluster> idToCluster = new HashMap<String, ISpectralCluster>();

    public InMemoryDatastore() {
    }

    /**
     * return a Cluster stored with a particular id
     *
     * @param id !null id
     * @return possibly null cluster
     */
    @Override
    public ISpectralCluster getById(final String id) {
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
     * store one spectrum in the database
     *
     * @param stored
     */
    @Override
    public void storeSpectrum(final ISpectrum stored) {
        idToSpectrum.put(stored.getId(), stored);
    }

    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectrum> getSpectrumByMz(final double minMz, final double mazMz) {
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
    public Iterable<ISpectrum> getSpectrumByMzAndCharge(final double minMz, final double mazMz, final int charge) {
        List<ISpectrum> ret = new ArrayList<ISpectrum>(idToSpectrum.values());
        Collections.sort(ret);  // sort by mz
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (ISpectrum sc : ret) {
            if(charge != 0 && charge != sc.getPrecursorCharge())
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
    public Iterable<ISpectralCluster> getClusterByMz(final double minMz, final double mazMz) {
        List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(idToCluster.values());
        Collections.sort(ret);  // sort by mz
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster sc : ret) {
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
    public Iterable<ISpectralCluster> getClusterByMzAndCharge(final double minMz, final double mazMz, final int charge) {
        List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(idToCluster.values());
         Collections.sort(ret);  // sort by mz
         List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
         for (ISpectralCluster sc : ret) {
             if(charge != 0 && charge != sc.getPrecursorCharge())
                  continue;
             float mz = sc.getPrecursorMz();
             if (mz < minMz || ((minMz == mazMz) && mz > mazMz) || (mz >= mazMz))
                 continue;
             holder.add(sc);
         }
          return holder;
    }

    /**
     * store one cluster in the database
     *
     * @param stored
     */
    @Override
    public void storeCluster(final ISpectralCluster stored) {
        idToCluster.put(stored.getId(), stored);

    }

    /**
     * delete one cluster in the database
     *
     * @param stored
     */
    @Override
    public void deleteCluster(final ISpectralCluster stored) {
        idToCluster.remove(stored.getId());

    }


    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectralCluster> getBPeptide(final String peptide) {
        List<ISpectralCluster> ret = new ArrayList<ISpectralCluster>(idToCluster.values());
         Collections.sort(ret);  // sort by mz
         List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
         for (ISpectralCluster sc : ret) {
              if(true)
                  throw new UnsupportedOperationException("Fix This"); // ToDo
         }
          return holder;
     }
}
