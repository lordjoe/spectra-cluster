package uk.ac.ebi.pride.spectracluster.annotation;


import java.util.Map;
import java.util.Set;

/**
 * ISpectralAnnotationRetriever is the interface for retrieving annotatioins
 * for spectrum, providing spectrum id
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectralAnnotationRetriever {

    /**
     * Get annotation of a given spectrum id on a specific annotation name
     *
     * @param annotationName    the name of the annotation
     * @param spectrumId    spectrum id
     * @return  not null annotation if exists, otherwise null
     */
    String getAnnotation(String annotationName, String spectrumId) throws SpectralAnnotationRetrieverException;

    /**
     * Get all the annotations for a given spectrum id
     *
     * @param spectrumId    spectrum id
     * @return  not null map of annotations
     */
    Map<String, String> getAnnotations(String spectrumId) throws SpectralAnnotationRetrieverException;

    /**
     * Get all the annotations for a given spectrum id
     *
     * @param spectrumId    spectrum id
     * @param annotationNames   the names of annotations
     * @return  not null map of annotations
     */
    Map<String, String> getAnnotations(String spectrumId, Set<String> annotationNames) throws SpectralAnnotationRetrieverException;

}
