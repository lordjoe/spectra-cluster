package uk.ac.ebi.pride.spectral_library_builder.converter;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectral_library_builder.util.PSM;
import uk.ac.ebi.pride.spectral_library_builder.util.ParameterExtractor;
import uk.ac.ebi.pridemod.ModReader;
import uk.ac.ebi.pridemod.model.PTM;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jg on 07.12.14.
 */
public class MspConsensusSpectrumConverter implements IConsensusSpectrumConverter {
    public final static String FILE_EXTENSION = "msp";
    public final static Double MOD_TOLERANCE = 0.01;

    private Map<String, Double> modToDeltaMap = new HashMap<String, Double>();
    private ModReader modReader = ModReader.getInstance();

    public MspConsensusSpectrumConverter() {
        loadModToDeltaMap();
    }

    private void loadModToDeltaMap() {
        modToDeltaMap.put("ICAT_light", 227.126991);
        modToDeltaMap.put("ICAT-C", 227.126991); // PSI new name

        modToDeltaMap.put("ICAT_heavy", 236.157185);
        modToDeltaMap.put("ICAT-C:13C(9)", 236.157185); // PSI new name

        modToDeltaMap.put("AB_old_ICATd0", 442.224991);
        modToDeltaMap.put("ICAT-D", 442.224991); // PSI new name

        modToDeltaMap.put("AB_old_ICATd8", 450.275205);
        modToDeltaMap.put("ICAT-D:2H(8)", 450.275205); // PSI new name


        modToDeltaMap.put("Carbamidomethyl", 57.021464);

        modToDeltaMap.put("Carboxymethyl", 58.005479);

        modToDeltaMap.put("Propionamide", 71.037114); // alkylation of acrylamide to cysteines
        modToDeltaMap.put("Propionamide:2H(3)", 74.055944); // alkylation of heavy acrylamide to cysteines
        modToDeltaMap.put("Propionamide:13C(3)", 74.047178); // alkylation of heavy acrylamide to cysteines

        modToDeltaMap.put("Oxidation", 15.99491);

        modToDeltaMap.put("Acetyl", 42.010565); // acetylation of N terminus

        modToDeltaMap.put("Deamidation", 0.984016);
        modToDeltaMap.put("Deamidated", 0.984016); // PSI new name

        modToDeltaMap.put("Pyro-cmC", 39.994915); // cyclicization of N-terminal CAM-cysteine (FIXED value 01/27/07)
        modToDeltaMap.put("Pyro-carbamidomethyl", 39.994915); // PSI new name

        modToDeltaMap.put("Pyro-glu", -17.026549); // loss of NH3 from glutamine
        modToDeltaMap.put("Gln->pyro-Glu", -17.026549); // PSI new name

        modToDeltaMap.put("Pyro_glu", -18.010565); // loss of H2O from glutamic acid
        modToDeltaMap.put("Glu->pyro-Glu", -18.010565); // PSI new name

        modToDeltaMap.put("Amide", -0.984016); // amidation of C terminus
        modToDeltaMap.put("Amidated", -0.984016); // PSI new name

        modToDeltaMap.put("Phospho", 79.966331); // phosphorylation

        modToDeltaMap.put("Thiophospho", 95.943487); // phosphorylation

        modToDeltaMap.put("Sulfo", 79.956815); // O-sulfonation

        modToDeltaMap.put("Methyl", 14.015650); // methylation

        //  modToDeltaMap.put("Deimination", 0.984016); // deamidation on R

        modToDeltaMap.put("Carbamyl", 43.005814); // carbamylation of N terminus or lysines

        modToDeltaMap.put("iTRAQ4plex", 144.102063); // iTRAQ 4-plex

        modToDeltaMap.put("iTRAQ4plexAcetyl", 186.112628); // iTRAQ 4-plex

        modToDeltaMap.put("iTRAQ8plex:13C(6)15N(2)", 304.19904); // iTRAQ on N terminus or K
        modToDeltaMap.put("iTRAQ8plex", 304.20536); // iTRAQ on N terminus or K


        modToDeltaMap.put("TMT6plex", 229.162932); // TMT 6-plex

        modToDeltaMap.put("PEO-Iodoacetyl-LC-Biotin", 414.52); // Hui Zhang's PEO alkylation agent on cysteines

        modToDeltaMap.put("Label:2H(3)", 3.018830); // SILAC heavy leucine (+3)

        modToDeltaMap.put("Label:2H(4)", 4.025107); // Lys4 label (+4)

        modToDeltaMap.put("Label:13C(6)", 6.020129); // SILAC heavy lysine and arginine (+6)
        modToDeltaMap.put("Label:13C(6)15N(1)", 7.017165);
        modToDeltaMap.put("Label:13C(6)15N(2)", 8.014199); // SILAC heavy lysine (+8)
        modToDeltaMap.put("Label:13C(6)15N(3)", 9.011235);
        modToDeltaMap.put("Label:13C(6)15N(4)", 10.008269); // SILAC heavy arginine (+10)

        modToDeltaMap.put("Methylthio", 45.987721); // methylthiolated cysteine (cys blocking by MMTS)

        modToDeltaMap.put("Leucyl", 113.08406); // leucine added to N-term or K
        modToDeltaMap.put("Leucyl:13C(6)15N(1)", 120.101224); // heavy leucine added to N-term or K


        modToDeltaMap.put("Nitro", 44.985078);
        modToDeltaMap.put("Dimethyl", 28.031300);
        modToDeltaMap.put("Trimethyl", 42.046950);

        modToDeltaMap.put("Bromo", 77.910511);

        // Ubl chains
        modToDeltaMap.put("SUMO_1", 2135.920495); // SUMO-1 Tryptic/LysC tail
        modToDeltaMap.put("SUMO_2_3_Tryp", 3549.536567); // SUMO-2/3 Tryptic tail
        modToDeltaMap.put("Smt3_R93A_Tryp", 3812.747563); // Smt3_R93A Tryptic tail
        modToDeltaMap.put("Smt3_R93A_LysC", 4544.074787); // Smt3_R93A LysC tail
        modToDeltaMap.put("NEDD8_LysC", 1555.956231); // NEDD8 LysC tail
        modToDeltaMap.put("Rub1_LysC", 2454.341699); // Rub1 LysC tail
        modToDeltaMap.put("Ub_LysC", 1431.831075); // Ubiquitin LysC tail
        modToDeltaMap.put("GlyGly", 114.042927); // Ubiquitin/NEDD8 Tryptic tail (2 glycines)

        // added based on PSI-MOD entries
        modToDeltaMap.put("Formyl", 27.994915);
    }

