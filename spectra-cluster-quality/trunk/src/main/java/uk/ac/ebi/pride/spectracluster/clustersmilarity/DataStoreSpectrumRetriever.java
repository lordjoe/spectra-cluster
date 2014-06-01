package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.export.*;
import uk.ac.ebi.pride.spectracluster.hadoop.datastore.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import javax.sql.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class DataStoreSpectrumRetriever implements IMutableSpectrumRetriever {


    private final IMutableSpectrumDataStore datastore;

    public DataStoreSpectrumRetriever(IMutableSpectrumDataStore datastore) {
        this.datastore = datastore;
    }


    public List<ISpectrum> getSpectra() {
        throw new UnsupportedOperationException("Bad idea from a database");
    }

    @Override
    public void addSpectra(ISpectrum... spectra) {
        for (ISpectrum spectrum : spectra) {
            datastore.storeSpectrum(spectrum);
        }
    }


    @Override
    public ISpectrum retrieve(String spectrumId) {
        return (ISpectrum) datastore.getSpectrumById(spectrumId);
    }

    public List<ISpectrum> retrieveByPeptide(String peptide) {
        List<ISpectrum> holder = new ArrayList<ISpectrum>();

        for (ISpectrum iSpectrum : datastore.getByPeptide(peptide)) {
            holder.add((ISpectrum) iSpectrum);
        }
        return holder;
    }


    @Override
    public List<ISpectrum> retrieveAll() {
        throw new UnsupportedOperationException("Bad idea from a database");
    }


    public int getSpectraCount() {

        return datastore.getSpectrumCount();
    }

    /**
     * quick sample
     * @param args
     */
    public static void main(String[] args) {
        DataSource source = WorkingClusteringDatabase.buildClusterDataSource("");
        DataSourceDefaults.INSTANCE.setDefaultDataSource(source);
        DataSourceDefaults.INSTANCE.setDatabaseFactory(WorkingClusterDatabase.FACTORY);

        SpectrumDataStore spectrumDataStore = new SpectrumDataStore(args[0], source);

        DataStoreSpectrumRetriever dr = new DataStoreSpectrumRetriever(spectrumDataStore);

        final int spectraCount = dr.getSpectraCount();

        System.out.println("count " + spectraCount);
    }
}
