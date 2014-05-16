package uk.ac.ebi.pride.spectracluster.cluster;

import com.lordjoe.algorithms.CountedString;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Rui Wang
 * @version $Id$
 *
 */
public class PeptideSpectralCluster extends SpectralCluster implements IPeptideSpectralCluster {


    private final List<ClusterPeptideFraction> byPurity = new ArrayList<ClusterPeptideFraction>();


    public PeptideSpectralCluster(IPeptideSpectralCluster copied) {
        super(copied);
    }

    /**
     * use this when the cluster is not stable
     */
    public PeptideSpectralCluster() {
        super();
    }

    public PeptideSpectralCluster(String id) {
        super(id);
    }

    public PeptideSpectralCluster(String id, IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        super(id, consensusSpectrumBuilder);
    }

    @Override
    public List<String> getPeptides() {
        String[] peptides = ClusterUtilities.getMostCommonPeptides(this);

        return new ArrayList<String>(Arrays.asList(peptides));
    }

    @Override
    public String getMostCommonPeptide() {
        List<String> peptideStrings = getPeptides();
        if (!peptideStrings.isEmpty()) {
            return peptideStrings.get(0);
        }
        return null;
    }


    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
    public
    @Nonnull
    List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator dd) {
        buildPeptidePurities(dd);
        return byPurity;

    }


    protected void buildPeptidePurities(IDecoyDiscriminator dd) {
        if (byPurity.size() > 0)
            return;
        byPurity.clear();
        double numberSpectra = getClusteredSpectraCount();
        CountedString[] items = CountedString.getCountedStrings(getPeptides());
        for (int i = 0; i < items.length; i++) {
            CountedString item = items[i];
            String value = item.getValue();
            String[] peptides = value.split(";");
            boolean decoy = false;
            for (int j = 0; j < peptides.length; j++) {
                String peptide = peptides[j];
                if (peptide != null && dd != null)
                    decoy |= dd.isDecoy(peptide);
                else
                    decoy = false; // break here
            }

            ClusterPeptideFraction e = new ClusterPeptideFraction(peptides[0], item.getCount() / numberSpectra, decoy);
            byPurity.add(e);
        }
    }

    @Override
    public String toString() {
        double precursorMZ = getPrecursorMz();
        String text =
                "charge= " + getPrecursorCharge() + "," +
                        "mz= " + String.format("%10.3f", precursorMZ).trim() + "," +
                        "count= " + getClusteredSpectra().size() +
                        "peptide= " + ClusterUtilities.mostCommonPeptides(this) +
                        ", spectrum = ";
        for (ISpectrum s : getClusteredSpectra())
            text += s.getId() + ",";

        text = text.substring(0, text.length() - 1);
        return text;
    }
}
