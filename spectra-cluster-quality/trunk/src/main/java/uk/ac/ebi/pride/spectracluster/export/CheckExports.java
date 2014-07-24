package uk.ac.ebi.pride.spectracluster.export;

import com.lordjoe.utilities.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.export.CheckExports
 * User: Steve
 * Date: 7/21/2014
 */
public class CheckExports {

    private Map<String, String> projectTodirectory = new HashMap<String, String>();
    private List<String> unseenDirectories = new ArrayList<String>();

    protected void readDirectories(File inp) {
        String[] lines = FileUtilities.readInLines(inp);
        for (String line : lines) {
            if (line.length() > 0)
                handleLine(line);
        }
    }

    protected void handleLine(String pLine) {
        String line = pLine.trim();
        String dir = line.substring(8);
        projectTodirectory.put(dir, line);
    }


    protected void appendUnseenDirectories(final Appendable pOut) {
        String[] unseen = unseenDirectories.toArray(new String[unseenDirectories.size()]);
        try {
            for (String s : unseen) {
                pOut.append(s);
                pOut.append("\n");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    protected void noteFiles(final File pSeen) {
        String[] lines = FileUtilities.readInLines(pSeen);
        for (String line : lines) {
            if (line.length() > 0)
                handleSeen(line);
        }
    }

    protected void handleSeen(final String pLine) {
        String dir = pLine.trim().replace(".mgf", "");
        if (!projectTodirectory.containsKey(dir))
            unseenDirectories.add(dir);

    }

    /**
     * Sample command line  UsefulDirectoriesToProcess.txt exported_directories.txt
      * @param args program arguments
     */
    public static void main(String[] args) {
        File attempted = new File(args[0]);
        File seen = new File(args[1]);
        if (!attempted.exists())
            throw new IllegalArgumentException("bad file " + attempted.getAbsolutePath()); // ToDo change
        if (!seen.exists())
            throw new IllegalArgumentException("bad file " + seen.getAbsolutePath()); // ToDo change
        CheckExports ce = new CheckExports();
        ce.readDirectories(attempted);
        ce.noteFiles(seen);
        ce.appendUnseenDirectories(System.out);
    }


}
