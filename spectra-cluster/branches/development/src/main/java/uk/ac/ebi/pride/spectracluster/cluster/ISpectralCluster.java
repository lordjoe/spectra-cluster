package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.*;
import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import javax.annotation.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectralCluster extends ISpectrumHolder, IPeaksHolder, Equivalent<ISpectralCluster>, Comparable<ISpectralCluster>, IMajorPeaksHolder {

    public static final Comparator<ISpectralCluster>  SIMPLE_CLUSTER_COMPARATOR =  new Comparator<ISpectralCluster>() {
        @Override
        public int compare(ISpectralCluster o1, ISpectralCluster o2) {
               int ret = CompareTo.compare(o1.getPrecursorMz(), o2.getPrecursorMz());
                if (ret != 0) {
                   return ret;
               }
                if (o1.getPrecursorCharge() != o2.getPrecursorCharge()) {
                    return o1.getPrecursorCharge() < o2.getPrecursorCharge() ? -1 : 1;
                }
                if (o1.getClusteredSpectraCount() != o2.getClusteredSpectraCount()) {
                    return o1.getClusteredSpectraCount() < o2.getClusteredSpectraCount() ? -1 : 1;
                }
        
                int hash1 = o1.hashCode();
                int hash2 = o2.hashCode();
                if (hash1 != hash2)
                    return hash1 < hash2 ? -1 : 1;
        
                return 0;
            }
            
     };
    
    public static final TypedPredicate<ISpectralCluster> STABLE_PREDICATE = new TypedPredicate<ISpectralCluster>() {
        @Override
        public boolean apply(@Nonnull ISpectralCluster pT, Object... otherdata) {
            return pT.isStable();
        }
    };

    public static final TypedPredicate<ISpectralCluster> SEMI_STABLE_PREDICATE = new TypedPredicate<ISpectralCluster>() {
        @Override
        public boolean apply(@Nonnull ISpectralCluster pT, Object... otherdata) {
            return pT.isSemiStable();
        }
    };

    /**
     * Get cluster id
     */
    public String getId();

    /**
     * concensus spectrum MZ
     *
     * @return
     */
    public float getPrecursorMz();

    /**
     * concensus spectrum Charge
     *
     * @return
     */
    public int getPrecursorCharge();

    /**
     * Get a list of peptide sequences
     *
     * @return
     */
    public  @Nonnull List<String> getPeptides();

    /**
     * Get the single most common peptide sequence
     * @return
     */
    public String getMostCommonPeptide();

    /**
     * get peptides with statistics
     * @return list ordered bu purity
     */
    public @Nonnull List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator dd);



    /**
     * Get consensus spectrum
     */
    public ISpectrum getConsensusSpectrum();

    /**
     * real spectrum with the highest quality - this is a
     * good way to compare clusters
     *
     * @return !null spectrum
     */
    public ISpectrum getHighestQualitySpectrum();

    /**
     * all internally spectrum
     */
    public  @Nonnull List<ISpectrum> getHighestQualitySpectra();

    /**
     * all internally spectrum
     */
    public  @Nonnull List<ISpectrum> getClusteredSpectra();



    /**
     * count of internal spectrum
     */
    public int getClusteredSpectraCount();


    public void appendData(Appendable out);


    public void append(Appendable out);


    /**
     * return a set of all ids
     *
     * @return
     */
    public @Nonnull  Set<String> getSpectralIds();

    /**
     * write out the data as a .cgf frago1nt
     *
     * @param out place to append   // todo move out of SpectralCLustering
     */
    public void appendSpectra(Appendable out);

    /**
     * write out the data as a .clustering frago1nt
     *
     * @param out place to append     // todo move out of SpectralCLustering
     */
    public void appendClustering(Appendable out);

//    /**
//     * needed so copy constructors work with the interface
//     *
//     * @return
//     */
//    public IConsensusSpectrumBuilder cloneConsensusSpectrumBuilder();

    /**
     * does the concensus spectrum contin this is a major peak
     *
     * @param mz peak as int
     * @return true if so
     */
    public boolean containsMajorPeak(int mz);


    /**
     * Add a list of spectrum to cluster
     */
    public void addSpectra(List<ISpectrum> added);

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isStable();

    /**
     * if true the cluster is stable and will not allow removal
     *
     * @return
     */
    public boolean isSemiStable();

//    /**
//     * if true the cluster is stable and will not allow removal
//     * @param stable as
//       */
//    @SuppressWarnings("UnusedDeclaration")
//    public void setStable(boolean stable);


}
