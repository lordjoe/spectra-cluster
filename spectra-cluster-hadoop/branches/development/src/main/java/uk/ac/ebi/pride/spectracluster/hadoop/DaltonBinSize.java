package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.xtandem.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.DaltonBinSize
 *
 * @author Steve Lewis
 * @date 30/10/13
 */
@SuppressWarnings("UnusedDeclaration")
public class DaltonBinSize {

    private static int gTotalSpectra;
    private static Map<Integer, Integer> gMZ_to_number = new HashMap<Integer, Integer>();

    @SuppressWarnings("UnusedDeclaration")
    public static int getNumberSpectraWithMZ(int mz, TaskAttemptContext context) {
        Configuration conf = context.getConfiguration();
        return getNumberSpectraWithMZ(mz, conf);
    }

    public static int getTotalSpectra(Configuration conf) {
        if (gTotalSpectra ==  0) {
              populateMap(conf);
          }
        return gTotalSpectra;
    }

    public static int getNumberSpectraWithMZ(int mz, Configuration conf) {
        getTotalSpectra(  conf); // this forces reading of the map
        Integer ret = gMZ_to_number.get(mz);
        if (ret == null) {
            gMZ_to_number.put(mz, 0);
            return 0;
        }
        return ret;
    }

    protected static void populateMap(Configuration conf) {
        populateMapFromCounters(conf);

    }

    protected static void populateMapFromCounters(Configuration conf) {
        try {
            String fileName = XTandemHadoopUtilities.buildCounterFileName("SpectraPeakClustererPass1.counters", conf);
            FileSystem fileSystem = FileSystem.get(conf);


            Map<Integer, Integer> ret = XTandemHadoopUtilities.readBinCounters(fileSystem, fileName);
            for (Integer key : ret.keySet()) {
                gTotalSpectra += ret.get(key);
            }
            gMZ_to_number.putAll(ret);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

    }
}
