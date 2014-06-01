package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
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

    public List<ICluster> getClusters();


    public int getNumberDuplicateClusters();

    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<ICluster> visitor, TypedVisitor<ICluster>... otherVisitors);

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    public List<ICluster> getMatchingClusters(TypedPredicate<ICluster> condition);

    public void addClusters(Collection<ICluster> clusters);

    public void addCluster(ICluster cluster);


    public List<ICluster> getClustersWithSpectrum(ISpectrum spectrum);

    public Collection<ICluster> getBestMatchingClusters(ICluster cluster, int maxMatches);

    public IClusterSet dropClustersLessThanSize(int minsize);

}
