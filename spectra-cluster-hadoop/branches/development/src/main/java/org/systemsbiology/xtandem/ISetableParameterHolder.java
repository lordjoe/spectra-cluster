package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.ISetableParameterHolder
 * User: Steve
 * Date: 6/20/11
 */
public interface ISetableParameterHolder extends IParameterHolder {
    public static final ISetableParameterHolder[] EMPTY_ARRAY = {};

    /**
     * set a parameter value
     * @param key  !null key
     * @param value  !null value
     */
    public void setParameter(String key,String value);



    public String[] getUnusedKeys();


}
