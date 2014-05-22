package uk.ac.ebi.spectracluster.retrievers;

import uk.ac.ebi.pride.spectracluster.datastore.*;

import javax.sql.*;
import java.util.*;

/**
 * uk.ac.ebi.spectracluster.retrievers.PrideArchiveRetrieverTests
 *
 * @author Steve Lewis
 * @date 20/05/2014
 */
public class PrideArchiveRetrieverTests {
    public static DataSource buildPrideArchiveDataSource() {
         Map<String, String> holder = new HashMap<String, String>();
         final String host = "mysql-pride-projects";
         holder.put(SpringJDBCUtilities.DATA_HOST_PARAMETER, host);
         holder.put(SpringJDBCUtilities.DATA_USER_PARAMETER, "pride_q_ro");
         String pw = "pride";
         holder.put(SpringJDBCUtilities.DATA_PASSWORD_PARAMETER, "Pride_Q_RO");
         holder.put(SpringJDBCUtilities.DATA_DRIVER_CLASS_PARAMETER, "com.mysql.jdbc.Driver");
         DataSource ds = SpringJDBCUtilities.buildDataSource(holder);
         return ds;
     }

}
