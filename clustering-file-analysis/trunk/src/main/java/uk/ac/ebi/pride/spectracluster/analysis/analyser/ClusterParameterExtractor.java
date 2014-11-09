package uk.ac.ebi.pride.spectracluster.analysis.analyser;

import uk.ac.ebi.pride.spectracluster.analysis.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.SequenceCount;

/**
 * Created by jg on 14.07.14.
 *
 * Extracts basic properties of every cluster (size,
 * max ratio, precursor_mz, precursor_intens) and returns
 * them as a tab-delimited table.
 */
public class ClusterParameterExtractor extends AbstractClusteringSourceAnalyser {
    public static String FILE_ENDING = ".cluster_parameters.tsv";
    public static String DESCRIPTION = "Extracts the basic properties of every cluster in the file and returns them in a TAB delimited table.";

    StringBuffer resultStringBuffer = new StringBuffer();
    public final char DELIMINATOR = '\t';
    public final String TABLE_HEADER=
                    "id" + DELIMINATOR +
                    "precusor_mz" + DELIMINATOR +
                    "precursor_intensity" + DELIMINATOR +
                    "size" + DELIMINATOR +
                    "max_ratio" + DELIMINATOR +
                    "max_il_ratio" + DELIMINATOR +
                    "precursor_mz_range" + DELIMINATOR +
                    "sequences" + DELIMINATOR + "\n";


    @Override
    public String getFileEnding() {
        return FILE_ENDING;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getAnalysisResultString() {
        return TABLE_HEADER + resultStringBuffer.toString();
    }

    @Override
    public void reset() {
        resultStringBuffer = new StringBuffer();
    }

    @Override
    public void onNewClusterRead(ICluster newCluster) {
        if (ignoreCluster(newCluster))
            return;

        // build the sequence string
        StringBuilder sequenceString = new StringBuilder();
        for (SequenceCount sequenceCount : newCluster.getSequenceCounts()) {
            if (sequenceString.length() > 0)
                sequenceString.append(",");

            sequenceString.append(sequenceCount.getSequence() + ":" + sequenceCount.getCount());
        }

        // add the string representing the cluster to the result buffer
        resultStringBuffer.append(String.format(
                "%s%c%.3f%c%.3f%c%d%c%.3f%c%.3f%c%.3f%c%s\n",
                newCluster.getId(), DELIMINATOR,
                newCluster.getAvPrecursorMz(), DELIMINATOR,
                newCluster.getAvPrecursorIntens(), DELIMINATOR,
                newCluster.getSpecCount(), DELIMINATOR,
                newCluster.getMaxRatio(), DELIMINATOR,
                ClusterUtilities.getClusterILAgnosticMaxRatio(newCluster), DELIMINATOR,
                newCluster.getSpectrumPrecursorMzRange(), DELIMINATOR,
                sequenceString.toString()
        ));
    }
}