    @Override
    public String convertConsensusSpectrum(ISpectrum spectrum, ParameterExtractor parameterExtractor) {
        StringBuilder mspString = new StringBuilder();
        PSM maxPsm = parameterExtractor.getMaximumPsm();

        mspString.append("Name: ").append(generateClusterName(maxPsm, spectrum)).append("\n");
        mspString.append("Comment: ").append(generateComments(maxPsm, spectrum, parameterExtractor)).append("\n");

        // count the peaks ignoring any peaks with 0 m/z or intensity
        int nPeaks = 0;
        for (IPeak peak : spectrum.getPeaks()) {
            // ignore peaks with 0 m/z and 0 intensity
            if (peak.getMz() == 0 || peak.getIntensity() == 0)
                continue;

            nPeaks++;
        }

        mspString.append("Num peaks: ").append(nPeaks).append("\n");

        for (IPeak peak : spectrum.getPeaks()) {
            // ignore peaks with 0 m/z and 0 intensity
            if (peak.getMz() == 0 || peak.getIntensity() == 0)
                continue;

            mspString.append(peak.getMz()).append(" ").append(peak.getIntensity()).append("\n");
        }

        return mspString.toString();
    }

    private String generateComments(PSM maxPsm, ISpectrum consensusSpectrum, ParameterExtractor parameterExtractor) {
        StringBuilder commentString = new StringBuilder();

        String modString = generateModString(maxPsm);

        commentString.append("Spec=Consensus");
        commentString.append(" Mods=" + modString);
        commentString.append(" Parent=").append(consensusSpectrum.getPrecursorCharge());
        commentString.append(" Nreps=").append(parameterExtractor.getNumberOfSpectra());
        commentString.append(" Naa=").append(maxPsm.getSequence().length());
        commentString.append(" MaxRatio=").append(parameterExtractor.getMaxPsmRatio());
        commentString.append(" PrecursorMzRange=").append(parameterExtractor.getPrecursorMzRange());

        return commentString.toString();
    }

    private String generateModString(PSM maxPsm) {
        // build the modification string based on the psm's modifications
        if (maxPsm.getPtms().size() < 1) {
            return "0";
        }

        StringBuilder modificationString = new StringBuilder();
        modificationString.append(maxPsm.getPtms().size());

        for (PSM.PTM ptm : maxPsm.getPtms()) {
            String modMspName;

            // first try to get the modification's delta mass
            PTM ptmObject = modReader.getPTMbyAccession(ptm.getAccession());

            // if we can't get the delta, we'll use the accession as a name
            if (ptmObject == null) {
                System.out.println("Warning: Failed to resolve modification " + ptm.getAccession());
                modMspName = ptm.getAccession();
            } else {
                Double delta = ptmObject.getMonoDeltaMass();

                // if there is no delta for the modification (incorrect modification supplied), use the accession
                if (delta == null) {
                    System.out.println("Warning: Modification accession supplied that is not associated with a mass delta: " + ptm.getAccession());
                    modMspName = ptm.getAccession();
                } else {
                    // using the delta we try to match it to the MSP name
                    modMspName = getMspNameForDetla(delta);

                    // if the delta cannot be matched, we'll use the delta
                    if (modMspName == null) {
                        System.out.println("Warning: Failed to match modification delta (" + ptm.getAccession() + " > " + delta + ") to MSP name.");
                        modMspName = delta.toString();
                    }
                }
            }

            // extract the sequence position
            Character sequenceChar;

            if (ptm.getPosition() < 1) {
                sequenceChar = maxPsm.getSequence().charAt(0);
            } else if (ptm.getPosition() >= maxPsm.getSequence().length()) {
                sequenceChar = maxPsm.getSequence().charAt(maxPsm.getSequence().length() - 1);
            } else {
                sequenceChar = maxPsm.getSequence().charAt(ptm.getPosition() - 1);
            }

            // add the modification to the string
            modificationString.append("/" + ptm.getPosition() + "," + sequenceChar + "," + modMspName);
        }

        return modificationString.toString();
    }

    private String getMspNameForDetla(Double delta) {
        for (String mspName : modToDeltaMap.keySet()) {
            // if the delta is within the tolerance, use this name
            Double modDelta = modToDeltaMap.get(mspName);

            if (modDelta > delta - MOD_TOLERANCE && modDelta < delta + MOD_TOLERANCE) {
                return mspName;
            }
        }

        return null;
    }

    private String generateClusterName(PSM maxPsm, ISpectrum consensusSpectrum) {
        return String.format("%s/%d", maxPsm.getSequence(), consensusSpectrum.getPrecursorCharge());
    }
}