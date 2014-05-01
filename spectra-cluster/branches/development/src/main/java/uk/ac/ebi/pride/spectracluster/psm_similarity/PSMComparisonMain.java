package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.chart.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterComparisonMain
 * User: Steve
 * Date: 1/17/14
 */
public class PSMComparisonMain implements IDecoyDiscriminator {

    private final Properties properties = new Properties();
    private final Set<String> decoys = new HashSet<String>();
    private final Set<String> used_decoys = new HashSet<String>();
    private final List<IClusterSet> clusterings = new ArrayList<IClusterSet>();
      //   private final SimpleSpectrumRetriever spectra = new SimpleSpectrumRetriever();

    private final PSM_Holder psms = new PSM_Holder();

    private PSMComparisonMain() {
    }

    public void handlePropertiesFile(final String propertyFileName) {
        try {
            properties.load(new FileReader(propertyFileName));
            handleProperties();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public int getDecoyCount() {
        return getPsms().getDecoyCount();
    }


    public int getPSMCount() {
        return getPsms().getPSMSpectrumCount();
    }


    protected void handleProperties() {

        String decoyFileName = getProperty("Decoys");
        File decoyFile = new File(decoyFileName);
        ClusterSimilarityUtilities.readPSMDecoySpectra(decoyFile, psms);
        int totalPSMs = psms.getPSMSpectrumCount();
        int totalDecoys = psms.getDecoyCount();


        String tsvFileName = getProperty("SpectraFile");
        File tsvFile = new File(tsvFileName);
        ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, psms);

        String analysisProperties = getProperty("AnalysisProperties");
        File aps = new File(analysisProperties);
        Properties devProps = new Properties();
        try {
            devProps.load(new FileReader(aps) );
            properties.putAll(devProps);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
         }

    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }


    @Override
    public boolean isDecoy(String peptideSequence) {
        boolean contains = decoys.contains(peptideSequence);
        if (contains) {
            used_decoys.add(peptideSequence);
        }
        return contains;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Set<String> getDecoys() {
        //noinspection UnnecessaryLocalVariable
        HashSet<String> ret = new HashSet<String>(decoys);
        return ret;
    }

    public void clearDecoyUse() {
        used_decoys.clear();
    }

    public Set<String> getUnusedDecoys() {
        HashSet<String> ret = new HashSet<String>(decoys);
        ret.removeAll(used_decoys);
        return ret;
    }

    public Set<String> getUsedDecoys() {
        return new HashSet<String>(used_decoys);
    }


    public static List<ClusterPeptideFraction> getCumulativeData(IClusterSet cs, int minimumSize) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateClusterPeptideFractionVisitor(decoys, minimumSize);
        //noinspection unchecked
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }


    public static List<ClusterPeptideFraction> getCumulativeDecoyData(IClusterSet cs, IDecoyDiscriminator discriminator, int minimumSize) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateDecoyVisitor(decoys, minimumSize, discriminator);
        //noinspection unchecked
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }

    public static List<ClusterPeptideFraction> getCumulativeTargetData(IClusterSet cs, IDecoyDiscriminator discriminator, int minimumSize) {
        List<ClusterPeptideFraction> targets = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv2 = new CummulativeFDR.AccumulateTargetVisitor(targets, minimumSize, discriminator);
        //noinspection unchecked
        cs.visitClusters(tv2);
        Collections.sort(targets);

        return targets;
    }

    public void addClustering(IClusterSet added) {
        clusterings.add(added);
    }

    public Properties getProperties() {
        return properties;
    }


    @SuppressWarnings("UnusedDeclaration")
    public List<IClusterSet> getClusterings() {
        return clusterings;
    }


    public void generateReports() {
        for (IClusterSet cs : clusterings) {
            generateReport(cs);
        }
    }

    public void generateReport(IClusterSet cs) {

//          ClusterStatistics stat = new ClusterStatistics(spectra1, cs, new FractionInClustersOfSizeStatistics(spectra1));
//        stat.gatherData();
//        String report = stat.generateReport();
//        Set<String> usedDecoys = getUsedDecoys();
//        Set<String> unusedDecoys = getUnusedDecoys();
//        System.out.println("Used decoys " + usedDecoys.size() + " Unused Decoys " + unusedDecoys.size());
//        System.out.println(report);
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<ClusterPeptidePurity> getCummulativeDecoy(IClusterSet cs) {
        throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    public static void usage() {
        System.out.println("Usage propertiesFile <ClusteringFile or Directory> ...");
    }


    public void showFDRCharts(String name) {

        PSMClusterDecoyChart.makeReliableIdentificationChart(name + " Reliable Identifications", this, false);
        if(true)
            return;

        PSMClusterDecoyChart.makeClusterRangeChart(name + " Cluster Ranges", this, false);

        PSMClusterDecoyChart.makeFractionalChart(name + " Fractional PSMS", this, false);

        PSMClusterDecoyChart.makeDuplicatesChart(name + " Fractional PSMS", this, false);
        //     PSMClusterDecoyChart.makeFractionalChart(name + " Total Fractional PSMS", this,true);
        //    PSMClusterDecoyChart.makeFDRChart(name + " FDR", this);
        PSMClusterDecoyChart.makeCummulativeTotalChart(name + " Total PSMS", this);
    }


    public static void showClusterSizesAndCounts(final String pArg, final IClusterSet pCs) {
        long totalSpectra = 0;
        long totalStableClusters = 0;
        Set<String> uniqueIds = new HashSet<String>();
        for (ISpectralCluster scs : pCs.getClusters()) {
            for (ISpectrum spec  : scs.getClusteredSpectra()) {
                uniqueIds.add(spec.getId());
            }
            totalSpectra += scs.getClusteredSpectraCount();
        }
        int uniqueSpectra = uniqueIds.size();
        // if 64 is a stable pride cluster then shink it for smaller sets
        double v = ((double) uniqueSpectra) / 30000000;
        double stableClusterSize = Math.max(16, 64.0 * v);

        for (ISpectralCluster scs : pCs.getClusters()) {
            if (scs.getClusteredSpectraCount() >= stableClusterSize)
                totalStableClusters++;
        }
        System.out.println(pArg + "  Unique Spectra " + uniqueSpectra +" Number Spectra " + totalSpectra + " Number Stable Clusters " + totalStableClusters + " Stable Cluster Size " + stableClusterSize);
    }

    public PSM_Holder getPsms() {
        return psms;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }

        PSMUtilities.startClusterSaver(new PrintWriter(new FileWriter("BigRangeClusters.clustering")));

        PSMComparisonMain mainClass = new PSMComparisonMain();

        ElapsedTimer et = new ElapsedTimer();
        mainClass.handlePropertiesFile(args[0]);

        et.formatElapsed("Handled Properties");
        et.reset();
        PSM_Holder psms1 = mainClass.getPsms();

        for (int i = 1; i < args.length; i++) {
            mainClass.clearDecoyUse();
            String arg = args[i];
            File originalFile = new File(arg);
            IClusterSet cs = PSMUtilities.readClusterSet(originalFile, arg + "SemiStableNew.clustering");
             showClusterSizesAndCounts(arg, cs);


               cs = cs.dropClustersLessThanSize(4); // drop the riff raff
            showClusterSizesAndCounts(arg, cs);
               cs.setName(arg);
            mainClass.addClustering(cs);
            // showChart(cs);
        }
        et.formatElapsed("Cluster Sets");


        mainClass.showFDRCharts(mainClass.getProperty("name"));
        mainClass.generateReports();

        PSMUtilities.closeClusterSaver();

    }



}
