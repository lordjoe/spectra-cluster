package uk.ac.ebi.pride.spectracluster.retrievers;

import javax.annotation.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.retrievers.ISpectralPropertiesRetriever
 *
 * @author Steve Lewis
 * @date 20/05/2014
 */
public interface ISpectralPropertiesRetriever {
    /**
     * by convention properties with this value are known to be null
     */
    public static Object NULL_OBJECT = new Object();

    /**
     * return property value or null of it does not exist
     *
     * @param spectralId id of the spectrum
     * @param propName   name of the property
     * @return value or null
     */
    public Object getProperty(@Nonnull String spectralId, @Nonnull String propName);

    /**
     * return a map of requested property values
     *
     * @param spectralId id of the spectrum
     * @param properties names of the properties
     * @return map    any property which can be fetched will be present or represented by NULL_OBJECT
     */
    public @Nonnull Map<String, Object> getProperties(@Nonnull String spectralId, @Nonnull Collection<String> properties);


    /**
     * return a map of requested all retrievable property values
     *
     * @param spectralId id of the spectrum
     * @return map    any property which can be fetched will be present or represented by NULL_OBJECT
     */
    public @Nonnull Map<String, Object> getProperties(@Nonnull String spectralId);

    /**
     * return a map of requested property values
     *
     * @param ids ids of the spectrum to bulk load
     * @return a retriever to use with the listed spectra
     */
    public @Nonnull ISpectralPropertiesRetriever bulkLoad(@Nonnull Collection<String> ids);

}
