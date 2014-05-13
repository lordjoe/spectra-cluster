package uk.ac.ebi.pride.spectracluster.cluster;

import java.io.IOException;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ClusteringHeader
 *
 * @author Steve Lewis
 * @date 14/11/13
 */
public class ClusteringHeader {
    private final String name;//  name=Frank et al.@R4
    private final String similarity_method; // Noramlized dot product (FrankEtAlDotProduct)
    private final String threshold; // =0.7
    private final String fdr; // =0.02
    private final String description; // =310.0 +-10.0 m

    public ClusteringHeader(String name, String similarity_method, String threshold, String fdr, String description) {
        this.name = name;
        this.similarity_method = similarity_method;
        this.threshold = threshold;
        this.fdr = fdr;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFdr() {
        return fdr;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getSimilarity_method() {
        return similarity_method;
    }

    public void appendHeader(Appendable out) {
        try {
            out.append("name=" + getName() + "\n");
            out.append("similarity_method=" + getSimilarity_method() + "\n");
            out.append("threshold=" + getThreshold() + "\n");
            out.append("fdr=" + getFdr() + "\n");
            out.append("description=" + getDescription() + "\n");
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb);
        return sb.toString();
    }
}
