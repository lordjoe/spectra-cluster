package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import com.lordjoe.algorithms.CountedString;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterPeptidePurity
 * User: Steve
 * Date: 1/9/14
 */
public class ClusterPeptidePurity implements Comparable<ClusterPeptidePurity> {

    public static List<ClusterPeptidePurity> getPurities(IClusterSet clusters) {
        List<IPeptideSpectrumCluster> clusters1 = clusters.getClusters();
        return getPurities(clusters1);
    }

    public static Map<ISpectrum, HashSet<IPeptideSpectrumCluster>> getSpectraToClusterMap(final List<IPeptideSpectrumCluster> pClusters1) {
        Map<ISpectrum, HashSet<IPeptideSpectrumCluster>> ret = new HashMap<ISpectrum, HashSet<IPeptideSpectrumCluster>>();
        for (IPeptideSpectrumCluster spc : pClusters1) {
            for (ISpectrum spec : spc.getClusteredSpectra()) {
                HashSet<IPeptideSpectrumCluster> set = ret.get(spec);
                if (set == null) {
                    set = new HashSet<IPeptideSpectrumCluster>();
                    ret.put(spec, set);
                }
                set.add(spc);
            }
        }
        return ret;
    }

    public static Map<IPeptideSpectrumCluster, ClusterPeptidePurity> getPuritiesMap(final List<IPeptideSpectrumCluster> pClusters1) {
        Map<IPeptideSpectrumCluster, ClusterPeptidePurity> ret = new HashMap<IPeptideSpectrumCluster, ClusterPeptidePurity>();
        for (IPeptideSpectrumCluster spc : pClusters1) {
            ClusterPeptidePurity purity = new ClusterPeptidePurity(spc);
            ret.put(spc, purity);
        }
        return ret;
    }

    public static List<ClusterPeptidePurity> getPurities(final List<IPeptideSpectrumCluster> pClusters1) {
        List<ClusterPeptidePurity> ret = new ArrayList<ClusterPeptidePurity>();
        for (IPeptideSpectrumCluster spc : pClusters1) {
            ClusterPeptidePurity purity = new ClusterPeptidePurity(spc);
            ret.add(purity);
        }
        Collections.sort(ret);
        return ret;
    }

    private final IPeptideSpectrumCluster cluster;
    private String mostCommonPeptide;
    private double fractionMostCommon;
    private double fractionNextMostCommon;
    private int numberPeptides;
    private int numberSpectra;

    public ClusterPeptidePurity(final IPeptideSpectrumCluster pCluster) {
        cluster = pCluster;
        generateStatistics();
    }

    private void generateStatistics() {
        List<String> peptides = new ArrayList<String>();
        IPeptideSpectrumCluster cluster1 = getCluster();
        List<ISpectrum> clusteredSpectra = cluster1.getClusteredSpectra();
        for (ISpectrum spc : clusteredSpectra) {
            if (spc instanceof IPeptideSpectrumMatch) {
                String peptide = ((IPeptideSpectrumMatch) spc).getPeptide();
                peptides.add(peptide);
            }
        }
        numberSpectra = peptides.size();
        CountedString[] sortedPeptides = CountedString.getCountedStrings(peptides);
        numberPeptides = sortedPeptides.length;
        if (numberPeptides == 0)
            return;
        fractionMostCommon = sortedPeptides[0].getCount() / (double) peptides.size();
        mostCommonPeptide = sortedPeptides[0].getValue();
        if (numberPeptides == 1)
            return;
        fractionNextMostCommon = sortedPeptides[1].getCount() / (double) peptides.size();
    }

    public IPeptideSpectrumCluster getCluster() {
        return cluster;
    }

    public double getFractionMostCommon() {
        return fractionMostCommon;
    }

    @SuppressWarnings("UnusedDeclaration")
    public double getFractionNextMostCommon() {
        return fractionNextMostCommon;
    }

    public int getNumberPeptides() {
        return numberPeptides;
    }

    public int getNumberSpectra() {
        return numberSpectra;
    }

    public String getMostCommonPeptide() {
        return mostCommonPeptide;
    }

    @Override
    public int compareTo(final ClusterPeptidePurity o) {
        double fractionMostCommon1 = getFractionMostCommon();
        double fractionMostCommon2 = o.getFractionMostCommon();
        if (fractionMostCommon1 != fractionMostCommon2) {
            return CompareTo.compare(fractionMostCommon2, fractionMostCommon1);
        }
        return getMostCommonPeptide().compareTo(o.getMostCommonPeptide());
    }

    @Override
    public String toString() {
        return String.format("%4.2f", getFractionMostCommon()).trim() + ":" + getNumberSpectra();

    }
}
