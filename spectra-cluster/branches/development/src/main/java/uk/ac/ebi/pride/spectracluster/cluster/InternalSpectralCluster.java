package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.InternalSpectralCluster
 * DO NOT USE Except With Concensus Spectrum Builder - these methods should be
 * protected but then we cannot isolate Concensus Spsctrum building
 * User: Steve
 * Date: 7/8/13
 */
public interface InternalSpectralCluster {
    public static final InternalSpectralCluster[] EMPTY_ARRAY = {};


    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guaranett clean can be byPassed
     *
     * @return
     */
    public List<ISpectrum> internalGetClusteredSpectra();

    /**
     * this should be protected but it needs to be used by spectral clustering so that
     * guarantee clean can be byPassed
     *
     * @return exactly the current concensus spectrum
     */
    public ISpectrum internalGetConcensusSpectrum();

}
