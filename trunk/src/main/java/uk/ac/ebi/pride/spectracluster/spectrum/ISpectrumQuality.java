package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 *
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectrumQuality {

    public static final int BAD_QUALITY_MEASURE = -1;

    /**
     * Get the quality measure of a spectrum
     */
    public double getQualityScore();

    /**
     * Set the quality measure of a spectrum
     */
    public void setQualityScore(double qualityMeasure);
}
