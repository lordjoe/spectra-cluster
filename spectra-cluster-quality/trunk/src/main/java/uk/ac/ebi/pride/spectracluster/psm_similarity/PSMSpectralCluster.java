package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.algorithms.CountedString;
import uk.ac.ebi.pride.spectracluster.cluster.ClusterPeptideFraction;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectrumHolderListener;
import uk.ac.ebi.pride.spectracluster.cluster.IDecoyDiscriminator;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.MZIntensityUtilities;
import uk.ac.ebi.pride.spectracluster.util.comparator.ClusterComparator;
import uk.ac.ebi.pride.spectracluster.util.comparator.SpectrumIDComparator;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PSMSpectralCluster implements IPepideHoldingCluster {


    private String id;
    private Float precursorMz;
    private Integer precursorCharge = 0;
    private ISpectrum consensusSpectrum;
    private final Set<ISpectrum> clusteredSpectra = new LinkedHashSet<ISpectrum>();
    private final List<String> peptides = new ArrayList<String>();
    private final List<ClusterPeptideFraction> byPurity = new ArrayList<ClusterPeptideFraction>();
    private final Set<String> spectraIds = new HashSet<String>();
    private final Properties properties = new Properties();

    public PSMSpectralCluster() {
    }


    private static int Bad_SpectraCount = 0;
    private static int MultipleSpectraCount = 0;
    private static int AllSpectraCount = 0;

    protected void buildPeptidePurities() {
        final List<String> spectral_peptides = new ArrayList<String>();
        for (ISpectrum iSpectrum : clusteredSpectra) {
            ISpectrum sc1 = (ISpectrum) iSpectrum;
            String peptide = sc1.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY);
            if (peptide != null) {
                String[] peptides = peptide.split(";");
                for (String s : peptides) {
                    spectral_peptides.add(s);
                }
            }
        }
        double numberSpectra = getClusteredSpectraCount();
        String pureDecoy = null;
        String purityStr = null;


        // debug bad case
        if (spectral_peptides.size() == 0) {
            for (ISpectrum iSpectrum : clusteredSpectra) {
                ISpectrum sc1 = (ISpectrum) iSpectrum;
                String peptide = sc1.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY);
                if (peptide != null)
                    spectral_peptides.add(peptide);
            }
            int n = Bad_SpectraCount++;
            Bad_SpectraCount = n; // so we can look
            if (true)
                return;
            throw new UnsupportedOperationException("Fix This"); // ToDo
        }
        // end bad case

        CountedString[] items = CountedString.getCountedStrings(spectral_peptides);
        Map<String, Double> peptideToFraction = new HashMap<String, Double>();
        int nunberSpectra = getClusteredSpectraCount();
        double total_items = 0;
        for (int i = 0; i < items.length; i++) {
            CountedString item = items[i];
            total_items += item.getCount();
        }
        for (int i = 0; i < items.length; i++) {
            CountedString item = items[i];
            int count = item.getCount();
            peptideToFraction.put(item.getValue(), count / total_items);
        }
        for (ISpectrum iSpectrum : clusteredSpectra) {
            AllSpectraCount++;
            PSMSpectrum sc1 = (PSMSpectrum) iSpectrum;
            String peptide = sc1.getProperty(KnownProperties.IDENTIFIED_PEPTIDE_KEY);
            boolean decoy = sc1.isDecoy();
            if (peptides.contains(";")) {
                MultipleSpectraCount++;
                throw new UnsupportedOperationException("Should never happen");
            }

            spectral_peptides.add(peptide);
            Double purity = peptideToFraction.get(peptide);
            if (purity == null)
                throw new UnsupportedOperationException("Should never happen");

            if (decoy && purity > 0.8) {
                pureDecoy = peptide;
                purityStr = String.format("%8.2f", purity).trim();
            }
            byPurity.add(new ClusterPeptideFraction(peptide, purity, decoy));

        }
        // show bad decoys
