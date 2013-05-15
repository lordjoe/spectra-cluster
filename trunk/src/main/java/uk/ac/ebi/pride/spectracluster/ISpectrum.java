package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;

/**
 * uk.ac.ebi.pride.spectracluster.ISpectrum
 *  interface implemented bny a spectrum which may or mat not be a cluster
 * @author Steve Lewis
 * @date 10/05/13
 */
public interface ISpectrum {
    public static ISpectrum[] EMPTY_ARRAY = {};

    /**
     * globally unique id
     * @return   !null id
     */
    public String getId();

    /**
     * usfful label - frequently the same as the id
     * @return   !null title
     */
    public String getTitle();


    /**
     * get MZ
     * @return
     */
    public double getMassChargeRatio();

    /**
     * get charge - mixed charge
     * @return
     */
    public int getCharge();

    /**
     * return peaks sorted by MZ
     * @return !null array of peaks
     */
    public ISpecClusterPeak[] getPeaks();


    /**
     * may be used in sorting spectra
     * @return
     */
    public double getQualityMeasure();


    /**
     * spectra may or may not be clusters -
     * this guarantees that they are
     * @return
     */

     public ISpectralCluster asCluster();

    /**
       * write out the data as an MGF file
       *
       * @param out place to append
       */

      public void appendMGF(Appendable out);


}
