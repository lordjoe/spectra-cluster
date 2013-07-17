package uk.ac.ebi.pride.spectracluster.datastore;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import javax.sql.*;
import java.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.SpringJDBCTests
 * User: Steve
 * Date: 7/15/13
 */
public class SpringJDBCTests {

    /*
       NOTE - these  tests will work only when a MYSQL database is running on local_host and
        has a user pride-spectra password pride   has enough privileges to create and delete databases
     */
    /**
     * this test says you have a test database built and a proper table
     * @throws Exception
     */
    @Test
    public void testDbAccessSetup() throws Exception {
        // run once with the right password - the machine will remember
        //   SpringJDBCUtilities.setHostPassword("localhost", "<secret>");
        DataSource ds = Defaults.INSTANCE.getDefaultDataSource();
        final Connection connection = ds.getConnection();
        Assert.assertNotNull(connection);
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery("describe test.spectrums");
        while (resultSet.next()) {
            final String field = resultSet.getString("field");
            final String type = resultSet.getString("type");
        }
        connection.close();


    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Test
    public void testDbLoad() throws Exception {

        DataSource ds = Defaults.INSTANCE.getDefaultDataSource();
        SQLDataStore db = new SQLDataStore("test", ds);

        db.clearAllData();


        Iterable<ISpectrum> allSpectra = db.getAllSpectra();
        if (allSpectra.iterator().hasNext())
            return; // already loaded

        List  originalSpectra = ClusteringTestUtilities.readISpectraFromResource();

        db.storeSpectra((List<ISpectrum>)originalSpectra);

        allSpectra = db.getAllSpectra();
        List<ISpectrum> holder = new ArrayList<ISpectrum>();

        for (ISpectrum sc : allSpectra) {
            holder.add(sc);
        }
        Collections.sort(holder);
        Collections.sort(originalSpectra);

        Assert.assertEquals(holder.size(), originalSpectra.size());

        for (int i = 0; i < holder.size(); i++) {
            ISpectrum orig = (ISpectrum)originalSpectra.get(i);
            if(orig.getPeaksCount() > SpectrumUtilities.MAXIMUM_ENCODED_PEAKS) {
                orig = new PeptideSpectrumMatch(orig,SpectrumUtilities.filterTop250Peaks(orig.getPeaks()));
            }
            ISpectrum read = holder.get(i);
            Assert.assertTrue(orig.equivalent(read));
        }

    }


    @Test
    public void testDbAccess() throws Exception {
        DataSource ds = Defaults.INSTANCE.getDefaultDataSource();

        SQLDataStore db = new SQLDataStore("test", ds);

        final Iterable<ISpectrum> allSpectra = db.getAllSpectra();
        List  originalSpectra = ClusteringTestUtilities.readISpectraFromResource();


        List<ISpectrum> holder = new ArrayList<ISpectrum>();

        for (ISpectrum sc : allSpectra) {
            holder.add(sc);
        }

        Collections.sort(holder);
        Collections.sort(originalSpectra);

        Assert.assertEquals(holder.size(), originalSpectra.size());

        for (int i = 0; i < holder.size(); i++) {
            ISpectrum orig = (ISpectrum)originalSpectra.get(i);
            if(orig.getPeaksCount() > SpectrumUtilities.MAXIMUM_ENCODED_PEAKS) {
                orig = new PeptideSpectrumMatch(orig,SpectrumUtilities.filterTop250Peaks(orig.getPeaks()));
            }
            ISpectrum read = holder.get(i);
            final boolean equivalent = orig.equivalent(read);
            Assert.assertTrue(equivalent);
        }


    }
}
