package uk.ac.ebi.pride.spectracluster.hadoop;


import org.junit.Assert;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.PeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrum;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ConsensusSpectraItems;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities
 * User: Steve
 * Date: 6/19/13
 */
public class ClusteringTestUtilities {

    public static final String SAMPLE_MGF_FILE = "uk/ac/ebi/pride/spectracluster/hadoop/spectra_400.0_4.0.mgf";

    public static final String SAMPLE_CGF_FILE = "uk/ac/ebi/pride/spectracluster/hadoop/spectra_400.0_4.0.cgf";


    /**
     * final an assertion of all clusters om the set are not equivalent
     *
     * @param pScs
     * @param pScs2
     */
    public static void assertEquivalentClusters(final List<IPeptideSpectralCluster> pScs, final List<IPeptideSpectralCluster> pScs2) {
        final ClusterContentComparator comparator = new ClusterContentComparator();
        Collections.sort(pScs, comparator);
        Collections.sort(pScs2, comparator);
        Assert.assertEquals(pScs.size(), pScs2.size());
        for (int i = 0; i < pScs.size(); i++) {
            IPeptideSpectralCluster cl1 = pScs.get(i);
            IPeptideSpectralCluster cl2 = pScs2.get(i);
            boolean equivalent = cl1.equivalent(cl2);
            Assert.assertTrue(equivalent);

        }
    }

    /**
     * read a resource mgf as a list of spectra
     *
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
        File inputFile = getSpectrumFile(resName);

        IPeptideSpectrumMatch[] mgfSpectra = ParserUtilities.readMGFScans(inputFile);
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

    public static List<IPeptideSpectralCluster> readSpectraClustersFromResource() {
        return readSpectraClustersFromResource(SAMPLE_CGF_FILE);
    }

    public static List<IPeptideSpectralCluster> readSpectraClustersFromResource(String resName) {
        List<ConsensusSpectraItems> items = readConsensusSpectraItemsFromResource(resName);
        int index = 1000;
        List<IPeptideSpectralCluster> holder = new ArrayList<IPeptideSpectralCluster>();
        for (ConsensusSpectraItems si : items) {
            IPeptideSpectralCluster cluster = new PeptideSpectralCluster(Integer.toString(index++), new ConsensusSpectrum());
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


