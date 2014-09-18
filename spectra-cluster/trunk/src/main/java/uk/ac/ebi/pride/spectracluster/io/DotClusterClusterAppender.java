package uk.ac.ebi.pride.spectracluster.io;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties;
import uk.ac.ebi.pride.spectracluster.util.*;
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
     * @param cluster   !null cluster
     * @param otherData any other cluster - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public void appendCluster(final Appendable out, final ICluster cluster, final Object... otherData) {
        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=").append(String.format("%10.3f", cluster.getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");


            String s = ClusterUtilities.mostCommonPeptides(cluster);
            out.append("sequence=[" + s + "]");
            out.append("\n");

            List<ISpectrum> clusteredSpectra1 = cluster.getClusteredSpectra();
            out.append("consensus_mz=").append(SpectrumUtilities.buildMZString(cluster.getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=").append(SpectrumUtilities.buildIntensityString(cluster.getConsensusSpectrum()));
            out.append("\n");

            ISimilarityChecker defaultSimilarityChecker = Defaults.getDefaultSimilarityChecker();
            Collections.sort(clusteredSpectra1, SpectrumIDComparator.INSTANCE);   // sort by id
            for (ISpectrum spec : clusteredSpectra1) {
                StringBuilder sb = new StringBuilder();
                sb.append("SPEC\t");
                String id1 = spec.getId();
                while (id1.startsWith("="))
                    id1 = id1.substring(1, id1.length()); // lots of ids start with == - is that a good thing
                sb.append(id1);
                sb.append("\ttrue");  // changed to look at output

                // append peptide sequence as a extra column
                String peptideSequence = spec.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY);
                sb.append("\t");
                if (peptideSequence != null) {
                    sb.append(peptideSequence);
                }

                // append precursor m/z as an extra column
                float precursorMz = spec.getPrecursorMz();
                sb.append("\t");
                sb.append(precursorMz);

                // append precursor charge as an extra column
                int precursorCharge = spec.getPrecursorCharge();
                sb.append("\t");
                sb.append(precursorCharge);

                // append species
                String species = spec.getProperty(KnownProperties.TAXONOMY_KEY);
                sb.append("\t");
                if (species != null) {
                    sb.append(species);
                }

                // append modifications
                String modifications = spec.getProperty(KnownProperties.MODIFICATION_KEY);
                sb.append("\t");
                if (modifications != null) {
                    sb.append(modifications);
                }

                // append similarity score between consensus spectrum and spectrum
                double similarity = defaultSimilarityChecker.assessSimilarity(cluster.getConsensusSpectrum(), spec);
                sb.append("\t");
                sb.append(similarity);

                sb.append("\n");

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
        appendDotClusterHeader(out, name);
    }

    /**
     * write the header of a .clustering file
     *
     * @param out
     * @param name
     */
    public void appendDotClusterHeader(Appendable out, String name) {

        if (name.endsWith(ParserUtilities.CLUSTERING_EXTENSION))
            name = name.substring(0, name.length() - ParserUtilities.CLUSTERING_EXTENSION.length());
        try {
            out.append("name=").append(name);
            out.append("\n");
            ISimilarityChecker similarityChecker = Defaults.getDefaultSimilarityChecker();

            Class<? extends ISimilarityChecker> scc = similarityChecker.getClass();
            out.append("similarity_method=").append(scc.getSimpleName());
            out.append("\n");


            double defaultSimilarityThreshold = Defaults.getSimilarityThreshold();

            out.append("version=").append(Version.version).append("\n");
            out.append("threshold=").append(String.valueOf(defaultSimilarityThreshold));
                out.append("\n");
            out.append("fdr=0");
            out.append("\n");
            out.append("description=").append(name);
            out.append("\n");
            out.append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
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
