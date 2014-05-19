package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusteringEngine implements IStableClusteringEngine {

    private final SortedSet<ICluster> unstableClusters = new TreeSet<ICluster>();

    private final SimilarityChecker similarityChecker;

    private boolean stableClusterProcessed;

    public StableClusteringEngine() {
        this(Defaults.INSTANCE.getDefaultSimilarityChecker());
    }

    public StableClusteringEngine(SimilarityChecker similarityChecker) {
        this.similarityChecker = similarityChecker;
    }

    /**
     * add one cluster and return any clusters which are too far in mz from further consideration
     * NOTE clusters MUST be added in ascending MZ order
     *
     * @param added !null cluster to add
     * @return !null list of clusters not far enough away they will no longer change
     */
    @Override
    public Collection<ICluster> addClusterIncremental(final ICluster added) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * add some clusters
     *
     * @param cluster
     */
    @Override
    public void addClusters(final ICluster... cluster) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    @Override
    public boolean processClusters() {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    @Override
    public String getName() {
        return "StableClusteringEngine";
    }

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    @Override
    public void setName(final String pName) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    @Override
    public int size() {
        return unstableClusters.size();
    }

    @Override
    public void addUnstableCluster(ICluster unstableCluster) {
        if (stableClusterProcessed) {
            // let this slide
            // throw new IllegalStateException("Adding unstable cluster after processing stable clusters");
        }

        unstableClusters.add(unstableCluster);
    }

    @Override
    public void processStableCluster(ICluster stableCluster) {
        stableClusterProcessed = true;

        Set<ICluster> emptyUnstableClusters = new HashSet<ICluster>();
        for (ICluster unstableCluster : unstableClusters) {
            boolean empty = mergeUnstableCluster(stableCluster, unstableCluster);
            if (empty) {
                emptyUnstableClusters.add(unstableCluster);
            }
        }

        unstableClusters.removeAll(emptyUnstableClusters);
    }

    private boolean mergeUnstableCluster(ICluster stableCluster, ICluster unstableCluster) {
        ISpectrum consensusSpectrum = stableCluster.getConsensusSpectrum();

        // find all the unstable spectra which can be merged into the stable cluster
        Set<ISpectrum> spectraToRemove = new HashSet<ISpectrum>();
        for (ISpectrum unstableSpectrum : unstableCluster.getClusteredSpectra()) {
            double similarity = similarityChecker.assessSimilarity(unstableSpectrum, consensusSpectrum);
            if (similarity >= similarityChecker.getDefaultThreshold()) {
                spectraToRemove.add(unstableSpectrum);
            }
        }

        // remove from unstable cluster
        ISpectrum[] arrayOfSpectraToRemove = spectraToRemove.toArray(new ISpectrum[spectraToRemove.size()]);
        unstableCluster.removeSpectra(arrayOfSpectraToRemove);

        // add into stable cluster
        stableCluster.addSpectra(arrayOfSpectraToRemove);

        return unstableCluster.getClusteredSpectraCount() == 0;
    }

    @Override
    public Collection<ICluster> getClusters() {
        return new ArrayList<ICluster>(unstableClusters);
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    @Override
    public List<ICluster> findNoneFittingSpectra(ICluster cluster) {
        List<ICluster> noneFittingSpectra = new ArrayList<ICluster>();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = similarityChecker.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = similarityChecker.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(ClusterUtilities.asCluster(spectrum));
                }
            }
        }

        return noneFittingSpectra;
    }

    public int getNumberOfUnstableSpectra() {
        int count = 0;

        for (ICluster unstableCluster : unstableClusters) {
            count += unstableCluster.getClusteredSpectraCount();
        }

        return count;
    }
}
