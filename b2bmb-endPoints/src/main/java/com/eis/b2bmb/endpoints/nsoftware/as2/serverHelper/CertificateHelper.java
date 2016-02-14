package com.eis.b2bmb.endpoints.nsoftware.as2.serverHelper;

import com.eis.core.api.v1.dao.KeyPairDAO;
import com.eis.core.api.v1.dao.PasswordDAO;
import com.eis.core.api.v1.dao.PublicCertificateDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.KeyPair;
import com.eis.core.api.v1.model.ObjectReference;
import com.eis.core.api.v1.model.Password;
import com.eis.core.api.v1.model.PublicCertificate;
import com.eis.core.api.v1.service.EncryptionService;
import com.eis.crypto.CaptureSystemPassword;
import inedi.Certificate;
import inedi.InEDIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * To be used to get cantata certificate on AS2 or SFTP side.
 * 
 * @author sudhakars date 2 may 2014
 */
public class CertificateHelper {
    /**
     * keyPairService - used to get keypair
     */
    @Autowired
    protected KeyPairDAO keyPairDAO;
    /**
     * trustedCertificateService - used to get certificate
     */
    @Autowired
    protected PublicCertificateDAO publicCertificateDAO;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    PasswordDAO passwordDAO;

    @Autowired
    CaptureSystemPassword captureSystemPassword;

    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(CertificateHelper.class);

