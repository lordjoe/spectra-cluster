package uk.ac.ebi.pride.spectracluster.io;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

/**
 * uk.ac.ebi.pride.spectracluster.io.FilteredClusterAppender
 * build with one appender and one filter - allend if the predicate says to do so
 * User: Steve
 * Date: 9/25/13
 */
@SuppressWarnings("UnusedDeclaration")
public class FilteredClusterAppender implements IClusterAppender {
    private final IClusterAppender m_Appender;
    private final TypedPredicate<ISpectralCluster> m_AppendIf;

    public FilteredClusterAppender(final IClusterAppender wrapped, TypedPredicate<ISpectralCluster> appendIf) {
        m_Appender = wrapped;
        m_AppendIf = appendIf;
    }

    /**
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendCluster(final Appendable out, final ISpectralCluster data, final Object... otherdata) {
        boolean anyThingDone = false;
        //noinspection  SimplifiableIfStatement
        if (m_AppendIf.apply(data, otherdata)) {
            return m_Appender.appendCluster(out, data, otherdata);
        } else {
            return false; // not appended
        }
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
        return m_Appender.appendStart(out, otherdata);
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
        return m_Appender.appendEnd(out, otherdata);
    }
}
