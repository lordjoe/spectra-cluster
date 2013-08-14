package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * interface implemented by ISpectrum and ISpectral cluster returns as ints
 * the highest peaks of the cluster
 *
 * User: Steve
 * Date: 7/5/13
 */
public interface IMajorPeaksHolder {

    /**
     * return as a spectrum the highest  MAJOR_PEAK_NUMBER
     * this follows Frank et all's suggestion that all spectra in a cluster will share at least one of these
     * @return
     */
     public int[] asMajorPeakMZs();

}
