package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.algorithms.CountedMap;
import com.lordjoe.utilities.ElapsedTimer;
import uk.ac.ebi.pride.spectracluster.ClusterSetListener;
import uk.ac.ebi.pride.spectracluster.cluster.ClusteringHeader;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSMSpectrum;
import uk.ac.ebi.pride.spectracluster.psm_similarity.PSM_Holder;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ClusterCreateListener;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSimilarityUtilities
 * User: Steve
 * Date: 6/27/13
 */
public class ClusterSimilarityUtilities {

    public static final Random RND = new Random();

    /**
     * return non-null if c1 contains all spactra in c2 or vice
     * versa
     *
     * @param c1 cluster
     * @param c2 cluster
     * @return non-null if we can use the return as an enclosing cluster
     */
    public static
    @Nullable
    ISpectralCluster clusterFullyContains(@Nonnull ISpectralCluster c1, @Nonnull ISpectralCluster c2) {
        int size1 = c1.getClusteredSpectraCount();
        int size2 = c1.getClusteredSpectraCount();
        if (size1 == size2) {
            if (c1.getSpectralId().equals(c2.getSpectralId()))
                return c1; // same size same spectra;
        }
        ISpectralCluster containing = c1;
        ISpectralCluster smaller = c2;
        if (size1 < size2) {
            containing = c2;
            smaller = c1;
        }
        Set<String> spectralIds = containing.getSpectralIds();
        if (spectralIds.containsAll(smaller.getSpectralIds()))
            return containing;   // contains all spectra in c1
        return null; // clusters are distinct
    }


    /**
     * score commonality
     *
     * @param existing cluster
     * @param added    cluster
     * @return non-null if we can use the return as an enclosing cluster
     */
    public static double clusterFullyContainsScore(@Nonnull ISpectralCluster existing, @Nonnull ISpectralCluster added) {
        Set<String> spectralIds1 = existing.getSpectralIds();
        Set<String> spectralIds2 = added.getSpectralIds();
        double maxSize = Math.max(spectralIds1.size(), spectralIds2.size());
        double minSize = Math.min(spectralIds1.size(), spectralIds2.size());

        Set<String> common = new HashSet<String>(spectralIds1);
        boolean changed = common.retainAll(spectralIds2);

        return (double) common.size() / minSize;
    }

    public static CountedMap<String> getCountedMap(IClusterSet cl) {
        CountedMap<String> ret = new CountedMap<String>();
        for (ISpectralCluster sc : cl.getClusters()) {
            Set<String> c1SpectralIds = sc.getSpectralIds();
            for (String c1SpectralId : c1SpectralIds) {
                ret.add(c1SpectralId);
            }
        }
        return ret;
    }

    public static Set<String> allSpectralIds(ISpectralCluster c1, ISpectralCluster c2) {
        Set<String> c1SpectralIds = c1.getSpectralIds();
        Set<String> c2SpectralIds = c2.getSpectralIds();

        Set<String> all = new HashSet<String>(c1SpectralIds);
        all.addAll(c2SpectralIds);
        return all;
    }

    public static Set<String> commonSpectralIds(ISpectralCluster c1, ISpectralCluster c2) {
        Set<String> c1SpectralIds = c1.getSpectralIds();
        Set<String> c2SpectralIds = c2.getSpectralIds();

        Set<String> all = new HashSet<String>(c1SpectralIds);
        all.retainAll(c2SpectralIds);
        return all;
    }

    public static Set<String> commonPeptides(ISpectralCluster c1, ISpectralCluster c2) {
        List<String> c1SpectralIds = c1.getPeptides();
        List<String> c2SpectralIds = c2.getPeptides();

        Set<String> all = new HashSet<String>(c1SpectralIds);
        all.retainAll(c2SpectralIds);
        return all;
    }

    public static Set<String> allPeptides(ISpectralCluster c1, ISpectralCluster c2) {
        List<String> c1SpectralIds = c1.getPeptides();
        List<String> c2SpectralIds = c2.getPeptides();

        Set<String> all = new HashSet<String>(c1SpectralIds);
        all.addAll(c2SpectralIds);
        return all;
    }

