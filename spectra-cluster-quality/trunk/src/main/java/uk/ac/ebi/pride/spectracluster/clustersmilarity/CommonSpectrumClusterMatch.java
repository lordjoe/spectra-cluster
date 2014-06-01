package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import java.util.HashSet;
import java.util.Set;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.CommonSpectrumClusterMatch
 * Comparison is based on the number of spectra in common
 *
 * @author Steve Lewis
 * @date 05/11/13
 */
public class CommonSpectrumClusterMatch extends AbstractClusterMatch {

    private final Set<ICluster> allSpectra = new HashSet<ICluster>();
    private final Set<ICluster> commonSpectra = new HashSet<ICluster>();

    public CommonSpectrumClusterMatch(ICluster source, ICluster target) {
        super(source, target);

    }

    public double fractionInCommon() {
        int total = allSpectra.size();
        if (total == 0)
            return 0;
        double common = commonSpectra.size();
        return common / total;
    }

    protected void extractComparisonData() {

    }

    /**
     * return some measure of the quality - base comparator use3s thia to
     * sort
     *
     * @param addedData - usually ignored but allows some implementations to bess other data
     * @return
     */
    @Override
    public double getDistance() {
        return fractionInCommon();
    }

}
