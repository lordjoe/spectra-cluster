package uk.ac.ebi.pride.spectracluster.datastore;

import javax.sql.DataSource;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class DataSourceDefaults {

    public static final DataSourceDefaults INSTANCE = new DataSourceDefaults();

    private DataSource defaultDataSource;
    private WorkingDatabaseFactory databaseFactory = WorkingClusterDatabase.FACTORY;

    private DataSourceDefaults() {
    }

    public synchronized DataSource getDefaultDataSource() {
        if (defaultDataSource == null) {
            defaultDataSource = SpringJDBCUtilities.buildDefaultDataSource();
        }
        return defaultDataSource;
    }

    public void setDefaultDataSource(final DataSource pDefaultDataSource) {
        defaultDataSource = pDefaultDataSource;
    }


    public WorkingDatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public void setDatabaseFactory(WorkingDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }
}
