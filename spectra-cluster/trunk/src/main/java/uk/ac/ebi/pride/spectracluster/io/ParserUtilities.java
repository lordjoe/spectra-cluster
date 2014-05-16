package uk.ac.ebi.pride.spectracluster.io;


import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusteringHeader;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.LazyLoadedSpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.LazyLoadedSpectrum;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.ParserUtilities
 * Classes for reading clusters
 *
 * @author Steve Lewis
 * @date 5/12/13
 */
public class ParserUtilities {

    public static final String BEGIN_IONS = "BEGIN IONS";
    public static final String END_IONS = "END IONS";
    public static final String BEGIN_CLUSTER = "BEGIN CLUSTER";
    public static final String END_CLUSTER = "END CLUSTER";
    public static final String BEGIN_CLUSTERING = "=Cluster=";


    public static final String AVERAGE_PRECURSOR_MZ = "av_precursor_mz=";
    public static final String AVERAGE_PRECURSOR_INTENSITY = "av_precursor_intens=";
    public static final String PEPTIDE_SEQUENCE = "sequence=";
    public static final String CONSENSUS_MZ = "consensus_mz=";
    public static final String CONSENSUS_INTENSITY = "consensus_intens=";
    public static final String SPECTRUM_ID = "SPEC";

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static IPeptideSpectrumCluster[] readSpectralCluster(String inp) {
        return readSpectralCluster(new File(inp));
    }

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static IPeptideSpectrumCluster[] readSpectralCluster(File inp) {
        try {
            return readSpectralCluster(new LineNumberReader(new FileReader(inp)));
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }


    /**
     * Read a set of clusters and process it
     *
     * @param inp        !null reader
     * @param listerners interested readers
     * @return
     */
    public static void readAndProcessSpectralClusters(LineNumberReader inp, ClusterCreateListener... listerners) {
        if (listerners.length == 0)
            return; // nothing to do
        for (ClusterCreateListener lstn : listerners) {
            lstn.onClusterStarted();
        }
        IPeptideSpectrumCluster cls = readSpectralCluster(inp, null);
        while (cls != null) {
            for (ClusterCreateListener lstn : listerners) {
                lstn.onClusterCreate(cls);
            }
            cls = readSpectralCluster(inp, null);
        }

        for (ClusterCreateListener lstn : listerners) {
            lstn.onClusterCreateFinished();
        }
    }

    /**
     * Read a set of apectra and process it
     *
     * @param inp        !null reader
     * @param listerners interested readers
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void readAndProcessSpectra(LineNumberReader inp, SpectrumCreateListener... listerners) {
        if (listerners.length == 0)
            return; // nothing to do
        for (SpectrumCreateListener lstn : listerners) {
            lstn.onSpectrumStarted();
        }
        ISpectrum cls = readMGFScan(inp, null);
        while (cls != null) {
            for (SpectrumCreateListener lstn : listerners) {
                lstn.onSpectrumCreate(cls);
            }
            cls = readMGFScan(inp, null);
        }

        for (SpectrumCreateListener lstn : listerners) {
            lstn.onSpectrumCreateFinished();
        }
    }

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static IPeptideSpectrumCluster[] readSpectralCluster(LineNumberReader inp) {
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        IPeptideSpectrumCluster cls = readSpectralCluster(inp, null);
        while (cls != null) {
            holder.add(cls);
            cls = readSpectralCluster(inp, null);
        }

        IPeptideSpectrumCluster[] ret = new IPeptideSpectrumCluster[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    /**
     * See ParserTests for an example
     *
     * @param inp
     * @param line
     * @return
     */
    public static IPeptideSpectrumCluster readSpectralCluster(LineNumberReader inp, String line) {
        IPeptideSpectrumCluster ret = null;
        try {
            if (line == null)
                line = inp.readLine();
            while (line != null) {
                if (line.startsWith(BEGIN_CLUSTER)) {
                    //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                    int charge = chargeFromClusterLine(line);
                    String id = idFromClusterLine(line);
                    ret = new SpectralCluster(id);
                    break;
                }
                // naked spectrum
                if (line.startsWith(BEGIN_IONS)) {
                    ISpectrum internal = readMGFScan(inp, line);
                    ret = new SpectralCluster(internal.getId());
                    ret.addSpectra(internal);
                    return ret;
                }
                line = inp.readLine();
            }

            line = inp.readLine();
            while (line != null) {
                ISpectrum internal = readMGFScan(inp, line);
                if (internal != null && ret != null)
                    ret.addSpectra(internal);

                line = inp.readLine();
                if (line == null)
                    return null; // huh - not terminated well

                if (line.startsWith(END_CLUSTER))
                    return ret;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; // nothing found or incloplete
    }


    /**
     * See ParserTests for an example
     *
     * @param inp
     * @param line
     * @return
     */
    public static ConsensusSpectraItems readConsensusSpectraItems(LineNumberReader inp, String line) {
        ConsensusSpectraItems ret = new ConsensusSpectraItems();
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        IConsensusSpectrumBuilder sb = Defaults.INSTANCE.getDefaultConsensusSpectrumBuilder();
        ISpectrum concensus;
        try {
            if (line == null)
                line = inp.readLine();
            while (line != null) {
                if (line.startsWith(BEGIN_CLUSTER)) {
                    break;
                }
                line = inp.readLine();
            }

            line = inp.readLine();
            if (line != null) {
                concensus = readMGFScan(inp, line);
                holder.add(concensus);
            }
            while (line != null) {

                line = inp.readLine();
                if (line.startsWith(END_CLUSTER))
                    break;
                ISpectrum first = readMGFScan(inp, line);
                if (first != null)
                    holder.add(first);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ret.setSpectra(holder);
        // add all spectra
        sb.onSpectraAdd(null, holder.toArray(new ISpectrum[holder.size()]));
        ret.setConcensus(sb.getConsensusSpectrum());
        return ret; // nothing found or incloplete
    }


    /**
     * read a header - should leave the reader in a form where the
     * rest of the fine can be read
     *
     * @param inp
     * @return
     */
    public static ClusteringHeader readClusterHeader(LineNumberReader inp) {
        int MAX_LOOKAHEAD = 100000;
        try {
            String name = null;
            String similarity_method = null;
            String threshold = null;
            String fdr = null;
            String description = null;
            inp.mark(MAX_LOOKAHEAD);
            String line = inp.readLine();
            while (line != null) {
                if (line.startsWith("name")) {
                    name = line.replace("name=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("similarity_method")) {
                    similarity_method = line.replace("similarity_method=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("fdr")) {
                    fdr = line.replace("fdr=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("threshold")) {
                    threshold = line.replace("threshold=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                if (line.startsWith("description")) {
                    description = line.replace("description=", "");
                    inp.mark(MAX_LOOKAHEAD);
                    line = inp.readLine();
                    continue;
                }
                inp.reset(); // not anything we want do go to the previous mark
                break; // I guess we are done
            }

            ClusteringHeader ret = new ClusteringHeader(
                    name, similarity_method, threshold, fdr, description
            );
            inp.reset();
            return ret;
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Read clustering file into a set of clusters
     *
     * @param inp
     * @return
     */
    public static IPeptideSpectrumCluster[] readClustersFromClusteringFile(LineNumberReader inp, ISpectrumRetriever spectrumRetriever) {
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();

        try {
            String line = inp.readLine();
            while (line != null && !line.startsWith(BEGIN_CLUSTERING)) {
                line = inp.readLine();
            }


            List<String> clusterContent = new ArrayList<String>();
            while (line != null) {
                if (line.startsWith(BEGIN_CLUSTERING)) {
                    if (!clusterContent.isEmpty()) {
                        IPeptideSpectrumCluster cluster = processIntoCluster(clusterContent, spectrumRetriever);
                        if (cluster != null) {
                            holder.add(cluster);
                        }
                    }
                    clusterContent.clear();
                } else {
                    clusterContent.add(line);
                }

                line = inp.readLine();
            }

            if (!clusterContent.isEmpty()) {
                IPeptideSpectrumCluster cluster = processIntoCluster(clusterContent, spectrumRetriever);
                if (cluster != null) {
                    holder.add(cluster);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to read ", ioe);
        }

        IPeptideSpectrumCluster[] ret = new IPeptideSpectrumCluster[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected static IPeptideSpectrumCluster processIntoCluster(List<String> clusterLines, ISpectrumRetriever spectrumRetriever) {

        LazyLoadedSpectralCluster cluster = new LazyLoadedSpectralCluster();

        String consensusMzLine = null;
        String consensusIntensityLine = null;
        for (String clusterLine : clusterLines) {
            if (clusterLine.length() == 0)
                break;
            if (clusterLine.startsWith("name=")) {
                break; // start of a new file
            }
            if (clusterLine.startsWith(AVERAGE_PRECURSOR_MZ)) {
                float precursorMz = Float.parseFloat(clusterLine.replace(AVERAGE_PRECURSOR_MZ, ""));
                cluster.setPrecursorMz(precursorMz);
            } else if (clusterLine.startsWith(CONSENSUS_MZ)) {
                consensusMzLine = clusterLine.replace(CONSENSUS_MZ, "");
            } else if (clusterLine.startsWith(CONSENSUS_INTENSITY)) {
                consensusIntensityLine = clusterLine.replace(CONSENSUS_INTENSITY, "");
            } else if (clusterLine.startsWith(PEPTIDE_SEQUENCE)) {
                String peptideSequence = clusterLine.replace(PEPTIDE_SEQUENCE, "");
                peptideSequence = peptideSequence.replace("[", "").replace("]", "");
                cluster.addPeptides(peptideSequence);
            } else if (clusterLine.startsWith(SPECTRUM_ID)) {
                String[] parts = clusterLine.split("\t");
                String id = parts[1];
                //     IPeptideSpectrumMatch spectrum = PSMSpectrum.getSpectrum(id );
                LazyLoadedSpectrum spectrum = new LazyLoadedSpectrum(parts[1], spectrumRetriever);
                cluster.addSpectra(spectrum);
            } else //noinspection StatementWithEmptyBody
                if (clusterLine.startsWith(AVERAGE_PRECURSOR_INTENSITY)) {
                    // do nothing here
                } else {
                    if (clusterLine.length() > 0) {
                        throw new IllegalArgumentException("cannot process line " + clusterLine);
                    }
                }
        }

        if (consensusIntensityLine == null)
            return null;

        List<IPeak> peaks = buildPeaks(consensusMzLine, consensusIntensityLine);
        if (peaks == null)
            return null;
        PeptideSpectrumMatch consensusSpectrum = new PeptideSpectrumMatch(null, null, 0, cluster.getPrecursorMz(), peaks);
        cluster.setConsensusSpectrum(consensusSpectrum);

        return cluster;
    }

    public static List<IPeak> buildPeaks(String commaDelimitecMZ, String commaDelimitedIntensity) {
        try {
            float[] mzValues = parseCommaDelimitedFloats(commaDelimitecMZ);
            float[] intensityValues = parseCommaDelimitedFloats(commaDelimitedIntensity);
            if (mzValues.length != intensityValues.length)
                throw new IllegalArgumentException("Unequal mz and intensity lists");
            List<IPeak> holder = new ArrayList<IPeak>();
            for (int i = 0; i < intensityValues.length; i++) {
                holder.add(new Peak(mzValues[i], intensityValues[i]));
            }
            Collections.sort(holder);  // sort peaks by mz
            return holder;
        } catch (RuntimeException e) {
            return null;
        }
    }

    protected static float[] parseCommaDelimitedFloats(String commaDelimitedFloats) {
        String[] items = commaDelimitedFloats.trim().split(",");
        float[] ret = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            ret[i] = Float.parseFloat(item);
        }
        return ret;
    }


    /**
     * take a line like BEGIN CLUSTER Charge=2 Id=VVXVXVVX  return id
     *
     * @param line
     * @return
     */
    protected static String idFromClusterLine(String line) {
        line = line.replace(BEGIN_CLUSTER, "").trim();
        String[] split = line.split(" ");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.startsWith("Id=")) {
                return s.substring("Id=".length());
            }
        }
        throw new IllegalArgumentException("no Id= part in " + line);
    }

    /**
     * take a line like BEGIN CLUSTER Charge=2 Id=VVXVXVVX  return charge
     *
     * @param line
     * @return
     */
    protected static int chargeFromClusterLine(String line) {
        line = line.replace(BEGIN_CLUSTER, "").trim();
        String[] split = line.split(" ");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.startsWith("Charge=")) {
                return (int) (0.5 + Double.parseDouble(s.substring("Charge=".length())));
            }
        }
        throw new IllegalArgumentException("no Charge= part in " + line);
    }

    public static final String[] NOT_HANDLED_MGF_TAGS = {
            "TOLU=",
            "TOL=",
            "SEQ=",
            "COMP=",
            "TAG=",
            "ETAG=",
            "SCANS=",
            "IT_MODS=",
            "INSTRUMENT=",
    };

    /**
     * @param inp !null reader
     * @return
     */
    public static IPeptideSpectrumMatch[] readMGFScans(LineNumberReader inp) {
        List<IPeptideSpectrumMatch> holder = new ArrayList<IPeptideSpectrumMatch>();
        IPeptideSpectrumMatch spectrum = readMGFScan(inp);
        while (spectrum != null) {
            holder.add(spectrum);
            spectrum = readMGFScan(inp);
        }
        IPeptideSpectrumMatch[] ret = new IPeptideSpectrumMatch[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * @param inp !null existing file
     * @return !null array of spectra
     */
    public static IPeptideSpectrumMatch[] readMGFScans(File inp) {
        try {
            return readMGFScans(new LineNumberReader(new FileReader(inp)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * read an mgf files and return as a list of single spectrum clusters
     *
     * @param inp !null existing file
     * @return !null array of spectra
     */
    public static List<IPeptideSpectrumCluster> readMGFClusters(File inp) {
        IPeptideSpectrumMatch[] scans = readMGFScans(inp);
        List<IPeptideSpectrumCluster> holder = new ArrayList<IPeptideSpectrumCluster>();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < scans.length; i++) {
            IPeptideSpectrumMatch scan = scans[i];
            final IPeptideSpectrumCluster e = ClusterUtilities.asCluster(scan);
            holder.add(e);
        }

        return holder;
    }


    /**
     * @param inp !null reader
     * @return
     */
    public static IPeptideSpectrumMatch readMGFScan(LineNumberReader inp) {
        return readMGFScan(inp, null);
    }

    /**
     * @param inp  !null reader
     * @param line if non null the firat line of the stricture
     * @return
     */
    @SuppressWarnings("ConstantConditions")
    public static IPeptideSpectrumMatch readMGFScan(LineNumberReader inp, String line) {
        String titleLine = null;
        String sequence = null;
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        String annotation = null;
        try {
            if (line == null)
                line = inp.readLine();

            double massToChargeCalledPpMass = 0;
            int dcharge = 1;
            String title = null;
            while (line != null) {

                if ("".equals(line)) {
                    line = inp.readLine();
                    continue;
                }
                if (BEGIN_IONS.equals(line)) {
                    line = inp.readLine();
                    break;
                }

                line = inp.readLine();
            }
            if (line == null)
                return null;

            List<IPeak> holder = new ArrayList<IPeak>();

            // add scan items
            while (line != null) {
                line = line.trim();
                // ignore empty lines
                if (line.length() == 0) {
                    line = inp.readLine();
                    continue;
                }

                // give up on lines not starting with a letter
                if (!Character.isLetterOrDigit(line.charAt(0))) {
                    line = inp.readLine();
                    continue;

                }


                if (line.contains("=")) {
                    if (line.startsWith("TITLE=")) {
                        titleLine = line;
                        title = buildMGFTitle(line);
                        int index = line.indexOf(",sequence=");
                        if (index > -1) {
                            sequence = line.substring(index + ",sequence=".length()).trim();
                        }
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("PEPMASS=")) {
                        massToChargeCalledPpMass = parsePepMassLine(line);
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("CHARGE=")) {
                        line = line.replace("+", "");
                        final String substring = line.substring("CHARGE=".length());
                        if (substring.contains("."))
                            dcharge = (int) (0.5 + Double.parseDouble(substring));
                        else
                            dcharge = Integer.parseInt(substring);
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("RTINSECONDS=")) {
                        //          retentionTime = line.substring("RTINSECONDS=".length());
                        line = inp.readLine();
                        continue;
                    }

                    boolean tagIsNotHandled = false;
                    // ignored for now
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < NOT_HANDLED_MGF_TAGS.length; i++) {
                        if (line.startsWith(NOT_HANDLED_MGF_TAGS[i])) {
                            tagIsNotHandled = true;
                            line = inp.readLine();
                            break;
                        }

                    }
                    if (tagIsNotHandled)
                        continue;
                    // huh???
                    throw new IllegalStateException("Cannot parse MGF line " + line);
                }
                if (END_IONS.equals(line)) {
                    //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                    double mz = massToChargeCalledPpMass;
                    // maybe this is what is meant - certainly scores better
                    String peptide = sequence;
                    //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                    sequence = null;
                    PeptideSpectrumMatch spectrum = new PeptideSpectrumMatch(
                            title,
                            peptide,
                            dcharge,
                            (float) mz,
                            holder,
                            title // save title - sequence as annotation
                    );
                    if (titleLine != null)
                        handleTitleLine(spectrum, titleLine);
                    return spectrum;
                } else {
                    line = line.replace("\t", " ");
                    String[] items = line.split(" ");
                    // not sure we should let other ceses go but this is safer
                    if (items.length >= 2) {
                        try {
                            float peakMass = Float.parseFloat(items[0].trim());
                            float peakIntensity = Float.parseFloat(items[1].trim());
                            Peak added = new Peak(peakMass, peakIntensity);
                            holder.add(added);
                        } catch (NumberFormatException e) {
                            // I am not happy but I guess we can forgive a little bad data
                            handleBadMGFData(line);
                        }
                    } else {
                        // I am not happy but I guess we can forgive a little bad data
                        handleBadMGFData(line);
                    }
                    line = inp.readLine();

                }
            }
            return null; // or should an exception be thrown - we did not hit an END IONS tag
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings("UnusedParameters")
    protected static void handleTitleLine(Spectrum spectrum, String titleLine) {
        String tl = titleLine.substring("Title=".length());
        String[] items = tl.split(",");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < items.length; i++) {
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String item = items[i];
            // if(item.startsWith(""))
        }

    }


    // constants for handling sptext files
//    public static final String NAME_START = "Name:";
//    public static final String LIBID_START = "LibID:";
//    public static final String MW_START = "MW:";
//    public static final String PRECURSORMZ_START = "PrecursorMZ:";
//    public static final String STATUS_START = "Status:";
//    public static final String FULL_NAME_START = "FullName:";
//    public static final String COMMENT_START = "Comment:";
//    public static final String NUM_PEAKS_START = "NumPeaks:";
//    public static final String NUM_PEAKS_START2 = "Num peaks:";

//    /**
//     * @param inp  !null reader
//     * @param line if non null the firat line of the stricture
//     * @return
//     */
//    public static IPeptideSpectrumMatch readSPTextScan(LineNumberReader inp) {
//        return readSPTextScan(inp, null);
//    }

//    /**
//     * @param inp  !null reader
//     * @param line if non null the first line of the stricture
//     * @return
//     */
//    public static IPeptideSpectrumMatch readSPTextScan(LineNumberReader inp, String line) {
//        String commentLine = null;
//        String peptide = null;
//        String id = null;
//        int charge = 0;
//        List<IPeak> peaks = new ArrayList<IPeak>();
//        Map<String, String> properties = new HashMap<String, String>();
//        int numberPeaks = 0;
//        double precursorMZ = 0;
//
//        String textPart;
//
//
//        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
//        String annotation = null;
//        try {
//            if (line == null)
//                line = inp.readLine();
//
//            while (line != null) {
//
//                if ("".equals(line)) {
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, NAME_START);
//                if (textPart != null) {
//                    String[] items = textPart.split("/");
//                    peptide = items[0];
//                    charge = Integer.parseInt(items[1]);
//                    line = inp.readLine();
//                    break;
//                }
//
//                line = inp.readLine();
//            }
//            if (line == null)
//                return null;
//
//
//            // add scan items
//            while (line != null) {
//                line = line.trim();
//                // ignore empty lines
//                if (line.length() == 0) {
//                    line = inp.readLine();
//                    continue;
//                }
//
//                // give up on lines not starting with a letter
//                char firstChar = line.charAt(0);
//                if (!Character.isLetterOrDigit(firstChar)) {
//                    line = inp.readLine();
//                    continue;
//
//                }
//
//                if (Character.isDigit(firstChar)) {
//                    IPeak peak = buildPeak(line.trim());
//                    if (peaks.size() >= numberPeaks) {
//                        throw new IllegalStateException("too many peaks");
//                    }
//                    peaks.add(peak);
//                    if (peaks.size() >= numberPeaks) {
//                        break;
//                    } else {
//                        line = inp.readLine();
//                        continue;
//                    }
//
//                }
//
//                textPart = getTextPart(line, LIBID_START);
//                if (textPart != null) {
//                    id = textPart;
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, MW_START);
//                if (textPart != null) {
//                    properties.put("molecularWeight", textPart);
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, PRECURSORMZ_START);
//                if (textPart != null) {
//                    precursorMZ = Double.parseDouble(textPart);
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, STATUS_START);
//                if (textPart != null) {
//                    properties.put("Status", textPart);
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, FULL_NAME_START);
//                if (textPart != null) {
//                    properties.put("FullName", textPart);
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, COMMENT_START);
//                if (textPart != null) {
//                    commentLine = textPart;
//                    handleCommentText(textPart, properties);
//                    line = inp.readLine();
//                    continue;
//                }
//                textPart = getTextPart(line, NUM_PEAKS_START);
//                if (textPart != null) {
//                    numberPeaks = Integer.parseInt(textPart);
//                    line = inp.readLine();
//                    //noinspection UnnecessaryContinue
//                    continue;
//                }
//                textPart = getTextPart(line, NUM_PEAKS_START2);
//                if (textPart != null) {
//                    numberPeaks = Integer.parseInt(textPart);
//                    line = inp.readLine();
//                    //noinspection UnnecessaryContinue
//                    continue;
//                }
//                throw new IllegalStateException("cannot understand line " + line);
//
//            }
//            // here we create and build a spectrum
//            //noinspection UnnecessaryLocalVariable
//            PeptideSpectrumMatch spectrum = new PeptideSpectrumMatch(
//                    id,
//                    peptide,
//                    charge,
//                    (float) precursorMZ,
//                    peaks,
//                    commentLine // save title - sequence as annotation
//            );
//            for (String s : properties.keySet()) {
//                spectrum.setProperty(s, properties.get(s));
//            }
//            return spectrum;
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }


    protected static void handleCommentText(String textPart, Map<String, String> properties) {
        List<String> items = splitNameValueText(textPart);
        //noinspection ForLoopReplaceableByForEach
        for (String item : items) {
            handleProperty(item, properties);
        }
    }

    protected static List<String> splitNameValueText(String textPart) {
        List<String> items = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        textPart = textPart.trim();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < textPart.length(); i++) {
            char c = textPart.charAt(i);
            switch (c) {
                case ' ':
                    if (sb.length() > 0) {
                        items.add(sb.toString());
                        sb.setLength(0);
                    }
                    break;

                case '\"':
                    c = textPart.charAt(++i);
                    while (c != '\"') {
                        sb.append(c);
                        c = textPart.charAt(++i);
                    }
                    break;
                default:
                    sb.append(c);

            }
        }
        if (sb.length() < 0) {
            items.add(sb.toString());
            sb.setLength(0);
        }
        return items;
    }

    protected static void handleProperty(String protertyExpression, Map<String, String> properties) {
        if (protertyExpression.contains("=")) {
            String[] items = protertyExpression.split("=");
            String key = items[0].trim();
            String value = items[1].trim();
            properties.put(key, value);
            return;
        }

        properties.put(protertyExpression, "true");


    }


    protected static IPeak buildPeak(String textPart) {
        String[] items = textPart.split("\t");
        float mz = Float.parseFloat(items[0]);
        float intensity = Float.parseFloat(items[1]);
        return new Peak(mz, intensity);
    }

    protected static String getTextPart(String line, String prefix) {
        if (line.startsWith(prefix))
            return new String(line.substring(prefix.length()).trim());  // I am worried about small substrings of big strings
        return null;
    }

    /**
     * *******************************
     * Error handling code for MGF parse failuer
     * *******************************
     */
    public static final int MAX_NUMBER_BAD_MGF_LINES = 2000;
    private static int gNumberBadMGFLines = 0;

    /**
     * we cannot parse a line of the form mass peak i.e.  370.2438965 3.906023979 in an
     * mgf file - the first  MAX_NUMBER_BAD_MGF_LINES output a message on stderr than
     * exceptions are thrown
     *
     * @param line !null line we cannot handle
     * @throws IllegalStateException after  MAX_NUMBER_BAD_MGF_LINES are seen
     */
    protected static void handleBadMGFData(String line) throws IllegalStateException {
        if (gNumberBadMGFLines++ > MAX_NUMBER_BAD_MGF_LINES)
            throw new IllegalStateException("cannot read MGF data line " + line +
                    " failing after " + gNumberBadMGFLines + " errors");
        System.err.println("Cannot parse mgf line " + line);

    }

    /**
     * parse an mgf file - ised in testing
     *
     * @param filename !null name of an existing readible file
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void guaranteeMGFParse(String filename) {
        try {
            guaranteeMGFParse(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * parse an mgf file - used in testing
     *
     * @param is !null open inputstream
     */
    public static void guaranteeMGFParse(InputStream is) {
        LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
        ISpectrum scan = readMGFScan(inp, null);
        while (scan != null) {
            scan = readMGFScan(inp, null);
        }

    }

    /**
     * comvert   PEPMASS=459.17000000000002 8795.7734375   into  459.17
     *
     * @param pLine line as above
     * @return indicasted mass
     */
    public static double parsePepMassLine(final String pLine) {
        final double mass;
        String numeric = pLine.substring("PEPMASS=".length());
        String massStr = numeric.split(" ")[0];
        mass = Double.parseDouble(massStr);
        return mass;
    }


    protected static String buildMGFTitle(String line) {
        String[] items = line.split(",");
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        String label = line.substring("TITLE=".length());
        String spectrumId = label;
        if (items.length > 1) {
            spectrumId = items[0].trim().substring("TITLE=id=".length());
        }
        return new String(spectrumId);  // I am worried about small substrings of big strings
    }


    /**
     * turn strings, resources and filenames into line number readers
     *
     * @param des
     * @return
     */
    public static LineNumberReader getDescribedReader(String des) {
        // maybe a string
        if (des.startsWith("str://")) {
            String substring = des.substring("str://".length());
            Reader isr = new StringReader(substring);
            return new LineNumberReader(isr);
        }

        // maybe a resource
        if (des.startsWith("res://")) {
            String substring = des.substring("res://".length());
            InputStream inputStream = ParserUtilities.class.getResourceAsStream(substring);
            if (inputStream == null)
                return null;
            return new LineNumberReader(new InputStreamReader(inputStream));
        }

        File f = new File(des);
        if (f.exists() && !f.isDirectory() && f.canRead()) {
            try {
                Reader isr = new FileReader(f);
                return new LineNumberReader(isr);
            } catch (FileNotFoundException e) {
                return null;
            }

        }

        return null; // give up
    }

    public static ConsensusSpectraItems[] readClusters(File file) {
        List<ConsensusSpectraItems> holder = new ArrayList<ConsensusSpectraItems>();

        try {
            LineNumberReader inp = new LineNumberReader(new FileReader(file));

            String line = inp.readLine();
            while (line != null) {
                ConsensusSpectraItems cluster = readConsensusSpectraItems(inp, line);
                holder.add(cluster);
                line = inp.readLine();
            }


            ConsensusSpectraItems[] ret = new ConsensusSpectraItems[holder.size()];
            holder.toArray(ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            boolean isMGF = arg.toLowerCase().endsWith(".mgf");

            ConsensusSpectraItems[] items = readClusters(new File(arg));
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < items.length; j++) {
                //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                ConsensusSpectraItems item = items[j];

            }

            IPeptideSpectrumCluster[] scs = readSpectralCluster(arg);
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < scs.length; j++) {
                IPeptideSpectrumCluster sc = scs[j];
                StringBuilder sb = new StringBuilder();
                final MGFSpectrumAppender spectrumAppender = new MGFSpectrumAppender();
                if (isMGF) {
                    spectrumAppender.appendSpectrum(sb, sc.getConsensusSpectrum());
                }else {
                    final CGFClusterAppender clusterAppender = new CGFClusterAppender(spectrumAppender);
                    clusterAppender.appendCluster(sb, sc);
                }
            }
        }
    }

}
