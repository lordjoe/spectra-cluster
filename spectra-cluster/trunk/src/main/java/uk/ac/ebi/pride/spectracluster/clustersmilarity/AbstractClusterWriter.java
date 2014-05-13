package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;
import com.lordjoe.utilities.TypedVisitor;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.OrPredicate;

import javax.annotation.Nonnull;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class AbstractClusterWriter implements TypedVisitor<ISpectralCluster> {
    private final Appendable writer;
    private final OrPredicate<ISpectralCluster> tests;

    public AbstractClusterWriter(Appendable writer, TypedPredicate<ISpectralCluster>... testClauses) {
        this.writer = writer;
        tests = new OrPredicate(testClauses);
    }

    @Override
    public void visit(@Nonnull ISpectralCluster pT) {
        if (tests.apply(pT))
            pT.appendClustering(writer);
    }

    public Appendable getWriter() {
        return writer;
    }

}
