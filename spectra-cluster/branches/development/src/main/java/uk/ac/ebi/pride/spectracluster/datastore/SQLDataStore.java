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
    private final IWorkingClusterDatabase database;
    private final String dataBaseName;

    @SuppressWarnings("UnusedDeclaration")
    public SQLDataStore(String database) {
        this(database, Defaults.INSTANCE.getDefaultDataSource());
    }

    public SQLDataStore(String databaseStr, DataSource ds) {
        datasource = ds;
        database = Defaults.INSTANCE.getDatabaseFactory().buildWorkingDatabase(databaseStr, ds);
        dataBaseName = databaseStr;

    }

    @SuppressWarnings("UnusedDeclaration")
    public DataSource getDatasource() {
        return datasource;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public IWorkingClusterDatabase getDatabase() {
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
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        int ndropped = template.update(query, removed.getId());


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
               //noinspection UnnecessaryLocalVariable
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
        final IWorkingClusterDatabase database1 = getDatabase();
        // make any changes in the query string
        query = database1.patchQueryString(query);
          //noinspection UnnecessaryLocalVariable
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
        final IWorkingClusterDatabase database1 = getDatabase();
        // make any changes in the query string
        query = database1.patchQueryString(query);
        final SimpleJdbcTemplate template = database1.getTemplate();

        Object[] values = new Object[6];
        int index = 0;
        values[index++] = stored.getId();    // 1
        values[index++] = stored.getPrecursorCharge();     // 2
        values[index++] = stored.getPrecursorMz();       // 3
        if (stored instanceof PeptideSpectrumMatch) {
            values[index++] = ((PeptideSpectrumMatch) stored).getPeptide();   // peptide 4

        } else {
            values[index++] = null;   // peptide 4

        }
        values[index++] = null;   // annotation  5
        final List<IPeak> peaks = stored.getPeaks();

//        if(peaks.size() > WorkingClusterDatabase.MAX_PEAKS_PER_SPECTRUM)    {
//            throw new UnsupportedOperationException("Fix This"); // ToDo sort and use the highest WorkingClusterDatabase.MAX_PEAKS_PER_SPECTRUM (512)
//        }
        final String encodedPeaks = SpectrumUtilities.peaksToDataString(peaks);

        int len = encodedPeaks.length();
        System.out.println("Spectrum length = " + len);
        //noinspection UnusedAssignment
        values[index++] = encodedPeaks;    // peaks     6

        if(len > WorkingClusterDatabase.MAX_PEAKS_STRING_LENGTH)  {
            throw new UnsupportedOperationException("Fix This"); // ToDo   filter if too many peaks
        }
         template.update(query, values);
    }


    /**
     * store one spectrum in the database
     *
     * @param stored
     */
    @Override
    public void storeSpectra(List<? extends ISpectrum> stored) {
        String query = queryForDatabase(SpectrumMapper.INSERT_SPECTRUM_STATEMENT);

        final IWorkingClusterDatabase database1 = getDatabase();

        // make any changes in the query string
        query = database1.patchQueryString(query);

        JdbcTemplate templateX = database1.getOldTemplate();
        if (database1.isBatchSupported()) {
            BatchPreparedStatementSetter batch = SpringJDBCUtilities.SPECTRUM_MAPPER.buildBatchSetter(stored);
            final int[] ints = templateX.batchUpdate(query, batch);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < ints.length; i++) {
                //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                int anInt = ints[i];

            }
        } else {
            for (ISpectrum spc : stored) {
                storeSpectrum(spc);
            }
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
        final IWorkingClusterDatabase database1 = getDatabase();
          //noinspection UnnecessaryLocalVariable
        List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(database1, query, SpringJDBCUtilities.SPECTRUM_MAPPER, peptide);
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
        final IWorkingClusterDatabase database1 = getDatabase();
          //noinspection UnnecessaryLocalVariable
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
        final IWorkingClusterDatabase database1 = getDatabase();
        //noinspection UnnecessaryLocalVariable
        List<ISpectrum> ret = SpringJDBCUtilities.getObjectsSatisfying(database1, query, SpringJDBCUtilities.SPECTRUM_MAPPER, charge, minMz, mazMz);
        return ret;
    }


}
