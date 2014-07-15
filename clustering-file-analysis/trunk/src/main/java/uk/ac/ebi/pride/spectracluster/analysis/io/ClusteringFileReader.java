package uk.ac.ebi.pride.spectracluster.analysis.io;

import uk.ac.ebi.pride.spectracluster.analysis.objects.ClusteringFileCluster;
import uk.ac.ebi.pride.spectracluster.analysis.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.analysis.objects.SequenceCount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jg on 10.07.14.
 */
public class ClusteringFileReader implements IClusterSourceReader {
    private final File clusteringFile;
    private BufferedReader br;
    private final Pattern sequenceCountPattern = Pattern.compile("([A-Za-z]+):(\\d+),?");
    private boolean inCluster = false;

    public ClusteringFileReader(File clusteringFile) {
        this.clusteringFile = clusteringFile;
    }

    @Override
    public Collection<ICluster> readAllClusters() throws Exception {
        // always reopen the file
        if (br != null) {
            br.close();
            inCluster = false;
        }

        br = new BufferedReader(new FileReader(clusteringFile));

        List<ICluster> clusters = new ArrayList<ICluster>();
        ICluster cluster;

        while ((cluster = readNextCluster(br)) != null)
            clusters.add(cluster);

        br.close();
        br = null;
        inCluster = false;

        return clusters;
    }

    @Override
    public boolean supportsReadAllClusters() {
        return true;
    }

    @Override
    public void readClustersIteratively(Collection<IClusterSourceListener> listeners) throws Exception {
        if (br == null) {
            br = new BufferedReader(new FileReader(clusteringFile));
            inCluster = false;
        }

        ICluster cluster;

        while ((cluster = readNextCluster(br)) != null) {
            for (IClusterSourceListener listener : listeners)
                listener.onNewClusterRead(cluster);
        }
    }

    private ICluster readNextCluster(final BufferedReader br) throws Exception {
        float avPrecursorMz = 0, avPrecursorIntens = 0;
        List<SequenceCount> sequenceCounts = new ArrayList<SequenceCount>();
        String line;
        List<String> spectrumIds = new ArrayList<String>();


        while((line = br.readLine()) != null) {
            if (line.trim().equals("=Cluster=")) {
                // if we're already in a cluster, the current cluster is complete
                if (inCluster) {
                    // create the cluster and return
                    ICluster cluster = new ClusteringFileCluster(avPrecursorMz, avPrecursorIntens, sequenceCounts, spectrumIds);

                    return cluster;
                }
                else {
                    // this means that this is the start of the first cluster in the file
                    inCluster = true;
                }
            }

            if (line.startsWith(("av_precursor_mz="))) {
                avPrecursorMz = Float.parseFloat(line.trim().substring(16));
            }

            if (line.startsWith("av_precursor_intens=")) {
                avPrecursorIntens = Float.parseFloat(line.trim().substring(20));
            }

            if (line.startsWith("sequence=")) {
                line = line.trim();
                String value = line.substring(10, line.length() - 1); // remove '[' and ']'
                Matcher sequenceCountMatcher = sequenceCountPattern.matcher(value);

                while (sequenceCountMatcher.find()) {
                    String sequence = sequenceCountMatcher.group(1);
                    int count = Integer.parseInt(sequenceCountMatcher.group(2));
                    sequenceCounts.add(new SequenceCount(sequence, count));
                }
            }

            if (line.startsWith("SPEC")) {
                String[] fields = line.trim().split("\t");
                if (fields.length != 4) {
                    throw new Exception("Illegal spectrum line encountered: " + line);
                }

                spectrumIds.add(fields[1]);
            }
        }

        if (inCluster && sequenceCounts.size() > 0 && avPrecursorMz > 0) {
            // create the cluster and return
            ICluster cluster = new ClusteringFileCluster(avPrecursorMz, avPrecursorIntens, sequenceCounts, spectrumIds);
            inCluster = false;

            return cluster;
        }

        // we're done, so return
        return null;
    }
}
