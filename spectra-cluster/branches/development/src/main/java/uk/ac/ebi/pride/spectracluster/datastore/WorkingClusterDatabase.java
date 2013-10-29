package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.dao.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.*;

import javax.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.WorkingClusterDatabase
 * an object representing Spring JDBC interface to a working spectrum database
 * The database may be cleared after clustering runs
 * User: Steve
 * Date: 7/15/13
 */
public class WorkingClusterDatabase implements IWorkingClusterDatabase {

    public static WorkingDatabaseFactory FACTORY = new WorkingDatabaseFactory() {
        /**
         * return a workling database - needed so we can subclass to handle Phoenix
         *
         * @param databaseName - name of the database - tables will have <databasename> . prepended
         * @param ds           !null data source - this provides the connection
         * @return !null   IWorkingClusterDatabase
         */
        @Override
        public IWorkingClusterDatabase buildWorkingDatabase(String databaseName, DataSource ds) {
            WorkingClusterDatabase ret = new WorkingClusterDatabase(databaseName, ds);
             return ret;

        }
    };


    public static final int MAX_IN_MEMORY_SPECTRA = 15000;
    public static final int MAX_PEAKS_PER_SPECTRUM = 512;
    public static final int MAX_PEAKS_STRING_LENGTH = MAX_PEAKS_PER_SPECTRUM * 8;

    public static final String[] TABLES =
            {
                    "spectrums",
                    //             "clusters",
            };

    public static final String[] CREATE_STATEMENTS =
            {
                    SpectrumMapper.TABLE_CREATE_STATEMENT,
                    //           ClusterMapper.TABLE_CREATE_STATEMENT,
            };


    public static final String SELECT_ALL_CLUSTERS_STATEMENT = "SELECT * FROM clusters  ";
    @SuppressWarnings("UnusedDeclaration")
    public static final String SELECT_CLUSTER_STATEMENT = SELECT_ALL_CLUSTERS_STATEMENT + " WHERE id = ?";


    private final String databaseName;
    private final JdbcTemplate m_OldTemplate;
    private final DataSource dataSource;
    private final SimpleJdbcTemplate m_Template;
    private final Map<String, String> m_NameToCreateStatement = new HashMap<String, String>();

    /**
     * PRIVATE - use the factory
     *
     * @param databaseName - name of the database - tables will have <databasename> . prepended
     * @param ds           !null data source - this provides the connection
     * @return !null   IWorkingClusterDatabase
     */
    protected WorkingClusterDatabase(String databaseName, final DataSource ds) {
        dataSource = ds;
        m_Template = new SimpleJdbcTemplate(ds);
        m_OldTemplate = new JdbcTemplate(ds);
        m_OldTemplate.setMaxRows(MAX_IN_MEMORY_SPECTRA);
        this.databaseName = databaseName;

        for (int i = 0; i < TABLES.length; i++) {
            m_NameToCreateStatement.put(TABLES[i], CREATE_STATEMENTS[i]);

        }
        guaranteeDatabase();

    }

    /**
     * true of batch operations are allowed
     *
     * @return
     */
    @Override
    public boolean isBatchSupported() {
           return true;
    }


    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public JdbcTemplate getOldTemplate() {
        return m_OldTemplate;
    }

    @Override
    public SimpleJdbcTemplate getTemplate() {
        return m_Template;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * crate a table if it does not exist
     *
     * @param tableName name of a known table
     */
    @Override
    public void guaranteeTable(String tableName) {
        SimpleJdbcTemplate template = getTemplate();
        try {
            String tableFullName = getDatabaseName() + "." + tableName;
            List<SpringJDBCUtilities.FieldDescription> fields = template.query("describe " + tableFullName, SpringJDBCUtilities.FIELD_MAPPER);

            if (fields.size() > 0)
                return;
        } catch (DataAccessException ignored) {

        }
        guaranteeDatabaseExists();
        String creator = m_NameToCreateStatement.get(tableName);
        if (creator == null)
            throw new IllegalArgumentException("cannot create table " + tableName);
        creator = creator.replace("<database>", getDatabaseName());
        final int update = getOldTemplate().update(creator);
        //   SpringJDBCUtilities.guaranteeTable(template, tableName, creator);
    }


    /**
     * make sure the tables exist
     */
    protected void guaranteeDatabaseExists() {
        SimpleJdbcTemplate template = getTemplate();

        final String dmyDB = getDatabaseName();
        String[] dbMames = SpringJDBCUtilities.queryForStrings(template, "SHOW DATABASES");
        for (int i = 0; i < dbMames.length; i++) {
            if (dmyDB.equalsIgnoreCase(dbMames[i]))
                return;  // already exists
        }
        final int update = template.update("CREATE DATABASE IF NOT EXISTS " + dmyDB);
        if (update == 0)
            //noinspection UnnecessaryReturnStatement
            return; // no change
        else
            //noinspection UnnecessaryReturnStatement
            return;
    }

    /**
     * make sure the tables exist
     */
    protected void guaranteeDatabase() {


        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            guaranteeTable(table);

        }
        guaranteeIndices();
    }


    public static final String FIND_INDEX_STATEMENT = "SELECT COUNT(1) IndexIsThere FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE table_schema=? AND table_name=? AND index_name=?";

    public static final String BUILD_INDEX_STATEMENT = "CREATE INDEX <index_name> on <full_table_name>(<column_to_index>);";


    protected void guaranteeIndices() {
        final String db1DatabaseName = getDatabaseName();
        final SimpleJdbcTemplate template = getTemplate();
        String tableName = "spectrums";
        String columnName = "precursor_charge";
        indexColumnIfNeeded(template, db1DatabaseName, tableName, columnName);
        columnName = "precursor_mz";
        indexColumnIfNeeded(template, db1DatabaseName, tableName, columnName);
        //     columnName = "peptide";
        //     indexColumnIfNeeded(template, db1DatabaseName, tableName, columnName);
    }

    protected void indexColumnIfNeeded(final SimpleJdbcTemplate template, final String pDb1DatabaseName, final String pTableName, final String pColumnName) {
        String indexName = pTableName + "_" + pDb1DatabaseName + "_" + pColumnName;
        final int indexCount = template.queryForInt(FIND_INDEX_STATEMENT, pDb1DatabaseName, pTableName, indexName);
        if (indexCount == 0) {
            String fullTable = pDb1DatabaseName + "." + pTableName;
            String createIndexUpdate = BUILD_INDEX_STATEMENT.replace("<full_table_name>", fullTable).replace("<column_to_index>", pColumnName).replace("<index_name>", indexName);
            int done = template.update(createIndexUpdate);
        }
    }


    /**
     * drop all data
     */
    @Override
    public void clearDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("delete from  " + getDatabaseName() + "." + table);

        }
    }

    /**
     * drop all tables
     */
    @Override
    public void expungeDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("drop table " + getDatabaseName() + "." + table);

        }
    }

    /**
     * subclasses support different syntax on queries - this code attampts to make
     * 'standard' queries compatible
     * @param originalQuery
     * @return  !null patched query
     */
    public String patchQueryString(String originalQuery)
    {
        // standard SQL works here
        return originalQuery;
    }

}
