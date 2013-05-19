package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 *
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectrumQuality {

    /**
     * Get the quality measure of a spectrum
     */
    public double getQualityMeasure();

    /**
     * Set the quality measure of a spectrum
     */
    public void setQualityMeasure(double qualityMeasure);
}
