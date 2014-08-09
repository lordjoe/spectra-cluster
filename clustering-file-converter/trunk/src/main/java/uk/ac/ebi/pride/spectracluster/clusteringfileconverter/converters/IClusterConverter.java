package uk.ac.ebi.pride.spectracluster.clusteringfileconverter.converters;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

/**
 * Created by jg on 01.08.14.
 */
public interface IClusterConverter extends IClusterSourceListener {
    public void setOutputPath(String outputPath);
    public String getOuputPath();

    public String getFileHeader();

    public String getFiletypeExtension();

    public String convertCluster(ICluster cluster);

    public void close() throws Exception;

    public void setMinSize(int minSize);
    public void setMaxSize(int maxSize);
    public void setMinRatio(float minRatio);
    public void setMaxRatio(float maxRatio);

    public int getMinSize();
    public int getMaxSize();
    public float getMinRatio();
    public float getMaxRatio();
}
