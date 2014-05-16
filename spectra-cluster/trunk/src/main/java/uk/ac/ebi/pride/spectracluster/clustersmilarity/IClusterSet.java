package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
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

    public List<IPeptideSpectrumCluster> getClusters();


    public int getNumberDuplicateClusters();

    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<IPeptideSpectrumCluster> visitor, TypedVisitor<IPeptideSpectrumCluster>... otherVisitors);

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    public List<IPeptideSpectrumCluster> getMatchingClusters(TypedPredicate<IPeptideSpectrumCluster> condition);

    public void addClusters(Collection<IPeptideSpectrumCluster> clusters);

    public void addCluster(IPeptideSpectrumCluster cluster);


    public List<IPeptideSpectrumCluster> getClustersWithSpectrum(ISpectrum spectrum);

    public Collection<IPeptideSpectrumCluster> getBestMatchingClusters(IPeptideSpectrumCluster cluster, int maxMatches);

    public IClusterSet dropClustersLessThanSize(int minsize);

}
