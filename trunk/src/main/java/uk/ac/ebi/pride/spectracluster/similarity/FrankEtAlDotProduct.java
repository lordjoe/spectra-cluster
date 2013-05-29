package uk.ac.ebi.pride.spectracluster.similarity;


import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assesses the similarity between two
 * spectra using the normalized
 * dot-product as implemented by Frank et al. (2008) JPR
 * 
 * This implementation uses only the k highest peaks.
 * k is calculated by taking 15 peaks per 1000 Da
 * peptide mass. Furthermore, the vectors for the
 * dot-product are filled with the 1+ln(I) where I
 * is the peak's normalized intensity.
 * @author jg
 *
 * todo: this class needs to be reviewed
 *
 */
public class FrankEtAlDotProduct implements SimilarityChecker {

    public static final int NUMBER_COMPARED_PEAKS = 15;

    public static final double DEFAULT_MZ_RANGE = 0.5;
	/**
	 * The logger to use.
	 */
	//private static final Logger logger = Logger.getLogger(FrankEtAlDotProduct.class);
	/**
	 * The versions available from this algorithm. The only
	 * difference is the way k is calculated.
	 * @author jg
	 *
	 */
	public enum AlgorithmVersion{JPR_2008, NAT_METH_2011}

    /**
      * Use Defaults which builds with reflection
      * Set the class with Defaults.setSimilarityCheckerClass
      */

    public FrankEtAlDotProduct() {
    }

    private double mzRange = 0.5;
	/**
	 * The algorithm version to use. By
	 * default the version described in
	 * Nature Methods 2011 will be used.
	 */
	private AlgorithmVersion version = AlgorithmVersion.NAT_METH_2011;

