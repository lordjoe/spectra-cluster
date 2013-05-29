package uk.ac.ebi.pride.spectracluster.util;


import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

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

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static ISpectralCluster[] readSpectralCluster(String inp) {
        return readSpectralCluster(new File(inp));
    }

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static ISpectralCluster[] readSpectralCluster(File inp) {
        try {
            return readSpectralCluster(new LineNumberReader(new FileReader(inp)));
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * See ParserTests for an example
     *
     * @param inp !null reader
     * @return
     */
    public static ISpectralCluster[] readSpectralCluster(LineNumberReader inp) {
        List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        ISpectralCluster cls = readSpectralCluster(inp, null);
        while (cls != null) {
            holder.add(cls);
            cls = readSpectralCluster(inp, null);
        }

        ISpectralCluster[] ret = new ISpectralCluster[holder.size()];
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
    public static ISpectralCluster readSpectralCluster(LineNumberReader inp, String line) {
        ISpectralCluster ret = null;
        try {
            if (line == null)
                line = inp.readLine();
            while (line != null) {
                if (line.startsWith(BEGIN_CLUSTER)) {
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
        ISpectrum concensus = null;
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
        ret.setConcensus(concensus);
        ret.setSpectra(holder);
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
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.startsWith("Charge=")) {
                return Integer.parseInt(s.substring("Charge=".length()));
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
    public static IPeptideSpectrumMatch readMGFScan(LineNumberReader inp, String line) {
        try {
            if (line == null)
                line = inp.readLine();

            double massToChargeCalledPpMass = 0;
            double dcharge = 1;
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

            //           ret.setLabel(title);

//            line = inp.readLine();

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
                        title = buildMGFTitle(line);
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
                        dcharge = Double.parseDouble(line.substring("CHARGE=".length()));
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
                    double mz = massToChargeCalledPpMass;
                    // maybe this is what is meant - certainly scores better
                    String peptide = null;

                    PeptideSpectrumMatch spectrum = new PeptideSpectrumMatch(
                            title,
                            peptide,
                            dcharge,
                            mz,
                            holder
                    );
                    return spectrum;
                } else {
                    line = line.replace("\t", " ");
                    String[] items = line.split(" ");
                    // not sure we should let other ceses go but this is safer
                    if (items.length >= 2) {
                        try {
                            double peakMass = Double.parseDouble(items[0].trim());
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
        String label = line.substring("TITLE=".length());
        String spot_id = label;
        if (items.length > 1)
            spot_id = items[1].trim().substring("Spot_Id: ".length());
        return spot_id;
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
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            boolean isMGF = arg.toLowerCase().endsWith(".mgf");

            ConsensusSpectraItems[] items = readClusters(new File(arg));
            for (int j = 0; j < items.length; j++) {
                ConsensusSpectraItems item = items[j];

            }

            ISpectralCluster[] scs = readSpectralCluster(arg);
            for (int j = 0; j < scs.length; j++) {
                ISpectralCluster sc = scs[j];
                StringBuilder sb = new StringBuilder();
                if (isMGF)
                    sc.appendSpectra(sb);
                else
                    sc.append(sb);

                System.out.println(sb.toString());
            }
        }
    }

}
