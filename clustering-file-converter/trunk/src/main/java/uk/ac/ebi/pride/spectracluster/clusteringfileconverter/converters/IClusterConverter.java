package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

/**
 * Created by jg on 01.08.14.
 */
public interface IClusterConverter {
    public String convertCluster(ICluster cluster);
}
