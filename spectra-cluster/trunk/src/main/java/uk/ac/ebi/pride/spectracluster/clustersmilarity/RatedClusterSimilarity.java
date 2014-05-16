package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CompareTo;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.io.IOException;
import java.util.*;

import static uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityEnum.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.RatedClusterSimilarity
 *
 * @author Steve Lewis
 * @date 14/11/13
 */
public class RatedClusterSimilarity implements Comparable<RatedClusterSimilarity> {


    private final MostSimilarClusters clusters;
    private final ClusterSimilarityEnum rating;
    private double similarityDistance;
    private final Set<ClusterSimilarityProblem> problems = new HashSet<ClusterSimilarityProblem>();
    private final SimilarityChecker similarity = Defaults.INSTANCE.getDefaultSimilarityChecker();

    public RatedClusterSimilarity(MostSimilarClusters clusters) {
        this.clusters = clusters;
        rating = buildRating(clusters);
    }

    public static final double GOOD_DISTANCE = 0.05;
    public static final double MEDIUM_DISTANCE = 0.10;
    public static final double BAD_DISTANCE = 0.3;

    protected ClusterSimilarityEnum buildRating(MostSimilarClusters clusters) {
        ClusterDistanceItem m1 = clusters.getBestMatch();

        if (m1 == null)
            return NoFit;

        if (m1.isIdentical())
            return Identical;

        if (m1.isSubset())
            return Subset;

        if (m1.isCloseSubset()) {
            m1.isCloseSubset(); // rerun to look at code
            return CloseSubset;
        }

        int testSize = m1.getSmallerClusterSize();
        double frac = m1.getCommonFraction();


        double distance = m1.getDistance();
        if (distance > MEDIUM_DISTANCE) {
            problems.add(ClusterSimilarityProblem.HighDotProduct);
        }

        if (frac > 0.7 && testSize > 10)
            return Good;

        if (false) {  // ignore this it is OK
            ClusterDistanceItem m2 = clusters.getNextBestMatches();
            double d2 = m2.getDistance();
            if (d2 < MEDIUM_DISTANCE) {
                problems.add(ClusterSimilarityProblem.CloseSecondCluster);
            }

        }

        IPeptideSpectralCluster source = m1.getSource();
        IPeptideSpectralCluster target = m1.getTarget();

        similarityDistance = similarity.assessSimilarity(source.getConsensusSpectrum(), target.getConsensusSpectrum());

        int sourceCount = source.getClusteredSpectraCount();
        int targetCount = target.getClusteredSpectraCount();

        double del = Math.abs(sourceCount - targetCount);
        double min = Math.min(targetCount, sourceCount);
        if (min == 0) {
            problems.add(ClusterSimilarityProblem.NoSpectra);
        } else {
            double farcDifference = del / min;
            if (farcDifference > 0.20) {
                problems.add(ClusterSimilarityProblem.UnevenSize);
            }
        }
        String peptide = source.getMostCommonPeptide();
        String p2 = target.getMostCommonPeptide();
        if (!peptide.equals(p2))
            problems.add(ClusterSimilarityProblem.DifferentBestPeptide);

        if (distance >= BAD_DISTANCE)
            return Bad;

        if (distance >= MEDIUM_DISTANCE)
            return Medium;

        if (problems.isEmpty())
            return Good;
        for (ClusterSimilarityProblem problem : problems) {
            String prob = problem.toString();
            prob = null;
        }
        return Medium;
    }


    public MostSimilarClusters getClusters() {
        return clusters;
    }

    public ClusterSimilarityEnum getRating() {
        return rating;
    }

    public boolean hasProblem(ClusterSimilarityProblem prob) {
        return problems.contains(prob);
    }


    public List<ClusterSimilarityProblem> getProblems() {
        List<ClusterSimilarityProblem> ret = new ArrayList<ClusterSimilarityProblem>(problems);
        Collections.sort(ret);
        return ret;
    }

