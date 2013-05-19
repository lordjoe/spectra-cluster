package uk.ac.ebi.pride.spectracluster.spectrum;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;

/**
 * uk.ac.ebi.pride.spectracluster.IScoredSpectrum
 *
 * @author Steve Lewis
 * @date 15/05/13
 */
public interface IPeptideSpectrumMatch extends ISpectrum, ISpectrumQuality,Equivalent<ISpectrum> {
    /**
     * return scored peptide - maybe null
     */
    public String getPeptide();

    /**
      * make a cluster contaiming a single spectrum - this
      * @return
      */
     public ISpectralCluster asCluster();
}
