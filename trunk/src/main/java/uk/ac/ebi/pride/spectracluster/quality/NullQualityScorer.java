package uk.ac.ebi.pride.spectracluster.quality;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

/**
 *
 * @author Rui Wang
 * @version $Id$
 */
public class NullQualityScorer implements QualityScorer {
    @Override
    public double calculateQualityScore(ISpectrum spectrum) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
