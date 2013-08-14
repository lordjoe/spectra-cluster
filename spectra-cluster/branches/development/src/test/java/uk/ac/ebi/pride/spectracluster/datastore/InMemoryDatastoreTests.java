package uk.ac.ebi.pride.spectracluster.datastore;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.DatastoreTests
 * User: Steve
 * Date: 7/15/13
 */
public class InMemoryDatastoreTests {

    private IMutableSpectrumDataStore ds;

    @Before
    public void setUp() throws Exception {

        ds = new InMemoryDatastore();
    }

    @Test
    public void testSpectrumStore() throws Exception {
        final List<IPeptideSpectrumMatch> spectrums = loadDataStrore();

        // make sure we have them
        for (IPeptideSpectrumMatch spectrum : spectrums) {
            ISpectrum test = ds.getSpectrumById(spectrum.getId());
            Assert.assertTrue(test.equivalent(spectrum));
        }
    }

    @Test
    public void testSpectrumRemove() throws Exception {
        final List<IPeptideSpectrumMatch> spectrums = loadDataStrore();

        List<ISpectrum> holder = new ArrayList<ISpectrum>();


        // make sure we have them
        for (ISpectrum spectrum : ds.getAllSpectra()) {
            holder.add(spectrum);
        }
        Assert.assertEquals(holder.size(), spectrums.size());
        for (ISpectrum spectrum : holder) {
            ds.removeSpectrum(spectrum);
        }
        holder.clear();
         // should be none left
        for (ISpectrum spectrum : ds.getAllSpectra()) {
            holder.add(spectrum);
        }
        Assert.assertEquals(0,holder.size() );

    }

    @Test
    public void testClear() throws Exception {
        final List<IPeptideSpectrumMatch> spectrums = loadDataStrore();

        List<ISpectrum> holder = new ArrayList<ISpectrum>();


        // make sure we have them
        for (ISpectrum spectrum : ds.getAllSpectra()) {
            holder.add(spectrum);
        }
        Assert.assertEquals(holder.size(), spectrums.size());

        ds.clearAllData();
        holder.clear();
         // should be none left
        for (ISpectrum spectrum : ds.getAllSpectra()) {
            holder.add(spectrum);
        }
        Assert.assertEquals(0,holder.size() );

    }

    protected List<IPeptideSpectrumMatch> loadDataStrore() {
        final List<IPeptideSpectrumMatch> spectrums = ClusteringTestUtilities.readISpectraFromResource();
        // add then all
        for (IPeptideSpectrumMatch spectrum : spectrums) {
            ds.addSpectrum(spectrum);
        }
        return spectrums;
    }
}
