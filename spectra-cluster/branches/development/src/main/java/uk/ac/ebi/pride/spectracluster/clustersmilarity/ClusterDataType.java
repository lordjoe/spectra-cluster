package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;

import javax.annotation.*;

/**
* uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDataType
* User: Steve
* Date: 2/5/14
*/
public enum ClusterDataType {
     Target,Decoy,All;

    public static TypedPredicate<ClusterPeptideFraction> USING_DECOYS = new TypedPredicate<ClusterPeptideFraction>() {
        /**
         * @param pClusterPeptideFraction
         * @param otherdata               - implementation specific and usually blank
         * @return what the implementation does
         */
        @Override
        public boolean apply(@Nonnull final ClusterPeptideFraction pClusterPeptideFraction, final Object... otherdata) {
            return pClusterPeptideFraction.isDecoy();
        }
    };
    public static  TypedPredicate<ClusterPeptideFraction>   USING_TARGETS = new TypedPredicate<ClusterPeptideFraction>() {
          /**
           * @param pClusterPeptideFraction
           * @param otherdata               - implementation specific and usually blank
           * @return what the implementation does
           */
          @Override
          public boolean apply(@Nonnull final ClusterPeptideFraction pClusterPeptideFraction, final Object... otherdata) {
              return !pClusterPeptideFraction.isDecoy();
          }
      };
    public static  TypedPredicate<ClusterPeptideFraction>   USING_ALL = new TypedPredicate<ClusterPeptideFraction>() {
            /**
             * @param pClusterPeptideFraction
             * @param otherdata               - implementation specific and usually blank
             * @return what the implementation does
             */
            @Override
            public boolean apply(@Nonnull final ClusterPeptideFraction pClusterPeptideFraction, final Object... otherdata) {
                return true;
            }
        };

}
