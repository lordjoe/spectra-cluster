package uk.ac.ebi.pride.spectracluster.hadoop;

import org.systemsbiology.hadoop.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.AbstractClusterLauncherJobBuilder
 *
 * @author Steve Lewis
 * @date 29/10/13
 */
public abstract class AbstractClusterLauncherJobBuilder implements IJobBuilder {
    public static AbstractClusterLauncherJobBuilder[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = AbstractClusterLauncherJobBuilder.class;
    protected final ClusterLauncher launcher;
    private int m_PassNumber = 1;

    public AbstractClusterLauncherJobBuilder(ClusterLauncher launcher) {
        this.launcher = launcher;
    }

    public ClusterLauncher getLauncher() {
        return launcher;
    }

    /**
     * where output directories go
     *
     * @return path name
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getOutputLocation() {
        return getLauncher().getOutputLocation(getPassNumber());
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getLastOutputLocation() {
        return getLauncher().getOutputLocation(getPassNumber() - 1);
    }

    public int getPassNumber() {
        return m_PassNumber;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPassNumber(final int pPassNumber) {
        m_PassNumber = pPassNumber;
    }

    public void incrementPassNumber() {
        m_PassNumber++;
    }

    /**
     * if we start at the first job - how many jobs are there in total
     *
     * @return
     */
    @Override
    public abstract int getNumberJobs();

    /**
     * build the list of jobs to run
     *
     * @return
     */
    @Override
    public List<IHadoopJob> buildJobs() {

        return buildJobs(0);
    }

    /**
     * build the list of jobs to run
     * NOTE look at ClusterLauncherJobBuilder.buildJobs for a good
     * sample - there are a few important steps there like setting the pass number which
     * sets the output directory name - default in Output + passnumber
     * @param  startjob job to start at (default = 0)
     * @return  !null
     */
    public abstract List<IHadoopJob> buildJobs(int startjob);
}
