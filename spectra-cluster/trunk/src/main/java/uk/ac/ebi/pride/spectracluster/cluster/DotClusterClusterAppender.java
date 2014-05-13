package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.DotClusterClusterAppender
 * append to a dotclusterFile
 * User: Steve
 * Date: 9/25/13
 */
public class DotClusterClusterAppender implements IClusterAppender {

    public static final DotClusterClusterAppender INSTANCE = new DotClusterClusterAppender();

    private DotClusterClusterAppender() {
    }


    /**
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendCluster(final Appendable out, final ISpectralCluster data, final Object... OtherData) {
        data.appendClustering(out);
        return true;
    }


    /**
     * add whatever happens at the start
     *
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendStart(final Appendable out, final Object... otherdata) {
        String name = (String) otherdata[0];
        ClusterUtilities.appendDotClusterHeader(out, name);
        return true;
    }

    /**
     * add whatever happens at the end
     *
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendEnd(final Appendable out, final Object... otherdata) {
        return false;
    }
}
