package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.io.TSVClusterAppender;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class SimpleClusterSet extends SimpleClusterRetriever implements IClusterSet {


    /**
     * merge similar clusters
     *
     * @param inp
     * @return
     */
    public static IClusterSet removeDuplicates(IClusterSet inp) {

        int total = 0;
        int mergable = 0;
        int samePeptideNonMergable = 0;

        ISimilarityChecker similarityChecker = Defaults.getDefaultSimilarityChecker();
        List<ICluster> holder = new ArrayList<ICluster>();
        List<ICluster> clusters = inp.getClusters();
        // NOTE this is a hack sine the ids are the most common peptide
        Collections.sort(clusters, new Comparator<ICluster>() {
            @Override
            public int compare(ICluster o1, ICluster o2) {
                String id1 = o1.getId();
                String id2 = o2.getId();
                return id1.compareTo(id2);
            }
        });
        String currentPeptide = "";
        ICluster lastCluster = null;
        List<ICluster> toMerge = new ArrayList<ICluster>();

        final TSVClusterAppender tsvClusterAppender = new TSVClusterAppender();
        for (ICluster cluster : clusters) {
            total++;
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String id = cluster.getId();
            String mostCommonPeptide = ClusterSimilarityUtilities.getMostCommonPeptide(cluster);
               // should only happen once
            if (lastCluster == null) {
                lastCluster = cluster;
                toMerge.clear();
                toMerge.add(cluster);
                currentPeptide = mostCommonPeptide;
            } else {
                if (!currentPeptide.equals(mostCommonPeptide)) {
                    if (toMerge.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        tsvClusterAppender.appendCluster(sb, lastCluster);
                        sb.append("\n");
                        for (ICluster sc : toMerge) {
                            if (sc != lastCluster) {
                                tsvClusterAppender.appendCluster(sb, sc);
                                sb.append("\n");
                                final List<ISpectrum> clusteredSpectra = sc.getClusteredSpectra();
                                lastCluster.addSpectra(clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]));
                            }
                        }
                        sb.append("\n");
                        System.out.println(sb.toString());
                    }
                    holder.add(lastCluster);
                    lastCluster = cluster;
                    toMerge.clear();
                    toMerge.add(cluster);
                    currentPeptide = mostCommonPeptide;

                } else {
                    // these may be the same
                    double sim = similarityChecker.assessSimilarity(lastCluster.getConsensusSpectrum(), cluster.getConsensusSpectrum());
                    if (sim > Defaults.getSimilarityThreshold()) {
                        mergable++;
                        toMerge.add(cluster);
                        //   lastCluster.addSpectra(cluster.getClusteredSpectra());
                    } else {
                        holder.add(lastCluster);
                        samePeptideNonMergable++;
                    }
                }
            }

        }

        System.out.println("Start Total " + total);
        System.out.println("Mergable " + mergable);
        System.out.println("Not Mergable " + samePeptideNonMergable);
        System.out.println("New Total " + holder.size());
        return new SimpleClusterSet(holder);
    }

    private String name;
    private ClusteringHeader header;
    private PeptideUseId usage = new PeptideUseId();

    public SimpleClusterSet(Collection<ICluster> clusters) {
        super(clusters);
    }

    public SimpleClusterSet() {
        super();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String pName) {
        name = pName;
    }


    /**
     * return header imformation
     *
     * @return
     */
    @Nullable
    @Override
    public ClusteringHeader getHeader() {
        return header;
    }

    public void setHeader(ClusteringHeader header) {
        this.header = header;
    }

    /**
     * get all clusters matching the predicate
     *
     * @param condition
     * @return
     */
    @Override
    public List<ICluster> getMatchingClusters(TypedPredicate<ICluster> condition) {
        List<ICluster> holder = new ArrayList<ICluster>();
        for (ICluster cluster : getClusters()) {
            if (condition.apply(cluster))
                holder.add(cluster);
        }
        return holder;
    }


    /**
     * visit all clusters
     *
     * @param visitor !null visitor(s)
     */
    public void visitClusters(TypedVisitor<ICluster> visitor, TypedVisitor<ICluster>... otherVisitors) {
        for (ICluster cluster : getClusters()) {
            visitor.visit(cluster);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < otherVisitors.length; i++) {
                otherVisitors[i].visit(cluster);

            }
        }

    }


    @Override
    public void addClusters(Collection<ICluster> clusters) {
        for (ICluster cluster : clusters) {
            guaranteeClusterId(cluster);
            addCluster(cluster);
        }
    }

    protected void buildAndSetIdForClusterWithoutId(ICluster cluster) {
        List<String> peptides = ClusterSimilarityUtilities.getClusterPeptides(cluster);
        String id = cluster.getSpectralId();
//        if (peptides.isEmpty())
//            id = cluster.toString();
//        else
//            id = usage.getPeptideId(peptides.get(0));

        if (cluster instanceof LazyLoadedSpectralCluster) {
            ((LazyLoadedSpectralCluster) cluster).setId(id);
            return;
        }
        if (cluster instanceof PSMSpectralCluster) {
            ((PSMSpectralCluster) cluster).setId(id);
            return;
        }
        if (cluster instanceof SpectralCluster) {
            ((SpectralCluster) cluster).setId(id);
            return;
        }
        throw new IllegalStateException("cannot guarantee non-null id");
    }

    @Override
    public List<ICluster> getClustersWithSpectrum(ISpectrum spectrum) {
        String id = spectrum.getId();
        List<ICluster> holder = new ArrayList<ICluster>();
        for (ICluster test : getClusters()) {
            List<ISpectrum> spcetrums = test.getClusteredSpectra();
            for (ISpectrum spcetrum : spcetrums) {
                if (id.equals(spcetrum.getId())) {
                    holder.add(test);
                    break;
                }
            }
        }

        return holder;
    }

    @Override
    public IClusterSet dropClustersLessThanSize(final int minsize) {
        SimpleClusterSet ret = new SimpleClusterSet();
        for (ICluster sc : getClusters()) {
            if (sc.getClusteredSpectraCount() >= minsize)
                ret.addCluster(sc);
        }
        return ret;
    }

    @Override
    public String toString() {
        if (getName() != null)
            return getName();
        return super.toString();
    }

    @Override
    public List<ICluster> getBestMatchingClusters(ICluster cluster, int maxMatches) {
        ClusterQualityComparator clusterQualityComparator = new ClusterQualityComparator(cluster);

        return getBestMatchesWithQuality(maxMatches, clusterQualityComparator);
    }

    private List<ICluster> getBestMatchesWithQuality(int maxMatches, ClusterQualityComparator clusterQualityComparator) {
        List<ICluster> clusters = getClusters();
        Collections.sort(clusters, clusterQualityComparator);

        int numberOfMatches = clusters.size() < maxMatches ? clusters.size() : maxMatches;
        return clusters.subList(0, numberOfMatches);
    }

    private static class ClusterQualityComparator implements Comparator<ICluster> {

        private final ICluster clusterToMatch;

        private ClusterQualityComparator(ICluster clusterToMatch) {
            this.clusterToMatch = clusterToMatch;
        }

        @Override
        public int compare(ICluster o1, ICluster o2) {
            double match1 = matchQuality(o1, clusterToMatch);
            double match2 = matchQuality(o2, clusterToMatch);

            return CompareTo.compare(match1, match2);
        }

        private double matchQuality(ICluster c1, ICluster c2) {
            Set<String> pep1 = new HashSet<String>(ClusterSimilarityUtilities.getClusterPeptides(c1));
            Set<String> pep2 = new HashSet<String>(ClusterSimilarityUtilities.getClusterPeptides(c2));
            int totalPeptides = pep1.size() + pep2.size();
            if (totalPeptides == 0)
                return 0;
            Set<String> common = new HashSet<String>(pep1);
            common.retainAll(pep2);

            double commonCunt = common.size();
            return commonCunt / totalPeptides;


        }
    }
}
