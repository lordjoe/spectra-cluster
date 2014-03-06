package uk.ac.ebi.pride.spectracluster.normalizer;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.util.IAlgorithm;

import java.util.List;

/**
 * Normalizes a spectrum's intensities.
 *
 * @author Rui Wang
 */
public interface IntensityNormalizer  extends IAlgorithm {

    /**
     * return the value normalized to - especially useful for total intensity normalization where
     * we may not weed to normalize
     * @return  as above
     *
     * TODO: @steve do we really need to this method?
     */
    public double getNormalizedValue();



    /**
     * normalize a list of peaks
     *
     * @param peaks
     * @return
     */
    public List<IPeak> normalize(List<IPeak> peaks);
}
