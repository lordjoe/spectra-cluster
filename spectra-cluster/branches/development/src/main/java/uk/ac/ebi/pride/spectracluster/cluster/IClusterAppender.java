package uk.ac.ebi.pride.spectracluster.cluster;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.IClusterWriter
 * Abstract the concept of appending a cluster to an appendable.
 * This may include filter
 * User: Steve
 * Date: 9/25/13
 */
public interface IClusterAppender {
    /**
     *
     * @param out  !null open appendale
     * @param data  !null cluster
     * @param OtherData  any other data - implementation specific and usually blank
     * @return  true if anything was appended otherwise false
     */
     public boolean appendCluster(Appendable out,ISpectralCluster data,Object... OtherData);

    /**
     *  add whatever happens at the start
     * @param out  !null open appendale
     * @param data  !null cluster
     * @param OtherData  any other data - implementation specific and usually blank
     * @return  true if anything was appended otherwise false
     */
     public boolean appendStart(Appendable out, Object... OtherData);

    /**
     *  add whatever happens at the end
     * @param out  !null open appendale
     * @param data  !null cluster
     * @param OtherData  any other data - implementation specific and usually blank
     * @return  true if anything was appended otherwise false
     */
     public boolean appendEnd(Appendable out, Object... OtherData);
 }
