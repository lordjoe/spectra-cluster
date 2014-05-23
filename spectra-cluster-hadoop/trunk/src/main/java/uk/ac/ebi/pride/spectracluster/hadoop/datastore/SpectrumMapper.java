package uk.ac.ebi.pride.spectracluster.hadoop.datastore;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.SpectrumMapper
 * Handle most of the work of inserting and retrieving spectra from a database
 * User: Steve
 * Date: 7/15/13
 */
public class SpectrumMapper implements ParameterizedRowMapper<ISpectrum> {


    public static final String SELECT_ALL_SPECTRA_STATEMENT = "SELECT * FROM <database>.spectrums  ";
    public static final String SELECT_WITH_PEPTIDE = "SELECT * FROM <database>.spectrums WHERE peptide = ? ORDERBY precursor_mz ";
    @SuppressWarnings("UnusedDeclaration")
    public static final String SELECT_WITH_CHARGE = "SELECT * FROM <database>.spectrums WHERE precursor_charge = ?  ";
    public static final String SELECT_WITH_MZ = "SELECT * FROM <database>.spectrums WHERE precursor_mz >= ? AND precursor_mz < ? ";
    public static final String SELECT_WITH_CHARGE_AND_MZ = "SELECT * FROM <database>.spectrums WHERE  precursor_charge = ? AND precursor_mz >= ? AND precursor_mz < ? ";
    public static final String SELECT_SPECTRUM_STATEMENT = SELECT_ALL_SPECTRA_STATEMENT + " WHERE id = ?";
    public static final String DELETE_SPECTRUM_STATEMENT = "DELETE FROM <database>.spectrums   WHERE id = ?";
    // insert except where key exists
    public static final String INSERT_SPECTRUM_STATEMENT = "INSERT IGNORE INTO <database>.spectrums  " +
            "(id,precursor_charge,precursor_mz,peptide,annotation,peaks) " +
            "VALUES  (?,?,?,?,?,? ) ";


    public static final int MAX_ANNOTATION_LENGTH = 512;
    public static final int MAX_PEPTIDE_LENGTH = 256;
    public static final int MAX_ID_LENGTH = 128;

    /**
     * statement to create a spectrums table in the database <database>  every
     * group of clustered spectra is in its own database
     */
    public static final String TABLE_CREATE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS <database>.spectrums  ( \n" +
                    "  id VARCHAR(" + MAX_ID_LENGTH + ")   NOT NULL PRIMARY KEY  , \n" +
                    "  precursor_charge INTEGER NOT NULL,\n" +
                    "  precursor_mz DOUBLE NOT  NULL,\n" +
                    "  peptide VARCHAR(" + MAX_PEPTIDE_LENGTH + ")   NULL,\n" +
                    "  annotation VARCHAR(" + MAX_ANNOTATION_LENGTH + ")   NULL,\n" +
                    //         "  peaks VARCHAR(" + WorkingClusterDatabase.MAX_PEAKS_STRING_LENGTH + ") NOT NULL\n" +
                    "  peaks VARCHAR  NOT NULL\n" +
                    //    "  peaks VARBINARY NOT NULL\n" +
                    //         "  PRIMARY KEY (id)\n" +
                    ")"
            //        "CREATE IF NOT EXISTS INDEX idx_charge_<database> on   <database>.spectrums(precursor_charge);" +   // index charge
            //        "CREATE IF NOT EXISTS  INDEX idx_mz_<database> on   <database>.spectrums(precursor_mz); "
            ;      // index mz

    /**
     * convert a spectrum to a suitable set of objects for an insert query
     *
     * @param p !null spectrum
     * @return !null array of objects for the spectrums table
     */
    public Object[] spectrumToArguments(ISpectrum p) {
        Object[] ret = new Object[6];
        int index = 0;
        ret[index++] = p.getId();
        ret[index++] = p.getPrecursorCharge();
        ret[index++] = p.getPrecursorMz();
        if (p instanceof PeptideSpectrumMatch) {
            ret[index++] = ((PeptideSpectrumMatch) p).getPeptide();
            ret[index++] = null;    // annotation
        } else {
            ret[index++] = null;    // peptide
            ret[index++] = null;    // annotation
        }
        final List<IPeak> peaks = p.getPeaks();
        final String encodedPeaks = SpectrumUtilities.peaksToDataString(peaks);
        ret[index++] = encodedPeaks;

        return ret;
    }


    public BatchPreparedStatementSetter buildBatchSetter(final List<? extends ISpectrum> spectra) {
        return new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ISpectrum p = spectra.get(i);
                ps.setString(1, p.getId());
                ps.setInt(2, p.getPrecursorCharge());
                ps.setDouble(3, p.getPrecursorMz());
                if (p instanceof PeptideSpectrumMatch) {
                    ps.setString(4, ((PeptideSpectrumMatch) p).getPeptide());
                    ps.setString(5, null);    // annotation
                } else {
                    ps.setString(4, null);
                    ps.setString(5, null);    // annotation

                }
                final List<IPeak> peaks = p.getPeaks();
                final String encodedPeaks = SpectrumUtilities.peaksToDataString(peaks);
                ps.setString(6, encodedPeaks);    // peaks
            }

            public int getBatchSize() {
                return spectra.size();
            }
        };

    }


    @Override
    public ISpectrum mapRow(final ResultSet rs, final int i) throws SQLException {
        String id = rs.getString("id");
        int precursor_charge = rs.getInt("precursor_charge");
        float precursor_mz = (float) rs.getDouble("precursor_mz");
        String peaksStr = rs.getString("peaks");
        String annotation = SpringJDBCUtilities.trimIfNotNUll(rs.getString("annotation"));
        String peptide = SpringJDBCUtilities.trimIfNotNUll(rs.getString("peptide"));
        List<IPeak> peaks = SpectrumUtilities.dataStringToPeaks(peaksStr);

        ISpectrum ret = new PeptideSpectrumMatch(id, peptide, precursor_charge, precursor_mz, peaks, new SignalToNoiseChecker(), annotation);
        return ret;
    }

}
