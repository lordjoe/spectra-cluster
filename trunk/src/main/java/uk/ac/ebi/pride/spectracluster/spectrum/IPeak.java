package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * uk.ac.ebi.pride.spectracluster.ISpecClusterPeak
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface IPeak {

    /**
     * Peak m/z
     */
    public double getMz();

    /**
     * Set peak m/z
     */
    public void setMz(double mz);

    /**
     * Peak intensity
     */
    public double getIntensity();

    /**
     * Set peak intensity
     */
    public void setIntensity(double intensity);

    /**
     * number associated spectra
     */
    public int getCount();


}
