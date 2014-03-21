package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.chart.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterComparisonMain
 * User: Steve
 * Date: 1/17/14
 */
public class ClusterComparisonMain implements IDecoyDiscriminator {
    public static final ClusterComparisonMain INSTANCE = new ClusterComparisonMain();

    private final Properties properties = new Properties();
    private final Set<String> decoys = new HashSet<String>();
    private final Set<String> used_decoys = new HashSet<String>();
    private final List<IClusterSet> clusterings = new ArrayList<IClusterSet>();
    private final SimpleSpectrumRetriever spectra = new SimpleSpectrumRetriever();

    private ClusterComparisonMain() {
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

    public int getNumberSpectra() {
        return spectra.getSpectraCount();
    }

    protected void handleProperties() {
        String tsvFileName = getProperty("SpectraFile");
        File tsvFile = new File(tsvFileName);
        ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, spectra);


        String decoyFileName = getProperty("Decoys");
        File decoyFile = new File(decoyFileName);
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(decoyFile));
            String line = rdr.readLine();
            while (line != null) {
                decoys.add(line.trim());
                line = rdr.readLine();
            }
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




    public static List<ClusterPeptideFraction> getDistributionData(IClusterSet cs,IDecoyDiscriminator discriminator,  int minimumSize,ClusterDataType type) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateClusterPeptideFractionVisitor(decoys,minimumSize,  type);
        //noinspection unchecked
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }


    public static List<ClusterPeptideFraction> getCumulativeData(IClusterSet cs,IDecoyDiscriminator discriminator,  int minimumSize) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateClusterPeptideFractionVisitor(decoys,  minimumSize);
        //noinspection unchecked
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }



    public static List<ClusterMZSpread> getClusterRangeData(IClusterSet cs,IDecoyDiscriminator discriminator,  int minimumSize) {
        List<ClusterMZSpread> ranges = new ArrayList<ClusterMZSpread>();
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateClusterMZRangeVisitor(ranges,  minimumSize);
        //noinspection unchecked
        cs.visitClusters(tv);
        Collections.sort(ranges);
        return ranges;
    }



    public static List<ClusterPeptideFraction> getCumulativeDecoyData(IClusterSet cs,IDecoyDiscriminator discriminator,  int minimumSize) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv = new CummulativeFDR.AccumulateDecoyVisitor(decoys,  minimumSize,discriminator );
        //noinspection unchecked
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }

    public static List<ClusterPeptideFraction> getCumulativeTargetData(IClusterSet cs,IDecoyDiscriminator discriminator, int minimumSize) {
        List<ClusterPeptideFraction> targets = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv2 = new CummulativeFDR.AccumulateTargetVisitor(targets, minimumSize,discriminator);
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

    public SimpleSpectrumRetriever getSpectra() {
        return spectra;
    }

    public void generateReports() {
        for (IClusterSet cs : clusterings) {
            generateReport(cs);
        }
    }

    public void generateReport(IClusterSet cs) {

        SimpleSpectrumRetriever spectra1 = getSpectra();
        ClusterStatistics stat = new ClusterStatistics(spectra1, cs, new FractionInClustersOfSizeStatistics(spectra1));
        stat.gatherData();
        String report = stat.generateReport();
        Set<String> usedDecoys = getUsedDecoys();
        Set<String> unusedDecoys = getUnusedDecoys();
        System.out.println("Used decoys " + usedDecoys.size() + " Unused Decoys " + unusedDecoys.size());
        System.out.println(report);
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<ClusterPeptidePurity> getCummulativeDecoy(IClusterSet cs) {
        throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    public static void usage() {
        System.out.println("Usage propertiesFile <ClusteringFile or Directory> ...");
    }

    public static void showChart(final IClusterSet pCs) {
        INSTANCE.generateReport(pCs);

        ClusterDecoyChart.makeDecoyChart(INSTANCE, pCs, pCs.getName());
    }

    public void showFDRCharts( ) {

        ClusterDecoyChart.makeFDRChart("FDR", this);
        ClusterDecoyChart.makeCummulativeTotalChart("Total PSMS", this);
    }

    public void compareClusters() {
        SimpleSpectrumRetriever spectra1 = getSpectra();
        List<IClusterSet> clst = getClusterings();
        if(clst.size() < 2)
            return;
        IClusterSet cs1 = clst.get(0);
        IClusterSet cs2 = clst.get(1);
        MostSimilarClusterSet.compareClusterSets(  spectra1, cs1,cs2) ;

    }


    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            return;
        }
        INSTANCE.handlePropertiesFile(args[0]);
        SimpleSpectrumRetriever spectra1 = INSTANCE.getSpectra();
        for (int i = 1; i < args.length; i++) {
            INSTANCE.clearDecoyUse();
            String arg = args[i];
            File originalFile = new File(arg);
            IClusterSet cs = MostSimilarClusterSet.readClusterSet(spectra1, originalFile, arg + "SemiStableNew.clustering");
            cs.setName(arg);
            INSTANCE.addClustering(cs);
           // showChart(cs);
        }
        INSTANCE.compareClusters();
        INSTANCE.showFDRCharts();
        INSTANCE.generateReports();
      }


}
