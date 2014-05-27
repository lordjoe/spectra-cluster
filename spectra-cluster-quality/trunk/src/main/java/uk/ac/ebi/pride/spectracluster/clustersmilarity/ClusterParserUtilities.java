package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.Peak;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterParserUtilities {

    public static final String BEGIN_CLUSTERING = "=Cluster=";
    public static final String AVERAGE_PRECURSOR_MZ = "av_precursor_mz=";
    public static final String AVERAGE_PRECURSOR_INTENSITY = "av_precursor_intens=";
    public static final String PEPTIDE_SEQUENCE = "sequence=";
    public static final String CONSENSUS_MZ = "consensus_mz=";
    public static final String CONSENSUS_INTENSITY = "consensus_intens=";
    public static final String SPECTRUM_ID = "SPEC";

    /**
     * read a header - should leave the reader in a form where the
     * rest of the fine can be read
     *
     * @param inp
     * @return
     */
    public static ClusteringHeader readClusterHeader(LineNumberReader inp) {
        int MAX_LOOKAHEAD = 100000;
        try {
            String name = null;
            String similarity_method = null;
            String threshold = null;
            String fdr = null;
            String description = null;
            inp.mark(MAX_LOOKAHEAD);
            String line = inp.readLine();
            while (line != null) {
                if (line.startsWith("name")) {
                    name = line.replace("name=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("similarity_method")) {
                    similarity_method = line.replace("similarity_method=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("fdr")) {
                    fdr = line.replace("fdr=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("threshold")) {
                    threshold = line.replace("threshold=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("description")) {
                    description = line.replace("description=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                inp.reset(); // not anything we want do go to the previous mark
                break; // I guess we are done
            }

            ClusteringHeader ret = new ClusteringHeader(
                    name, similarity_method, threshold, fdr, description
            );
            inp.reset();
            return ret;
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Read clustering file into a set of clusters
     *
     * @param inp
     * @return
     */
    public static IPeptideSpectralCluster[] readClustersFromClusteringFile(LineNumberReader inp, ISpectrumRetriever spectrumRetriever) {
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();

        try {
            String line = inp.readLine();
            while (line != null && !line.startsWith(BEGIN_CLUSTERING)) {
                line = inp.readLine();
            }


            List<String> clusterContent = new ArrayList<String>();
            while (line != null) {
                if (line.startsWith(BEGIN_CLUSTERING)) {
                    if (!clusterContent.isEmpty()) {
                        IPeptideSpectralCluster cluster = processIntoCluster(clusterContent, spectrumRetriever);
                        if (cluster != null) {
                            holder.add(cluster);
                        }
                    }
                    clusterContent.clear();
                } else {
                    clusterContent.add(line);
                }

                line = inp.readLine();
            }

            if (!clusterContent.isEmpty()) {
                IPeptideSpectralCluster cluster = processIntoCluster(clusterContent, spectrumRetriever);
                if (cluster != null) {
                    holder.add(cluster);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to read ", ioe);
        }

        IPeptideSpectralCluster[] ret = new IPeptideSpectralCluster[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected static IPeptideSpectralCluster processIntoCluster(List<String> clusterLines, ISpectrumRetriever spectrumRetriever) {

        LazyLoadedSpectralCluster cluster = new LazyLoadedSpectralCluster();

        String consensusMzLine = null;
        String consensusIntensityLine = null;
        for (String clusterLine : clusterLines) {
            if (clusterLine.length() == 0)
                break;
            if (clusterLine.startsWith("name=")) {
                break; // start of a new file
            }
            if (clusterLine.startsWith(AVERAGE_PRECURSOR_MZ)) {
                float precursorMz = Float.parseFloat(clusterLine.replace(AVERAGE_PRECURSOR_MZ, ""));
                cluster.setPrecursorMz(precursorMz);
            } else if (clusterLine.startsWith(CONSENSUS_MZ)) {
                consensusMzLine = clusterLine.replace(CONSENSUS_MZ, "");
            } else if (clusterLine.startsWith(CONSENSUS_INTENSITY)) {
                consensusIntensityLine = clusterLine.replace(CONSENSUS_INTENSITY, "");
            } else if (clusterLine.startsWith(PEPTIDE_SEQUENCE)) {
                String peptideSequence = clusterLine.replace(PEPTIDE_SEQUENCE, "");
                peptideSequence = peptideSequence.replace("[", "").replace("]", "");
                cluster.addPeptides(peptideSequence);
            } else if (clusterLine.startsWith(SPECTRUM_ID)) {
                String[] parts = clusterLine.split("\t");
                //     IPeptideSpectrumMatch spectrum = PSMSpectrum.getSpectrum(id );
                LazyLoadedSpectrum spectrum = new LazyLoadedSpectrum(parts[1], spectrumRetriever);
                cluster.addSpectra(spectrum);
            } else //noinspection StatementWithEmptyBody
                if (clusterLine.startsWith(AVERAGE_PRECURSOR_INTENSITY)) {
                    // do nothing here
                } else {
                    if (clusterLine.length() > 0) {
                        throw new IllegalArgumentException("cannot process line " + clusterLine);
                    }
                }
        }

        if (consensusIntensityLine == null)
            return null;

        List<IPeak> peaks = buildPeaks(consensusMzLine, consensusIntensityLine);
        if (peaks == null)
            return null;
        PeptideSpectrumMatch consensusSpectrum = new PeptideSpectrumMatch(null, null, 0, cluster.getPrecursorMz(), peaks, Defaults.getDefaultQualityScorer(),null);
        cluster.setConsensusSpectrum(consensusSpectrum);

        return cluster;
    }

    public static List<IPeak> buildPeaks(String commaDelimitecMZ, String commaDelimitedIntensity) {
        try {
            float[] mzValues = parseCommaDelimitedFloats(commaDelimitecMZ);
            float[] intensityValues = parseCommaDelimitedFloats(commaDelimitedIntensity);
            if (mzValues.length != intensityValues.length)
                throw new IllegalArgumentException("Unequal mz and intensity lists");
            List<IPeak> holder = new ArrayList<IPeak>();
            for (int i = 0; i < intensityValues.length; i++) {
                holder.add(new Peak(mzValues[i], intensityValues[i]));
            }
            Collections.sort(holder);  // sort peaks by mz
            return holder;
        } catch (RuntimeException e) {
            return null;
        }
    }

    protected static float[] parseCommaDelimitedFloats(String commaDelimitedFloats) {
        String[] items = commaDelimitedFloats.trim().split(",");
        float[] ret = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            ret[i] = Float.parseFloat(item);
        }
        return ret;
    }
}
