package uk.ac.ebi.pride.spectracluster.psm_similarity;

import com.lordjoe.algorithms.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PSMSpectralCluster implements ISpectralCluster {


    private String id;
    private Float precursorMz;
    private Integer precursorCharge = 0;
    private ISpectrum consensusSpectrum;
    private final Set<ISpectrum> clusteredSpectra = new LinkedHashSet<ISpectrum>();
    private final List<String> peptides = new ArrayList<String>();
    private final List<ClusterPeptideFraction> byPurity = new ArrayList<ClusterPeptideFraction>();
    private final Set<String> spectraIds = new HashSet<String>();

    public PSMSpectralCluster() {
    }


    private static int Bad_SpectraCount = 0;
    private static int MultipleSpectraCount = 0;
    private static int AllSpectraCount = 0;

    protected void buildPeptidePurities() {
        final List<String> spectral_peptides = new ArrayList<String>();
        for (ISpectrum iSpectrum : clusteredSpectra) {
            IPeptideSpectrumMatch sc1 = (IPeptideSpectrumMatch) iSpectrum;
            String peptide = sc1.getPeptide();
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
                IPeptideSpectrumMatch sc1 = (IPeptideSpectrumMatch) iSpectrum;
                String peptide = sc1.getPeptide();
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
            String peptide =sc1.getPeptide();
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
        List<String> spectralIds = new ArrayList<String>( getSpectralIds());
        Collections.sort(spectralIds);
        sb.append(spectralIds.get(0)) ;
        for (int i = 1; i < spectralIds.size(); i++) {
            sb.append(",") ;
            sb.append(spectralIds.get(i)) ;
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
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    @Override
    public List<String> getPeptides() {
        return new ArrayList<String>(peptides);
    }

    @Override
    public String getMostCommonPeptide() {
        List<String> peptideStrings = getPeptides();
        if (!peptideStrings.isEmpty()) {

            String s = peptideStrings.get(0);
            int index = s.indexOf(":");
            if(index > -1)       // drop things like AERERTRYTE:9
                return s.substring(0,index);
            return s;
        }
        return null;
    }


    /**
     * get peptides with statistics
     *
     * @return list ordered bu purity
     */
    @Override
    public @Nonnull  List<ClusterPeptideFraction> getPeptidePurity(IDecoyDiscriminator igonred) {
        if (byPurity.size() == 0)
            buildPeptidePurities();
        return byPurity;

    }




    public void addPeptides(String peptides) {
        String[] parts = peptides.split(",");
        if(parts[0].contains((":")))
            parts = dropPeptideCounts(parts);
        addPeptide(parts);
    }

    public String[] dropPeptideCounts(String[] countedPeptides)
    {
        String[] ret = new String[countedPeptides.length];
        for (int i = 0; i < countedPeptides.length; i++) {
            ret[i] = dropPeptideCount(countedPeptides[i]);

        }
        return ret;
    }

    protected  String dropPeptideCount(final String p) {
        int index = p.indexOf(":") ;
        if(index < 0)
            return p;
        return p.substring(0,index);
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
        Collections.sort(iSpectrums, ISpectrum.ID_COMAPRATOR); // sort by id
        return iSpectrums;
    }

    @Override
    public int getClusteredSpectraCount() {
        return clusteredSpectra.size();
    }


    /**
     * make a one line report
     *
     * @param out
     */
    @Override
    public void appendData(Appendable out) {
        try {
            out.append(getId());
            out.append("\t");

            String mz = String.format("%f8.2", getPrecursorMz());
            out.append(mz);
            out.append("\t");

            out.append(Integer.toString(getPrecursorCharge()));
            out.append("\t");


        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }


    }

    /**
     * write out the data as a CGF file
     *
     * @param out place to append
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void append(Appendable out) {

        try {
            out.append("BEGIN CLUSTER");
            out.append(" Id=" + getId());
            out.append(" Charge=" + getPrecursorCharge());

            out.append("\n");

            appendSpectra(out);
            //            ISpecClusterPeak[] peaks = getPeaks();
            //            for (int i = 0; i < peaks.length; i++) {
            //                ISpecClusterPeak peak = peaks[i];
            //                out.append(peak.toString());
            //                out.append("\n");
            //            }
            out.append("END CLUSTER");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * write out the data as a .clustering file
     *
     * @param out place to append
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public void appendClustering(Appendable out) {

        try {
            out.append("=Cluster=\n");
            out.append("av_precursor_mz=" + String.format("%10.3f", getPrecursorMz()).trim());
            out.append("\n");
            out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
            out.append("\n");

            final List<String> peptides1 = getPeptides();
            if(peptides1.isEmpty())  {
                out.append("sequence=[" + ClusterUtilities.mostCommonPeptides(getClusteredSpectra()) + "]");
             }
            else {
                out.append("sequence=[");
                boolean first = true;
                for (String s : peptides1) {
                  if(first)
                      first = !first;
                    else
                      out.append(",") ;
                    out.append(s);
                }
                out.append("]");
            }

             out.append("\n");

            out.append("consensus_mz=" + ClusterUtilities.buildMZString(getConsensusSpectrum()));
            out.append("\n");
            out.append("consensus_intens=" + ClusterUtilities.buildIntensityString(getConsensusSpectrum()));
            out.append("\n");

            for (ISpectrum spec : getClusteredSpectra()) {
                StringBuilder sb = new StringBuilder();
                sb.append("SPEC\t");
                String id1 = spec.getId();
                while (id1.startsWith("="))
                    id1 = id1.substring(1, id1.length()); // lots of ids start with == - is that a good thing
                sb.append(id1);
                sb.append("\ttrue\n");  // changed to look at output
                String csq = sb.toString();
                out.append(csq);

            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * do not add begin and end cluster - useful for rebuilding a mgf
     *
     * @param out
     */
    @Override
    public void appendSpectra(Appendable out) {
        List<ISpectrum> clusteredSpectra = getClusteredSpectra();
        for (ISpectrum cs : clusteredSpectra) {
            cs.appendMGF(out);  // single spectgra become mgfs

        }
    }

    @Override
    public boolean containsMajorPeak(int mz) {
        return getConsensusSpectrum().containsMajorPeak(mz);
    }

    @Override
    public void addSpectra(List<ISpectrum> added) {
        spectraIds.clear();
        clusteredSpectra.addAll(added);
    }

    @Override
    public boolean isStable() {
        return ClusterUtilities.isClusterStable(this);
    }


    /**
     * if true the cluster is semi stable and will not allow removal
     *
     * @return
     */
    @Override
    public boolean isSemiStable() {
        return ClusterUtilities.isClusterSemiStable(this);
    }


    @Override
    public int compareTo(ISpectralCluster o) {
        return ISpectralCluster.SIMPLE_CLUSTER_COMPARATOR.compare(this, o);
       }

    @Override
    public boolean equivalent(ISpectralCluster o) {
        if (o == this)
            return true;
        if (getPrecursorCharge() != o.getPrecursorCharge())
            return false;
        double del = o.getPrecursorMz() - getPrecursorMz();
        double abs = Math.abs(del);
        if (abs > IPeak.SMALL_MZ_DIFFERENCE) {
            return false;
        }

        List<ISpectrum> spc1 = getClusteredSpectra();
        List<ISpectrum> spc2;
        if (o instanceof InternalSpectralCluster)
            spc2 = ((InternalSpectralCluster) o).internalGetClusteredSpectra();  // no copy or clean needed
        else
            spc2 = o.getClusteredSpectra();

        if (spc1.size() != spc2.size()) {
            return false;
        }
        if (spc1.size() <= 1) {

            List<IPeak> peaks = getPeaks();
            List<IPeak> peaks1 = o.getPeaks();
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
        }
        else {
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
    public int[] asMajorPeakMZs() {
        return getConsensusSpectrum().asMajorPeakMZs();
    }

    @Override
    public List<IPeak> getPeaks() {
        return getConsensusSpectrum().getPeaks();
    }

    @Override
    public int getPeaksCount() {
        return getConsensusSpectrum().getPeaksCount();
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
}
