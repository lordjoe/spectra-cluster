package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.PyroState
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
public class PyroState
{
    public static PyroState[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = PyroState.class;

    private boolean m_bPotential;
    private boolean m_bPyro;
    private double m_dModMass;
    private char m_cRes;

    public PyroState()
    {
    }

    public boolean isbPotential()
    {
        return m_bPotential;
    }

    public boolean isbPyro()
    {
        return m_bPyro;
    }

    public double getdModMass()
    {
        return m_dModMass;
    }

    public char getcRes()
    {
        return m_cRes;
    }
}
