package uk.ac.ebi.pride.tools.fast_spectra_clustering;


import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.jmzreader.*;
import uk.ac.ebi.pride.tools.jmzreader.model.*;
import uk.ac.ebi.pride.tools.mgf_parser.*;
import uk.ac.ebi.pride.tools.mgf_parser.model.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.tools.fast_spectra_clustering.ClusteringTestUtulities
 * User: Steve
 * Date: 6/19/13
 */
public class ClusteringTestUtilities {

    public static final String SAMPLE_MGF_FILE = "spectra_400.0_4.0.mgf";
    public static final String SAMPLE_CGF_FILE = "uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf";

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
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
        // load a file contains a list of clusters
        URL url = null;
        url = ClusteringEngineMgfTests.class.getClassLoader().getResource(resName);
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = null;
        try {
            inputFile = new File(url.toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }

        ISpectrum[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);
        return Arrays.asList(mgfSpectra);
    }

    //        // load a file contains a list of clusters
    //        URL url = FrankEtAClusterEngineTest.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf");
    //        if (url == null) {
    //            throw new IllegalStateException("no file for input found!");
    //        }
    //        File inputFile = new File(url.toURI());
    //
    //        consensusSpectraItems = ParserUtilities.readClusters(inputFile);
    //
    //        url = FrankEtAClusterEngineTest.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.mgf");
    //        if (url == null) {
    //            throw new IllegalStateException("no file for input found!");
    //        }
    //        inputFile = new File(url.toURI());
    //
    //      //  consensusSpectraItems = ParserUtilities.guaranteeMGFParse(inputFile);

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<IPeptideSpectrumMatch> readISpectraFromResource() {
        return readISpectraFromResource(SAMPLE_MGF_FILE);
    }

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<IPeptideSpectrumMatch> readISpectraFromResource(String resName) {
        // load a file contains a list of clusters
        URL url = null;
        url = ClusteringEngineMgfTests.class.getClassLoader().getResource(resName);
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = null;
        try {
            inputFile = new File(url.toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }

        IPeptideSpectrumMatch[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);
        return Arrays.asList(mgfSpectra);
    }

    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<Spectrum> readSpectrumsFromResource() {
        return readSpectrumsFromResource(SAMPLE_MGF_FILE);
    }



    /**
     * read a resource mgf as a list of spectra
     *
     * @param resName
     * @return
     */
    public static List<Spectrum> readSpectrumsFromResource(String resName) {
        try {
            File specFile;
            MgfFile mgfFile;
            List<Spectrum> spectra;
            URL testFile = ClusteringTestUtilities.class.getClassLoader().getResource(resName);

            assert testFile != null;
            specFile = new File(testFile.toURI());

            mgfFile = new MgfFile(specFile);

            spectra = new ArrayList<Spectrum>(mgfFile.getMs2QueryCount());
            Iterator<Ms2Query> it = mgfFile.getMs2QueryIterator();
            while (it.hasNext()) {
                Ms2Query query = it.next();
                if (query.getPrecursorIntensity() == null)
                    query.setPeptideIntensity(1.0);

                spectra.add(query);
            }
            return spectra;
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }
        catch (JMzReaderException e) {
            throw new RuntimeException(e);

        }
    }

    public static List<ISpectralCluster>  readSpectraClustersFromResource() {
        return readSpectraClustersFromResource(SAMPLE_CGF_FILE);
    }


    public static List<ISpectralCluster>  readSpectraClustersFromResource(String resName) {
        List<ConsensusSpectraItems> items = readConsensusSpectraItemsFromResource(  resName) ;
        int index = 1000;
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ConsensusSpectraItems si : items) {
            ISpectralCluster cluster = new SpectralCluster(Integer.toString(index++));
            for(ISpectrum sr : si.getSpectra())
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
    public static List<ConsensusSpectraItems> readConsensusSpectraItemsFromResource() {
        return readConsensusSpectraItemsFromResource(SAMPLE_CGF_FILE);
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
            URL url = FrankEtAClusterEngineTest.class.getClassLoader().getResource(resName);
            if (url == null) {
                throw new IllegalStateException("no file for input found!");
            }
            File inputFile = new File(url.toURI());

            List<ConsensusSpectraItems> consensusSpectraItems = Arrays.asList(ParserUtilities.readClusters(inputFile));
            return consensusSpectraItems;

        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }

    }


}


