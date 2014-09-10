package uk.ac.ebi.pride.spectracluster.psmassessmentextractor.assessment;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.SequenceCount;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jg on 09.09.14.
 * Calculates the reliability score for every PSM. If the PSM is found in multiple
 * clusters, the highest score is being reported.
 *
 * Scores are:
 * 1 - ratio < MIN_RATIO
 * 2 - ratio >= MIN_RATIO but cluster size < MIN_SIZE
 * 3 - reliable PSM
 */
public class PsmAssessmentExtractor implements IClusterSourceListener {
    private Map<String, Integer> psmAssessments = new HashMap<String, Integer>();
    /**
     * If multiple assessments for a given PSM are encountered, the best score
     * is kept if this boolean is set to true, otherwise the worst score is used.
     */
    private boolean usingBestScore = true;

    private int minClusterSize = DefaultReliableRequirements.MIN_RELIABLE_CLUSTER_SIZE;

    private float minSequenceRatio = DefaultReliableRequirements.MIN_RELIABLE_SEQUENCE_RATIO;

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        // process all PSMs
        for (ISpectrumReference specRef : newCluster.getSpectrumReferences()) {
            String specId = specRef.getSpectrumId();
            String sequence = specRef.getSequence();

            // get the sequence's ratio
            float ratio = getSequenceRatio(sequence, newCluster);

            // calculate the PSM assessment
            int assessment = calculatePsmAssessment(ratio, newCluster.getSpecCount());

            if (psmAssessments.containsKey(specId)) {
                if (usingBestScore) {
                    // use the best assessment
                    if (assessment > psmAssessments.get(specId))
                        psmAssessments.put(specId, assessment);
                }
                else {
                    // use the worst assessment
                    if (assessment < psmAssessments.get(specId))
                        psmAssessments.put(specId, assessment);
                }
            }
            else {
                // no assessment encountered yet
                psmAssessments.put(specId, assessment);
            }
        }
    }

    /**
     * Calculates the PSM assessment based on the sequence's ratio within the cluster
     * and the cluster's size.
     * @param ratio
     * @param clusterSize
     * @return
     */
    private int calculatePsmAssessment(float ratio, int clusterSize) {
        if (ratio < minSequenceRatio)
            return 1;

        if (clusterSize < minClusterSize)
            return 2;

        return 3;
    }

    /**
     * Calculates the ratio for a given sequence within the supplied cluster.
     * In case the sequence is not found in the cluster 0 is returned.
     * @param sequence
     * @param cluster
     * @return
     */
    private float getSequenceRatio(String sequence, ICluster cluster) {
        for (SequenceCount sequenceCount : cluster.getSequenceCounts()) {
            if (sequenceCount.getSequence().equals(sequence)) {
                return (float) sequenceCount.getCount() / (float) cluster.getSpecCount();
            }
        }

        return 0;
    }

    /**
     * Returns the PSM assessments as an unmodifiable map with the
     * PSM id as key and the assessment as value.
     * @return
     */
    public Map<String, Integer> getPsmAssessments() {
        return Collections.unmodifiableMap(psmAssessments);
    }

    /**
     * Removes all recorded PSM assessments
     */
    public void reset() {

    }

    /**
     * If set to true (default) the best PSM assessment is kept
     * in case multiple assessments are encountered for a single
     * PSM.
     * @return
     */
    public boolean isUsingBestScore() {
        return usingBestScore;
    }

    /**
     * If set to true (default) the best PSM assessment is kept
     * in case multiple assessments are encountered for a single
     * PSM.
     * @param usingBestScore
     */
    public void setUsingBestScore(boolean usingBestScore) {
        this.usingBestScore = usingBestScore;
    }

    public int getMinClusterSize() {
        return minClusterSize;
    }

    public void setMinClusterSize(int minClusterSize) {
        this.minClusterSize = minClusterSize;
    }

    public float getMinSequenceRatio() {
        return minSequenceRatio;
    }

    public void setMinSequenceRatio(float minSequenceRatio) {
        this.minSequenceRatio = minSequenceRatio;
    }
}
