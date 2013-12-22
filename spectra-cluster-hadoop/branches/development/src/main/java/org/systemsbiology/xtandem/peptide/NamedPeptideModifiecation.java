package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.NamedPeptideModifiecation
 * User: Steve
 * Date: 12/19/13
 */
@SuppressWarnings("UnusedDeclaration")
public enum NamedPeptideModifiecation {
    Oxidation(15.994915),
    Carbamidomethyl(57.02146),
    ICAT_light(227.12),
    ICAT_heavy(236.12),
    AB_old_ICATd0(442.20),
    AB_old_ICATd8(450.20),
    Acetyl(42.0106),
    Deamidation(0.9840),
    Pyro0cmC(-17.026549),    // Pyro-cmC
    Pyro_glu(-18.010565),
    Amide(-0.984016),
    Phospho(79.9663),
    Methyl(14.0157),
    Carbamyl(43.00581);

    public static double nameToModifiedMass(String s)
    {
        if(s.equalsIgnoreCase("Pyro-cmC"))
            return  Pyro0cmC.getMassChange();
        NamedPeptideModifiecation mod = NamedPeptideModifiecation.valueOf(s);
        if(mod != null)
            return mod.getMassChange();
        throw new UnsupportedOperationException("unknown " + s);
    }


    private final double m_MassChange;

    private NamedPeptideModifiecation(final double pMassChange) {
        m_MassChange = pMassChange;
    }

    public double getMassChange() {
        return m_MassChange;
    }
}
