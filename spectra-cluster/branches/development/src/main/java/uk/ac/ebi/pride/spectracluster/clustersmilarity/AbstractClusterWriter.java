package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.filters.*;
import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class AbstractClusterWriter implements TypedVisitor<ISpectralCluster> {
    private final Appendable writer;
    private final OrPredicate<ISpectralCluster> tests;

    public AbstractClusterWriter(Appendable writer,TypedPredicate<ISpectralCluster>... testClauses) {
        this.writer = writer;
        tests = new OrPredicate(testClauses);
    }

    @Override
    public void visit(@Nonnull ISpectralCluster pT) {
        if(tests.apply(pT))
           pT.appendClustering(writer);
    }

    public Appendable getWriter() {
        return writer;
    }

}
