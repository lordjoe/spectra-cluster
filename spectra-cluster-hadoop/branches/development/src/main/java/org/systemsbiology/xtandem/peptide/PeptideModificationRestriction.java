package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.PeptideModificationRestriction
 * User: steven
 * Date: 7/5/11
 */
public enum PeptideModificationRestriction {
    Global,NTerminal,CTerminal;
    public static final PeptideModificationRestriction[] EMPTY_ARRAY = {};

    public String getRestrictionString() {
        switch(this)  {
            case NTerminal:
                  return "[" ;
            case CTerminal:
                  return "]" ;
            default:
                return "";
          }
    }
}
