package uk.ac.ebi.pride.spectracluster.hadoop;

import uk.ac.ebi.pride.spectracluster.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectraHadoopUtilities
 * User: Steve
 * Date: 8/13/13
 * static general purpose routines for handling hadoopy things
 */
public class SpectraHadoopUtilities {


    /**
     * convert am int into an mz for east comparison
     *
     * @param mz input
     * @return MZ_RESOLUTION * mz as int
     */
    public static String mzToKey(double mz) {
        int peak = ClusterUtilities.mzToInt(mz);
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
     public static int keyToMZ(String key) {
         //noinspection UnnecessaryLocalVariable
          int ret = Integer.parseInt(key); // (double)MZ_RESOLUTION;
          return ret;
     }

}
