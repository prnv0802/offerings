package com.eis.b2bmb.camel.custom;

/**
 * @author harjeets
 */
public abstract class AS2Constants {
     
     /**
     * Data domain name.
     */
    public static final String DATA_DOMAIN_MYCOMPANY = "com.mycompanyxyz";
    
    /**
     * This denotes the user inbox name
     */
    public static final String DIR_INBOX = "Inbox";
    
    /**
     * Origin IP address to set while creating metadata
     */
    public static final String ORIGIN_IP_ADDRESS = "AS2-INTERNAL-PROCESS";
    
    /**
     * This denotes Ref Name
     */
    public static final String MAILBOX_REFNAME = "as2RefName";
    
    /**
     * This denotes the participant role as Host
     */
    public static final String PARTICIPANT_ROLE_HOST = "HostRole";
    
    /**
     * This denotes the participant role as Host
     */
    public static final String PARTICIPANT_ROLE_CLIENT = "ClientRole";
    
    /**
     * This denotes constant value for as2id
     */
    public static final String PROP_AS2ID = "as2Id";
    
    /**
     * This denotes constant value for url
     */
    public static final String PROP_URL = "url";
    
    /**
     * This denotes constant value for encryptCert
     */
    public static final String PROP_ENCRYPTCERT= "encryptCert";
    
    /**
     * This denotes constant value for signCert
     */
    public static final String PROP_SIGNCERT = "signCert";
    
    /**
     * This denotes constant value for ediId
     */
    public static final String PROP_EDIID = "ediId";
    
    /**
     * This denotes constant value for ediQual
     */
    public static final String PROP_EDIQUAL = "ediQual";
    
    /**
     * This denotes constant value for encryptionAlgorithm
     */
    public static final String PROP_ENCRYPTIONALGORITHM = "encryptionAlgorithm";
    /**
     * This denotes constant value for signMessages
     */
   
    public static final String PROP_SIGNMESSAGES = "signMessages";
    
    /**
     * This denotes constant value for signAlgorithm
     */
    public static final String PROP_SIGNALGORITHM = "signAlgorithm";
    /**
     * This denotes constant value for signCertRepo
     */
    public static final String PROP_SIGNCERTREPO = "signCertRepo";
    /**
     * This denotes constant value for signCertTrustRepo
     */
    
    public static final String PROP_SIGNCERTTRUSTREPO = "signCertTrustRepo";
    
    /**
     * This denotes constant value for encryptCertRepo
     */
    public static final String PROP_ENCRYPTCERTREPO = "encryptCertRepo";
   
    /**
     * This denotes constant value for encryptCertTrustRepo
     */
    public static final String PROP_ENCRYPTCERTTRUSTREPO = "encryptCertTrustRepo";
    
    /**
     * This denotes constant value for zipCompression
     */
    public static final String PROP_ZIPCOMPRESSION = "zipCompression";
    
    /**
     * This denotes constant value for requestMDM
     */
    public static final String PROP_REQUESTMDM = "requestMDM";
    
    /**
     * This denotes constant value for mdmType
     */
    public static final String PROP_MDMTYPE = "mdmType";
   
    /**
     * This denotes constant value for compressMDM
     */
    public static final String PROP_COMPRESSMDM = "compressMDM";
    
    /**
     * This denotes constant value for compressMDM
     */
    public static final int PROP_TIMEOUT = 80;
    
    /**
     * This denotes constant value for SMTPFrom
     */
    public static final String PROP_SMTPFROM = "admin@envistacorp.com";
    
    /**
     * This denotes constant value for SMTPServer
     */
    public static final String PROP_SMTPSERVER = "mail.google.com";
    

}
