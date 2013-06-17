package uk.ac.ebi.pride.spectracluster;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.similarity.*;
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
     *
     * @param files
     */
    public static void clusterFiles(File... files) {
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            ISpectrum[] sps = ParserUtilities.readMGFScans(file);
            holder.addAll(Arrays.asList(sps));
        }
        List<ISpectralCluster> scs = ClusterUtilities.asClusters(holder);
        IClusteringEngine newEngine  = Defaults.INSTANCE.getDefaultClusteringEngine();
        newEngine.addClusters(scs.toArray(new ISpectralCluster[scs.size()]));

        IClusteringEngine oldengine = new ClusteringEngine(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        oldengine.addClusters(scs.toArray(new ISpectralCluster[scs.size()]));

        IClusteringEngine engine = oldengine; // test the old engine

        long start = System.currentTimeMillis();
        long lap = System.currentTimeMillis();
        int tryNumber = 0;
        while (tryNumber++ < MAX_RECLUSTERING_TRYS) {
            boolean done = !engine.mergeClusters(); // true if nothing done
            if (done)
                break;
            double delCycle = (System.currentTimeMillis() - lap) / 1000.0;

            System.out.println("Loop took " + (int) delCycle + " sec");
            lap = System.currentTimeMillis();
        }
        long endNewEngine = System.currentTimeMillis();
        double delSec = (endNewEngine - start) / 1000.0;

        System.out.println("Clustering took " + (int) delSec + " sec");
        List<ISpectralCluster> output = engine.getClusters();

//        for (ISpectralCluster sc : output) {
//
//        }
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
