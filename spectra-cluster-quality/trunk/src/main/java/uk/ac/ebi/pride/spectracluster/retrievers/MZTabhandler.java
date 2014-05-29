package uk.ac.ebi.pride.spectracluster.retrievers;

import com.lordjoe.filters.*;
import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.*;
import uk.ac.ebi.pride.jmztab.utils.errors.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.retrievers.MZTabhandler
 *
 * @author Steve Lewis
 * @date 20/05/2014
 */
public class MZTabhandler {

    public static List<File> getMZTabFiles(File directory) {
        ITypedFilter<File> mzTabFiler = FileFilters.getHasExtensionFilter("mztab");
        final List<File> files = FileFilters.applyFileFilters(directory, mzTabFiler);
        return files;
    }

    private MZTabFile mzTabs;

    public static final int LOTS_OF_ERRORS = 100000;
    public MZTabhandler(File inp) {
        try {
            // maybe info means do not report
            // specify  LOTS_OF_ERRORS since the constructor containing level has a bug
            MZTabFileParser parser = new MZTabFileParser(inp, new NullOutputStream(), MZTabErrorType.Level.Info,LOTS_OF_ERRORS);
            mzTabs = parser.getMZTabFile();
            if (mzTabs == null)
                return;
            final Collection<PSM> psMs = mzTabs.getPSMs();
            final Collection<Comment> comments = mzTabs.getComments();
            final Collection<Protein> proteins = mzTabs.getProteins();

        } catch (MZTabErrorOverflowException e) {
            e.printStackTrace();   // might happen on too many errors
            mzTabs = null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();   // might happen on too many errors
            mzTabs = null;
        } catch (RuntimeException e) {
            e.printStackTrace();  // might happen on too many errors
            mzTabs = null;
        } catch (IOException e) {
            e.printStackTrace();  // might happen on too many errors
            mzTabs = null;
        }
    }

    public MZTabFile getMzTabs() {
        return mzTabs;
    }

    /**
     * Writes to nowhere
     */
    public static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }

        @Override public void close() throws IOException {
        }
    }

    public static void main(String[] args) throws Exception {
        ElapsedTimer et = new ElapsedTimer();
        List<MZTabhandler> holder = new ArrayList<MZTabhandler>();
        for (int i = 0; i < args.length; i++) {
            File directory = new File(args[i]);
            List<File> files = getMZTabFiles(directory);
            for (File tabFile : files) {
                MZTabhandler handler = new MZTabhandler(tabFile);
                holder.add(handler);
                et.showElapsed("Read " + tabFile + " of length " + tabFile.length());
                et.reset();
            }
            for (MZTabhandler mzTabhandler : holder) {
                final MZTabFile mzTabs = mzTabhandler.getMzTabs();
                final Collection<PSM> psMs = mzTabs.getPSMs();
                final Collection<Comment> comments = mzTabs.getComments();
                final Collection<Protein> proteins = mzTabs.getProteins();
            }
        }
    }
}
