package uk.ac.ebi.pride.spectracluster.datastore;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterDataStore implements IMutableClusterDataStore {

    public static final String SPECTRUM_ID_SEPARATOR = ",";

    private String tableName;
    private ClusterMapper clusterMapper;
    private IQueryFormatter queryFormatter;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ClusterDataStore(String tableName, DataSource dataSource, IQueryFormatter queryFormatter) {
        this.tableName = tableName;
        this.clusterMapper = new ClusterMapper();
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.queryFormatter = queryFormatter;
    }

    private void guaranteeTableExist(String tableName) {

    }

    @Override
    public void clearAllData() {
        String sql = "DELETE FROM " + tableName;

        sql = queryFormatter.format(sql);

        MapSqlParameterSource params = new MapSqlParameterSource();

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public void storeCluster(ISpectralCluster clusterToStore) {
        String sql = "INSERT INTO " + tableName +
                " (id,precursor_charge,precursor_mz,peaks,spectrum_ids) VALUES (:clusterId, :precursorCharge, :precursorMz, :peaks, :spectrumIds)";

        sql = queryFormatter.format(sql);

        MapSqlParameterSource params = new MapSqlParameterSource();

        // cluster id
        params.addValue("clusterId", clusterToStore.getId());

        // precursor charge
        params.addValue("precursorCharge", clusterToStore.getPrecursorCharge());

        // precursor m/z
        params.addValue("precursorMz", clusterToStore.getPrecursorMz());

        // store consensus spectrum
        ISpectrum consensusSpectrum = clusterToStore.getConsensusSpectrum();
        final String encodedPeaks = SpectrumUtilities.peaksToDataString(consensusSpectrum.getPeaks());
        params.addValue("peaks", encodedPeaks);

        // spectrum ids
        String spectrumIds = getSpectrumIds(clusterToStore);
        params.addValue("spectrum_ids", spectrumIds);

        namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * Get a concatenated spectrum ids separated by SPECTRUM_ID_SEPARATOR
     */
    private String getSpectrumIds(ISpectralCluster clusterToStore) {
        StringBuilder sb = new StringBuilder();

        for (ISpectrum spectrum : clusterToStore.getClusteredSpectra()) {
            sb.append(spectrum.getId()).append(SPECTRUM_ID_SEPARATOR);
        }

        return sb.substring(0, sb.length() - 1);
    }

    @Override
    public void storeClusters(Collection<ISpectralCluster> clustersToStore) {
        for (ISpectralCluster cluster : clustersToStore) {
            storeCluster(cluster);
        }
    }

    @Override
    public void removeCluster(ISpectralCluster clusterToRemove) {
        String sql = "DELETE FROM " + tableName + " WHERE id=:clusterId";

        sql = queryFormatter.format(sql);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clusterId", clusterToRemove.getId());

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public ISpectralCluster getById(String id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id=:clusterId";

        sql = queryFormatter.format(sql);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clusterId", id);

        List<ISpectralCluster> results = namedParameterJdbcTemplate.query(sql, params, clusterMapper);

        return results == null ? null : results.get(0);
    }

    @Override
    public Iterable<? extends ISpectralCluster> getClusterByMz(double minMz, double maxMz) {
        String sql = "SELECT * FROM " + tableName + " WHERE precursor_mz >= :minMz AND precursor_mz <= :maxMz";

        sql = queryFormatter.format(sql);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("minMz", minMz);
        params.addValue("maxMz", maxMz);

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<? extends ISpectralCluster> getClusterByMzAndCharge(double minMz, double maxMz, int charge) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<? extends ISpectralCluster> getClustersByPeptide(String peptide) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
