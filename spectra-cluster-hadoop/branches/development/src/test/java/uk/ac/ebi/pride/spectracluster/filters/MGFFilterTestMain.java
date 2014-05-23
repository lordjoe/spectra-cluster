package uk.ac.ebi.pride.spectracluster.filters;

import com.lordjoe.filters.*;
import com.lordjoe.utilities.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.filters.MGFFilterTestMain
 *
 * @author Steve Lewis
 * @date 19/05/2014
 */
public class MGFFilterTestMain {
    public static final String MGF_FILTER =
            "<Filters>\n" +
                    "<Not>\n" +
                    "<FileFilter name=\"temp\" />\n" +
                    "</Not>\n" +
                    "<Not>\n" +
                    "<FileFilter name=\"internal\" />\n" +
                    "</Not>\n" +
                    "<Not>\n" +
                    "<FileFilter name=\"submitted\" />\n" +
                    "</Not>\n" +
                    "<Or>\n" +
                    "<FileFilter extension=\"mgf\" />\n" +
                    "<FileFilter extension=\"mztab\" />\n" +
                    "</Or>\n" +
                    "</Filters>\n";

     /**
     * find directories with mztab and mgf files
     * @param passing all files with mgf and mztab extension
     * @return
     */
    public static @Nonnull List<File> findProcessedDirectories(@Nonnull List<File> passing) {
         Set<File> holder = new HashSet<File>();
        for (File file : passing) {
            final String name = file.getAbsolutePath();
            final long length = file.length();
            if (name.endsWith(".mztab")) {
                holder.add(file.getParentFile());
            }
        }

        Set<File> toRemove = new HashSet<File>();
        for (File file : holder) {
           if(!containsMGF(file))
               toRemove.add(file);
        }
        holder.removeAll(toRemove);
        List<File> ordered = new ArrayList<File>(holder);
        Collections.sort(ordered);
         return ordered;

    }

    /**
     * true if the directory contains an mgf file
     * @param file
     * @return
     */
    public static boolean containsMGF(@Nonnull File file) {
         File[] items = file.listFiles(new FilenameFilter() {
             @Override public boolean accept(File dir, String name) {
                return name.endsWith(".mgf");
             }
         });
        if(items != null && items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                File item = items[i];
                 if(item.length() > 0)
                     return true;
            }
            return false;
        } else {
            return false;
        }
    }


    public static void addInterestingFiles(@Nonnull TypedFilterCollection filters,@Nonnull List<File> passing, @Nonnull File startDir) {
        ElapsedTimer dir = new ElapsedTimer();
        List<File> passed = FileFilters.applyFilters(startDir, filters);
        passing.addAll(passed);
        dir.formatElapsed("examined " + startDir.getName());
    }

    public static void main(String[] args) throws Exception {
        //    File filterFile = new File(args[1]);
        final TypedFilterCollection filters = TypedFilterCollection.parse(MGF_FILTER);


        List<File> passing = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            ElapsedTimer total = new ElapsedTimer();
            String arg = args[i];
            File startDir = new File(arg);
            addInterestingFiles(filters, passing, startDir);
             total.showElapsed("handled " + arg + " number " + i + " files " + passing.size());
           }

        List<File> processedDirectories = findProcessedDirectories(passing);

        PrintWriter out = new PrintWriter(new FileWriter("c:/Processing/DirectoriesToProcess.txt"));
        for (File processedDirectory : processedDirectories) {
             String absolutePath = processedDirectory.getAbsolutePath();
            absolutePath = absolutePath.replace("\\","/");
            absolutePath = absolutePath.replace("P://","");
            absolutePath = absolutePath.replace("/generated","");
              out.println(absolutePath);
        }
        out.close();

        long mzTabSizes = 0;
        long mgfSizes = 0;
        int mzTabCount = 0;
        int mgfCount = 0;


        for (File file : passing) {
            final String name = file.getAbsolutePath();
            final long length = file.length();
            if (name.endsWith(".mgf")) {
                mgfCount++;
                mgfSizes += length;
            } else {
                mzTabCount++;
                mzTabSizes += length;

            }
            //      System.out.println(name);
        }
        System.out.println("mzTab size " + mzTabSizes / 1000000 + "m" + " count " + mzTabCount);
        System.out.println("MGF size " + mgfSizes / 1000000 + "m" + " count " + mgfCount);

    }


}
