package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.normalizer.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.NormailzedPeptideSpectrumMatch
 * User: Steve
 * Date: 6/20/13
 */
public class NormailzedPeptideSpectrumMatch extends PeptideSpectrumMatch implements INormalizedSpectrum {


    protected static List<IPeak> buildNormalizedPeaks(List<IPeak> peaks,double totalIntensity) {
        return new TotalIntensityNormalizer(totalIntensity).normalizePeaks(peaks);
       }

    private final double requiredTotalIntensity;
    /**
     * simple copy constructor
     *
     * @param spectrum
     */
    public NormailzedPeptideSpectrumMatch(final PeptideSpectrumMatch spectrum,double requiredTtalIntensity) {
        super(spectrum.getId(),
                spectrum.getPeptide(),
                spectrum.getPrecursorCharge(),
                spectrum.getPrecursorMz(),
                buildNormalizedPeaks(spectrum.getPeaks(),requiredTtalIntensity));
        requiredTotalIntensity = requiredTtalIntensity;
    }
    /**
      * simple copy constructor
      *
      * @param spectrum
      */
     public NormailzedPeptideSpectrumMatch(final ISpectrum spectrum,double requiredTtalIntensity) {
         super(spectrum.getId(),
                 null,  // no peptide
                 spectrum.getPrecursorCharge(),
                 spectrum.getPrecursorMz(),
                 buildNormalizedPeaks(spectrum.getPeaks(),requiredTtalIntensity));
         requiredTotalIntensity = requiredTtalIntensity;
     }

    /**
     * return a spectrum normalized to the specific total intensity
     *
     * @return !null spectrum - might be this
     */
    @Override
    public INormalizedSpectrum asNormalizedTo(final double totalIntensity) {
        if(Math.abs(getRequiredTotalIntensity() - totalIntensity) <  IPeak.SMALL_MZ_DIFFERENCE)
              return this;
        return super.asNormalizedTo(totalIntensity);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * normalized spectral are normalized to a specific total intensity'this is that value
     *
     * @return as above
     */
    @Override
    public double getRequiredTotalIntensity() {
        return requiredTotalIntensity;
    }


}
