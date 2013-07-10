package uk.ac.ebi.pride.spectracluster.consensus;


import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * Creates a consensus spectrum from
 * the passed list of spectra.
 *
 * @author jg
 * @author Rui Wang
 *
 */
public interface ConsensusSpectrumBuilder {
    /**
   	 * Creates a consensus spectrum based on the
   	 * passed list of spectra.
   	 * @param spectra A list of spectra as sorted peak lists according to intensity.
   	 * @return A list of Peaks sorted according to their intensities.
   	 */
   	public ISpectrum buildConsensusSpectrum(ISpectralCluster spectra,Object... otherData);

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
