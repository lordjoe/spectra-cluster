package uk.ac.ebi.pride.spectracluster.psm_similarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

import javax.annotation.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.psm_similarity.IPepideHoldingCluster
 *
 * @author Steve Lewis
 * @date 04/06/2014
 */
public interface IPepideHoldingCluster extends ICluster {

    /**
     *
     * @return
     */
      public @Nonnull List<String> getPeptides();


    /**
     * return the most common peptide
     * @return
     */
      public String getMostCommonPeptide();


     /**
      * get peptides with statistics
      *
      * @return list ordered bu purity
      */
     public @Nonnull  List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator dd);

}
