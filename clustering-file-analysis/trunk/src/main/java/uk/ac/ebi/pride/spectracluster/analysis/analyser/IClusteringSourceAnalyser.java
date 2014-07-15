package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.analysis.io.IClusterSourceReader;

/**
 * Created by jg on 12.07.14.
 */
public interface IClusteringSourceAnalyser extends IClusterSourceListener {
    public String getAnalysisResultString();

    public void reset();

    public String getFileEnding();

    public String getDescription();
}
