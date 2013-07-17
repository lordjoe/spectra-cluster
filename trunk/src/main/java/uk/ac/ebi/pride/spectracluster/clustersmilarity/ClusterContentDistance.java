package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterContentDistance
 * User: Steve
 * Date: 6/17/13
 */
public class ClusterContentDistance implements ClusterDistance {

    public static final ClusterContentDistance INSTANCE = new ClusterContentDistance();

    public static final double MAX_DISTANCE = 1;

    public static final String VERSION = "1.0";

    private ClusterContentDistance() {

    }

    /**
       * return a name which should not change
       *
       * @return !null name
       */
      @Override
      public String getName() {
          return getClass().getSimpleName();
      }

      /**
       * return a version number - this may be updated over time
       *
       * @return !null version
       */
      @Override
      public String getCurrentVersion() {
          return VERSION;
      }


    /**
     * measure the distance between two clusters - a value of 0 says the clusters are the same or equivalent.
     * a cluster will always have a distance of 0 with itself
     * looks at the fraction of spectra which are not in common
     *
     * @param c1 !null cluster
     * @param c2 !null cluster
     * @return distance >= 0
     */
    @Override
    public double distance(final ISpectralCluster c1, final ISpectralCluster c2) {
        final ISpectrum s1 = c1.getHighestQualitySpectrum();
        final ISpectrum s2 = c2.getHighestQualitySpectrum();

        // do not even share highest quality
        if (!s1.equivalent(s2)) {
            return MAX_DISTANCE;
        }

        final List<ISpectrum> sp1 = c1.getClusteredSpectra(); // these are by mz
        final List<ISpectrum> sp2 = c2.getClusteredSpectra(); // these are by mz


        final Set<ISpectrum> allSpectra = new HashSet<ISpectrum>(sp1);
        allSpectra.addAll(sp2);

        final Set<ISpectrum> sp1NotSp2 = new HashSet<ISpectrum>(allSpectra);
        sp1NotSp2.removeAll(sp2);

        final Set<ISpectrum> sp2NotSp1 = new HashSet<ISpectrum>(allSpectra);
        sp2NotSp1.removeAll(sp1);


        double ret = sp1NotSp2.size() + sp2NotSp1.size();
        ret /= allSpectra.size();

        return ret;

    }
}
