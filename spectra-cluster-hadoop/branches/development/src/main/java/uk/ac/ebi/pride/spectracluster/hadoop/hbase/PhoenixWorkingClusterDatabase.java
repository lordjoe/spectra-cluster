package uk.ac.ebi.pride.spectracluster.hadoop.hbase;


import org.apache.commons.dbcp.*;
import uk.ac.ebi.pride.spectracluster.datastore.*;
import org.springframework.dao.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.*;

import javax.sql.*;
import java.util.*;


/**
 * uk.ac.ebi.pride.spectracluster.hadoop.hbase.PhoenixWorkingClusterDatabase
 *
 * @author Steve Lewis
 * @date 29/10/13
 */
public class PhoenixWorkingClusterDatabase extends WorkingClusterDatabase {

    public static WorkingDatabaseFactory FACTORY = new WorkingDatabaseFactory() {
        /**
         * return a workling database - needed so we can subclass to handle Phoenix
         *
         * @param databaseName - name of the database - tables will have <databasename> . prepended
         * @param ds           !null data source - this provides the connection
         * @return !null   IWorkingClusterDatabase
         */
        @Override
        public IWorkingClusterDatabase buildWorkingDatabase(String databaseName, DataSource ds) {
            // is it Phoenix - then use this class
          if (ds instanceof BasicDataSource) {
              if(((BasicDataSource)ds).getDriverClassName().equals("com.salesforce.phoenix.jdbc.PhoenixDriver"))
                 return  new PhoenixWorkingClusterDatabase(databaseName, ds);

          }
           // else use the superclass
           return WorkingClusterDatabase.FACTORY.buildWorkingDatabase(databaseName,   ds);
        }
    };


    protected PhoenixWorkingClusterDatabase(String databaseName, DataSource ds) {
        super(databaseName, ds);

    }


    /**
     * true of batch operations are allowed
     *
     * @return
     */
    @Override
    public boolean isBatchSupported() {
           return false;
    }

    /**
     * make sure the tables exist
     */
    @Override
    protected void guaranteeDatabaseExists() {
       // super.guaranteeDatabaseExists();
        // do nothing this is not supported
    }

    /**
     * make sure the tables exist
     */
    @Override
    protected void guaranteeDatabase() {
       // do nothing this is not supported
       super.guaranteeDatabase();
    }

    @Override
    protected void guaranteeIndices() {
         // do nothing this is not supported

    }

    /**
     * subclasses support different syntax on queries - this code attampts to make
     * 'standard' queries compatible
     *
     * @param originalQuery
     * @return !null patched query
     */
    @Override
    public String patchQueryString(String originalQuery) {

        if(originalQuery.toUpperCase().contains("INSERT"))  {
            String ret =  originalQuery.replace("INSERT IGNORE","UPSERT");
            ret =  ret.replace("INSERT IF NOT EXISTS","UPSERT");
            ret =  ret.replace("INSERT","UPSERT");
            return ret;
         }

        if(originalQuery.toUpperCase().contains("UPDATE"))  {
            String  ret =  originalQuery.replace("UPDATE IF NOT EXISTS","UPSERT");
            ret =  ret.replace("UPDATE","UPSERT");
            return ret;
         }
        return super.patchQueryString(originalQuery);

    }
}
