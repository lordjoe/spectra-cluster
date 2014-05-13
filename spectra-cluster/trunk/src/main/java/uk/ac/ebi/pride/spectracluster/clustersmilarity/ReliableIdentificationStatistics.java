package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ReliableIdentificationStatistics
 * User: Steve
 * Date: 5/1/2014
 */
public class ReliableIdentificationStatistics {

    public static final double reverse_cummulativePurityPoints[] = {1.0, 0.99, 0.95, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0};


    private final int mimumumClusterSize;
    private int total_spectra;
    private int totalClusterTooSmall;
    private int totalNoPeptide;
    private int totalWrongPeptide;
    private final int[] count_by_purity;

    public ReliableIdentificationStatistics(IClusterSet cst, int tminCluster) {
        count_by_purity = new int[reverse_cummulativePurityPoints.length];
        mimumumClusterSize = tminCluster;
        List<ISpectralCluster> clusters = cst.getClusters();
        Map<ISpectrum, HashSet<ISpectralCluster>> idToMap = ClusterPeptidePurity.getSpectraToClusterMap(clusters);
        Map<ISpectralCluster, ClusterPeptidePurity> purities = ClusterPeptidePurity.getPuritiesMap(clusters);
        for (ISpectrum s : idToMap.keySet()) {
            total_spectra++;
            HashSet<ISpectralCluster> spectrunClusters = idToMap.get(s);
            ISpectralCluster usedCluster = getBiggestCluster(spectrunClusters);
            double purity = handleClusterPurity(purities, s, usedCluster);
            addPurity(purity);
        }
    }

    public double handleClusterPurity(final Map<ISpectralCluster, ClusterPeptidePurity> pPurities, final ISpectrum s, final ISpectralCluster pUsedCluster) {
        ClusterPeptidePurity purity = pPurities.get(pUsedCluster);
        if (purity.getNumberSpectra() < mimumumClusterSize) {
            totalClusterTooSmall++;
            return 0;
        }
        if (s instanceof IPeptideSpectrumMatch) {
            String peptide = ((IPeptideSpectrumMatch) s).getPeptide();
            if (peptide == null) {
                totalNoPeptide++;
                return 0;
            }
            if (!peptide.equals(purity.getMostCommonPeptide())) {
                totalWrongPeptide++;
                return 0;
            }
            return purity.getFractionMostCommon();
        }
        throw new IllegalArgumentException("should always be a IPeptideSpectrumMatch");
    }


    protected ISpectralCluster getBiggestCluster(Collection<ISpectralCluster> clusters) {
        ISpectralCluster ret = null;
        for (ISpectralCluster cluster : clusters) {
            if (ret == null) {
                ret = cluster;
                continue;
            }
            if (ret.getClusteredSpectraCount() < cluster.getClusteredSpectraCount())
                ret = cluster;
        }
        return ret;
    }

    protected void addPurity(double purity) {

        for (int i = 0; i < reverse_cummulativePurityPoints.length; i++) {
            double rcp = reverse_cummulativePurityPoints[i];
            if (purity >= rcp) {
                count_by_purity[i]++;
                return;
            }
        }
        throw new IllegalArgumentException("negative purities not allowed");

    }


    public int getTotal_spectra() {
        return total_spectra;
    }

    public int getTotalClusterTooSmall() {
        return totalClusterTooSmall;
    }

    public int getTotalNoPeptide() {
        return totalNoPeptide;
    }

    public int getTotalWrongPeptide() {
        return totalWrongPeptide;
    }

    public double fractionWithPurity(double requiredPurity) {
        double sum = 0;
        for (int i = 0; i < reverse_cummulativePurityPoints.length; i++) {
            double rcp = reverse_cummulativePurityPoints[i];
            if (rcp < requiredPurity)
                return sum / total_spectra;
            sum += count_by_purity[i];
        }
        return sum / total_spectra;   // todo  is this possible
    }
}
