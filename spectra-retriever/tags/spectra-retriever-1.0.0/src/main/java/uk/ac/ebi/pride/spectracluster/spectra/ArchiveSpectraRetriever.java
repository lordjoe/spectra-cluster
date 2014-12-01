package uk.ac.ebi.pride.spectracluster.spectra;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.pride.spectracluster.spectra.util.SearchUtils;
import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;
import uk.ac.ebi.pride.spectrumindex.search.service.SpectrumSearchService;
import uk.ac.ebi.pride.spectrumindex.search.service.repository.SolrSpectrumRepository;
import uk.ac.ebi.pride.spectrumindex.search.service.repository.SolrSpectrumRepositoryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Class to retrieve spectrum from PRIDE Archive
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveSpectraRetriever implements ISpectraRetriever {

    private SpectrumSearchService spectrumSearchService;

    public ArchiveSpectraRetriever() {
        Properties properties = readConfigurations();

        initSpectrumSearchService(properties);
    }

    private void initSpectrumSearchService(Properties properties) {
        String solrUrl = (String) properties.get("pride.solr.server.spectrum.core.url");
        HttpSolrServer httpSolrServer = new HttpSolrServer(solrUrl);
        SolrSpectrumRepositoryFactory solrSpectrumRepositoryFactory = new SolrSpectrumRepositoryFactory(new SolrTemplate(httpSolrServer));
        SolrSpectrumRepository solrSpectrumRepository = solrSpectrumRepositoryFactory.create();
        spectrumSearchService = new SpectrumSearchService(solrSpectrumRepository);
    }

    private Properties readConfigurations() {
        InputStream inputStream = ArchiveSpectraRetriever.class.getClassLoader().getResourceAsStream("solr.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to load solr properties");
        }
        return properties;
    }

    /**
     * Get spectra belong to a given assay
     *
     * @param assayAccession assay accession
     * @param pageable       pagination settings
     * @return paged spectra
     */
    @Override
    public Page<Spectrum> findByAssayAccession(String assayAccession, Pageable pageable) {
        return spectrumSearchService.findByAssayAccession(assayAccession, pageable);
    }

    /**
     * Get spectra belong to a given collection of project accessions
     *
     * @param projectAccessions a collection of project accessions
     * @param pageable          pagination configuration
     * @return paged spectra
     */
    @Override
    public Page<Spectrum> findByProjectAccession(Collection<String> projectAccessions, Pageable pageable) {
        return spectrumSearchService.findByProjectAccession(projectAccessions, pageable);
    }

    /**
     * Get a list of spectra using a given spectrum id
     *
     * @param id spectrum id
     * @return a list of spectrum
     */
    @Override
    public List<Spectrum> findById(String id) {
        String queryID = SearchUtils.escapeQueryChars(id);
        return spectrumSearchService.findById(queryID);
    }

    /**
     * Get spectra belong to a given project
     *
     * @param projectAccession project accession
     * @param pageable         pagination configuration
     * @return paged spectra
     */
    @Override
    public Page<Spectrum> findByProjectAccession(String projectAccession, Pageable pageable) {
        return spectrumSearchService.findByProjectAccession(projectAccession, pageable);
    }

    /**
     * Get spectra belong to a given collection of assays
     *
     * @param assayAccessions a collection of assay accessions
     * @param pageable        pagination configuration
     * @return paged spectra
     */
    @Override
    public Page<Spectrum> findByAssayAccession(Collection<String> assayAccessions, Pageable pageable) {
        return spectrumSearchService.findByAssayAccession(assayAccessions, pageable);
    }

    public static void main(String[] args) {
        ArchiveSpectraRetriever archiveSpectraRetriever = new ArchiveSpectraRetriever();

        List<Spectrum> spectra = archiveSpectraRetriever.findById("PRD000165;PRIDE_Exp_Complete_Ac_10291.xml;spectrum=2001");
        System.out.println(spectra.get(0).getNumPeaks());
    }
}
