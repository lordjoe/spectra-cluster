package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.io;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.IModification;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;

import java.io.FileWriter;
import java.util.List;

/**
 * Created by jg on 07.12.14.
 */
public class MgfSpectrumWriter implements ISpectrumWriter {
    @Override
    public void writeClusterSpectra(List<Spectrum> spectra, String outputFilePath, ICluster cluster) throws Exception {
        FileWriter fw = new FileWriter(outputFilePath);

        for (Spectrum s : spectra) {
            double[] mzValues = s.getPeaksMz();
            double[] intensityValues = s.getPeaksIntensities();

            if (mzValues.length != intensityValues.length) {
                throw new Exception("Spectrum " + s.getId() + " contains a different number of m/z and intensity values.");
            }

            // get the spectrum's sequence
            String spectrumTitle = generateSpectrumTitle(s, cluster);

            fw.write("BEGIN IONS\n");
            fw.write("TITLE=" + spectrumTitle + "\n");
            fw.write("PEPMASS=" + s.getPrecursorMz() + "\n");
            fw.write("CHARGE=" + s.getPrecursorCharge() + "\n");
            // write the peaks
            for (int i = 0; i < mzValues.length; i++) {
                fw.write(mzValues[i] + " " + intensityValues[i] + "\n");
            }

            fw.write("END IONS\n");
        }

        fw.close();
    }

    /**
     * Generates the MGF spectrum title for the given spectrum. This title
     * includes the original id, the PSM infos as well as the taxid.
     * @param spectrum
     * @param cluster
     * @return
     */
    private String generateSpectrumTitle(Spectrum spectrum, ICluster cluster) throws Exception {
        // add the original id as first field
        String spectrumTitle = spectrum.getId() + "|";

        ISpectrumReference spectrumReference = null;

        // find the spectrum reference for the current spectrum
        for (ISpectrumReference currentSpecRef : cluster.getSpectrumReferences()) {
            if (currentSpecRef.getSpectrumId().equals(spectrum.getId())) {
                spectrumReference = currentSpecRef;
                break; // only one spec ref to look for
            }
        }

        if (spectrumReference == null) {
            throw new Exception("Failed to retrieve spectrum reference for spectrum " + spectrum.getId());
        }

        // process all PSMs (unfortunately, there can be multiple PSMs per spectrum)
        for (IPeptideSpectrumMatch psm : spectrumReference.getPSMs()) {
            spectrumTitle += (spectrumTitle.endsWith("|") ? "" : ";") + psm.getSequence();

            // add the PSMs
            if (psm.getModifications().size() > 0) {
                spectrumTitle += "(";
            }
            for (int i = 0; i < psm.getModifications().size(); i++) {
                IModification mod = psm.getModifications().get(i);
                spectrumTitle += (i > 0 ? "," : "") + mod.getPosition() + "-" + mod.getAccession();
            }
            if (psm.getModifications().size() > 0) {
                spectrumTitle += ")";
            }
        }

        // add the taxonomy
        spectrumTitle += ";taxid=" + spectrumReference.getSpecies();

        return spectrumTitle;
    }
}
