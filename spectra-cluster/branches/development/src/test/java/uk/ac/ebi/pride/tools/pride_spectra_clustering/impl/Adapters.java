package uk.ac.ebi.pride.tools.pride_spectra_clustering.impl;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.consensus_spectrum_builder.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.consensus_spectrum_builder.impl.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.normalizer.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.normalizer.impl.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.*;
import uk.ac.ebi.pride.tools.pride_spectra_clustering.util.Peak;

import java.util.*;

/**
 * uk.ac.ebi.pride.tools.pride_spectra_clustering.impl.Adapters
 * Static methods to convert to and from old code structures to new
 * User: Steve
 * Date: 6/18/13
 */
public class Adapters {

    public static final ConsensusSpectrumBuilder consensusBuilder = new FrankEtAlConsensusSpectrumBuilder();
    /**
     * The intensity normalizer used
     */
    public static final IntensityNormalizer normalizer = new TotalIntensityNormalizer();


    /**
     * convert old  SpectraCluster to new
     *
     * @param inp !null cluster
     * @return !null equivalent cluster
     */
    public static ISpectralCluster fromSpectraCluster(SpectraCluster inp) {
        String id = "";
        ISpectralCluster ret = new SpectralCluster(id);
        final List<ClusteringSpectrum> spectra = inp.getSpectra();

        for (ClusteringSpectrum sc : spectra) {
            ISpectrum spc = fromSpectrum(sc);
            ret.addSpectra(spc);
        }
        return ret;
    }

    /**
     * convert old  Spectrum to new
     *
     * @param inp !null Spectrum
     * @return !null equivalent Spectrum
     */
    public static ISpectrum fromSpectrum(ClusteringSpectrum inp) {
        final Integer precursorCharge = inp.getPrecursorCharge();
        final double precursorMZ = inp.getPrecursorMZ();
        final String id = inp.getId();
        final ParamGroup additional = inp.getAdditional();
        String peptide = inp.getPeptide();
        final List<Peak> peaklist = inp.getPeaklist();
        List<IPeak> newPeaks = new ArrayList<IPeak>();
        for (Peak peak : peaklist) {
            newPeaks.add(fromPeak(peak));
        }
        Collections.sort(newPeaks); // sort by mz

        ISpectrum ret = new PeptideSpectrumMatch(id,
                peptide,
                (int)(precursorCharge + 0.5),
                (float)precursorMZ,
                 newPeaks);

        return ret;
    }

    /**
     * convert old peak to new  peak
     *
     * @param pk !null peak
     * @return !null equivalent peak
     */
    public static IPeak fromPeak(Peak pk) {
        IPeak ret = new uk.ac.ebi.pride.spectracluster.spectrum.Peak((float)pk.getMz(),(float) pk.getIntensity(), pk.getCount());
        return ret;
    }


    /**
      * convert old peak to new  peak
      *
      * @param pk !null peak list
      * @return !null equivalent peak list
      */
     public static  List<IPeak>  fromPeaks(List<Peak> pks) {
         List<IPeak> ret = new ArrayList<IPeak>();
         for (Peak k : pks) {
             if (k != null) {
                ret.add(fromPeak(k));
             }
         }
         Collections.sort(ret)   ;
         return ret;
     }

    /**
      * convert old peak to new  peak
      *
      * @param pk !null peak list
      * @return !null equivalent peak list
      */
     public static  List<Peak>  fromIPeaks(List<IPeak> pks) {
         Collections.sort(pks)   ;
         List<Peak> ret = new ArrayList<Peak>();
         for (IPeak k : pks) {
              ret.add(fromPeak(k));
         }
          return ret;
     }


     /**
     * convert old  SpectraCluster to new
     *
     * @param inp !null cluster
     * @return !null equivalent cluster
     */
    public static SpectraCluster fromSpectraCluster(ISpectralCluster inp) {
        String id = "";
        SpectraCluster ret = new SpectraCluster(consensusBuilder,normalizer);
        final List<ISpectrum> spectra = inp.getClusteredSpectra();

        for (ISpectrum sc : spectra) {
            ClusteringSpectrum spc = fromSpectrum(sc);
            ret.addSpectrum(spc);
        }
        return ret;
    }


    /**
     * convert new  Spectrum to old
     *
     * @param inp !null Spectrum
     * @return !null equivalent Spectrum
     */
    public static ClusteringSpectrum fromSpectrum(ISpectrum inp) {
        final double precursorCharge = inp.getPrecursorCharge();
        final double precursorMZ = inp.getPrecursorMz();
        final double precursorIntensity = 1;
        final String id = inp.getId();
        String peptide = null; // todo do better
        final List<IPeak> peaklist = inp.getPeaks();
        List<Peak> newPeaks = new ArrayList<Peak>();
        for (IPeak peak : peaklist) {
            newPeaks.add(fromPeak(peak));
        }

        ClusteringSpectrum ret = new ClusteringSpectrum(
                id,
                precursorMZ,
                precursorIntensity,
                (int) precursorCharge,
                newPeaks);

        if(inp instanceof IPeptideSpectrumMatch)  {
            ret.setPeptide(((IPeptideSpectrumMatch)inp).getPeptide());
        }
        return ret;
    }


    /**
     * convert old peak to new  peak
     *
     * @param pk !null peak
     * @return !null equivalent peak
     */
    public static Peak fromPeak(IPeak pk) {
        Peak ret = new Peak(pk.getMz(), pk.getIntensity(), pk.getCount());
        return ret;
    }



    /**
     * operations that merge peaks in lists had better preserve the total count
     * useful in testing and debuggine
     * @param lst !null list of peaks
     * @return   total count from all peaks
     */
    public static int getTotalCount(List<Peak> lst)    {
        int total = 0;
        for (Peak pk : lst) {
            if(pk != null)
              total += pk.getCount();
        }
        return total;
    }



}
