package com.lordjoe.credentials;

import com.lordjoe.utilities.*;

import javax.swing.*;
import java.util.*;
import java.util.prefs.*;

/**
 * com.lordjoe.credentials.Credentials
 * User: Steve
 * Date: 4/5/12
 */
public class Credentials {
    public static final Credentials[] EMPTY_ARRAY = {};

    private static Credentials gInstance;

    public static synchronized Credentials getInstance() {
        if (gInstance == null)
            gInstance = new Credentials();
        return gInstance;
    }

    private final Map<CredentialType, String> m_Credentials = new HashMap<CredentialType, String>();
    private final Preferences m_Prefs = Preferences.userNodeForPackage(Credentials.class);

    /**
     * use getInstance
     */
    private Credentials() {
    }

    /**
     * get unencrypted gredentials
     *
     * @param type !null type
     * @return possibly null credeitial
     */
    public String getCredentials(CredentialType type) {
        String ret = m_Credentials.get(type);
        if (ret != null) {
            return decryptAsNeeded(type, ret);
        }
        ret = readCredentialsFromPreferences(type);
        if (ret != null) {
            m_Credentials.put(type, ret);
            return decryptAsNeeded(type, ret);
        }

        ret = askCredentials(type);
        if (ret != null)
            storeCredentials(type, encryptAsNeeded(type, ret));
        return ret;
    }

    /**
     * clear all remembered credentials and preferences
     */
    public synchronized void clear() {
        m_Credentials.clear();
        CredentialType[] values = CredentialType.values();
        for (int i = 0; i < values.length; i++) {
            CredentialType key = values[i];
            m_Prefs.remove(key.toKey());
            try {
                m_Prefs.flush();
            }
            catch (BackingStoreException e) {
                throw new RuntimeException(e);

            }
        }
    }


    /**
     * set credentials with an unencrypted string
     *
     * @param pType !null type
     * @param raw   !null credential
     */
    public void setRawCredentials(final CredentialType pType, String raw) {
        String stored = encryptAsNeeded(pType, raw);
        setCredentials(pType, stored);
    }

    /**
     * set credentials with an unencrypted string  for secret types
     *
     * @param pType !null type
     * @param raw   !null credential
     */
    public void setCredentials(final CredentialType type, String possiblyEncrypted) {
        storeCredentials(type, possiblyEncrypted);
    }


    protected String decryptAsNeeded(final CredentialType pType, String raw) {
        if (pType.isSecret())
            return Encrypt.decryptString(raw);
        else
            return raw;
    }


    protected String encryptAsNeeded(final CredentialType pType, String raw) {
        if (pType.isSecret())
            return Encrypt.encryptString(raw);
        else
            return raw;
    }


    protected void storeCredentials(final CredentialType type, final String value) {
        m_Credentials.put(type, value);
        String key = type.toKey();
        if (value == null)
            m_Prefs.remove(key);
        else
            m_Prefs.put(key, value);
        try {
              m_Prefs.flush();
          }
          catch (BackingStoreException e) {
              throw new RuntimeException(e);

          }
     }

    protected String readCredentialsFromPreferences(final CredentialType pType) {
        String key = pType.toKey();
        String ans = m_Prefs.get(key, null);
        return ans;
    }

    protected String askCredentials(final CredentialType pType) {
        String ans;
        ans = JOptionPane.showInputDialog(null, "What is your " + pType + "?");
        return ans;
    }

    public static void main(String[] args) {
        Credentials instance = getInstance();
//        instance.clear();
        String ans;
        ans = instance.getCredentials(CredentialType.GoogleDataUser);
        ans = instance.getCredentials(CredentialType.GoogleDataPassword);
        ans = instance.getCredentials(CredentialType.HadoopClusterUser);
        ans = instance.getCredentials(CredentialType.HadoopClusterPassword);
        ans = null;
    }

}
