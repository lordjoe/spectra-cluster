package uk.ac.ebi.pride.spectral_library_builder.util;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.*;

/**
 * Created by jg on 05.12.14.
 */
public class ParameterExtractor {
    private int numberOfSpectra;

    private final PSM maximumPsm;
    private final List<PSM> psms;
    private final Set<String> projects;
    private final Set<String> assays;
    private final Map<String, Integer> countPerILIndependentSequence;
    private final Map<String, PSM> psmPerILIndependentSequence;
    private final int maximumSequenceCount;
    private float minMz = Float.MAX_VALUE;
    private float maxMz = 0;

    /**
     * Creates a new ParameterExtractor that extracts the available
     * parameters from the supplied spectra.
     * @param spectra Collection of ISpectrum to extract the parameters from.
     */
    public ParameterExtractor(Collection<ISpectrum> spectra) throws Exception {
        psms = new ArrayList<PSM>(spectra.size());
        projects = new HashSet<String>();
        assays = new HashSet<String>();
        countPerILIndependentSequence = new HashMap<String, Integer>();
        psmPerILIndependentSequence = new HashMap<String, PSM>();

        // create the list of psms
        numberOfSpectra = 0;
        for (ISpectrum spectrum : spectra) {
            psms.addAll(SpectrumConverter.extractSpectrumPsms(spectrum));
            projects.add(spectrum.getProperty(SpectrumConverter.PROPERTY_PROJECT));
            assays.add(spectrum.getProperty(SpectrumConverter.PROPERTY_ASSAY));

            if (minMz > spectrum.getPrecursorMz()) {
                minMz = spectrum.getPrecursorMz();
            }
            if (maxMz < spectrum.getPrecursorMz()) {
                maxMz = spectrum.getPrecursorMz();
            }

            numberOfSpectra++;
        }

        // get the counts per (I/L independent) sequence
        for (PSM psm : psms) {
            String ilIndependentSequence = psm.getSequence().replaceAll("I", "L");

            if (!countPerILIndependentSequence.containsKey(ilIndependentSequence)) {
                countPerILIndependentSequence.put(ilIndependentSequence, 0);
                psmPerILIndependentSequence.put(ilIndependentSequence, psm);
            }

            countPerILIndependentSequence.put(ilIndependentSequence, countPerILIndependentSequence.get(ilIndependentSequence) + 1);
        }

        // get the maximum count
        List<Integer> sequenceCounts = new ArrayList<Integer>( countPerILIndependentSequence.values() );
        Collections.sort(sequenceCounts);
        int maxSequenceCount = sequenceCounts.get(sequenceCounts.size() - 1);

        int secondMaxSequenceCount = 0;
        if (sequenceCounts.size() > 1) {
            secondMaxSequenceCount = sequenceCounts.get(sequenceCounts.size() - 2);
        }

        if (secondMaxSequenceCount == maxSequenceCount) {
            throw new Exception("Multiple equal identifications encountered.");
        }
        maximumSequenceCount = maxSequenceCount;

        String maxIlIndependentSequence = null;
        for (String ilIndependentSequence : countPerILIndependentSequence.keySet()) {
            if (countPerILIndependentSequence.get(ilIndependentSequence) == maxSequenceCount) {
                maxIlIndependentSequence = ilIndependentSequence;
                break;
            }
        }

        maximumPsm = psmPerILIndependentSequence.get(maxIlIndependentSequence);
    }

    public int getNumberOfProjects() {
        return projects.size();
    }

    public int getNumberOfSpectra() {
        return numberOfSpectra;
    }

    public int getNumberOfPSMs() {
        return psms.size();
    }

    public String getMaximumSequence() {
        return maximumPsm.getSequence();
    }

    public PSM getMaximumPsm() {
        return maximumPsm;
    }

    public List<PSM> getPsms() {
        return psms;
    }

    public Set<String> getProjects() {
        return projects;
    }

    public Set<String> getAssays() {
        return assays;
    }

    public int getMaximumSequenceCount() {
        return maximumSequenceCount;
    }

    public Map<String, Integer> getCountPerILIndependentSequence() {
        return Collections.unmodifiableMap( countPerILIndependentSequence );
    }

    public PSM getPsmForIlIndependentSequence(String ilIndependentSequence) {
        return psmPerILIndependentSequence.get(ilIndependentSequence);
    }

    public float getMaxPsmRatio() {
        return (float) maximumSequenceCount / numberOfSpectra;
    }

    public float getPrecursorMzRange() {
        return maxMz - minMz;
    }
}
