package uk.ac.ebi.pride.spectracluster.spectra;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.pride.spectrumindex.search.model.Spectrum;

import java.util.Collection;
import java.util.List;

/**
 * Spectrum retriever interface
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectraRetriever {
    /**
     * Get spectra belong to a given assay
     *
     * @param assayAccession assay accession
     * @param pageable       pagination settings
     * @return paged spectra
     */
    Page<Spectrum> findByAssayAccession(String assayAccession, Pageable pageable);

    /**
     * Get spectra belong to a given collection of project accessions
     *
     * @param projectAccessions a collection of project accessions
     * @param pageable          pagination configuration
     * @return paged spectra
     */
    Page<Spectrum> findByProjectAccession(Collection<String> projectAccessions, Pageable pageable);

    /**
     * Get a list of spectra using a given spectrum id
     *
     * @param id spectrum id
     * @return a list of spectrum
     */
    List<Spectrum> findById(String id);

    /**
     * Get spectra belong to a given project
     *
     * @param projectAccession project accession
     * @param pageable         pagination configuration
     * @return paged spectra
     */
    Page<Spectrum> findByProjectAccession(String projectAccession, Pageable pageable);

    /**
     * Get spectra belong to a given collection of assays
     *
     * @param assayAccessions a collection of assay accessions
     * @param pageable        pagination configuration
     * @return paged spectra
     */
    Page<Spectrum> findByAssayAccession(Collection<String> assayAccessions, Pageable pageable);
}
