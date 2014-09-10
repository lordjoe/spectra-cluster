package uk.ac.ebi.pride.spectracluster.psmassessmentextractor.assessment;

/**
 * Created by jg on 09.09.14.
 * This class defines the default requirements for a PSM
 * to be defined as reliable.
 */
public class DefaultReliableRequirements {
    /**
     * Private constructor to prevent this class to be instantiated.
     */
    private DefaultReliableRequirements() {

    }

    /**
     * According to our Nat. Meth. paper a minimum of 10 spectra within
     * a cluster is required to define a PSM as reliable
     */
    public static final int MIN_RELIABLE_CLUSTER_SIZE = 10;

    /**
     * According to our Nat. Meth. paper a minimum ratio of 0.7 is
     * required to define a PSM as reliable.
     */
    public static final float MIN_RELIABLE_SEQUENCE_RATIO = 0.7F;
}
