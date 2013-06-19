package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ConsensusSpectrumTests {

    private List<String> spectrumIds = new ArrayList<String>(Arrays.asList("83931", "1258781", "3722"));
    private List<ISpectrum> filteredOriginalSpectra = new ArrayList<ISpectrum>();
    private ConsensusSpectrumBuilder consensusSpectrumBuilder;

    @Before
    public void setUp() throws Exception {
        List<ISpectrum> mgfSpectra = ClusteringTestUtilities.readISpectraFromResource();

        consensusSpectrumBuilder = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();

        for (ISpectrum originalSpectrum : mgfSpectra) {
            if (spectrumIds.contains(originalSpectrum.getId())) {
                filteredOriginalSpectra.add(originalSpectrum);
            }
        }
    }

    @Test
    public void testConsensusSpectrum() throws Exception {
        ISpectrum consensusSpectrum1 = consensusSpectrumBuilder.buildConsensusSpectrum(filteredOriginalSpectra);

        Collections.shuffle(filteredOriginalSpectra);
        ISpectrum consensusSpectrum2 = consensusSpectrumBuilder.buildConsensusSpectrum(filteredOriginalSpectra);

        Collections.shuffle(filteredOriginalSpectra);
        ISpectrum consensusSpectrum3 = consensusSpectrumBuilder.buildConsensusSpectrum(filteredOriginalSpectra);

        Assert.assertTrue(consensusSpectrum1.equivalent(consensusSpectrum2));
        Assert.assertTrue(consensusSpectrum2.equivalent(consensusSpectrum3));
    }
}
