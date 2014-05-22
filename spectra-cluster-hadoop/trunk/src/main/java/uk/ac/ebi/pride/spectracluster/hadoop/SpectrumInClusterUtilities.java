package uk.ac.ebi.pride.spectracluster.hadoop;

import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * Utility methods for SpectrumInCluster
 *
 * @author Rui Wang
 * @version $Id$
 */
public final class SpectrumInClusterUtilities {

    public static final String PLACE_SPECTRUM_IN_BEST_CLUSTER = "uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster.PlaceSpectrumInBestCluster";
    public static final String BREAK_UP_CLUSTERS_LESS_THAN = "uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster.BreakUpClustersLessThan";

    private SpectrumInClusterUtilities() {
    }

    public static SpectrumInCluster readSpectrumInCluster(String str) {
        LineNumberReader rdr = new LineNumberReader(new StringReader(str));
        return readSpectrumInCluster(rdr);
    }


    public static SpectrumInCluster readSpectrumInCluster(LineNumberReader rdr) {
        //todo: make this work

//        try {
//            String line = rdr.readLine();
//            SpectrumInCluster ret = new SpectrumInCluster();
//            while (line != null) {
//                if ("=SpectrumInCluster=".equals(line.trim())) {
//                    line = rdr.readLine();
//                    break;
//                }
//            }
//
//            if (!line.startsWith("removeFromCluster="))
//                throw new IllegalStateException("badSpectrumInCluster");
//            ret.setRemoveFromCluster(Boolean.parseBoolean(line.substring("removeFromCluster=".length())));
//            line = rdr.readLine();
//
//            if (!line.startsWith("distance="))
//                throw new IllegalStateException("badSpectrumInCluster");
//            double distance = Double.parseDouble(line.substring("distance=".length()));
//            if (distance >= 0)    // todo fix later
//                ret.setDistance(distance);
//
//            IPeptideSpectrumMatch spec = ParserUtilities.readMGFScan(rdr, line);
//
//            ret.setSpectrum(spec);
//
//            IPeptideSpectralCluster[] clusters = ParserUtilities.readClustersFromClusteringFile(rdr, null);
//            if (clusters.length != 1)
//                throw new IllegalStateException("badSpectrumInCluster");
//
//            ret.setCluster(clusters[0]);
//            return ret;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }

        return null;
    }
}