    public void appendReportTSV(Appendable appendable) {
        try {
            MostSimilarClusters mostSimilarClusters = getClusters();
            ClusterDistanceItem bestMatch = mostSimilarClusters.getBestMatch();
            appendable.append(getRating().toString());
            appendable.append("\t");
            if (bestMatch != null) {
                IPeptideSpectralCluster source = bestMatch.getSource();
                IPeptideSpectralCluster target = bestMatch.getTarget();
                appendable.append(source.getId());
                appendable.append("\t");
                appendable.append(target.getId());
                appendable.append("\t");
                appendable.append(Integer.toString(source.getClusteredSpectraCount()));
                appendable.append("\t");
                appendable.append(Integer.toString(target.getClusteredSpectraCount()));
                appendable.append("\t");
                Set<String> commonIds = ClusterSimilarityUtilities.commonSpectralIds(source, target);
                appendable.append(Integer.toString(commonIds.size()));
                appendable.append("\t");
                // header up tp here

                double distance = bestMatch.getDistance();
                String distStr = String.format("%f8.3", distance);
                appendable.append(distStr);
                appendable.append("\t");

                String mostCommonPeptide = source.getMostCommonPeptide();
                //         if (!hasProblem(ClusterSimilarityProblem.DifferentBestPeptide))
                appendable.append(mostCommonPeptide);
                appendable.append("\t");

                String mostCommonTargetPeptide = target.getMostCommonPeptide();
                appendable.append(mostCommonTargetPeptide);
                appendable.append("\t");

                if (mostCommonPeptide.equals(mostCommonTargetPeptide))
                    appendable.append("SAME");

                appendable.append("\t");

                String simdistStr = String.format("%f8.3", similarityDistance);
                appendable.append(simdistStr);
                appendable.append("\t");


            }


            appendable.append("\n");
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static void appendHeaderTSV(Appendable appendable) {
        try {
            appendable.append("rating");
            appendable.append("\t");
            appendable.append("source id");
            appendable.append("\t");
            appendable.append("Target id");
            appendable.append("\t");

            appendable.append("source count");
            appendable.append("\t");
            appendable.append("target count");
            appendable.append("\t");
            appendable.append("common count");


            appendable.append("distance");
            appendable.append("\t");
            appendable.append("source peptide");
            appendable.append("\t");
            appendable.append("target peptide");
            appendable.append("\t");
            appendable.append("similarity distance");
            appendable.append("\t");

            appendable.append("\n");


        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        MostSimilarClusters mostSimilarClusters = getClusters();
        List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();

        if (bestMatches.size() == 0)
            return "===== BAD BAD ========";
        ClusterDistanceItem bestMatch = bestMatches.get(0);
        bestMatch.appendReport(sb);
        return sb.toString();

    }

    public void appendReport(Appendable appendable) {
        try {
            if (getRating() == ClusterSimilarityEnum.Bad) {
                appendable.append("===== BAD BAD ========\n");
                // return;
            }
            if (getRating() == ClusterSimilarityEnum.NoFit) {
                appendable.append("===== NOFIT  ========\n");
                return;
            }
            String simdistStr = String.format("%f8.3", similarityDistance);
            appendable.append(getRating().toString() + " " + getProblemString() +
                    simdistStr +
                    "\n");
            MostSimilarClusters mostSimilarClusters = getClusters();
            List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();

            ClusterDistanceItem bestMatch = bestMatches.get(0);
            bestMatch.appendReport(appendable);
            appendable.append("\n");


//                    for (int i = 1; i < bestMatches.size(); i++) {
//                        ClusterDistanceItem clusterDistanceItem = bestMatches.get(i);
//                             clusterDistanceItem.appendReport(appendable);
//                            appendable.append("\n");
//                        }
//                    }
            appendable.append("============================================\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getProblemString() {
        StringBuilder sb = new StringBuilder();
        for (ClusterSimilarityProblem prob : problems) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(prob);
        }

        return sb.toString();

    }


    @Override
    public int compareTo(RatedClusterSimilarity o2) {
        ClusterSimilarityEnum r1 = getRating();
        ClusterSimilarityEnum r2 = o2.getRating();
        if (r1 != r2)
            return ClusterSimilarityEnum.compare(r1, r2);
        MostSimilarClusters clusters1 = getClusters();
        ClusterDistanceItem m1 = clusters1.getBestMatch();
        MostSimilarClusters clusters2 = o2.getClusters();
        ClusterDistanceItem m2 = clusters2.getBestMatch();

        if (m1 == null || m2 == null)
            return clusters1.toString().compareTo(clusters2.toString());

        double d1 = m1.getDistance();
        double d2 = m2.getDistance();

        return CompareTo.compare(d1, d2);


    }
}
