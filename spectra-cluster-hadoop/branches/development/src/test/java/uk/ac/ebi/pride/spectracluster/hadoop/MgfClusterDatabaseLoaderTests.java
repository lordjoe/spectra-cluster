package uk.ac.ebi.pride.spectracluster.hadoop;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.datastore.MgfClusterDatabaseLoader;
import uk.ac.ebi.pride.spectracluster.datastore.SQLDataStore;
import uk.ac.ebi.pride.spectracluster.hadoop.hbase.HBaseUtilities;
import uk.ac.ebi.pride.spectracluster.hadoop.hbase.PhoenixWorkingClusterDatabase;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.sql.DataSource;
import java.io.File;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterDatabaseLoaderTests {

    private SQLDataStore sqlDataStore;

    @Before
    public void setUp() throws Exception {
        DataSource source = HBaseUtilities.getHBaseDataSource();
        Defaults.INSTANCE.setDefaultDataSource(source);
        Defaults.INSTANCE.setDatabaseFactory(PhoenixWorkingClusterDatabase.FACTORY);

        sqlDataStore = new SQLDataStore("mgftest", source);
    }


    @Test
    public void testSingleMgf() throws Exception {
        MgfClusterDatabaseLoader mgfClusterDatabaseLoader = new MgfClusterDatabaseLoader(sqlDataStore);
        ClassLoader classLoader = MgfClusterDatabaseLoaderTests.class.getClassLoader();
        mgfClusterDatabaseLoader.load(new File(classLoader.getResource("spectra_400.0_4.0.mgf").toURI()));
    }
}
