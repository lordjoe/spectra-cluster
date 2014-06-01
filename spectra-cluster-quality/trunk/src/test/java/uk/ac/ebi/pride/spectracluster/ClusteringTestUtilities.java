package uk.ac.ebi.pride.spectracluster;


import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.io.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities
 * User: Steve
 * Date: 6/19/13
 */
public class ClusteringTestUtilities {

    public static final String SAMPLE_MGF_FILE = "uk/ac/ebi/pride/spectracluster/similarity/spectra_400.0_4.0.mgf";

    public static final String SAMPLE_CLUSTERING_FILE = "uk/ac/ebi/pride/spectracluster/similarity/Sample.clustering";

    public static final String SAMPLE_CGF_FILE = "uk/ac/ebi/pride/spectracluster/similarity/spectra_400.0_4.0.cgf";

    public static final String SAMPLE_SECOND_CGF_FILE = "uk/ac/ebi/pride/spectracluster/similarity/ClusterBin0400.cgf";

    /**
     * read a resource mgf as a list of spectra
     *
     * @return
     */
    public static List<ISpectrum> readISpectraFromResource() {
        return readISpectraFromResource(SAMPLE_MGF_FILE);
    }

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<ISpectrum> readISpectraFromResource(String resName) {
        // load a file contains a list of clusters
        File inputFile = getSpectrumFile(resName);

        ISpectrum[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);
        return Arrays.asList(mgfSpectra);
    }

    /**
     * read a resource mgf as a list of spectra
     *
     * @return
     */
    public static List<ISpectrum> readConsensusSpectralItems() {
        return readConsensusSpectralItems(SAMPLE_MGF_FILE);
    }

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<ISpectrum> readConsensusSpectralItems(String resName) {
        File inputFile = getSpectrumFile(resName);


        ISpectrum[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);
        return Arrays.asList(mgfSpectra);
    }


    public static File getSpectrumFile(String resName) {
        // load a file contains a list of clusters
        URL url;
        url = ClusteringTestUtilities.class.getClassLoader().getResource(resName);
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile;
        try {
            inputFile = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }
        return inputFile;
    }

    public static LineNumberReader getResourceClusteringReader() {
        return getResourceClusteringReader(SAMPLE_CLUSTERING_FILE);
    }

    public static LineNumberReader getResourceClusteringReader(String resName) {
        URL testFile = ClusteringTestUtilities.class.getClassLoader().getResource(resName);

        assert testFile != null;
        try {
            File specFile = new File(testFile.toURI());
            return new LineNumberReader(new FileReader(specFile));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ICluster> readSpectraClustersFromResource() {
        return readSpectraClustersFromResource(SAMPLE_CGF_FILE);
    }

    public static List<ICluster> readSecondSpectraClustersFromResource() {
        return readSpectraClustersFromResource(SAMPLE_SECOND_CGF_FILE);
    }

    public static List<ICluster> readSpectraClustersFromResource(String resName) {
        List<ConsensusSpectraItems> items = readConsensusSpectraItemsFromResource(resName);
        int index = 1000;
        List<ICluster> holder = new ArrayList<ICluster>();
        for (ConsensusSpectraItems si : items) {
            ICluster cluster = new SpectralCluster(Integer.toString(index++),
                    Defaults.getDefaultConsensusSpectrumBuilder());
            for (ISpectrum sr : si.getSpectra())
                cluster.addSpectra(sr);
            holder.add(cluster);
        }

        return holder;
    }

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<ConsensusSpectraItems> readConsensusSpectraItemsFromResource(String resName) {
        try {
            // load a file contains a list of clusters
            URL url = ClusteringTestUtilities.class.getClassLoader().getResource(resName);
            if (url == null) {
                throw new IllegalStateException("no file for input found!");
            }
            File inputFile = new File(url.toURI());

            //noinspection UnusedDeclaration,UnnecessaryLocalVariable
            List<ConsensusSpectraItems> consensusSpectraItems = Arrays.asList(ParserUtilities.readClusters(inputFile));
            return consensusSpectraItems;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }

    }


}


