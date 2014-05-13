package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.utilities.IProgressHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.IClusteringEngine
 * <p/>
 * This object does the clusters
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface IClusteringEngine {

    /**
     * Get clustered clusters sorted by MZ is useful
     *
     * @return !null list this will be sorted by mz a include clusters of all sizes
     */
    public Collection<ISpectralCluster> getClusters();


    /**
     * add some clusters
     */
    public void addClusters(ISpectralCluster... cluster);

    /**
     * clusters are merged in the internal collection
     *
     * @return true is  anything happened
     */
    public boolean processClusters();

    /**
     * nice for debugging to name an engine
     *
     * @return possibly null name
     */
    public String getName();

    /**
     * nice for debugging to name an engine
     *
     * @param pName possibly null name
     */
    public void setName(final String pName);

    /**
     * total number of clusters including queued clustersToAdd
     *
     * @return
     */
    public int size();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public
    @Nonnull
    List<ISpectralCluster> findNoneFittingSpectra(@Nonnull ISpectralCluster cluster);


    /**
     * allow nonfittinf spectra to leave and retuen a list of clusters to write out
     *
     * @return !null List<ISpectralCluster
     */
    public
    @Nonnull
    List<ISpectralCluster> asWritttenSpectra(@Nonnull ISpectralCluster cluster);


    /**
     * add code to monitor progress
     *
     * @param handler !null monitor
     */
    public void addProgressMonitor(IProgressHandler handler);

}
