package uk.ac.ebi.pride.spectracluster.hadoop;

import org.systemsbiology.hadoop.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.NextGenClusterLauncherJobBuilder
 *  Add a wide cluster launch
 * @author Steve Lewis
 * @date 29/10/13
 */
public class NextGenClusterLauncherJobBuilder extends AbstractClusterLauncherJobBuilder {

    public static final IJobBuilderFactory FACTORY = new IJobBuilderFactory() {
        @Override
        public IJobBuilder getJobBuilder(IStreamOpener launcher,Object... otherData) {
            return new NextGenClusterLauncherJobBuilder((ClusterLauncher)launcher);
        }
    };

    public static final Class[] JOB_CLASSES =
            {
                    SpectraPeakClustererPass1.class,
                    SpectraClustererMerger.class,
                    StableClusterAccumulator.class, // now add to stable clusters
                    SpectraClustererMerger.class,
                    ClusterConsolidator.class
            } ;



    public NextGenClusterLauncherJobBuilder(ClusterLauncher launcher) {
        super(launcher,JOB_CLASSES);
    }

    @Override
    protected IHadoopJob buildJob(String inputFile, Class<? extends IJobRunner> mainClass, int jobNumber, String... addedArgs) {
        return super.buildJob(inputFile, mainClass, jobNumber, addedArgs);

    }
}
