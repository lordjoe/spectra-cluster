package uk.ac.ebi.pride.spectracluster;

import com.lordjoe.algorithms.*;

/**
 * uk.ac.ebi.pride.spectracluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @date 10/05/13
 */
public interface ISpectralCluster extends ISpectrum,Equivalent<ISpectralCluster> {
    public static ISpectralCluster[] EMPTY_ARRAY = {};

      /**
     * all internally clusters
     * @return
     */
    public ISpectrum[] getClusteredSpectra();

    /**
     * count of internal clusters
     * @return
     */
    public int getClusteredSpectraCount();


    /**
     *
     * @param merged
     */
    public  void mergeClusters(ISpectrum... merged);


    /**
     *   take
     * @param removed
     */
    public  void  removeClusters(ISpectrum... removed);


    /**
     * append to make a CGF - cluster file or mgf file
     * @param out !null open appender
     */
    public void append(Appendable out);

    /**
       * write out the data as an MGF file
       *
       * @param out place to append
       */
      public void appendMGF(Appendable out);

    /**
      * do not add begin and end cluster - useful for rebuilding a mgf
      * @param out
      */
     public void appendSpectra(Appendable out);

}
