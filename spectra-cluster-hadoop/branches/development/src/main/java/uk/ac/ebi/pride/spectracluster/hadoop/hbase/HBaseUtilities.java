package uk.ac.ebi.pride.spectracluster.hadoop.hbase;
   /*
     * Compile and run with:
     * javac -cp `hbase classpath` TestHBase.java
     * java -cp `hbase classpath` TestHBase
     */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.hbase.HBaseUtilities
 * eventuaklly thisd will be test code and ststic functions but for now it
 * is where I try to access HBase
 *
 * @author Steve Lewis
 * @date 30/09/13
 */
public class HBaseUtilities {

    @SuppressWarnings("UnusedDeclaration")
    public static void runHbase(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);
        try {
            HTable table = new HTable(conf, "test-table");
            Put put = new Put(Bytes.toBytes("test-key"));
            put.add(Bytes.toBytes("cf"), Bytes.toBytes("q"), Bytes.toBytes("value"));
            table.put(put);
        } finally {
            admin.close();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }
}
