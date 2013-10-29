package uk.ac.ebi.pride.spectracluster.datastore;

import javax.sql.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.WorkingDatabaseFactory
 *
 * @author Steve Lewis
 * @date 29/10/13
 */
public interface WorkingDatabaseFactory {

    /**
     * return a workling database - needed so we can subclass to handle Phoenix
     * @param databaseName  - name of the database - tables will have <databasename> . prepended
     * @param ds  !null data source - this provides the connection
     * @return   !null   IWorkingClusterDatabase
     */
    public IWorkingClusterDatabase buildWorkingDatabase(String databaseName, final DataSource ds);
}
