package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ConsensusSpectrumTests {

    private Set<String> spectrumIds = new HashSet<String>(Arrays.asList("83931","1258781","3722"));
    private List<ISpectrum> filteredOriginalSpectra = new ArrayList<ISpectrum>();
    private ConsensusSpectrumBuilder consensusSpectrumBuilder;

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        URL url = ConsensusSpectrumTests.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.mgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        ISpectrum[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);

        consensusSpectrumBuilder = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();

        for (ISpectrum originalSpectrum : mgfSpectra) {
            if (spectrumIds.contains(originalSpectrum.getId())) {
                filteredOriginalSpectra.add(originalSpectrum);
            }
        }
    }

    @Test
    public void testConsensusSpectrum() throws Exception {
        ISpectrum consensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(filteredOriginalSpectra);
        System.out.println(consensusSpectrum.toString());
    }
}
