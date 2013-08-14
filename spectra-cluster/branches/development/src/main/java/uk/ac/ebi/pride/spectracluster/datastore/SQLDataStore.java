package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.SQLDataStore
 * version of ClusterDataStore where all spectra and clusters are in a SQL database
 * to flesh out the code
 * User: Steve
 * Date: 7/15/13
 */
public class SQLDataStore implements IMutableSpectrumDataStore {

    private final DataSource datasource;
    private final WorkingClusterDatabase database;
    private final String dataBaseName;

    public SQLDataStore(String database) {
        this(database, Defaults.INSTANCE.getDefaultDataSource());
    }

    public SQLDataStore(String databaseStr, DataSource ds) {
        datasource = ds;
        database = new WorkingClusterDatabase(databaseStr, ds);
        dataBaseName = databaseStr;
        database.guaranteeDatabase();

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
        storeSpectrum(added);
    }


    /**
     * add a spectrum
     *
     * @param removed !null added
     */
    @Override
    public void removeSpectrum(final ISpectrum removed) {
        final SimpleJdbcTemplate template = getDatabase().getTemplate();
        String query = queryForDatabase(SpectrumMapper.DELETE_SPECTRUM_STATEMENT);
        int ndropped = template.update(query,removed.getId());


    }


    /**
     * return a spectrum stored with a particular id
     *
     * @param id !null id
     * @return possibly null cluster
     */
    @Override
    public ISpectrum getSpectrumById(final String id) {
        String query = queryForDatabase(SpectrumMapper.SELECT_SPECTRUM_STATEMENT);
        ISpectrum ret = SpringJDBCUtilities.getItemWithId(getDatabase(), query, id, ISpectrum.class);
        return ret;
    }

    /**
     * make the query for the current database
     *
     * @param query !null oroginal query with "<database>" as a place holder
     * @return query with the proper database
     */
    protected String queryForDatabase(String query) {
        return query.replace("<database>", getDataBaseName());
    }

    /**
     * iterate over all spectra in the database
     *
     * @return
     */
    @Override
    public Iterable<? extends ISpectrum> getAllSpectra() {
        String query = queryForDatabase(SpectrumMapper.SELECT_ALL_SPECTRA_STATEMENT);
        final WorkingClusterDatabase database1 = getDatabase();
        List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(database1, query, SpringJDBCUtilities.SPECTRUM_MAPPER);
        return ret;
    }

    /**
     * store one spectrum in the database
     *
     * @param stored
     */
    @Override
    public void storeSpectrum(final ISpectrum stored) {
        String query = queryForDatabase(SpectrumMapper.INSERT_SPECTRUM_STATEMENT);
        final WorkingClusterDatabase database1 = getDatabase();
        final SimpleJdbcTemplate template = database1.getTemplate();
        template.update(query);
    }



    /**
     * store one spectrum in the database
     *
     * @param stored
     */
    @Override
    public void storeSpectra(List<? extends ISpectrum> stored) {
        String query = queryForDatabase(SpectrumMapper.INSERT_SPECTRUM_STATEMENT);
        final WorkingClusterDatabase database1 = getDatabase();
        JdbcTemplate templateX = database1.getOldTemplate();
        BatchPreparedStatementSetter batch = SpringJDBCUtilities.SPECTRUM_MAPPER.buildBatchSetter(stored);
        final int[] ints = templateX.batchUpdate(query, batch);
        for (int i = 0; i < ints.length; i++) {
            int anInt = ints[i];

        }
    }

    /**
     * return all spectra mapped tp a specific peptide
     *
     * @param peptide !null !empty peptide
     * @return !null iterable
     */
    @Override
    public Iterable<? extends ISpectrum> getByPeptide(final String peptide) {              // todo test
        String query = queryForDatabase(SpectrumMapper.SELECT_WITH_PEPTIDE);
         final WorkingClusterDatabase database1 = getDatabase();
         List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(database1, query, SpringJDBCUtilities.SPECTRUM_MAPPER,peptide);
         return ret;
     }



    /**
     * @param minMz >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz >= minMZ clusters returned have mz < this unless it == minMz
     * @return !null iterable
     */
    @Override
    public Iterable<? extends ISpectrum> getSpectrumByMz(final double minMz, final double mazMz) {       // todo test
        String query = queryForDatabase(SpectrumMapper.SELECT_WITH_MZ);
        final WorkingClusterDatabase database1 = getDatabase();
         List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(database1, query, SpringJDBCUtilities.SPECTRUM_MAPPER, minMz, mazMz);
         return ret;

    }

    /**
     * @param minMz  >0 <= maxMz  clusters returned have mz >= this
     * @param mazMz  >= minMZ clusters returned have mz < this unless it == minMz
     * @param charge 0 meaqns all charges otherwise a specific charge is called for
     * @return !null iterable
     */
    @Override
    public Iterable<? extends ISpectrum> getSpectrumByMzAndCharge(final double minMz, final double mazMz, final int charge) {   // todo test
        String query = queryForDatabase(SpectrumMapper.SELECT_WITH_CHARGE_AND_MZ);
        final WorkingClusterDatabase database1 = getDatabase();
         List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(database1, query, SpringJDBCUtilities.SPECTRUM_MAPPER, charge, minMz, mazMz);
         return ret;
     }


}
