package com.lordjoe.credentials;


/**
 * com.lordjoe.credentials.CredentialType
 * User: Steve
 * Date: 4/5/12
 */
public enum CredentialType {
    MYSQL_USER("MySQL User",false), SecretKey("MySQL User",true) ,
     AWS_Key("Amazon Key",false), AWS_SecretKey("Amazon Secret Key",true) ,
    GoogleDataUser("Google User",false),GoogleDataPassword("Google Password",true),
    HadoopClusterUser("Hadoop Cluster User",false),HadoopClusterPassword("Hadoop Password",true),
    HadoopClusterUrl("Hadoop Cluster URL",false),HadoopClusterPort("Hadoop Cluster Port",false),
    AWSKey("Amazon EC2 Key",false),AWSSecretKey("Amazon EC2 Key",true),

    ;

    private final String m_Name;
    private final boolean m_Secret;

    private CredentialType(final String pName,boolean secret) {
        m_Name = pName;
        m_Secret = secret;
    }


    /**
     * make a key without spaces
     * @return
     */
     public String toKey() {
        return super.toString().toLowerCase();
    }

    /**
     * make something that a user can be queried
     * @return
     */
    @Override
    public String toString() {
        return m_Name;
    }

    public boolean isSecret() {
        return m_Secret;
    }
}
