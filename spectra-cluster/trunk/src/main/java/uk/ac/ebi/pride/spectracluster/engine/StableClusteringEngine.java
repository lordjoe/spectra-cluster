package uk.ac.ebi.pride.spectracluster.engine;

import com.lordjoe.utilities.IProgressHandler;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusteringEngine implements IStableClusteringEngine {

    private final SortedSet<ISpectralCluster> unstableClusters = new TreeSet<ISpectralCluster>();

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
    public Collection<ISpectralCluster> addClusterIncremental(final ISpectralCluster added) {
        throw new UnsupportedOperationException("This Should NEVER be Called");
    }

    /**
     * add some clusters
     *
     * @param cluster
     */
    @Override
    public void addClusters(final ISpectralCluster... cluster) {
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

    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    @Override
    public void addProgressMonitor(final IProgressHandler handler) {
        throw new UnsupportedOperationException("This Should NEVER be Called");

    }

    @Override
    public void addUnstableCluster(ISpectralCluster unstableCluster) {
        if (stableClusterProcessed) {
            // let this slide
            // throw new IllegalStateException("Adding unstable cluster after processing stable clusters");
        }

        unstableClusters.add(unstableCluster);
    }

    @Override
    public void processStableCluster(ISpectralCluster stableCluster) {
        stableClusterProcessed = true;

        Set<ISpectralCluster> emptyUnstableClusters = new HashSet<ISpectralCluster>();
        for (ISpectralCluster unstableCluster : unstableClusters) {
            boolean empty = mergeUnstableCluster(stableCluster, unstableCluster);
            if (empty) {
                emptyUnstableClusters.add(unstableCluster);
            }
        }

        unstableClusters.removeAll(emptyUnstableClusters);
    }

    private boolean mergeUnstableCluster(ISpectralCluster stableCluster, ISpectralCluster unstableCluster) {
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
    public Collection<ISpectralCluster> getClusters() {
        return new ArrayList<ISpectralCluster>(unstableClusters);
    }

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ISpectralCluster> findNoneFittingSpectra(ISpectralCluster cluster) {
        List<ISpectralCluster> noneFittingSpectra = new ArrayList<ISpectralCluster>();

        if (cluster.getClusteredSpectra().size() > 1) {
            for (ISpectrum spectrum : cluster.getClusteredSpectra()) {
                final ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
                final double similarityScore = similarityChecker.assessSimilarity(consensusSpectrum, spectrum);
                final double defaultThreshold = similarityChecker.getDefaultRetainThreshold();  // use a lower threshold to keep as to add
                if (similarityScore < defaultThreshold) {
                    noneFittingSpectra.add(spectrum.asCluster());
                }
            }
        }

        return noneFittingSpectra;
    }


    /**
     * allow nonfitting spectra to leave and return a list of clusters to write out
     *
     * @param cluster
     * @return !null List<ISpectralCluster
     */
    @Nonnull
    @Override
    public List<ISpectralCluster> asWritttenSpectra(@Nonnull ISpectralCluster cluster) {
        return ClusteringUtilities.asWritttenSpectra(cluster, this);
    }

    public int getNumberOfUnstableSpectra() {
        int count = 0;

        for (ISpectralCluster unstableCluster : unstableClusters) {
            count += unstableCluster.getClusteredSpectraCount();
        }

        return count;
    }
}
