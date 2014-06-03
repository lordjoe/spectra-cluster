package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import com.lordjoe.algorithms.CountedString;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterPeptidePurity
 * User: Steve
 * Date: 1/9/14
 */
public class ClusterPeptidePurity implements Comparable<ClusterPeptidePurity> {

    public static List<ClusterPeptidePurity> getPurities(IClusterSet clusters) {
        List<ICluster> clusters1 = clusters.getClusters();
        return getPurities(clusters1);
    }

    public static Map<ISpectrum, HashSet<ICluster>> getSpectraToClusterMap(final List<ICluster> pClusters1) {
        Map<ISpectrum, HashSet<ICluster>> ret = new HashMap<ISpectrum, HashSet<ICluster>>();
        for (ICluster spc : pClusters1) {
            for (ISpectrum spec : spc.getClusteredSpectra()) {
                HashSet<ICluster> set = ret.get(spec);
                if (set == null) {
                    set = new HashSet<ICluster>();
                    ret.put(spec, set);
                }
                set.add(spc);
            }
        }
        return ret;
    }

    public static Map<ICluster, ClusterPeptidePurity> getPuritiesMap(final List<ICluster> pClusters1) {
        Map<ICluster, ClusterPeptidePurity> ret = new HashMap<ICluster, ClusterPeptidePurity>();
        for (ICluster spc : pClusters1) {
            ClusterPeptidePurity purity = new ClusterPeptidePurity(spc);
            ret.put(spc, purity);
        }
        return ret;
    }

    public static List<ClusterPeptidePurity> getPurities(final List<ICluster> pClusters1) {
        List<ClusterPeptidePurity> ret = new ArrayList<ClusterPeptidePurity>();
        for (ICluster spc : pClusters1) {
            ClusterPeptidePurity purity = new ClusterPeptidePurity(spc);
            ret.add(purity);
        }
        Collections.sort(ret);
        return ret;
    }

    private final ICluster cluster;
    private String mostCommonPeptide;
    private double fractionMostCommon;
    private double fractionNextMostCommon;
    private int numberPeptides;
    private int numberSpectra;

    public ClusterPeptidePurity(final ICluster pCluster) {
        cluster = pCluster;
        generateStatistics();
    }

    private void generateStatistics() {
        List<String> peptides = new ArrayList<String>();
        ICluster cluster1 = getCluster();
        List<ISpectrum> clusteredSpectra = cluster1.getClusteredSpectra();
        for (ISpectrum spc : clusteredSpectra) {
                 String peptide =  spc.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY);
                peptides.add(peptide);
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

    public ICluster getCluster() {
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
