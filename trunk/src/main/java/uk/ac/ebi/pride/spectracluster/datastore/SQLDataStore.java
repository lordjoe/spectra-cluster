package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.SQLDataStore
 *   version of ClusterDataStore where all spectra and clusters are in a SQL database
 *   to flesh out the code
 * User: Steve
 * Date: 7/15/13
 */
public class SQLDataStore  implements IMutableClusterDataStore {

    private final DataSource datasource;
    private final WorkingClusterDatabase database;
    private final String dataBaseName;

    public SQLDataStore(String database)   {
        this(database, Defaults.INSTANCE.getDefaultDataSource());
    }
    public SQLDataStore(String databaseStr ,DataSource ds )   {
        datasource =  ds;
       database = new WorkingClusterDatabase(databaseStr,ds) ;
        dataBaseName = databaseStr;
        database.guaranteeDatabase( );

    }

    public DataSource getDatasource() {
        return datasource;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public WorkingClusterDatabase getDatabase() {
        return database;
    }

    /**
     * delete ALL data - use with caution
     */
    @Override
    public void clearAllData() {
        getDatabase().clearDatabase();
    }

    /**
     * add a spectrum
     *
     * @param added !null added
     */
    @Override
    public void addSpectrum(final ISpectrum added) {
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo


    }





    /**
     * add a spectrum
     *
     * @param added !null added
     */
    @Override
    public void addCluster(final ISpectralCluster added) {
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    /**
     * add a spectrum
     *
     * @param removed !null added
     */
    @Override
    public void removeSpectrum(final ISpectrum removed) {
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
         if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    /**
     * add a spectrum
     *
     * @param removed !null added
     */
    @Override
    public void removeCluster(final ISpectralCluster removed) {
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    /**
     * return a Cluster stored with a particular id
     *
     * @param id !null id
     * @return possibly null cluster
     */
    @Override
    public ISpectralCluster getById(final String id) {
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * return a spectrum stored with a particular id
     *
     * @param id !null id
     * @return possibly null cluster
     */
    @Override
    public ISpectrum getSpectrumById(final String id) {
        String query =  queryForDatabase(SpectrumMapper.SELECT_SPECTRUM_STATEMENT);
        ISpectrum ret = SpringJDBCUtilities.getItemWithId(getDatabase(),query,   id,ISpectrum.class) ;
        return ret;
    }

    /**
     * make the query for the current database
     * @param query !null oroginal query with "<database>" as a place holder
     * @return query with the proper database
     */
    protected String queryForDatabase(String query)    {
        return query.replace("<database>",getDataBaseName());
    }

    /**
     * iterate over all spectra in the database
     *
     * @return
     */
    @Override
    public Iterable<ISpectrum> getAllSpectra() {
        String query =  queryForDatabase(SpectrumMapper.SELECT_ALL_SPECTRA_STATEMENT);
        List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(getDatabase(),query, SpringJDBCUtilities.SPECTRUM_MAPPER) ;
         return ret;
      }

    /**
     * store one spectrum in the database
     *
     * @param stored
     */
    @Override
    public void storeSpectrum(final ISpectrum stored) {
        String query =  queryForDatabase(SpectrumMapper.INSERT_SPECTRUM_STATEMENT);
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
        template.update(query);
    }


    /**
     * store one spectrum in the database
     *
     * @param stored
     */
    @Override
    public void storeSpectra(List<ISpectrum> stored) {
        String query =  queryForDatabase(SpectrumMapper.INSERT_SPECTRUM_STATEMENT);
        JdbcTemplate templateX =  getDatabase().getOldTemplate();
        BatchPreparedStatementSetter batch = SpringJDBCUtilities.SPECTRUM_MAPPER.buildBatchSetter(stored);
        final int[] ints = templateX.batchUpdate(query, batch);
        for (int i = 0; i < ints.length; i++) {
            int anInt = ints[i];

        }
    }

    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectrum> getSpectrumByMz(final double minMz, final double mazMz) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectrum> getSpectrumByMzAndCharge(final double minMz, final double mazMz, final int charge) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * store one cluster in the database
     *
     * @param stored
     */
    @Override
    public void storeCluster(final ISpectralCluster stored) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    /**
     * delete one cluster in the database
     *
     * @param stored
     */
    @Override
    public void deleteCluster(final ISpectralCluster stored) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectralCluster> getClusterByMz(final double minMz, final double mazMz) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectralCluster> getClusterByMzAndCharge(final double minMz, final double mazMz, final int charge) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    @Override
    public Iterable<ISpectralCluster> getBPeptide(final String peptide) {
        if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }
}
