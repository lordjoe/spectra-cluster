package org.systemsbiology.xml;

import java.io.*;

/**
 * org.systemsbiology.xml.XMLUtilities
 *
 * @author Steve Lewis
 * @date Feb 1, 2011
 */
public class XMLUtilities
{
    public static XMLUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = XMLUtilities.class;

    /**
     * copy code that works well for text files organized into lines
     * @param inp - !null exisating readable file
     * @param out - writable file
     */
    public static void copyFileLines(File inp,File out) {
        LineNumberReader reader = null;
         PrintWriter outWriter = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(inp)));
            outWriter = new PrintWriter(new FileWriter(out));
            String line = reader.readLine();
            while(line != null) {
                outWriter.println(line);
                line = reader.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if(reader != null)
                    reader.close();
                if(outWriter != null)
                    outWriter.close();
            }
            catch (IOException e) {
              }
        }

    }
}
