package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.cli;

/**
 * Created by jg on 07.12.14.
 */
public interface IClusterProcessor {
    public void processClusters() throws Exception;

    public void setDisableSpectraFetcher(boolean disableSpectraFetcher);
    public boolean isDisableSpectraFetcher();

    public void setOutputPath(String outputPath);
    public String getOutputPath();

    public void setIgnoreIncompleteClusters(boolean ignoreIncompleteCluster);
    public boolean isIgnoreIncompleteClusters();

    public void setIgnoreExisting(boolean ignoreExisting);
    public boolean isIgnoreExisting();
}
