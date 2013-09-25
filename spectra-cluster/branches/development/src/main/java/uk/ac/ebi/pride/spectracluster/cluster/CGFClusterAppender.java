package uk.ac.ebi.pride.spectracluster.cluster;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.CGFClusterAppender
 * User: Steve
 * Date: 9/25/13
 */
@SuppressWarnings("UnusedDeclaration")
public class CGFClusterAppender implements IClusterAppender {

    public static final CGFClusterAppender INSTANCE = new CGFClusterAppender();
    private CGFClusterAppender() {
    }

    /**
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendCluster(final Appendable out, final ISpectralCluster data, final Object... OtherData) {
        data.append(out);
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
    public boolean appendStart(final Appendable out , final Object... OtherData) {
        return false;
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
    public boolean appendEnd(final Appendable out , final Object... OtherData) {
        return false;
    }
}
