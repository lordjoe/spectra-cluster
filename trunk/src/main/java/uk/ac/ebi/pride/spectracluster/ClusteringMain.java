package uk.ac.ebi.pride.spectracluster;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.ClusteringMain
 *
 * @author Steve Lewis
 * @date 29/05/13
 */
public class ClusteringMain {

    public static final int MAX_RECLUSTERING_TRYS = 4;

    public static void clusterFiles(String... fileNames) {
        File[] files = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            files[i] = new File(fileName);
            if (!files[i].exists())
                throw new IllegalArgumentException("File " + files[i] + " does not exist");
        }
        clusterFiles(files);
    }

    /**
     * cluster spectra from a group of existing mgf files
     * @param files
     */
    public static void clusterFiles(File... files) {
        List<IPeptideSpectrumMatch> holder = new ArrayList<IPeptideSpectrumMatch>();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            IPeptideSpectrumMatch[] sps = ParserUtilities.readMGFScans(file);
            holder.addAll(Arrays.asList(sps));
        }
        List<ISpectralCluster> scs = ClusterUtilities.asClusters(holder);
        IClusteringEngine engine = Defaults.INSTANCE.getDefaultClusteringEngine();
        engine.addClusters(scs.toArray(new ISpectralCluster[0]));

        int tryNumber = 0;
        while (tryNumber++ < MAX_RECLUSTERING_TRYS) {
            boolean done = !engine.mergeClusters(); // true if nothing done
            if (done)
                break;
        }
        List<ISpectralCluster> output = engine.getClusters();
        for (ISpectralCluster iSpectralCluster : output) {
            
        }
    }

    public static void usage() {
        System.out.println("Usage file.mgf <otherfile.mgf>");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }
        clusterFiles(args);
    }
}