//        if (pureDecoy != null) {
//            System.out.println(pureDecoy + " " + purityStr);
//        }
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    public String getSpectralId() {
        StringBuilder sb = new StringBuilder();
        List<String> spectralIds = new ArrayList<String>(getSpectralIds());
        Collections.sort(spectralIds);
        sb.append(spectralIds.get(0));
        for (int i = 1; i < spectralIds.size(); i++) {
            sb.append(",");
            sb.append(spectralIds.get(i));
        }
        return sb.toString();
    }


    public void setId(String id) {
        this.id = id;
    }

    @Override
    public float getPrecursorMz() {
        return precursorMz;
    }

    /**
     * return a set of all ids
     *
     * @return
     */
    @Override
    public Set<String> getSpectralIds() {
        if (this.spectraIds.isEmpty()) {
            List<ISpectrum> clusteredSpectra1 = getClusteredSpectra();
            for (ISpectrum iSpectrum : clusteredSpectra1) {
                spectraIds.add(iSpectrum.getId());
            }
        }
        return Collections.unmodifiableSet(spectraIds);
    }

    public void setPrecursorMz(float precursorMz) {
        this.precursorMz = precursorMz;
    }

    @Override
    public int getPrecursorChargeX() {
        return precursorCharge;
    }

      public List<String> getPeptides() {
        return new ArrayList<String>(peptides);
    }

      public String getMostCommonPeptide() {
        List<String> peptideStrings = getPeptides();
        if (!peptideStrings.isEmpty()) {

            String s = peptideStrings.get(0);
            int index = s.indexOf(":");
            if (index > -1)       // drop things like AERERTRYTE:9
                return s.substring(0, index);
            return s;
        }
        return null;
    }


    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
      public
    @Nonnull
    List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator igonred) {
        if (byPurity.size() == 0)
            buildPeptidePurities();
        return byPurity;

    }


    public void addPeptides(String peptides) {
        String[] parts = peptides.split(",");
        if (parts[0].contains((":")))
            parts = dropPeptideCounts(parts);
        addPeptide(parts);
    }

    public String[] dropPeptideCounts(String[] countedPeptides) {
        String[] ret = new String[countedPeptides.length];
        for (int i = 0; i < countedPeptides.length; i++) {
            ret[i] = dropPeptideCount(countedPeptides[i]);

        }
        return ret;
    }

    protected String dropPeptideCount(final String p) {
        int index = p.indexOf(":");
        if (index < 0)
            return p;
        return p.substring(0, index);
    }

    public void addPeptide(String... peptides) {
        this.peptides.addAll(Arrays.asList(peptides));
    }

    public void setPrecursorCharge(int precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    @Override
    public ISpectrum getConsensusSpectrum() {
        return consensusSpectrum;
    }

    public void setConsensusSpectrum(ISpectrum consensusSpectrum) {
        this.consensusSpectrum = consensusSpectrum;
    }

    @Override
    public ISpectrum getHighestQualitySpectrum() {
        return null;
    }

    @Override
    public List<ISpectrum> getHighestQualitySpectra() {
        throw new UnsupportedOperationException("This method is not supported");
    }

    @Override
    public List<ISpectrum> getClusteredSpectra() {
        ArrayList<ISpectrum> iSpectrums = new ArrayList<ISpectrum>(clusteredSpectra);
        Collections.sort(iSpectrums, SpectrumIDComparator.INSTANCE); // sort by id
        return iSpectrums;
    }

    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }


