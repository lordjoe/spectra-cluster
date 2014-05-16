package uk.ac.ebi.pride.spectracluster.annotation;

import java.util.Set;

/**
 * ILoadableSpectralAnnotationRetriever retrieves annotations in batch
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ILoadableSpectralAnnotationRetriever extends ISpectralAnnotationRetriever {

    /**
     * Get all the annotations of a given set of spectra in batch
     *
     * @param spectrumIds   a set of spectrum ids
     */
    void getAnnotationsInBatch(Set<String> spectrumIds) throws SpectralAnnotationRetrieverException;

    /**
     * Get annotations in batch
     *
     * @param spectrumIds   a set of spectrum ids
     * @param annotationNames   a specific set of annotation names
     */
    void getAnnotationsInBatch(Set<String> spectrumIds, Set<String> annotationNames) throws SpectralAnnotationRetrieverException;
}
