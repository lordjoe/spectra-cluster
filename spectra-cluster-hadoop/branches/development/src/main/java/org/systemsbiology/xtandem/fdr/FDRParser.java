package org.systemsbiology.xtandem.fdr;

import java.io.*;
import java.util.*;


/**
 * org.systemsbiology.xtandem.fdr.FDRParser
 *
 * @author attilacsordas
 * @date 09/05/13
 */
public class FDRParser {

    public static final boolean USE_EXPECTED = false; // otherwise use score

    private final File m_File;
    private final IDiscoveryDataHolder m_Handler;
    private final IDiscoveryDataHolder m_ModifiedHandler;
    private final IDiscoveryDataHolder m_UnModifiedHandler;
     private final int m_MaxHits;


    public FDRParser(String filename) {
        this(filename, 1);
    }

    public FDRParser(String filename, int maxhits) {
        m_File = new File(filename);
        m_MaxHits = maxhits;
        if (USE_EXPECTED)
            m_Handler = FDRUtilities.getDiscoveryDataHolder("Default Algorighm", false);      // better us low
        else
            m_Handler = FDRUtilities.getDiscoveryDataHolder("Default Algorighm", true);   // better us high


        m_ModifiedHandler = FDRUtilities.getDiscoveryDataHolder("Default Algorighm", true);   // better us high

        m_UnModifiedHandler = FDRUtilities.getDiscoveryDataHolder("Default Algorighm", true);   // better us high


        readFileAndGenerateFDR();    // read file populate the  IDiscoveryDataHolder

    }

    public File getFilename() {
        return m_File;
    }

    public IDiscoveryDataHolder getHandler() {
        return m_Handler;
    }

    public IDiscoveryDataHolder getModifiedHandler() {
        return m_ModifiedHandler;
    }

    public IDiscoveryDataHolder getUnModifiedHandler() {
        return m_UnModifiedHandler;
    }

    /**
     *
     */
    private void readFileAndGenerateFDR() {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(m_File));
            String line = rdr.readLine();
            while (line != null) {
                if (line.contains("<search_hit")) {
                    String[] searchHitLines = readSearchHitLines(line, rdr);
                    //              System.out.println(line);
                    handleSearchHit(searchHitLines);
                 }
                line = rdr.readLine();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected String[] readSearchHitLines(String line, LineNumberReader rdr) {
        List<String> holder = new ArrayList<String>();

        try {
            while (line != null) {
                holder.add(line);
                if (line.contains("</search_hit")) {
                    break; // done
                }
                line = rdr.readLine();  // read next line

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    protected void handleSearchHit(String[] lines) {

        Double expectedValue = null;
        Double hyperScoreValue = null;
        int index = 0;
        String line = lines[index++];   // handle first line
        boolean trueHit = !line.contains("protein=\"DECOY_");
        boolean processSpectrum = parseHitValue(line) <= m_MaxHits;
        boolean isModified =  false;

        for (; index < lines.length; index++) {
            line = lines[index];

            if (line.contains("</search_hit"))
                break;         // we are done

            if (line.contains(" modified_peptide="))
                isModified =  true;


            if (line.contains("<search_score name=\"hyperscore\" value=\"")) {
                hyperScoreValue = parseValue(line);
            }
            if (line.contains("<search_score name=\"expect\" value=\"")) {
                expectedValue = parseValue(line);
            }
            if (line.contains("protein=\"DECOY_")) {  // another protein
                if (trueHit)
                    processSpectrum = false; // one decoy one not
            }
            if (line.contains("<alternative_protein")) {  // another protein
                if (!trueHit && !line.contains("protein=\"DECOY_")) // we start as decoy and fit to a real
                    processSpectrum = false; // one decoy one not
            }
        }

        if (processSpectrum) {
            final IDiscoveryDataHolder hd = getHandler();
            double score;
            if (USE_EXPECTED)
                score = expectedValue;
            else
                score = hyperScoreValue;

            if (trueHit) {
                hd.addTrueDiscovery(score);
                if(isModified)
                    m_ModifiedHandler.addTrueDiscovery(score);
                else
                    m_UnModifiedHandler.addTrueDiscovery(score);
            } else {
                hd.addFalseDiscovery(score);
                if(isModified)
                    m_ModifiedHandler.addFalseDiscovery(score);
                else
                    m_UnModifiedHandler.addFalseDiscovery(score);
            }

        }


    }

    public static double parseValue(String line) {
        String s = parseQuotedValue(line, "value");
        if (s.length() == 0)
            return 0;
        return Double.parseDouble(s);
    }

    public static boolean parseIsModifiedValue(String line) {
        String s = parseQuotedValue(line, "peptide");
        if (s.length() == 0)
            return false;
        return s.contains("[");    // modification string
    }

    public static int parseHitValue(String line) {
        String s = parseQuotedValue(line, "hit_rank");
        if (s.length() == 0)
            return 0;
        return Integer.parseInt(s);
    }

    /**
     * return a section of
     *
     * @param line
     * @param start
     * @return
     */
    public static String parseQuotedValue(String line, String start) {
        final String str = start + "=\"";
        int index = line.indexOf(str);
        if (index == -1)
            return "";
        index += str.length();
        int endIndex = line.indexOf("\"", index);
        if (endIndex == -1)
            return "";
        return line.substring(index, endIndex);
    }


    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            FDRParser fdrParser = new FDRParser(arg);
            final IDiscoveryDataHolder handler = fdrParser.getHandler();
            //final String s = FDRUtilities.listFDRAndCount(handler);
            final String s = FDRUtilities.listFDRAndRates(handler);
            System.out.println(s);
            System.out.println("====================================");
             final String smod = FDRUtilities.listFDRAndRates(fdrParser.getModifiedHandler());
            System.out.println(smod);
            System.out.println("====================================");
            final String sunmod = FDRUtilities.listFDRAndRates(fdrParser.getUnModifiedHandler());
            System.out.println(sunmod);
        }

    }

}
