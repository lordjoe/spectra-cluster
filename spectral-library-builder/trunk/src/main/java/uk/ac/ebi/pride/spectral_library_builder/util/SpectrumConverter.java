package uk.ac.ebi.pride.spectral_library_builder.util;

import uk.ac.ebi.pride.spectracluster.normalizer.IIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.normalizer.TotalIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.Peak;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;

import java.util.*;

/**
 * Created by jg on 05.12.14.
 */
public class SpectrumConverter {
    public final static IIntensityNormalizer intensityNormalizer = new TotalIntensityNormalizer(1000);
    public final static IQualityScorer qualityScorer = new SignalToNoiseChecker();

    // labels of the available spectrum properties
    public final static String PROPERTY_PROJECT = "project";
    public final static String PROPERTY_ASSAY = "assay";
    public final static String PROPERTY_PSM_STRING = "psm_string";
    public final static String PROPERTY_TAXID = "taxid";

    /**
     * This class only provides static functions to convert
     * Spectrum objects.
     */
    private SpectrumConverter() {}

    public static List<IPeak> asPeaks(Spectrum spectrum) {
        List<IPeak> peaks = new ArrayList<IPeak>(spectrum.getPeakList().size());

        for (Double mz : spectrum.getPeakList().keySet()) {
            peaks.add(new Peak(mz.floatValue(), spectrum.getPeakList().get(mz).floatValue()));
        }

        return peaks;
    }

    public static ISpectrum asClusteringSpectrum(Spectrum spectrum) throws Exception {
        // normalize the peaks
        List<IPeak> normalizedPeaks = intensityNormalizer.normalizePeaks(SpectrumConverter.asPeaks(spectrum));

        // remove all empty peaks
        List<IPeak> emptyPeaks = new ArrayList<IPeak>();
        for (IPeak p : normalizedPeaks) {
            if (p.getMz() == 0 || p.getIntensity() == 0) {
                emptyPeaks.add(p);
            }
        }
        normalizedPeaks.removeAll(emptyPeaks);

        // create the new spectrum object
        ISpectrum clusteringSpectrum = new uk.ac.ebi.pride.spectracluster.spectrum.Spectrum(
                spectrum.getId(), spectrum.getPrecursorCharge(), spectrum.getPrecursorMZ().floatValue(),
                qualityScorer, normalizedPeaks);

        // extract spectrum properties from title
        // PRD000127;PRIDE_Exp_Complete_Ac_9808.xml;spectrum=5308|EVPVAGADR(1-MOD:00420)
        ParamGroup pg = spectrum.getAdditional();
        String title = pg.getCvParams().get(0).getValue();

        // remove the duplication from the title (we use some illegal characters)
        int secondTitleIndex = title.indexOf("TITLE=", 6);
        if (secondTitleIndex >= 0) {
            title = title.substring(secondTitleIndex);
        }

        // remove the TITLE= start
        title = title.substring(6);

        int indexSeparator = title.indexOf('|');
        if (indexSeparator < 0) {
            throw new Exception("Invalid spectrum title encountered: " + title);
        }

        String archiveIdString = title.substring(0, indexSeparator - 1);

        // extract the project and accession id
        String[] archiveIdFields = archiveIdString.split(";");
        if (archiveIdFields.length != 3) {
            throw new Exception("Invalid number of fields in archive id string in " + title);
        }
        clusteringSpectrum.setProperty(PROPERTY_PROJECT, archiveIdFields[0]);
        clusteringSpectrum.setProperty(PROPERTY_ASSAY, archiveIdFields[1]);

        // only store the PSM string
        String psmString = title.substring(indexSeparator + 1);
        clusteringSpectrum.setProperty(PROPERTY_PSM_STRING, psmString);

        // get the taxid
        int taxidIndex = title.indexOf("taxid=");
        if (taxidIndex < 0) {
            throw new Exception("Missing taxid in title: " + title);
        }
        clusteringSpectrum.setProperty(PROPERTY_TAXID, title.substring(taxidIndex + 6));

        return clusteringSpectrum;
    }

    public static Set<PSM> extractSpectrumPsms(ISpectrum spectrum) {
        String psmString = spectrum.getProperty(SpectrumConverter.PROPERTY_PSM_STRING);

        if (psmString == null) {
            throw new IllegalStateException("Spectrum without PSM_STRING encountered");
        }

        // process all PSM definitions
        String psmStrings[] = psmString.split(";");
        List<PSM> psms = new ArrayList<PSM>(psmStrings.length);

        for (String currentPsmString : psmStrings) {
            if (currentPsmString.startsWith("taxid=")) {
                continue;
            }

            int modDefinitionStart = currentPsmString.indexOf('(');

            // if there are no modifications set, just add the sequence
            if (modDefinitionStart < 0) {
                psms.add(new PSM(currentPsmString, null, 1));
                continue;
            }

            // create the new PTM including the mods
            String sequence = currentPsmString.substring(0, modDefinitionStart);
            String[] ptmStrings = currentPsmString.substring(modDefinitionStart + 1, currentPsmString.length() - 1).split(",");
            Set<PSM.PTM> ptms = new HashSet<PSM.PTM>(ptmStrings.length);

            for (String ptmString : ptmStrings) {
                int separatorIndex = ptmString.indexOf('-');
                if (separatorIndex < 0) {
                    throw new IllegalStateException("Invalid PTM defintion encountered: " + ptmString);
                }

                int position = Integer.parseInt(ptmString.substring(0, separatorIndex));
                String accessison = ptmString.substring(separatorIndex + 1);

                ptms.add(new PSM.PTM(position, accessison));
            }

            psms.add(new PSM(sequence, ptms, 1));
        }

        // combine duplicate PSMs and increment their count
        Map<PSM, Integer> countPerPsm = new HashMap<PSM, Integer>();
        for (PSM psm : psms) {
            if (!countPerPsm.containsKey(psm)) {
                countPerPsm.put(psm, 0);
            }

            countPerPsm.put(psm, countPerPsm.get(psm) + 1);
        }

        // update the counts
        for (PSM psm : countPerPsm.keySet()) {
            psm.setCount( countPerPsm.get(psm) );
        }

        return countPerPsm.keySet();
    }

    public static boolean isSpectrumIdentifiedAsSequence(ISpectrum spectrum, String sequence) {
        Set<PSM> psms = extractSpectrumPsms(spectrum);
        sequence = sequence.replaceAll("I", "L");

        for (PSM psm : psms) {
            if (psm.getSequence().replaceAll("I", "L").equals(sequence)) {
                return true;
            }
        }

        return false;
    }
}
