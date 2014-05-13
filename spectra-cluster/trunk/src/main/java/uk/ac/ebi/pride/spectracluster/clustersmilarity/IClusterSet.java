package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringHeader;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterSet {

    public String getName();

    public void setName(String name);

    /**
     * return header imformation
     *
     * @return
     */
    public
    @Nullable
    ClusteringHeader getHeader();

    public int getClusterCount();

    public List<ISpectralCluster> getClusters();


    public int getNumberDuplicateClusters();

    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<ISpectralCluster> visitor, TypedVisitor<ISpectralCluster>... otherVisitors);

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    public List<ISpectralCluster> getMatchingClusters(TypedPredicate<ISpectralCluster> condition);

    public void addClusters(Collection<ISpectralCluster> clusters);

    public void addCluster(ISpectralCluster cluster);


    public List<ISpectralCluster> getClustersWithSpectrum(ISpectrum spectrum);

    public Collection<ISpectralCluster> getBestMatchingClusters(ISpectralCluster cluster, int maxMatches);

    public IClusterSet dropClustersLessThanSize(int minsize);

}
