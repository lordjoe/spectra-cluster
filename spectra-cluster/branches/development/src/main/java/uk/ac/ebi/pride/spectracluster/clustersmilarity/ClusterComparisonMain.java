package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.chart.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterComparisonMain
 * User: Steve
 * Date: 1/17/14
 */
public class ClusterComparisonMain {
    public static final ClusterComparisonMain INSTANCE = new ClusterComparisonMain();

    private final Properties properties = new Properties();
    private final Set<String> decoys = new HashSet<String>();
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

    public boolean isDecoy(String peptideSequence) {
        return decoys.contains(peptideSequence);
    }

    /**
     * collect all decoys as  ClusterPeptideFraction
     */
    public class AccumulateDecoyVisitor implements TypedVisitor<ISpectralCluster> {

        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateDecoyVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
        }

        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final ISpectralCluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : cluster.getPeptidePurity()) {
                if (isDecoy(pp.getPeptide())) {
                    data.add(pp);
                }
            }

        }
    }

    /**
     * collect all targets as  ClusterPeptideFraction
     */
    public class AccumulateTargetVisitor implements TypedVisitor<ISpectralCluster> {

        private final List<ClusterPeptideFraction> data;
        private final int minimumClusterSize;

        public AccumulateTargetVisitor(final List<ClusterPeptideFraction> pData, int pminimumClusterSize) {
            data = pData;
            minimumClusterSize = pminimumClusterSize;
        }

        /**
         * @param pISpectralCluster interface implemented by the visitor pattern
         */
        @Override
        public void visit(@Nonnull final ISpectralCluster cluster) {
            if (cluster.getClusteredSpectraCount() < minimumClusterSize)
                return;
            for (ClusterPeptideFraction pp : cluster.getPeptidePurity()) {
                if (!isDecoy(pp.getPeptide())) {
                    data.add(pp);
                }
            }

        }
    }

    public List<ClusterPeptideFraction> getCumulativeDecoyData(IClusterSet cs, int minimumSize) {
        List<ClusterPeptideFraction> decoys = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv = new AccumulateDecoyVisitor(decoys, minimumSize);
        cs.visitClusters(tv);
        Collections.sort(decoys);
        return decoys;
    }

    public List<ClusterPeptideFraction> getCumulativeTargetData(IClusterSet cs, int minimumSize) {
        List<ClusterPeptideFraction> targets = new ArrayList<ClusterPeptideFraction>();
        TypedVisitor<ISpectralCluster> tv2 = new AccumulateTargetVisitor(targets, minimumSize);
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
        System.out.println(report);
    }

    public List<ClusterPeptidePurity> getCummulativeDecoy(IClusterSet cs) {
        throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    public static void usage() {
        System.out.println("Usage propertiesFile <ClusteringFile or Directory> ...");
    }

    public static void showChart(  final IClusterSet pCs) {
        INSTANCE.generateReport(  pCs);

        ClusterDecoyChart.makeDecoyChart(INSTANCE,pCs, pCs.getName());
    }



    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            return;
        }
        INSTANCE.handlePropertiesFile(args[0]);
        SimpleSpectrumRetriever spectra1 = INSTANCE.getSpectra();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            File originalFile = new File(arg);
            IClusterSet cs = MostSimilarClusterSet.readClusterSet(spectra1, originalFile, arg + "SemiStableNew.clustering");
            cs.setName(arg);
            INSTANCE.addClustering(cs);
            showChart( cs);
        }


        INSTANCE.generateReports();
    }


}
