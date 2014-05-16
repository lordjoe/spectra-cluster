package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
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

    public List<IPeptideSpectralCluster> getClusters();


    public int getNumberDuplicateClusters();

    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<IPeptideSpectralCluster> visitor, TypedVisitor<IPeptideSpectralCluster>... otherVisitors);

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    public List<IPeptideSpectralCluster> getMatchingClusters(TypedPredicate<IPeptideSpectralCluster> condition);

    public void addClusters(Collection<IPeptideSpectralCluster> clusters);

    public void addCluster(IPeptideSpectralCluster cluster);


    public List<IPeptideSpectralCluster> getClustersWithSpectrum(ISpectrum spectrum);

    public Collection<IPeptideSpectralCluster> getBestMatchingClusters(IPeptideSpectralCluster cluster, int maxMatches);

    public IClusterSet dropClustersLessThanSize(int minsize);

}
