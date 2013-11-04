package uk.ac.ebi.pride.spectracluster.hadoop.mgf;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.systemsbiology.hadoop.AbstractParameterizedMapper;
import org.systemsbiology.hadoop.ISetableParameterHolder;
import uk.ac.ebi.pride.spectracluster.datastore.SQLDataStore;
import uk.ac.ebi.pride.spectracluster.hadoop.hbase.HBaseUtilities;
import uk.ac.ebi.pride.spectracluster.hadoop.hbase.PhoenixWorkingClusterDatabase;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * Hadoop mapper for loading a group of mgf files into HBase using Phoenix
 *
 * @author Rui Wang
 * @version $Id$
 */
public class MgfClusterDBLoadingMapper extends AbstractParameterizedMapper<Writable> {

    private SQLDataStore sqlDataStore;

    @Override
    protected void setup(Mapper.Context context) throws IOException, InterruptedException {
        super.setup(context);

        ISetableParameterHolder application = getApplication();

        String tableName = application.getParameter("table_name");

        DataSource source = HBaseUtilities.getHBaseDataSource();
        Defaults.INSTANCE.setDefaultDataSource(source);
        Defaults.INSTANCE.setDatabaseFactory(PhoenixWorkingClusterDatabase.FACTORY);

        this.sqlDataStore = new SQLDataStore(tableName, source);
    }

    @Override
    public void map(Writable key, Text value, Context context) throws IOException, InterruptedException {
        String label = key.toString();
        String text = value.toString();

        if (label == null || text == null || label.length() == 0 || text.length() == 0)
            return;

        LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
        final IPeptideSpectrumMatch match = ParserUtilities.readMGFScan(rdr);
        if(match == null)
            sqlDataStore.addSpectrum(match);
    }


}