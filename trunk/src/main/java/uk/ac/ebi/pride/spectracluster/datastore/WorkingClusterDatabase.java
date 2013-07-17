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
public class WorkingClusterDatabase implements ITemplateHolder {

    public static final int MAX_PEAKS_PER_SPECTRUM = 500;
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

    public WorkingClusterDatabase(String databaseName, final DataSource ds) {
        dataSource = ds;
        m_Template = new SimpleJdbcTemplate(ds);
        m_OldTemplate = new  JdbcTemplate(ds);
        this.databaseName = databaseName;

        for (int i = 0; i < TABLES.length; i++) {
            m_NameToCreateStatement.put(TABLES[i], CREATE_STATEMENTS[i]);

        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JdbcTemplate getOldTemplate() {
        return m_OldTemplate;
    }

    public SimpleJdbcTemplate getTemplate() {
        return m_Template;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * crate a table if it does not exist
     *
     * @param tableName name of a known table
     */
    public void guaranteeTable(String tableName) {
        SimpleJdbcTemplate template = getTemplate();
        try {
            List<SpringJDBCUtilities.FieldDescription> fields = template.query("describe " + getDatabaseName() + "." + tableName, SpringJDBCUtilities.FIELD_MAPPER);
            if (fields.size() > 0)
                return;
        }
        catch (DataAccessException ignored) {

        }
        String creator = m_NameToCreateStatement.get(tableName);
        if (creator == null)
            throw new IllegalArgumentException("cannot create table " + tableName);
        creator =  creator.replace("<database>", getDatabaseName());
        final int update = getOldTemplate().update(creator);
     //   SpringJDBCUtilities.guaranteeTable(template, tableName, creator);
    }


    /**
     * drop all data
     */
    public void guaranteeDatabase( ) {

        SimpleJdbcTemplate template = getTemplate();
        //    template.update("DROP SCHEMA IF EXISTS " + SCHEMA_NAME);
        //     template.update("CREATE SCHEMA  " + SCHEMA_NAME);

        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            guaranteeTable(table);

        }
    }


    /**
     * drop all data
     */
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
    public void expungeDatabase() {
        SimpleJdbcTemplate template = getTemplate();
        for (int i = 0; i < TABLES.length; i++) {
            String table = TABLES[i];
            template.update("drop table " + table);

        }
    }

}
