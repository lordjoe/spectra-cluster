package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.PartitioningTests
 * We are having problems with partitioning -
 * This code reads the sequence files generated by a job and looks at the partition distribution
 * It does not use hadoop for speed and debugging
 *
 * @author Steve Lewis
 * @date 8/30/13
 */
public class PartitioningTests {

    private final List<String> keys = new ArrayList<String>();
    private final File targetDirectory;
    private final Configuration conf = new Configuration();
    private final Text onlyKey = new Text();
    @SuppressWarnings("UnusedDeclaration")
    private final Text onlyValue = new Text();


    public PartitioningTests(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void addKey(String key) {
        keys.add(key);
    }

    protected void writeKeys(File out) {
        try {
            PrintWriter pout = new PrintWriter(new FileWriter(out));
            for (String key : keys) {
                pout.println(key);
            }
            pout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void readKeys() {
        File[] files = targetDirectory.listFiles();
        if (files == null)
            throw new IllegalArgumentException("Directory is empty" + targetDirectory.getAbsolutePath());
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            readKeys(file);
        }
    }

    public void readKeys(File file) throws RuntimeException {
        SequenceFile.Reader rdr = null;
        try {
            rdr = SpectraHadoopUtilities.buildSequenceFileReader(file, conf);
        } catch (Exception e) {
            return;   // forgive - not everything will work
           }
        try {
            System.out.println(file.getPath());
            while (rdr.next(onlyKey)) {
                String key = onlyKey.toString();
                addKey(key);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        } finally {
            try {
                rdr.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new RuntimeException(e);

            }
        }


    }

    @SuppressWarnings("UnusedDeclaration")
    protected static void readAndWriteKeys(String[] args) {
        File f = new File(args[0]);
        if (!f.exists() || !f.isDirectory())
            throw new IllegalArgumentException("Directory does not exits " + args[0]);
        File out = new File(args[1]);

        PartitioningTests pt = new PartitioningTests(f);
        pt.readKeys();
        pt.writeKeys(out);
    }


    protected static void partitionKeys(String[] args) {
        IWideBinner binner = SpectraHadoopUtilities.DEFAULT_WIDE_MZ_BINNER;
        File f = new File(args[0]);
        if (!f.exists() || !f.isFile())
            throw new IllegalArgumentException("File does not exits " + args[0]);
        String[] lines = FileUtilities.readInLines(f);
        List<ChargeBinMZKey> holder = new ArrayList<ChargeBinMZKey>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            ChargeMZKey k1 = new ChargeMZKey(line);
            int charge = k1.getCharge();
            double mz = k1.getPrecursorMZ();
            int[] bins = binner.asBins(mz);
            for (int j = 0; j < bins.length; j++) {
                int bin = bins[j];
                holder.add(new ChargeBinMZKey(charge, bin, mz));
            }
        }
        ChargeBinMZKey[] keys = holder.toArray(new ChargeBinMZKey[holder.size()]);
        examinePartitions(keys);

    }


    protected static void examinePartitions(ChargeBinMZKey[] keys) {
        int numberReducers = HadoopUtilities.DEFAULT_NUMBER_REDUCERS;
        int[] partitions = new int[numberReducers];
        for (int i = 0; i < keys.length; i++) {
            ChargeBinMZKey key = keys[i];
            int hash = key.getPartitionHash();
            int partition = hash % numberReducers;
            partitions[partition]++;
        }

        for (int i = 0; i < partitions.length; i++) {
            int partition = partitions[i];
            System.out.println("partition " + partition);
        }
    }

    public static void main(String[] args) {
        //    readAndWriteKeys(args);
        partitionKeys(args);
    }


}