    /**
     * Get the certificate for the as2 client
     * 
     * @param certificateObjRef
     *            - Public certificate object reference should be the ref name.
     * @return certificate - the InEDI certificate instance.
     * @throws B2BTransactionFailed
     *             - the underlying data source caused the transaction to fail
     *             the system.
     * @throws B2BNotAuthenticatedException
     *             - the user is not authenticated
     * @throws B2BNotAuthorizedException
     *             - the user is not authorized to make the call
     * @throws InEDIException
     *             - InEDIException while creating certificate
     */
    public Certificate getpublicCertificate(ObjectReference certificateObjRef)
            throws B2BNotAuthenticatedException, B2BNotAuthorizedException,
            B2BTransactionFailed, InEDIException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getpublicCertificate method started in >> "
                    + this.getClass().getCanonicalName());
        }

        if (certificateObjRef == null) {
            throw new IllegalArgumentException(
                    "certificateObjRef can not be null");
        }

        String cerDataDomain = certificateObjRef.getDataDomain();

        if (cerDataDomain == null) {
            throw new IllegalArgumentException(
                    "certificate DataDomain can not be null");
        }

        String cerRefName = certificateObjRef.getRefName();
        if (cerRefName == null) {
            throw new IllegalArgumentException(
                    "certificate RefName can not be null");
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" Looking for Cert:" + certificateObjRef.getRefName() + " in data Domain:" +
            certificateObjRef.getDataDomain());
        }


        Certificate certificate = null;

        PublicCertificate publicCertificate = publicCertificateDAO
                .getByRefName(cerRefName, cerDataDomain);
        if (publicCertificate == null) {
            throw new IllegalArgumentException(
                    "publicCertificate not found in database for RefName ::"
                            + cerRefName + " and data domain ::"
                            + cerDataDomain);
        }

        String pemCert = publicCertificate.getPemCertificate();

        if (pemCert == null) {
            throw new IllegalArgumentException("pemCert can not be null for cert with reName:" +
                    publicCertificate.getRefName() + " in dataDomain:" + publicCertificate.getDataDomain());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating InEDI Certificate object>>>>>>>> >> "
                    + pemCert);
        }

        // creating InEDI certificate object to be used in nsoftware API
        certificate = new Certificate(pemCert.getBytes());

        if (LOG.isDebugEnabled()) {
            LOG.debug("getpublicCertificate method completed in >> "
                    + this.getClass().getCanonicalName());
        }

        return certificate;
    }

    /**
     * Get the certificate for the as2 client
     * 
     * @param certificateObjRef
     *            - Public certificate object reference should be the ref name.
     * @param orgRefName
     *            - Sender Organization Reference
     * @return certificate - the InEDI certificate instance.
     * @throws B2BTransactionFailed
     *             - the underlying data source caused the transaction to fail
     *             the system.
     * @throws B2BNotAuthenticatedException
     *             - the user is not authenticated
     * @throws B2BNotAuthorizedException
     *             - the user is not authorized to make the call
     * @throws InEDIException
     *             - InEDIException while creating certificate
     * @throws IOException
     *             - Input Output exception occurred
     * @throws CertificateException
     *             - Indicates that a creation of the Certificate failed.
     * @throws B2BNotFoundException
     *             - Indicates that a retrieval of object failed.
     */
    public synchronized Certificate  getCertificateWithPrivateKey(
            ObjectReference certificateObjRef, ObjectReference orgRefName)
            throws InEDIException, B2BNotAuthenticatedException,
            B2BNotAuthorizedException, B2BTransactionFailed,
            B2BNotFoundException, CertificateException, IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("getPublicCertificateWithPrivateKey method started in >> "
                    + this.getClass().getCanonicalName());
        }

        if (certificateObjRef == null) {
            throw new IllegalArgumentException(
                    "certificateObjRef can not be null");
        }

        if (orgRefName == null) {
            throw new IllegalArgumentException(
                    "Organization ref name found null >>>>>>>");
        }
        String cerDataDomain = certificateObjRef.getDataDomain();

        if (cerDataDomain == null) {
            throw new IllegalArgumentException(
                    "certificate DataDomain can not be null");
        }

        String cerRefName = certificateObjRef.getRefName();
        if (cerRefName == null) {
            throw new IllegalArgumentException(
                    "certificate RefName can not be null");
        }

        Certificate certificate = null;

        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting public certificate for refName::  "+cerRefName +"   dataDomain ::"+cerDataDomain);
        }
        
        PublicCertificate publicCertificate = publicCertificateDAO
                .getByRefName(cerRefName, cerDataDomain);
        if (publicCertificate == null) {
            throw new IllegalArgumentException(
                    "publicCertificate not found in database for RefName ::"
                            + cerRefName + " and data domain ::"
                            + cerDataDomain);
        }

        String pemCert = publicCertificate.getPemCertificate();
        if (pemCert == null) {
            throw new IllegalArgumentException("pemCert can not be null");
        }

        ObjectReference keyPairRef = publicCertificate.getCreationKeyPair();

        if (keyPairRef == null) {
             throw new IllegalArgumentException("keyPair Ref can not be null");
        }

        String keypairRefName = keyPairRef.getRefName();
        if (keypairRefName == null) {
            throw new IllegalArgumentException(
                    "keyPair RefName not found in object reference");
        }

        String keypairDataDomain = keyPairRef.getDataDomain();

        if (keypairDataDomain == null) {
            throw new IllegalArgumentException(
                    "keypairDataDomain not found in object reference");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting key pair  for refName  ::   "+keypairRefName +"  dataDomain ::"+keypairDataDomain);
        }
        
        KeyPair keyPair = keyPairDAO.getByRefName(keypairRefName,
                keypairDataDomain);

        if (keyPair == null) {
            throw new IllegalArgumentException(
                    "keyPair not found in data base >>>>> for refname ::"
                            + keypairRefName);
        }

        String systemPassPhrase = captureSystemPassword.getSystemPassPhrase();

        if (systemPassPhrase == null) {
            throw new IllegalArgumentException(
                    "System level password found null");
        }

        KeyPair objKeyPair = keyPairDAO.getByRefName(keyPairRef.getRefName(), keyPairRef.getDataDomain());

        if (objKeyPair == null) {
            throw new IllegalArgumentException(
                    "Key pair found null from public certificate with refname ::  "+keypairRefName);
        }

        ObjectReference pvtKeyPasswordRef = objKeyPair.getPasswordRef();

        if (pvtKeyPasswordRef == null) {
            throw new IllegalArgumentException(
                    "Key pair private key object reference instance cannot be null  objectKeyPair RefName:" +
                            objKeyPair.getRefName() +
            " dataDomain:" + objKeyPair.getDataDomain());
        }

        String keyPasswordRefName = pvtKeyPasswordRef.getRefName();

        if (keyPasswordRefName == null) {
            throw new IllegalArgumentException("Key pair refname not be null");
        }

        String keyPasswordDataDomain = pvtKeyPasswordRef.getDataDomain();

        if (keyPasswordDataDomain == null) {
            throw new IllegalArgumentException(
                    "Key pair dataDomain not be null");
        }
        
        Password privateKeyPassword = passwordDAO.getByRefName(keyPasswordRefName, keyPasswordDataDomain);

        if (privateKeyPassword == null) {
            throw new IllegalArgumentException(
                    "Organization privateKeyPassword found null for refName :"+keyPasswordRefName);
        }
        
        String orgPassRefName=orgRefName.getRefName()+"-OrgPassword";

        Password orgPassword = passwordDAO.getByRefName
          (orgPassRefName,orgRefName.getDataDomain());

        if (orgPassword == null) {
            throw new IllegalArgumentException(
                    "Organization level password found null for refName :" + orgPassRefName +
            " in dataDomain " + orgRefName.getDataDomain());
        }

        String encryptOrgPassword = orgPassword.getEncryptedPassword();
        
        if (encryptOrgPassword == null) {
            throw new IllegalArgumentException(
                    " Encrypted Organization password cannot be null for orgPassword with refName:" +
            orgPassword.getRefName() + " in data domain:" + orgPassword.getDataDomain());
        }

        String decryptedOrgPassword = encryptionService.decryptText( encryptOrgPassword, systemPassPhrase);

        String decryptedPrivatePassword = encryptionService.decryptText( privateKeyPassword.getEncryptedPassword(),
                decryptedOrgPassword);

        String encryptPemKey = objKeyPair.getEncryptedPEMPrivateKey();

        if (encryptPemKey == null) {
            throw new IllegalArgumentException(
                    " Encrypted Pem Key  cannot be null");
        }
        


        // Decrypted pem private key
        String decryptPrivatePemKey = encryptionService.decryptText(
                encryptPemKey, decryptedPrivatePassword);

        if (decryptPrivatePemKey == null) {
            throw new IllegalArgumentException(
                    "Decrypt Private Pem Key  cannot be null");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found pem decrypted private key");
        }

        // String privateKey =
        // keyPairService.decryptPEMPrivateKey(encryptPemKey,
        // decryptOrgPassword);
        if ( !decryptPrivatePemKey.isEmpty()) {
            String serverCert = pemCert + decryptPrivatePemKey;

            if (!serverCert.isEmpty()) {

                certificate = new Certificate(serverCert.getBytes());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Certificate including public key and private key created successfully >>>>>>>");
                }

            } else {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Certificate Including public key and private key not created >>>>>>>");
                }
                throw new CertificateException(
                        "Unable to create "
                                + "certificate because server decrypted certificate string is null");
            }

        } else {

            if (LOG.isErrorEnabled()) {
                LOG.error("Certificate Including public key and private key not created >>>>>>>");
                LOG.error("Because private key string is mandatory to create this certificate !!!");
            }

            throw new IllegalStateException(
                    " private key found null for keypair refName ::"
                            + keypairRefName);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getpublicCertificate method completed successfully>> "
                    + this.getClass().getCanonicalName());
        }

        return certificate;
    }

  

}
