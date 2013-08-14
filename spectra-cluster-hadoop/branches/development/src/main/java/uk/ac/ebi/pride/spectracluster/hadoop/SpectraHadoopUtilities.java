package uk.ac.ebi.pride.spectracluster.hadoop;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraHadoopUtilities
 * User: Steve
 * Date: 8/13/13
 * static general purpose routines for handling hadoopy things
 */
public class SpectraHadoopUtilities {

    public static final int MZ_RESOLUTION = 100; // we care about differences of 0.01 dalton

    /**
     * convert am int into an mz for east comparison
     *
     * @param mz input
     * @return MZ_RESOLUTION * mz as int
     */
    public static int mzToInt(double mz) {
        return (int) (MZ_RESOLUTION * mz);
    }

    /**
     * convert am int into an mz for east comparison
     *
     * @param mz input
     * @return MZ_RESOLUTION * mz as int
     */
    public static String mzToKey(double mz) {
        int peak = SpectraHadoopUtilities.mzToInt(mz);
        //noinspection UnnecessaryLocalVariable
        String str = String.format("%010d", peak);
        return str;
    }
    /**
      * convert am int into an mz for east comparison
      *
      * @param key input
      * @return MZ_RESOLUTION * mz as int
      */
     public static double keyToMZ(String key) {
         //noinspection UnnecessaryLocalVariable
          double ret = Integer.parseInt(key) / (double)MZ_RESOLUTION;
          return ret;
     }

}
