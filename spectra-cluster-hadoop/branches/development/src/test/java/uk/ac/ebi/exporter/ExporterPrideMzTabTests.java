package uk.ac.ebi.exporter;

import com.lordjoe.filters.TypedFilterCollection;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.pride.spectracluster.export.Exporter;
import uk.ac.ebi.pride.spectracluster.filters.SpectrumFilters;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ExporterPrideMzTabTests {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File projectFolder;
    private File inputFileFolder;
    private File outputFileFolder;

    @Before
    public void setUp() throws Exception {
        // create test folders
        projectFolder = temporaryFolder.newFolder("PXD000211");
        inputFileFolder = temporaryFolder.newFolder("PXD000211/generated");

        // output folder
        outputFileFolder = temporaryFolder.newFolder("output");

        // copy input files into the input folder
        ClassLoader classLoader = ExporterPrideMzTabTests.class.getClassLoader();
        FileUtils.copyFile(new File(classLoader.getResource("mztab/PRIDE_Exp_Complete_Ac_12575.mztab").toURI()),
                new File(inputFileFolder.getCanonicalPath() + File.separator + "PRIDE_Exp_Complete_Ac_12575.mztab"));
        FileUtils.copyFile(new File(classLoader.getResource("mztab/PRIDE_Exp_Complete_Ac_12575.pride.mgf").toURI()),
                new File(inputFileFolder.getCanonicalPath() + File.separator + "PRIDE_Exp_Complete_Ac_12575.pride.mgf"));
    }

    @Test
    public void testIdentifiedSpectraExportedToMGF() throws Exception {
        // Filter string
        String filterXML = "<Filters>" +
                           "<SpectrumFilter minimumLength=\"16\"/>" +
                           "</Filters>";

        // export
        TypedFilterCollection.registerHandler(SpectrumFilters.TAG, new SpectrumFilters.SpectrumFilterSaxHandler(null));
        TypedFilterCollection filters = TypedFilterCollection.parse(filterXML);
        Exporter exp = new Exporter(outputFileFolder,projectFolder,filters);
        exp.exportDirectory();

        // read output MGF mgfFile
        final File mgfFile = new File(outputFileFolder.getCanonicalPath() + File.separator + "PXD000211.mgf");
        IPeptideSpectrumMatch[] psm = ParserUtilities.readMGFScans(mgfFile);
        Assert.assertSame("Number of identified spectra should match", 4, psm.length);
    }
}
