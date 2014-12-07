package uk.ac.ebi.pride.spectracluster.clusteringfileclusterfetcher.spectrum_fetcher;

import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.pride_wrapper.PRIDEXmlWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches spectra from the file system instead of using
 * the web service
 * Created by jg on 07.12.14.
 */
public class SpectrumFileFetcher {
    public final static String SPECTRUM_STORAGE_PATH="/nfs/pride/prod/archive/";
    public static Map<String, String> projectIdToPath = null;

    public SpectrumFileFetcher() throws Exception {
        if (projectIdToPath == null) {
            loadProjectPaths();
        }
    }

    private void loadProjectPaths() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(SpectrumFileFetcher.class.getClassLoader().getResourceAsStream("PRD_DIR_LIST")));

        projectIdToPath = new HashMap<String, String>();

        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            String projectId = line.substring(32);
            if (projectIdToPath.containsKey(projectId)) {
                throw new Exception("Multiple paths found for " + projectId);
            }

            projectIdToPath.put(projectId, line);
        }
    }

    public List<Spectrum> getSpectrumAsList(String spectrumId) throws Exception {
        List<Spectrum> spectrumList = new ArrayList<Spectrum>(1);
        spectrumList.add(getSpectrum(spectrumId));

        return spectrumList;
    }

    public Spectrum getSpectrum(String spectrumId) throws Exception {
        // PRD000474;PRIDE_Exp_Complete_Ac_17963.xml;spectrum=3697904
        String[] spectrumIdFields = spectrumId.split(";");

        if (spectrumIdFields.length != 3) {
            throw new Exception("Invalid spectrum id encountered: " + spectrumId);
        }

        if (!spectrumIdFields[2].startsWith("spectrum=")) {
            throw new Exception("Invalid spectrum id encountered: " + spectrumId);
        }

        String projectId = spectrumIdFields[0];
        String filename = spectrumIdFields[1];
        String spectrumFileId = spectrumIdFields[2].substring(9);

        // get the file
        File spectrumFile = getSpectrumFile(projectId, filename);

        if (!spectrumFile.exists()) {
            throw new Exception("Spectrum file " + filename + " for " + projectId + " could not be found.");
        }

        // open the file
        JMzReader fileReader = null;
        if (filename.endsWith(".xml")) {
            fileReader = new PRIDEXmlWrapper(spectrumFile);
        }
        else if (filename.endsWith(".mgf")) {
            fileReader = new MgfFile(spectrumFile);
        }
        else {
            throw new Exception("Failed to determined spectrum filetype for " + filename);
        }

        // get the spectrum
        uk.ac.ebi.pride.tools.jmzreader.model.Spectrum spectrum = fileReader.getSpectrumById(spectrumFileId);

        Spectrum fetchedSpectrum = new Spectrum();
        fetchedSpectrum.setId(spectrumId);
        fetchedSpectrum.setPrecursorCharge(spectrum.getPrecursorCharge());
        fetchedSpectrum.setPrecursorIntensity(spectrum.getPrecursorIntensity());
        fetchedSpectrum.setPrecursorMz(spectrum.getPrecursorMZ());

        double[] mzValues = new double[spectrum.getPeakList().size()];
        double[] intensValues = new double[spectrum.getPeakList().size()];
        int index = 0;

        for (Double mz : spectrum.getPeakList().keySet()) {
            mzValues[index] = mz;
            intensValues[index] = spectrum.getPeakList().get(mz);
            index++;
        }

        fetchedSpectrum.setPeaksMz(mzValues);
        fetchedSpectrum.setPeaksIntensities(intensValues);

        fetchedSpectrum.setProjectAccession(projectId);

        return fetchedSpectrum;
    }

    private File getSpectrumFile(String projectId, String filename) {
        String projectPath = "";

        if (projectIdToPath.containsKey(projectId)) {
            projectPath = projectIdToPath.get(projectId);
        }
        else {
            projectPath = SPECTRUM_STORAGE_PATH + projectId;
        }

        String filePath = projectPath + "/internal/" + filename;

        return new File(filePath);
    }
}