	/**
	 * Assesses the spectra's similarity using
	 * the normalized dot-product
	 */
	public double assessSimilarity(IPeptideSpectrumMatch spectrum1, IPeptideSpectrumMatch spectrum2) {
		// initialize the number of peaks to use with 15
		int k = NUMBER_COMPARED_PEAKS;
		switch (version) {
			case NAT_METH_2011:
				k = calculateK2011(spectrum1.getPrecursorMz(), spectrum2.getPrecursorMz());
				break;
			case JPR_2008:
				k = calculateK2008(spectrum1.getPrecursorMz(), spectrum2.getPrecursorMz(), spectrum1.getPrecursorCharge(), spectrum2.getPrecursorCharge());
				break;
		}
		
		// get the k highest peaks from every spectrum
		List<IPeak> kHighestPeaks1 = spectrum1.getHighestNPeaks(NUMBER_COMPARED_PEAKS);
        IPeak[] peaks =  kHighestPeaks1.toArray(new IPeak[kHighestPeaks1.size()] );
		List<IPeak> kHighestPeaks2 = spectrum2.getHighestNPeaks(NUMBER_COMPARED_PEAKS);
//
//        int t = 0;
//         int e = 0;
//         int MatchingProducts = 0;
//         int TotalProducts = 0;
//         double MatchingIntensity = 0.0;
//         int charge = theory.getCharge();
//         while (t < tps.length && e < peaks.length) {
//             TotalProducts++;
//             double massChargeRatio = tps[t].getMassChargeRatio() - PROTON_MASS * charge;
//             double mass_difference = peaks[e].getMassChargeRatio() - massChargeRatio;
//             if (Math.abs(mass_difference) <= m_MassTolerance) {
//                 MatchingProducts++;
//                 MatchingIntensity += peaks[e].getPeak();
//                 t++;
//             }
//             else if (mass_difference < 0) {
//                 e++;
//             }
//             else if (mass_difference > 0) {
//                 t++;
//             }
//         }
//         double MatchingProductsFraction = (double) MatchingProducts / TotalProducts;

		// create two intensity vectors
		List<Double> intensities1 = new ArrayList<Double>(k*2);
		List<Double> intensities2 = new ArrayList<Double>(k*2);
		
		// indicates the last item in the k2HighestPeakList that was merged
		int lastIndex2 = 0;
		
		for (IPeak p1 : kHighestPeaks1) {
			// add the intensity to the intensity array of spectrum 1
			intensities1.add(1 + Math.log(p1.getIntensity()));
			
			double mz1 = p1.getMz();
			
			// get the indexes of the comparable masses from peak list 2
			List<Integer> comparableIndexes = new ArrayList<Integer>(3);
			
			for (int i = lastIndex2; i < kHighestPeaks2.size(); i++) {
				// make sure the object exists
				if (kHighestPeaks2.get(i) == null)
					continue;
				
				// compare the m/z
				double mz2 = kHighestPeaks2.get(i).getMz();
				
				// check if they are unique
				if (mz2 >= mz1 - mzRange && mz2 <= mz1 + mzRange) {
					comparableIndexes.add(i);
				}
				
				// make sure the m/z isn't too big already
				if (mz2 > mz1 + mzRange)
					break;
			}
			
			// get the comparable mass closest to the current one
			int closestIndex = -1;
			double closestDiff = 100;
			
			for (Integer i : comparableIndexes) {
				double mz2 = kHighestPeaks2.get(i).getMz();
				double diff = Math.abs(mz1 - mz2);
				
				if (diff < closestDiff) {
					closestIndex = i;
					closestDiff = diff;
				}
			}
			
			// set the intensity 2
			if (closestIndex >= 0) {
				intensities2.add( 1 + Math.log( kHighestPeaks2.get(closestIndex).getIntensity()) );
				kHighestPeaks2.set(closestIndex, null);
			}
			else {
				intensities2.add(0.0);
			}
		}
		
		// add the intensities for the second spectrum
		for (IPeak p2 : kHighestPeaks2) {
			// ignore all peaks set to NULL as they were already processed
			if (p2 == null)
				continue;
			
			// the peak doesn't exist in spectrum 1 as this was already checked
			intensities1.add(0.0);
			
			intensities2.add(1 + Math.log( p2.getIntensity() ));
		}
		
		// make sure both intensities have the same size
		if (intensities1.size() != intensities2.size())
			throw new IllegalStateException("Different sizes of intensity arrays encountered.");
		
		// calculate the dot product
		double dotProduct = 0;
		double sumSquareIntensity1 = 0;
		double sumSquareIntensity2 = 0;
		
		for (int i = 0; i < intensities1.size(); i++) {
			dotProduct += intensities1.get(i) * intensities2.get(i);
			
			sumSquareIntensity1 += Math.pow(intensities1.get(i), 2);
			sumSquareIntensity2 += Math.pow(intensities2.get(i), 2);
		}
		
		// normalize the dot product
		double normalizedDotProduct = dotProduct / Math.sqrt(sumSquareIntensity1 * sumSquareIntensity2);
		
		return normalizedDotProduct;
	}


	/**
	 * Calculate k by using 15 per 1000 Da of
	 * peptide mass.
	 * @param precursor1
	 * @param precursor2
	 * @param charge1
	 * @param charge2
	 * @return
	 */
	private int calculateK2008(Double precursor1, Double precursor2,
			Double charge1, Double charge2) {
		// if any of the required values is missing, return 15
		if (precursor1 == null || precursor2 == null || charge1 == null || charge2 == null || charge1 <= 0 || charge2 <= 0)
			return NUMBER_COMPARED_PEAKS;
		
		// take 15 peaks / 1000Da peptide mass
		double peptideMass = (precursor1 * charge1 + precursor2 * charge2) / 2;
			
		int k = NUMBER_COMPARED_PEAKS * (int) (peptideMass / 1000);
		
		if (peptideMass % 1000 > 0)
			k += NUMBER_COMPARED_PEAKS;
		
		return k;
	}
	
	/**
	 * Calculate k by using the precursor m/z / 50.
	 * @param precursor1
	 * @param precursor2
	 * @return
	 */
	private int calculateK2011(Double precursor1, Double precursor2) {
		// if any of the required values is missing, return 15
		if (precursor1 == null || precursor2 == null)
			return NUMBER_COMPARED_PEAKS;
		
		// use m/z / 50
		int k = (int) ( (precursor1 / 50 + precursor2 / 50) / 2 );
		
		return k;
	}

	public void setMzRange(double mzRange) {
		this.mzRange = mzRange;
	}
	
	public void setVersion(AlgorithmVersion version) {
		this.version = version;
	}
}
