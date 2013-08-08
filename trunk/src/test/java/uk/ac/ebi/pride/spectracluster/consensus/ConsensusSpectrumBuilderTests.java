package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.consensus.ConsensusSpectrumBuilderTests
 *
 * @author Steve Lewis
 *         This tests the difference between  IConsensusSpectrumBuilder implementations
 */
public class ConsensusSpectrumBuilderTests {


    public static boolean peakListsEquivalent(List<IPeak> l1, List<IPeak> l2) {
        if (l1.size() != l2.size())
            return false;
        for (int i = 0; i < l1.size(); i++) {
            IPeak p1 = l1.get(i);
            IPeak p2 = l2.get(i);
            if (!p1.equivalent(p2))
                return false;
        }
        return true;
    }



    @Test
    public void testConsensusSpectrum() throws Exception
    {
        long start = System.currentTimeMillis();
        List<ISpectralCluster>  clusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        for (ISpectralCluster cluster : clusters) {
             testSpectrumBuilder(cluster) ;
        }
        long end = System.currentTimeMillis();
        double delSec = (end - start ) / 1000.0;
        double delMin = delSec / 60.0;

    }

    /**
     * do all the work here
     * @param cluster  !null cluster
     * @throws Exception
     */
    public void testSpectrumBuilder(ISpectralCluster cluster) throws Exception
      {

          final ConsensusSpectrum currentCode = (ConsensusSpectrum) ConsensusSpectrum.FACTORY.getConsensusSpectrumBuilder();
          final ConsensusSpectrumNew newCode  = (ConsensusSpectrumNew)ConsensusSpectrumNew.FACTORY.getConsensusSpectrumBuilder();


          final List<IPeak> allPeaks = ClusterUtilities.getAllPeaks(cluster);

          // use internal methods to add code
          currentCode.addPeaks(allPeaks);
          newCode.addPeaks(allPeaks);



    }


}
