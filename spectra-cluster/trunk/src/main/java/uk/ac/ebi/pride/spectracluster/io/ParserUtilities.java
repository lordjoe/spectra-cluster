package uk.ac.ebi.pride.spectracluster.io;


import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectralCluster;
import uk.ac.ebi.pride.spectracluster.consensus.IConsensusSpectrumBuilder;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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

    public static final String CLUSTERING_EXTENSION = ".clustering";
    public static final String CGF_EXTENSION = ".cgf";

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static ICluster[] readSpectralCluster(String inp) {
        return readSpectralCluster(new File(inp));
    }

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static ICluster[] readSpectralCluster(File inp) {
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
        ICluster cls = readSpectralCluster(inp, null);
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
        ISpectrum cls = readFilteredMGFScan(inp);  // filter peaks

        while (cls != null) {
            for (SpectrumCreateListener lstn : listerners) {
                lstn.onSpectrumCreate(cls);
            }
            cls = readFilteredMGFScan(inp);
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
    public static ICluster[] readSpectralCluster(LineNumberReader inp) {
        List<ICluster> holder = new ArrayList<ICluster>();
        ICluster cls = readSpectralCluster(inp, null);
        while (cls != null) {
            holder.add(cls);
            cls = readSpectralCluster(inp, null);
        }

        ICluster[] ret = new ICluster[holder.size()];
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
    public static ICluster readSpectralCluster(LineNumberReader inp, String line) {
        ICluster ret = null;
        try {
            if (line == null)
                line = inp.readLine();
            while (line != null) {
                if (line.startsWith(BEGIN_CLUSTER)) {
                    //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                    int charge = chargeFromClusterLine(line);
                    String id = idFromClusterLine(line);
                    ret = new SpectralCluster(id, Defaults.getDefaultConsensusSpectrumBuilder());
                    break;
                }
                // naked spectrum
                if (line.startsWith(BEGIN_IONS)) {
                    ISpectrum internalComplete = readMGFScan(inp, line);
                    final List<IPeak> peaks = internalComplete.getPeaks();
                    final List<IPeak> filteredPeaks = Defaults.getDefaultPeakFilter().filter(peaks);
                    ISpectrum internal = new Spectrum(internalComplete, filteredPeaks);
                    ret = new SpectralCluster(internal.getId(), Defaults.getDefaultConsensusSpectrumBuilder());
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
        IConsensusSpectrumBuilder sb = Defaults.getDefaultConsensusSpectrumBuilder();
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
            "USER00",
            "USER01",
               "USER04",
            "USER05",
            "USER06",
            "USER08",
            "USER09",
            "USER10",
            "USER11",
            //   "TAXONOMY=",
            //      "SEQ=",
            "COMP=",
            "TAG=",
            "ETAG=",
            "SCANS=",
            "IT_MODS=",
            //         "INSTRUMENT=",
    };

    /**
     * @param inp !null reader
     * @return
     */
    public static ISpectrum[] readMGFScans(LineNumberReader inp) {
        List<ISpectrum> holder = new ArrayList<ISpectrum>();
        ISpectrum spectrum = readMGFScan(inp);
        while (spectrum != null) {
            holder.add(spectrum);
            spectrum = readMGFScan(inp);
         }
        ISpectrum[] ret = new ISpectrum[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * @param inp !null existing file
     * @return !null array of spectra
     */
    public static ISpectrum[] readMGFScans(File inp) {
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
    public static List<ICluster> readMGFClusters(File inp) {
        ISpectrum[] scans = readMGFScans(inp);
        List<ICluster> holder = new ArrayList<ICluster>();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < scans.length; i++) {
            ISpectrum scan = scans[i];
             final ICluster e = ClusterUtilities.asCluster(scan);
              holder.add(e);
        }

        return holder;
    }


    /**
     * filter peaks list
     *
     * @param inp
     * @return
     */
    public static ISpectrum readFilteredMGFScan(LineNumberReader inp) {
        ISpectrum cls = readMGFScan(inp);
        if (cls == null)
            return null;

        final List<IPeak> peaks = cls.getPeaks();
        final List<IPeak> filteredPeaks = Defaults.getDefaultPeakFilter().filter(peaks);
        cls = new Spectrum(cls, filteredPeaks);
        return cls;
    }


    /**
     * @param inp !null reader
     * @return
     */
    public static ISpectrum readMGFScan(LineNumberReader inp) {
        return readMGFScan(inp, null);
    }

    /**
     * @param inp  !null reader
     * @param line if non null the firat line of the stricture
     * @return
     */
    @SuppressWarnings("ConstantConditions")
    public static ISpectrum readMGFScan(LineNumberReader inp, String line) {
        String titleLine = null;
        String sequence = null;
        String protein = null;
        String species = null;
        String modifications = null;

        Properties props = new Properties();
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        String annotation = null;
        try {
            if (line == null)
                line = inp.readLine();

             double massToChargeCalledPpMass = 0;
            int dcharge = 1;
            String title = null;
            while (line != null) {
                line = line.trim();

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

                    if (line.startsWith("TAXONOMY=")) {
                        species = line.substring("TAXONOMY=".length());
                        line = inp.readLine();
                        continue;
                    }

                    if (line.startsWith("USER02=")) {
                        protein = line.substring("USER02=".length());
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("USER03=")) {
                          modifications = line.substring("USER03=".length());
                          line = inp.readLine();
                          continue;
                      }
                     if (KnownProperties.addMGFProperties(props, line)) {
                        line = inp.readLine();
                        continue;
                    }

                    boolean tagIsNotHandled = false;
                    // ignored for now
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < NOT_HANDLED_MGF_TAGS.length; i++) {
                        String notHandledMgfTag = NOT_HANDLED_MGF_TAGS[i];
                        if (line.startsWith(notHandledMgfTag)) {
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


                    Collections.sort(holder);

                    // Filter peaks
                    holder = Defaults.getDefaultPeakFilter().filter(holder);


                    ISpectrum spectrum = new Spectrum(
                            title,
                            dcharge,
                            (float) mz,
                            Defaults.getDefaultQualityScorer(),
                            holder
                    );

                    // add any properties we find
                    for (String s : props.stringPropertyNames()) {
                        spectrum.setProperty(s, props.getProperty(s));
                    }
                    props.clear();

                    if (species != null)
                        spectrum.setProperty(KnownProperties.TAXONOMY_KEY, species);
                    if (peptide != null)
                        spectrum.setProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY, peptide);
                    if (peptide != null)
                        spectrum.setProperty(KnownProperties.ANNOTATION_KEY, title);
                    if (protein != null)
                        spectrum.setProperty(KnownProperties.PROTEIN_KEY, protein);
                    if (modifications != null)
                          spectrum.setProperty(KnownProperties.MODIFICATION_KEY, modifications);
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
    protected static void handleTitleLine(ISpectrum spectrum, String titleLine) {
        String tl = titleLine.substring("Title=".length());
        String[] items = tl.split(",");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < items.length; i++) {
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String item = items[i];
            // if(item.startsWith(""))
        }

    }

    /**
     * *******************************
     * Error handling code for MGF parse failure
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
     * convert   PEPMASS=459.17000000000002 8795.7734375   into  459.17
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
        while (label.startsWith("id="))
            label = label.substring("id=".length()); // drop starting id=
        String spectrumId = label;
        if (items.length > 1) {
            spectrumId = items[0].trim().substring("TITLE=id=".length());
        }
        return spectrumId;
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

            ICluster[] scs = readSpectralCluster(arg);
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < scs.length; j++) {
                ICluster sc = scs[j];
                StringBuilder sb = new StringBuilder();
                final MGFSpectrumAppender spectrumAppender = MGFSpectrumAppender.INSTANCE;
                if (isMGF) {
                    spectrumAppender.appendSpectrum(sb, sc.getConsensusSpectrum());
                } else {
                    final CGFClusterAppender clusterAppender = CGFClusterAppender.INSTANCE;
                    clusterAppender.appendCluster(sb, sc);
                }
            }
        }
    }

}
