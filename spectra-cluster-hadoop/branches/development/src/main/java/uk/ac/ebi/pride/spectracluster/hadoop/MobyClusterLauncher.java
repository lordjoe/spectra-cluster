package uk.ac.ebi.pride.spectracluster.hadoop;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.hadoop.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.NextGenClusterLauncher
 * Launches a hadoop job on the cluster  using the NextGen job builder
 * specialized for spectral clustering
 * User: steven
 * Date: Jan 5, 2011
 * Singleton representing a JXTandem job -
 * This has the program main
 */
public class MobyClusterLauncher {


    // Call with
    // params=tandem.params remoteHost=Glados remoteBaseDirectory=/user/howdah/JXTandem/data/largeSample
    //
    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            ClusterLauncher.usage();
            return;
        }
        if ("params=".equals(args[1])) {
            ClusterLauncher.usage2();
            return;
        }

        boolean isVersion1 = HadoopMajorVersion.CURRENT_VERSION != HadoopMajorVersion.Version0;
        HadoopDeployer.setOnlyClassesInJar(true); // no outside jars
        ClusterLauncher.setDirectoryInputHandled(true);  // pass directory not file list

        // change this if debugging from another job
        ClusterLauncher.setDefaultStartAtJob(0);
        if (!isVersion1) {
            final IJobBuilderFactory builder = new IJobBuilderFactory() {
                @Override
                public IJobBuilder getJobBuilder(IStreamOpener launcherx, Object... otherData) {
                    Class[] jobs = {CapitolWordCountRunner.class};
                    return new AbstractClusterLauncherJobBuilder((ClusterLauncher) launcherx, jobs);
                }
            };
            ClusterLauncher.workingMain(args, builder);
        } else {
            final IJobBuilderFactory builder = new IJobBuilderFactory() {
                @Override
                public IJobBuilder getJobBuilder(IStreamOpener launcherx, Object... otherData) {
                    Class[] jobs = {CapitolWordCountRunner.class};
                    return new AbstractClusterLauncherJobBuilder((ClusterLauncher) launcherx, jobs);
                }
            };
            ClusterLauncher.workingMain(args, builder);
        }
    }

}
