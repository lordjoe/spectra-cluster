package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.*;

import javax.sql.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.IWorkingClusterDatabase
 *
 * @author Steve Lewis
 * @date 29/10/13
 */
public interface IWorkingClusterDatabase extends ITemplateHolder {


    public DataSource getDataSource();

    public JdbcTemplate getOldTemplate();

    public SimpleJdbcTemplate getTemplate();

    public String getDatabaseName();

    /**
     * crate a table if it does not exist
     *
     * @param tableName name of a known table
     */
    public void guaranteeTable(String tableName);

    /**
     * drop all data
     */
    public void clearDatabase();

    /**
     * drop all tables
     */
    public void expungeDatabase();

    /**
     * true of batch operations are allowed
     * @return
     */
    public boolean isBatchSupported();

    /**
        * subclasses support different syntax on queries - this code attampts to make
        * 'standard' queries compatible
        * @param originalQuery
        * @return  !null patched query
        */
       public String patchQueryString(String originalQuery);





}