//    /**
//     * write out the data as a .clustering file
//     *
//     * @param out place to append
//     */
//    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
//    @Override
//    public void appendClustering(Appendable out) {
//
//        try {
//            out.append("=Cluster=\n");
//            out.append("av_precursor_mz=" + String.format("%10.3f", getPrecursorMz()).trim());
//            out.append("\n");
//            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
//            out.append("\n");
//
//            final List<String> peptides1 = getPeptides();
//            if (peptides1.isEmpty()) {
//                out.append("sequence=[" + ClusterUtilities.mostCommonPeptides(getClusteredSpectra()) + "]");
//            } else {
//                out.append("sequence=[");
//                boolean first = true;
//                for (String s : peptides1) {
//                    if (first)
//                        first = !first;
//                    else
//                        out.append(",");
//                    out.append(s);
//                }
//                out.append("]");
//            }
//
//            out.append("\n");
//
//            out.append("consensus_mz=" + ClusterUtilities.buildMZString(getConsensusSpectrum()));
//            out.append("\n");
//            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(getConsensusSpectrum()));
//            out.append("\n");
//
//            for (ISpectrum spec : getClusteredSpectra()) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("SPEC\t");
//                String id1 = spec.getId();
//                while (id1.startsWith("="))
//                    id1 = id1.substring(1, id1.length()); // lots of ids start with == - is that a good thing
//                sb.append(id1);
//                sb.append("\ttrue\n");  // changed to look at output
//                String csq = sb.toString();
//                out.append(csq);
//
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//
//    }


    @Override
    public int compareTo(ICluster o) {
        return ClusterComparator.INSTANCE.compare(this, o);
    }

    @Override
    public boolean equivalent(ICluster o) {
        if (o == this)
            return true;
        if (getPrecursorChargeX() != o.getPrecursorChargeX())
            return false;
        double del = o.getPrecursorMz() - getPrecursorMz();
        double abs = Math.abs(del);
        if (abs > MZIntensityUtilities.SMALL_MZ_DIFFERENCE) {
            return false;
        }

        List<ISpectrum> spc1 = getClusteredSpectra();
        List<ISpectrum> spc2 = o.getClusteredSpectra();

        if (spc1.size() != spc2.size()) {
            return false;
        }
        if (spc1.size() <= 1) {

            List<IPeak> peaks = getConsensusSpectrum().getPeaks();
            List<IPeak> peaks1 = o.getConsensusSpectrum().getPeaks();
            if (peaks.size() != peaks1.size()) {
                return false;
            }
            for (int i = 0; i < peaks1.size(); i++) {
                IPeak pk0 = peaks.get(i);
                IPeak pk1 = peaks1.get(i);
                if (!pk0.equivalent(pk1))
                    return false;
            }
            return true; // just one spectrum so check peaks
        } else {
            if (spc1.size() != spc2.size())
                return false;


            for (int i = 0; i < spc1.size(); i++) {
                ISpectrum pk1 = spc1.get(i);
                ISpectrum pk2 = spc2.get(i);
                boolean test = !pk2.equivalent(pk1);
                if (test)
                    return false;
            }
            return true;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (ISpectrum spectrum : clusteredSpectra) {
            sb.append(spectrum.getId()).append(",");
        }

        return "LazyLoadedSpectralCluster{" +
                ", precursorMz=" + precursorMz +
                ", precursorCharge=" + precursorCharge +
                ", peptides=" + peptides +
                ", clusteredSpectra=" + sb.toString() +
                '}';
    }

    @Override
    public void addSpectra(ISpectrum... merged) {
        spectraIds.clear();
        Collections.addAll(clusteredSpectra, merged);
    }

    @Override
    public boolean isRemoveSupported() {
        return false;
    }

    @Override
    public void removeSpectra(ISpectrum... removed) {
        throw new UnsupportedOperationException("Removal is not supported");
    }

    @Override
    public void addSpectrumHolderListener(SpectrumHolderListener added) {
        throw new UnsupportedOperationException("Add spectrum holder listener is not supported");
    }

    @Override
    public void removeSpectrumHolderListener(SpectrumHolderListener removed) {
        throw new UnsupportedOperationException("Remove spectrum holder listener is not supported");
    }

    /**
      * return a property of null if none exists
      * See ISpectrum for known property names
      *
      * @param key
      * @return possible null value
      */
     @Override
     public String getProperty(String key) {
         if(KnownProperties.MOST_COMMON_PEPTIDE_KEY.equals(key))
             return getMostCommonPeptide();

         return properties.getProperty(key);
     }


     /**
      * @param key
      * @param value
      */
     @Override
     public void setProperty(String key, String value) {
         if(key == null)
             return;
         if( value == null)   {
             properties.remove(key);
             return;
         }

         properties.setProperty(key, value);
     }

     /**
      * Only for internal use in copy constructor
      * Note this is not safe
      * This is not really deprecated but it warns only for
      * internal use
      */
     @Override
     public Properties getProperties() {
         return properties;
     }

}
