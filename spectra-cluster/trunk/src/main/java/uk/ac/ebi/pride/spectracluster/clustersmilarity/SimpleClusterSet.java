package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.PeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.io.TSVClusterAppender;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
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

        SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        List<IPeptideSpectralCluster> clusters = inp.getClusters();
        // NOTE this is a hack sine the ids are the most common peptide
        Collections.sort(clusters, new Comparator<IPeptideSpectralCluster>() {
            @Override
            public int compare(IPeptideSpectralCluster o1, IPeptideSpectralCluster o2) {
                String id1 = o1.getId();
                String id2 = o2.getId();
                return id1.compareTo(id2);
            }
        });
        String currentPeptide = "";
        IPeptideSpectralCluster lastCluster = null;
        List<IPeptideSpectralCluster> toMerge = new ArrayList<IPeptideSpectralCluster>();

        final TSVClusterAppender tsvClusterAppender = new TSVClusterAppender();
        for (IPeptideSpectralCluster cluster : clusters) {
            total++;
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String id = cluster.getId();
            String mostCommonPeptide = cluster.getMostCommonPeptide();
            if ("HGHLGFLPR".equals(mostCommonPeptide))
                mostCommonPeptide = cluster.getMostCommonPeptide(); // break here
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
                        for (IPeptideSpectralCluster sc : toMerge) {
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
                    if (sim > similarityChecker.getDefaultThreshold()) {
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

    public SimpleClusterSet(Collection<IPeptideSpectralCluster> clusters) {
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
    public List<IPeptideSpectralCluster> getMatchingClusters(TypedPredicate<IPeptideSpectralCluster> condition) {
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster cluster : getClusters()) {
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
    public void visitClusters(TypedVisitor<IPeptideSpectralCluster> visitor, TypedVisitor<IPeptideSpectralCluster>... otherVisitors) {
        for (IPeptideSpectralCluster cluster : getClusters()) {
            visitor.visit(cluster);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < otherVisitors.length; i++) {
                otherVisitors[i].visit(cluster);

            }
        }

    }


    @Override
    public void addClusters(Collection<IPeptideSpectralCluster> clusters) {
        for (IPeptideSpectralCluster cluster : clusters) {
            guaranteeClusterId(cluster);
            addCluster(cluster);
        }
    }

    protected void buildAndSetIdForClusterWithoutId(IPeptideSpectralCluster cluster) {
        List<String> peptides = cluster.getPeptides();
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
        if (cluster instanceof PeptideSpectralCluster) {
            ((PeptideSpectralCluster) cluster).setId(id);
            return;
        }
        throw new IllegalStateException("cannot guarantee non-null id");
    }

    @Override
    public List<IPeptideSpectralCluster> getClustersWithSpectrum(ISpectrum spectrum) {
        String id = spectrum.getId();
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (IPeptideSpectralCluster test : getClusters()) {
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
        for (IPeptideSpectralCluster sc : getClusters()) {
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
    public List<IPeptideSpectralCluster> getBestMatchingClusters(IPeptideSpectralCluster cluster, int maxMatches) {
        ClusterQualityComparator clusterQualityComparator = new ClusterQualityComparator(cluster);

        return getBestMatchesWithQuality(maxMatches, clusterQualityComparator);
    }

    private List<IPeptideSpectralCluster> getBestMatchesWithQuality(int maxMatches, ClusterQualityComparator clusterQualityComparator) {
        List<IPeptideSpectralCluster> clusters = getClusters();
        Collections.sort(clusters, clusterQualityComparator);

        int numberOfMatches = clusters.size() < maxMatches ? clusters.size() : maxMatches;
        return clusters.subList(0, numberOfMatches);
    }

    private static class ClusterQualityComparator implements Comparator<IPeptideSpectralCluster> {

        private final IPeptideSpectralCluster clusterToMatch;

        private ClusterQualityComparator(IPeptideSpectralCluster clusterToMatch) {
            this.clusterToMatch = clusterToMatch;
        }

        @Override
        public int compare(IPeptideSpectralCluster o1, IPeptideSpectralCluster o2) {
            double match1 = matchQuality(o1, clusterToMatch);
            double match2 = matchQuality(o2, clusterToMatch);

            return CompareTo.compare(match1, match2);
        }

        private double matchQuality(IPeptideSpectralCluster c1, IPeptideSpectralCluster c2) {
            Set<String> pep1 = new HashSet<String>(c1.getPeptides());
            Set<String> pep2 = new HashSet<String>(c2.getPeptides());
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
