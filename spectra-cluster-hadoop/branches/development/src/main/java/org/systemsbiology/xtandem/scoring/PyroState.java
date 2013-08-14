package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.PyroState
 * User: steven
 * Date: Jan 12, 2011
 * not sure why but this does what mpyrostate does
 */
public class PyroState
{
    public static final PyroState[] EMPTY_ARRAY = {};

    private static Map<FastaAminoAcid, PyroState> gStates = new HashMap<FastaAminoAcid, PyroState>();


    public static void add(FastaAminoAcid key, PyroState added)
    {
        gStates.put(key, added);
    }


    public static PyroState get(FastaAminoAcid key)
    {
        PyroState state = gStates.get(key);
        if (state == null) {
            state = buildState(key);
        }
        return state;
    }

    private static PyroState buildState(final FastaAminoAcid pKey)
    {
        PyroState ret = null;
        double mass;
        switch (pKey) {
            case C:
            case Q:
                mass = MassCalculator.getDefaultCalculator().calcMass("NH3");
                ret = new PyroState(mass, pKey);
                break;
            case E:
                mass = MassCalculator.getDefaultCalculator().calcMass("H2O");
                ret = new PyroState(mass, pKey);
                break;
            default:
                ret = new PyroState(); // do nothing
        }
        gStates.put(pKey, ret);
        return ret;

    }


    private final double m_ModificationMass;
    private final FastaAminoAcid m_Residue;
    private final boolean m_Modified;

    private PyroState()
    {
        m_ModificationMass = 0;
        m_Residue = null;
        m_Modified = false;
    }

    private PyroState(final double pModificationMass, final FastaAminoAcid pResidue)
    {
        m_ModificationMass = pModificationMass;
        m_Residue = pResidue;
        m_Modified = true;
    }


    public double getModificationMass()
    {
        return m_ModificationMass;
    }

    public FastaAminoAcid getResidue()
    {
        return m_Residue;
    }

    public boolean isModified()
    {
        return m_Modified;
    }
}