    public static String idsToString(Collection<String> strings) {
        List<String> sorted = new ArrayList<String>(strings);
        Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        for (String string : sorted) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(string);
        }

        return sb.toString();
    }


    public static IClusterSet buildFromClusteringFile(File file, ISpectrumRetriever spectrumRetriever) {
        SimpleClusterSet simpleClusterSet = new SimpleClusterSet();
        simpleClusterSet.setName(file.getName());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return null;
            for (File file1 : files) {
                IClusterSet clusterSet = buildFromClusteringFile(file1, spectrumRetriever);
                if (simpleClusterSet.getHeader() == null)
                    simpleClusterSet.setHeader(clusterSet.getHeader());

                simpleClusterSet.addClusters(clusterSet.getClusters());
            }
        } else

        {
            if (file.getName().endsWith(".clustering")) {
                try {
                    LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));

                    ClusteringHeader header = simpleClusterSet.getHeader();
                    if (header == null) {
                        header = ParserUtilities.readClusterHeader(lineNumberReader);
                        simpleClusterSet.setHeader(header);
                    }

                    ISpectralCluster[] clusters = ParserUtilities.readClustersFromClusteringFile(lineNumberReader, spectrumRetriever);
                    simpleClusterSet.addClusters(Arrays.asList(clusters));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (file.getName().endsWith(ClusterUtilities.CGF_EXTENSION)) {
                try {
                    LineNumberReader rdr = new LineNumberReader(new FileReader(file));

                    ISpectralCluster[] clusters = ParserUtilities.readSpectralCluster(rdr);
                    simpleClusterSet.addClusters(Arrays.asList(clusters));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return simpleClusterSet;
    }

    public static void buildFromMgfFile(File file, IMutableSpectrumRetriever spectrumRetriever) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (File file1 : files) {
                buildFromMgfFile(file1, spectrumRetriever);
            }
        } else if (file.getName().endsWith(".mgf")) {
            try {
                System.out.println(file.getName());
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                IPeptideSpectrumMatch[] spectra = ParserUtilities.readMGFScans(lineNumberReader);
                spectrumRetriever.addSpectra(spectra);
                lineNumberReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * create ,clustering files from CGF files
     *
     * @param pF !null existing file or directory with cgf files
     */
    public static IClusterSet createClusteringSetFromCGF(final File pF) {
        IClusterSet cs = new SimpleClusterSet();
        if (!pF.exists())
            return cs;
        ClusterSetListener lstn = new ClusterSetListener(cs);
        createClusteringSetFromCGF(pF, lstn);
        int duplicates = cs.getNumberDuplicateClusters();

        return cs;
    }

    private static void createClusteringSetFromCGF(final File pF, ClusterCreateListener lstn) {
        if (pF.isDirectory()) {
            File[] subfiles = pF.listFiles();
            if (subfiles != null) {
                for (File subfile : subfiles) {
                    createClusteringSetFromCGF(subfile, lstn);
                }
            }
        } else {
            String name = pF.getName();
            if (name.toLowerCase().endsWith(ClusterUtilities.CGF_EXTENSION)) {
                try {
                    LineNumberReader rdr = new LineNumberReader(new FileReader(pF));
                    String clusteringName = name.substring(0, name.length() - ClusterUtilities.CGF_EXTENSION.length()) + ClusterUtilities.CLUSTERING_EXTENSION;
                    File outFile = new File(pF.getParent(), clusteringName);
                    ParserUtilities.readAndProcessSpectralClusters(rdr, lstn);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);

                }

            }

        }
    }


    /**
     * build a retriever saving only essentials
     *
     * @param file
     * @param spectrumRetriever
     */
    public static void buildLazyLoadedFromMgfFile(File file, IMutableSpectrumRetriever spectrumRetriever) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (File file1 : files) {
                buildLazyLoadedFromMgfFile(file1, spectrumRetriever);
            }
        } else if (file.getName().endsWith(".mgf")) {
            try {
                System.out.println(file.getName());
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                IPeptideSpectrumMatch[] spectra = ParserUtilities.readMGFScans(lineNumberReader);
                for (IPeptideSpectrumMatch pm : spectra) {
                    SimplifiedSpectrum ll = new SimplifiedSpectrum(pm);
                    spectrumRetriever.addSpectra(ll);
                }
                lineNumberReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void buildFromTSVFile(File file, IMutableSpectrumRetriever spectrumRetriever) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (File file1 : files) {
                buildFromTSVFile(file1, spectrumRetriever);
            }
        } else if (file.getName().endsWith(".tsv")) {
            try {

                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                String line;
                while ((line = lineNumberReader.readLine()) != null) {
                    IPeptideSpectrumMatch peptideSpectrumMatch = constructPeptideSpectrumMatch(line);
                    spectrumRetriever.addSpectra(peptideSpectrumMatch);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void buildFromTSVFile(File file, PSM_Holder spectrumRetriever) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (File file1 : files) {
                buildFromTSVFile(file1, spectrumRetriever);
            }
        } else if (file.getName().endsWith(".tsv")) {
            try {

                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                String line;
                while ((line = lineNumberReader.readLine()) != null) {
                    IPeptideSpectrumMatch pp = constructPeptideSpectrumMatch(line);
                    List<PSMSpectrum> holder = new ArrayList<PSMSpectrum>();
                    for (PSMSpectrum psm : spectrumRetriever.getPSMSpectrums(pp.getId())) {
                        psm.setPrecursorCharge(pp.getPrecursorCharge());
                        psm.setPrecursorMz(pp.getPrecursorMz());
                        holder.add(psm);
                    }
                    for (PSMSpectrum psm : holder) {
                        spectrumRetriever.addPSMSpectrum(psm);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * write the stable clusters into a file
     *
     * @param clusters clusters to select
     * @param outFile  output file - will be overwritten
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void saveStableClusters(IClusterSet clusters, File outFile) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(outFile));
            //noinspection unchecked
            AbstractClusterWriter visitor = new AbstractClusterWriter(writer, ISpectralCluster.STABLE_PREDICATE);
            //noinspection unchecked
            clusters.visitClusters(visitor);
            writer.close();
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

    }

    /**
     * write the stable clusters into a file
     *
     * @param clusters clusters to select
     * @param outFile  output file - will be overwritten
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void saveSemiStableClusters(IClusterSet clusters, File outFile) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(outFile));
            ClusteringHeader header = clusters.getHeader();
            if (header != null) {
                header.appendHeader(writer);
                writer.append("\n");
            }
            //noinspection unchecked
            AbstractClusterWriter visitor = new AbstractClusterWriter(writer, ISpectralCluster.SEMI_STABLE_PREDICATE);
            //noinspection unchecked
            clusters.visitClusters(visitor);
            writer.close();
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

    }

    private static IPeptideSpectrumMatch constructPeptideSpectrumMatch(String line) {
        String[] parts = line.split("\t");
        if (parts.length < 4)
            throw new IllegalArgumentException("bad line");
        String spectrumId = parts[0];
        int precursorCharge = Integer.parseInt(parts[1]);
        float precursorMz = Float.parseFloat(parts[2]);
        String peptide = parts[3];

        //noinspection unchecked
        return new PeptideSpectrumMatch(spectrumId, peptide, precursorCharge, precursorMz, Collections.EMPTY_LIST);
    }

    /**
     * return all clusters with more than one spectrum
     *
     * @param clusters !null list of clusters
     * @return !null list of clusters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static List<ISpectralCluster> nonSingleClusters(List<ISpectralCluster> clusters) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusters) {
            if (cluster.getClusteredSpectraCount() > 1)
                holder.add(cluster);
        }
        return holder;
    }

    /**
     * return all clusters with only  one spectrum
     *
     * @param clusters !null list of clusters
     * @return !null list of clusters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static List<ISpectralCluster> singleClusters(List<ISpectralCluster> clusters) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster cluster : clusters) {
            if (cluster.getClusteredSpectraCount() == 1)
                holder.add(cluster);
        }
        return holder;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void loadSpectrumTSVFile(String[] args) {
        SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();
        buildFromTSVFile(new File(args[0]), simpleSpectrumRetriever);
        System.out.println(simpleSpectrumRetriever.getSpectra().size());
    }


    /**
     * return common spectra ids
     *
     * @param c1 cluster 1
     * @param c2 cluster2
     * @return set of common ids
     */
    public static
    @Nonnull
    Set<String> getSpectraOverlap(@Nonnull final ISpectralCluster c1, @Nonnull final ISpectralCluster c2) {
        Set<String> ret = new HashSet<String>(c1.getSpectralIds());
        ret.retainAll(c2.getSpectralIds());
        return ret;
    }

    /**
     * return common spectra ids
     *
     * @param c1 cluster 1
     * @param c2 cluster2
     * @return set of common ids
     */
    public static
    @Nonnull
    Set<String> getSpectraOverlap(@Nonnull final Set<String> firstIds, @Nonnull final ISpectralCluster c2) {
        Set<String> ret = new HashSet<String>(firstIds);
        ret.retainAll(c2.getSpectralIds());
        return ret;
    }

    /**
     * why were these not clustered
     *
     * @param cps
     */
    public static void testNotClustered(final List<ISpectralCluster> cps) {
        int compareCount = 0;
        SimilarityChecker sCheck = Defaults.INSTANCE.getDefaultSimilarityChecker();

        for (int i = 0; i < cps.size() - 1; i++) {
            ISpectralCluster cluster = cps.get(i);
            Set<String> ids = cluster.getSpectralIds();
            ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
            for (int j = i + 1; j < cps.size(); j++) {
                ISpectralCluster c2 = cps.get(j);

                Set<String> overlap = ClusterSimilarityUtilities.getSpectraOverlap(cluster, c2);
                boolean majorOverlap = hasMajorOverlap(cluster, c2);
                if (majorOverlap)
                    overlap = ClusterSimilarityUtilities.getSpectraOverlap(cluster, c2); // break here
                Set<String> ids2 = c2.getSpectralIds();
                ISpectrum consensusSpectrum1 = c2.getConsensusSpectrum();
                double similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);
                boolean pass1 = similarityScore >= sCheck.getDefaultThreshold();
                // look at all spectra
                for (ISpectrum sc : c2.getClusteredSpectra()) {
                    double similarityScore1 = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);
                    boolean pass2 = similarityScore >= sCheck.getDefaultThreshold();
                    if (ids.contains(sc.getId()))
                        pass2 = similarityScore >= sCheck.getDefaultThreshold();
                }

            }

        }

//         ISpectralCluster mostSimilarCluster = null;
//         ISpectrum consensusSpectrum1 = clusterToAdd.getConsensusSpectrum();  // subspectra are really only one spectrum clusters
//         // find the cluster with the highest similarity score
//         for (ISpectralCluster cluster : myClusters) {
//
//             String mostCommonPeptide = cluster.getMostCommonPeptide();
//
//             ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();
//
//             double similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);
//
//             if(mcpIn.equals(mostCommonPeptide))  {
//                 similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1); // break here
//             }
//
//             if (similarityScore >= sCheck.getWindowSize() && similarityScore > highestSimilarityScore) {
//                 highestSimilarityScore = similarityScore;
//                 mostSimilarCluster = cluster;
//             }
//
//         }

    }


    /**
     * read a file looking like
     * <p/>
     * spectrum_id	sequence	is_decoy
     * 237	QSNNKYAASSYLSLTPEQWK	0
     * 238	QSNNKYAASSYLSLTPEQWK	0
     * 388	DMDGEQLEGASSEKR	0
     *
     * @param pDecoyFile non-null readable existing file
     * @return non-null  PSM_Holder
     */
    public static PSM_Holder readPSMDecoySpectra(final File pDecoyFile) {
        PSM_Holder ret = new PSM_Holder();
        readPSMDecoySpectra(pDecoyFile, ret);
        return ret;
    }

    /**
     * read a file looking like
     * <p/>
     * spectrum_id	sequence	is_decoy
     * 237	QSNNKYAASSYLSLTPEQWK	0
     * 238	QSNNKYAASSYLSLTPEQWK	0
     * 388	DMDGEQLEGASSEKR	0
     *
     * @param pDecoyFile   non-null readable existing file
     * @param psms  non-null  PSM_Holder
     */

    public static int Number_Lines_read = 0;

    public static void readPSMDecoySpectra(final File pDecoyFile, PSM_Holder psms) {
        ElapsedTimer timer = new ElapsedTimer();
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(pDecoyFile));
            String line = rdr.readLine();  // drop header
            line = rdr.readLine();
            while (line != null) {
                PSMSpectrum added = PSMSpectrum.getSpectrumFromLine(line);
                psms.addPSMSpectrum(added);
                line = rdr.readLine();
                Number_Lines_read++;
            }
            timer.showElapsed("Finished PSM Decoy Read");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * look at cluster addition code for testing
     *
     * @param clusterToAdd
     * @param myClusters
     */
    public static void testAddToClusters(final ISpectralCluster clusterToAdd, Collection<ISpectralCluster> myClusters) {
        String mcpIn = clusterToAdd.getMostCommonPeptide();
        Map<String, List<ISpectralCluster>> clusterPeptides = mapByMostCommonPeptide(myClusters);

        for (String mostCommonPeptide : clusterPeptides.keySet()) {
            List<ISpectralCluster> cps = clusterPeptides.get(mostCommonPeptide);
            if (cps.size() > 1)
                testNotClustered(cps);
        }

        examineIndividualSpectra(clusterToAdd, myClusters, mcpIn);

    }

    public static void examineIndividualSpectra(final ISpectralCluster clusterToAdd, final Collection<ISpectralCluster> myClusters, final String pMcpIn) {
        SimilarityChecker sCheck = Defaults.INSTANCE.getDefaultSimilarityChecker();

        double highestSimilarityScore = 0;
        int compareCount = 0;

        ISpectralCluster mostSimilarCluster = null;
        ISpectrum consensusSpectrum1 = clusterToAdd.getConsensusSpectrum();  // subspectra are really only one spectrum clusters
        // find the cluster with the highest similarity score
        for (ISpectralCluster cluster : myClusters) {

            String mostCommonPeptide = cluster.getMostCommonPeptide();

            ISpectrum consensusSpectrum = cluster.getConsensusSpectrum();

            double similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1);

            if (pMcpIn.equals(mostCommonPeptide)) {
                similarityScore = sCheck.assessSimilarity(consensusSpectrum, consensusSpectrum1); // break here
            }

            if (similarityScore >= sCheck.getDefaultThreshold() && similarityScore > highestSimilarityScore) {
                highestSimilarityScore = similarityScore;
                mostSimilarCluster = cluster;
            }

        }
    }

    public static Map<String, List<ISpectralCluster>> mapByMostCommonPeptide(final Collection<ISpectralCluster> myClusters) {
        Map<String, List<ISpectralCluster>> clusterPeptides = new HashMap<String, List<ISpectralCluster>>();
        for (ISpectralCluster cluster : myClusters) {
            String mostCommonPeptide = cluster.getMostCommonPeptide();
            List<ISpectralCluster> cps = clusterPeptides.get(mostCommonPeptide);
            if (cps == null) {
                cps = new ArrayList<ISpectralCluster>();
                clusterPeptides.put(mostCommonPeptide, cps);
            }
            cps.add(cluster);
        }
        return clusterPeptides;
    }

    /**
     * return common spectra ids
     *
     * @param c1 cluster 1
     * @param c2 cluster2
     * @return true if over half of the spectra in the lower cluster overlap
     */
    public static boolean hasMajorOverlap(@Nonnull final ISpectralCluster c1, @Nonnull final ISpectralCluster c2) {
        int size1 = c1.getClusteredSpectraCount();
        int size2 = c2.getClusteredSpectraCount();
        Set<String> spectraOverlap = getSpectraOverlap(c1, c2);
        int testSize = Math.min(size1, size2) / 2;
        return spectraOverlap.size() > testSize;
    }

    /**
     * compare two spectral retrievers for equality
     *
     * @param r1
     * @param r2
     * @return true if the same
     */
    public static boolean compareSpectralRetrievers(@Nonnull SimpleSpectrumRetriever r1, @Nonnull SimpleSpectrumRetriever r2) {
        List<IPeptideSpectrumMatch> spectra = r1.getSpectra();
        for (IPeptideSpectrumMatch ps : spectra) {
            IPeptideSpectrumMatch p2 = r2.retrieve(ps.getId());
            if (p2 == null)
                return false;
            if (p2.getPrecursorCharge() != ps.getPrecursorCharge())
                return false;
            if (Math.abs(p2.getPrecursorMz() - ps.getPrecursorMz()) > 0.1)
                return false;
            if (!p2.getPeptide().equals(ps.getPeptide()))
                return false;

        }
        return true;
    }

    /**
     * build a tsv file from an mgf directory
     *
     * @param pArg
     * @param outFile
     * @throws IOException
     */
    public static void buildTSV(final String pArg, String outFile) throws IOException {
        SimpleSpectrumRetriever spectrumRetriever = new SimpleSpectrumRetriever();
        File mgfs = new File(pArg);
        buildLazyLoadedFromMgfFile(mgfs, spectrumRetriever);

        File outTSV = new File(outFile);
        PrintWriter out = new PrintWriter(new FileWriter(outTSV));

        List<IPeptideSpectrumMatch> spectra = spectrumRetriever.getSpectra();
        for (IPeptideSpectrumMatch pm : spectra) {
            pm.appendTSV(out);
        }

//        SimpleSpectrumRetriever r2 = new SimpleSpectrumRetriever();
//        buildFromTSVFile(new File(args[1]), r2);
//
//        boolean same = compareSpectralRetrievers(spectrumRetriever,r2) ;

        System.out.println(spectrumRetriever.getSpectra().size());
    }


    /**
     * build a set of lines in the file - might be sequences
     *
     * @param decoyFileName existing readable file
     * @return non-nuill set of lines
     */
    public static Set<String> readDecoySequences(@Nonnull String decoyFileName) {
        Set<String> ret = new HashSet<String>();
        File decoyFile = new File(decoyFileName);
        populateSequenceSet(ret, decoyFile);
        return ret;
    }

    /**
     * fill a set with the constnets of line - presumably sequences
     *
     * @param decoys     set to fill
     * @param pDecoyFile file to read from
     */
    public static void populateSequenceSet(@Nonnull final Set<String> decoys, @Nonnull final File pDecoyFile) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(pDecoyFile));
            String line = rdr.readLine();
            while (line != null) {
                decoys.add(line.trim());
                line = rdr.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * build a tsv file from an mgf directory
     *
     * @param pArg
     * @param outFile
     * @throws IOException
     */
    public static void compareDecoys(final String ruiTSV, final String johannesTSV, String outFile) throws IOException {

        File f = new File(johannesTSV);
        PSM_Holder psms = readPSMDecoySpectra(f);

        Set<String> decoys = readDecoySequences(ruiTSV);


//        SimpleSpectrumRetriever spectrumRetriever = new SimpleSpectrumRetriever();
//        File tsvFile = new File(johannesTSV);
//        ClusterSimilarityUtilities.buildFromTSVFile( tsvFile,spectrumRetriever);
//        List<IPeptideSpectrumMatch> spectra = spectrumRetriever.getSpectra();
//        for (IPeptideSpectrumMatch sm : spectra) {
//            if(sm.isDecoy()) {
//                String peptide = sm.getPeptide();
//                if(!decoys.contains(peptide))
//                    System.out.println("not listed decoy " + peptide);
//            }
//        }


        Set<PSMSpectrum> spectra = psms.getAllSpectrums();
        for (IPeptideSpectrumMatch sm : spectra) {
            if (sm.isDecoy()) {
                String peptide = sm.getPeptide();
                if (!decoys.contains(peptide))
                    System.out.println("not listed decoy " + peptide);
            }
        }


        File outTSV = new File(outFile);
        PrintWriter out = new PrintWriter(new FileWriter(outTSV));

        System.out.println(spectra.size());
    }

    public static void usage() {
        System.out.println("Usage propertiesFile <MfgFile or Directory> <outputFile>");
    }


    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }
        compareDecoys(args[0], args[1], args[2]);
//        buildTSV(args[0], args[1]);
    }


}
