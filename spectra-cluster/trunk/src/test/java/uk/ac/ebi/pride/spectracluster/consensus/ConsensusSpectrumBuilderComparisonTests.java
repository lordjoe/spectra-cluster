package uk.ac.ebi.pride.spectracluster.consensus;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.AllPeaksDotProduct;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.List;

/**
 * Unit tests on ClusteringEngine to compare the clusters produced by AllPeaksDotProduct and
 * FrankAtElDotProduct.
 *
 *
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ConsensusSpectrumBuilderComparisonTests {
    public static final String SMALL_CLUSTER_FILE = "uk/ac/ebi/pride/spectracluster/engine/cluster_spec_39546.mgf";

    @Test
    public void testConsensusSpectrumSimilarity() throws Exception {

        List<ISpectrum> spectra= ClusteringTestUtilities.readISpectraFromResource(SMALL_CLUSTER_FILE);

        final IConsensusSpectrumBuilder consensusSpectrumBuilder = Defaults.getDefaultConsensusSpectrumBuilder();
        consensusSpectrumBuilder.addSpectra(spectra.toArray(new ISpectrum[spectra.size()]));

        final ISpectrum consensusSpectrum1 = consensusSpectrumBuilder.getConsensusSpectrum();

        final IConsensusSpectrumBuilder filteredConsensusSpectrumBuilder = FilteredConsensusSpectrum.FACTORY.getConsensusSpectrumBuilder();
        filteredConsensusSpectrumBuilder.addSpectra(spectra.toArray(new ISpectrum[spectra.size()]));

        final ISpectrum consensusSpectrum2 = filteredConsensusSpectrumBuilder.getConsensusSpectrum();


        final AllPeaksDotProduct allPeaksDotProduct = new AllPeaksDotProduct(Defaults.getSimilarityMZRange());

        final double similarity = allPeaksDotProduct.assessSimilarity(consensusSpectrum1, consensusSpectrum2);

        Assert.assertTrue("Consensus spectrum must be similar", similarity > 0.85);

    }
}
