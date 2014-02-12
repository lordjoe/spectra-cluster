package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.IWideBinner;
import com.lordjoe.utilities.ElapsedTimer;
import org.apache.hadoop.io.Text;
import org.systemsbiology.hadoop.AbstractParameterizedReducer;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

/**
 * Merge spectra with unstable clusters
 */
public class StableSpectrumMergeReducer extends AbstractParameterizedReducer {

    private double majorMZ;
    private int currentCharge;
    private int currentBin;
    private IWideBinner binner = StableClusterMapper.BINNER;
    private IStableClusteringEngine clusteringEngine;
    private ElapsedTimer binTime = new ElapsedTimer();
    private ElapsedTimer jobTime = new ElapsedTimer();
    private int currentGroup;


    @SuppressWarnings("UnusedDeclaration")
    public double getMajorMZ() {
        return majorMZ;
    }

    public int getCurrentCharge() {
        return currentCharge;
    }

    public IStableClusteringEngine getClusteringEngine() {
        return clusteringEngine;
    }

    public int getCurrentBin() {
        return currentBin;
    }

    public IWideBinner getBinner() {
        return binner;
    }

    public int getCurrentGroup() {
        return currentGroup;
    }


    @Override
    protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);
        Defaults.configureAnalysisParameters(getApplication());

    }


    @Override
    public void reduceNormal(Text key, Iterable<Text> values,
                             Context context) throws IOException, InterruptedException {

        String keyStr = key.toString();
        //    System.err.println(keyStr);
        StableChargeBinMZKey realKey;
        if (keyStr.contains(StableChargeBinMZKey.SORT_PREFIX))
            realKey = new StableChargeBinMZKey(keyStr);
        else {
            realKey = new UnStableChargeBinMZKey(keyStr);
        }

        // we only need to change engines for different charges
        if (realKey.getCharge() != getCurrentCharge() ||
                realKey.getBin() != getCurrentBin() ||
                realKey.getGroup() != getCurrentGroup() ||
                clusteringEngine == null) {
            updateEngine(context, realKey);
        }

        IStableClusteringEngine stableClusteringEngine = getClusteringEngine();

        int numberProcessed = 0;

        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();

            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            final ISpectralCluster cluster = ParserUtilities.readSpectralCluster(rdr, null);

            if (cluster != null && stableClusteringEngine != null) {  // todo why might this happen
                if (!cluster.isStable()) {
                    stableClusteringEngine.addUnstableCluster(cluster);
                } else {
                   stableClusteringEngine.processStableCluster(cluster);
                    List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
                    //
                    for (ISpectrum spc : clusteredSpectra) {
                        writeCluster(context,spc.asCluster());
                    }
                }
            }
            if (numberProcessed % 100 == 0)
                binTime.showElapsed("processed " + numberProcessed, System.err);
            //     System.err.println("processed " + numberProcessed);
            numberProcessed++;
        }
    }

    /**
     * write cluster and key
     *
     * @param context  !null context
     * @param clusters !null list of clusters
     */
    protected void writeClusters(final Context context, final Collection<ISpectralCluster> clusters) throws IOException, InterruptedException {
        for (ISpectralCluster cluster : clusters) {
            writeCluster(context, cluster);
        }
    }


    /**
     * write one cluster and key
     *
     * @param context !null context
     * @param cluster !null cluster
     */
    protected void writeCluster(final Context context, final ISpectralCluster cluster) throws IOException, InterruptedException {
        final List<ISpectralCluster> allClusters = getClusteringEngine().findNoneFittingSpectra(cluster);
        if (!allClusters.isEmpty()) {
            for (ISpectralCluster removedCluster : allClusters) {

                // drop all spectra
                final List<ISpectrum> clusteredSpectra = removedCluster.getClusteredSpectra();
                ISpectrum[] allRemoved = clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]);
                cluster.removeSpectra(allRemoved);

                // and write as stand alone
                writeOneCluster(context, removedCluster);
            }

        }
        // now write the original
        if (cluster.getClusteredSpectraCount() > 0)
            writeOneCluster(context, cluster);     // nothing removed
    }

    protected void writeOneCluster(final Context context, final ISpectralCluster cluster) throws IOException, InterruptedException {
        if (cluster.getClusteredSpectraCount() == 0)
            return; // empty dont bother

        IWideBinner binner1 = getBinner();
        float precursorMz = cluster.getPrecursorMz();
        int bin = binner1.asBin(precursorMz);
        // you can merge clusters outside the current bin but not write them
        if (bin != currentBin)
            return;
        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), precursorMz);

        StringBuilder sb = new StringBuilder();
        cluster.append(sb);
        String string = sb.toString();

        if (string.length() > SpectraHadoopUtilities.MIMIMUM_CLUSTER_LENGTH) {
            writeKeyValue(key.toString(), string, context);

        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMajorMZ(double majorMZ) {
        this.majorMZ = majorMZ;
    }

    public void setCurrentCharge(int currentCharge) {
        if (currentCharge == this.currentCharge)
            return;
        this.currentCharge = currentCharge;
        System.err.println("Setting charge   " + currentCharge);
    }

    public void setCurrentBin(int currentBin) {
        this.currentBin = currentBin;
        double mid = getBinner().fromBin(currentBin);
        String midStr = String.format("%10.1f", mid).trim();
        binTime.reset();
        jobTime.showElapsed("Handling bin " + currentBin + " " + midStr, System.err);
    }

    public void setCurrentGroup(int currentGroup) {
        this.currentGroup = currentGroup;
    }

    /**
     * make a new engine because  either we are in a new peak or at the end (pMZKey == null
     *
     * @param context !null context
     * @param pMzKey  !null unless done
     */
    protected void updateEngine(final Context context, final StableChargeBinMZKey pMzKey) throws IOException, InterruptedException {
        if (clusteringEngine != null) {
            Collection<ISpectralCluster> clusters = clusteringEngine.getClusters();
            writeClusters(context, clusters);
            clusteringEngine = null;
        }

        // if not at end make a new engine
        if (pMzKey != null) {
            clusteringEngine = new StableClusteringEngine();
            majorMZ = pMzKey.getPrecursorMZ();
            setCurrentBin(pMzKey.getBin());
            setCurrentCharge(pMzKey.getCharge());
            setCurrentGroup(pMzKey.getGroup());
        }
    }

    /**
     * Called once at the end of the task.
     */
    @Override
    protected void cleanup(final Context context) throws IOException, InterruptedException {
        //    writeParseParameters(context);
        updateEngine(context, null); // write any left over clusters
        super.cleanup(context);

    }



}
