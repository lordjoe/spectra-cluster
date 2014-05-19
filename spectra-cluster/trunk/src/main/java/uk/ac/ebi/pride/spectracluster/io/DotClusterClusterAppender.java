package uk.ac.ebi.pride.spectracluster.io;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.comparator.SpectrumIDComparator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.io.DotClusterClusterAppender
 * append to a dotclusterFile
 * User: Steve
 * Date: 9/25/13
 */
public class DotClusterClusterAppender implements IClusterAppender {


    /**
     * @param out       !null open appendale
     * @param cluster      !null cluster
     * @param otherData any other cluster - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public void appendCluster(final Appendable out, final ICluster cluster, final Object... otherData) {
        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", cluster.getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");


            List<ISpectrum> clusteredSpectra1 = cluster.getClusteredSpectra();
//            String s = ClusterUtilities.mostCommonPeptides(clusteredSpectra1);
//            out.append("sequence=[" + s + "]");
//            out.append("\n");

            out.append("consensus_mz=" + ClusterUtilities.buildMZString(cluster.getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(cluster.getConsensusSpectrum()));
            out.append("\n");

            List<ISpectrum> spectra = clusteredSpectra1;
            Collections.sort(spectra, SpectrumIDComparator.INSTANCE);   // sort by id
            for (ISpectrum spec : spectra) {
                StringBuilder sb = new StringBuilder();
                sb.append("SPEC\t");
                String id1 = spec.getId();
                while (id1.startsWith("="))
                    id1 = id1.substring(1, id1.length()); // lots of ids start with == - is that a good thing
                sb.append(id1);
                sb.append("\ttrue\n");  // changed to look at output
                String csq = sb.toString();
                out.append(csq);

            }
        } catch (IOException e) {
            throw new AppenderException(e);

        }
    }


    /**
     * add whatever happens at the start
     *
     * @param out       !null open appendale
     * @param otherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public void appendStart(final Appendable out, final Object... otherData) {
        String name = (String) otherData[0];
        ClusterUtilities.appendDotClusterHeader(out, name);
    }

    /**
     * add whatever happens at the end
     *
     * @param out       !null open appendale
     * @param otherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public void appendEnd(final Appendable out, final Object... otherData) {
    }
}
