package org.systemsbiology.xtandem;

import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

/**
 * org.systemsbiology.xtandem.ITandemScoringAlgorithm
 * User: steven
 * Date: 1/14/11
 */
public interface ITandemScoringAlgorithm extends IScoringAlgorithm {
    public static final ITandemScoringAlgorithm[] EMPTY_ARRAY = {};

//    public int getMinimumIonCount();
//
//    public int getMaximumMissedCleavageSites();
//
//    public boolean isScoringXIons();
//
//    public boolean isScoringYIons();
//
//    public boolean isScoringAIons();
//
//    public boolean isScoringBIons();
//
//    public boolean isScoringCIons();
//
//    public boolean isScoringCyclicPermutation();
//
//    public boolean isScoringIncludeReverse();
//
//    public boolean isRefineSpectrumSynthesis();

    public float getSpectrumMassError();

//    public float getSpectrumMonoIsotopicMassError();
//
//    public float getSpectrumMonoIsotopicMassErrorMinus();
//
//    public float getSpectrumHomologyError();

    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(IParameterHolder params);


    /**
     * return the product of the factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */
    public double getCountFactor(IonUseScore counter);

    /**
     * an algorithm may choose not to score a petide - for example high resolution algorithms may
     * choose not to score ppetides too far away
     * @param ts !null peptide spectrum
     * @param pScan  !null scan to score
     * @return true if scoring is desired
     */
    public boolean isTheoreticalSpectrumScored(ITheoreticalSpectrum ts,IMeasuredSpectrum pScan);

    /**
     * score the two spectra
     *
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @return value of the score
     */
    public double scoreSpectrum(IMeasuredSpectrum measured, ITheoreticalSpectrum theory, Object... otherdata);

    /**
     * actually do the scorring
     * @param scorer  !null scorrer
     * @param scan  !null scan to score
     * @param pPps  !null set of peptides ot score
     * @return !null score
     */
    public IScoredScan handleScan(final Scorer scorer,  final RawPeptideScan scan, final IPolypeptide[] pPps,TaskInputOutputContext context);

}
