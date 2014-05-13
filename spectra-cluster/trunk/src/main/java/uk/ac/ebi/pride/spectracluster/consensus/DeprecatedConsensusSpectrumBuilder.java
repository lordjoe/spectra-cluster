package uk.ac.ebi.pride.spectracluster.consensus;


import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.IAlgorithm;

/**
 * Creates a consensus spectrum from
 * the passed list of spectra.
 *
 * @author jg
 * @author Rui Wang
 */
@Deprecated
public interface DeprecatedConsensusSpectrumBuilder extends IAlgorithm {
    /**
     * Creates a consensus spectrum based on the
     * passed list of spectra.
     *
     * @param spectra A list of spectra as sorted peak lists according to intensity.
     * @return A list of Peaks sorted according to their intensities.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ISpectrum buildConsensusSpectrum(ISpectralCluster spectra, Object... otherData);

//    /**
//   	 * Creates a consensus spectrum based on the
//   	 * passed list of spectra.
//   	 * @param spectra A list of spectra as sorted peak lists according to intensity.
//   	 * @return A list of Peaks sorted according to their intensities.
//   	 */
//   	public ISpectrum buildConsensusSpectrum(List<ISpectrum> spectra);
//
//
//    /**
// 	 * Creates a consensus spectrum based on the
// 	 * passed list of spectra.
// 	 * @param spectra A list of spectra as sorted peak lists according to intensity.
// 	 * @return A list of Peaks sorted according to their intensities.
// 	 */
//    public ISpectrum buildConsensusSpectrum(ISpectrum... spectra);

}
