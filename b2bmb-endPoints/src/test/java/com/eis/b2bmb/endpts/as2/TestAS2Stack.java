package com.eis.b2bmb.endpts.as2;

import com.eis.b2bmb.api.v1.dao.AS2ClientConnectionConfigDAO;
import com.eis.b2bmb.api.v1.dao.AS2ServerConnectionConfigDAO;
import com.eis.b2bmb.api.v1.model.AS2ClientConnectionConfig;
import com.eis.b2bmb.api.v1.model.AS2ServerConnectionConfig;
import com.eis.b2bmb.api.v1.model.MDNOptions;
import com.eis.base.test.TestBase;
import com.eis.core.api.v1.dao.*;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.*;
import com.eis.core.api.v1.service.EncryptionService;
import com.eis.core.api.v1.service.KeyPairService;
import com.eis.core.api.v1.service.PublicCertificateService;
import com.eis.crypto.CaptureSystemPassword;
import inedi.Certificate;
import inedi.InEDIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.UUID;

/**
 * User: mingardia
 * Date: 11/16/14
 * Time: 4:53 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
//@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class TestAS2Stack extends TestBase {

    private static final Logger LOG = LoggerFactory
            .getLogger(TestAS2Stack.class);

    //@Autowired
    // RegistrationRequestDAO registrationRequestDAO;

    @Autowired
    AS2ClientConnectionConfigDAO clientConnectionDAO;

    @Autowired
    AS2ServerConnectionConfigDAO serverConnectionConfigDAO;


    @Autowired
    AS2Client as2Client;

    @Autowired
    PublicCertificateDAO publicCertificateDAO;


    @Autowired
    KeyPairDAO keyPairDAO;

    @Autowired
    KeyPairService keyPairService;

    @Autowired
    PublicCertificateService publicCertificateService;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    PasswordDAO passwordDAO;


    @Autowired
    CaptureSystemPassword captureSystemPassword;

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired
    MailboxDAO mailboxDAO;




  /*  public void setUpData() {
        // We will create two participants each want to exchange documents via AS2
        // RetailerA and SupplierA.   RetailerA wants to send orders to the supplier
        // vai AS2.  This means the supplier needs to have a AS2 Server.  Each will
        // have to have certificates.

        // First we need to create the two different accounts in b2bmailbox
        // we need to register each and then approve each account.

        try {
            RegistrationRequest retailerRequest = registrationRequestDAO.getByRefName(
                    "Retailer1", Constants.B2BMAILBOX_APP_DATADOMAIN);


            if (retailerRequest == null) {
                retailerRequest = new RegistrationRequest();
                retailerRequest.setRefName("Retailer1");
                retailerRequest.setDataDomain(Constants.B2BMAILBOX_APP_DATADOMAIN);
                retailerRequest.setApplication("Cantata");
                retailerRequest.setCompanyName("Retailer1");
                retailerRequest.setDepartment("Retailer");
                retailerRequest.setEmailAddress("retailer1@retailer1.com");
                retailerRequest.setFirstName("Retailer1");
                retailerRequest.setLastName("Retailer1");
                retailerRequest.setPassword("password");
                retailerRequest.setCity("Alpharetta");
                retailerRequest.setState("GA");
                retailerRequest.setCountry("USA");

                retailerRequest = registrationRequestDAO.save(retailerRequest);

            }

            if (!retailerRequest.getStatus().equalsIgnoreCase("CLOSED")) {
                registrationRequestDAO.createAccountFrom(retailerRequest);
            }

        } catch (B2BTransactionFailed b2BTransactionFailed) {
            b2BTransactionFailed.printStackTrace();
            fail(b2BTransactionFailed.getMessage());
        } catch (B2BNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (ValidationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


    } */

    String createPublicCertificate(KeyPair keyPair, String orgName, String dataDomain, String privateKeyDecryptionPassword) throws
            B2BTransactionFailed,
            CertificateException, B2BNotFoundException, B2BNotAuthenticatedException, IOException, B2BNotAuthorizedException, InvalidKeySpecException,
            NoSuchAlgorithmException, CertificateEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException {


        if (keyPair == null) {
            throw new IllegalArgumentException("KeyPair can not be null");
        }

        if (orgName == null) {
            throw new IllegalArgumentException("OrgName can not be null");
        }

        if (dataDomain == null) {
            throw new IllegalArgumentException("dataDomain can not be null");
        }

        if (privateKeyDecryptionPassword == null) {
            throw new IllegalArgumentException("privateKeyDecryptionPassword can not be null");
        }

        if (keyPair.getPublicPEMPublicKey() == null) {
            throw new IllegalArgumentException("key pair public pem key is null");
        }

        if (keyPair.getEncryptedPEMPrivateKey() == null) {
            throw new IllegalArgumentException("key pair private encrypted PEM key is null");
        }


        String publicPEMKey = keyPair.getPublicPEMPublicKey();
        String privatePEMKey = encryptionService.decryptText(keyPair.getEncryptedPEMPrivateKey(),
                privateKeyDecryptionPassword);

        PublicCertificate publicCert = new PublicCertificate();

                /* Certificate validity Effective start date */
        Date effectiveStartDate = new Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L);

                /* Certificate validity Effective end date */
        Date effectiveEndDate = new Date(System.currentTimeMillis() + 1 * 365 * 24 * 60 * 60 * 1000L);

        java.security.KeyPair javaKeyPair = keyPairService.createJavaKeyPair(publicPEMKey,
                privatePEMKey, "RSA");


        X509Certificate signingCert = publicCertificateService.generateSelfSignedCertificate(javaKeyPair,
                orgName, orgName, orgName, orgName, effectiveStartDate, effectiveEndDate,
                PublicCertificate.SigningAlgorithms.SHA256WITHRSAENCRYPTION.value());

        String pemCert = PEMEncodeDecodeUtil.pemEncodeX509Certificate(signingCert);

        return pemCert;
    }

    protected Password savePassword(String passwordString, String encryptionPassword, String refName, String dataDomain,
                                    PasswordType type)
            throws B2BNotFoundException, B2BTransactionFailed, ValidationException {
        Password password = new Password();
        password.setRefName(refName);
        password.setDataDomain(dataDomain);
        password.setCurrent(true);
        password.setType(type);

        String encryptedText = encryptionService.encryptText(passwordString,
                encryptionPassword);
        password.setEncryptedPassword(encryptedText);

        password = passwordDAO.save(password);
        return password;
    }


    @Test
    public void testAs2ClientConnectionCRUD() throws B2BTransactionFailed, InEDIException,
            CertificateException, B2BNotFoundException, B2BNotAuthenticatedException,
            IOException, B2BNotAuthorizedException, InvalidKeySpecException, NoSuchAlgorithmException,
            CertificateEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException,
            ValidationException {

        serverSideLogin();

        String dataDomain = "com.mycompanyxyz";

        try {


            Organization org1 = organizationDAO.getByRefName("SupplierA", dataDomain);

            if (org1 == null) {
                Organization as2client1 = new Organization();
                as2client1.setRefName("SupplierA");
                as2client1.setDataDomain(dataDomain);
                as2client1.setOrgDataDomain(dataDomain);
                Contact contact = new Contact();
                contact.setRefName("mikessupplier");
                contact.setFirstName("Mike");
                contact.setLastName("Supplier");
                contact.setPrimaryPhone("201-555-1212");
                contact.setPrimaryEmail("msupplier@mycompanyxyz.com");
                as2client1.setPrimaryContact(contact);

                org1 = organizationDAO.save(as2client1);

            }

            String orgPassword = "password";


            // First ensure that there is an org level password saved
            Password retailerOrgPassword = passwordDAO.getByRefName("RetailerA" + "-OrgPassword", dataDomain);

            if (retailerOrgPassword == null) {

                retailerOrgPassword = savePassword("password", captureSystemPassword.getSystemPassPhrase(),
                        "RetailerA" + "-OrgPassword", dataDomain, PasswordType.Organization);
            }

            Password supplierOrgPassword = passwordDAO.getByRefName("SupplierA" + "-OrgPassword", dataDomain);

            if (supplierOrgPassword == null) {
                supplierOrgPassword = savePassword("password", captureSystemPassword.getSystemPassPhrase(),
                        "SupplierA" + "-OrgPassword", dataDomain, PasswordType.Organization);
            }

            // So we are going to set up the following scenario
            // SupplierA  will Send EDI 810's to Retailer A via AS2


            // First we need to create the supplier to retailer client configuration
            AS2ClientConnectionConfig clientConfig = clientConnectionDAO.getByRefName("RetailerA|SupplierA",
                    dataDomain);


            ObjectReference supplierSigningCertReference = null;
            supplierSigningCertReference = new ObjectReference();
            supplierSigningCertReference.setRefName("SupplierA:SigningCert");
            supplierSigningCertReference.setDataDomain(dataDomain);
            supplierSigningCertReference.setType(PublicCertificate.class.getName());


            ObjectReference encryptionCertReference = null;
            encryptionCertReference = new ObjectReference();
            encryptionCertReference.setRefName("RetailerA:EncryptionCert");
            encryptionCertReference.setDataDomain(dataDomain);
            encryptionCertReference.setType(PublicCertificate.class.getName());

            if (clientConfig == null) {
                clientConfig = new AS2ClientConnectionConfig();

                // We are sending data do the retailer
                clientConfig.setAS2To("RetailerA");

                // From the supplier
                clientConfig.setAS2From("SupplierA");

                // RefName is To|From
                clientConfig.setRefName("RetailerA|SupplierA");

                clientConfig.setDataDomain(dataDomain);

                // the URL of the retailers as2 server
                clientConfig.setServerURL("http://localhost:8181");

                // The from Organization is the Supplier
                ObjectReference orgRef = new ObjectReference();
                orgRef.setRefName("SupplierA");
                orgRef.setDataDomain(dataDomain);
                orgRef.setType(Organization.class.getName());
                clientConfig.setFromOrgObjectRef(orgRef);

                // Check to see if the signing certificate exists
                // This is "our" certificate i.e. te suppliers as the supplier will be signing the data
                // we are sending
                PublicCertificate supplierSigningCertificate = publicCertificateDAO.getByRefName
                        (supplierSigningCertReference.getRefName(),
                                dataDomain);


                String pemCert = null;


                if (supplierSigningCertificate == null) {
                    String orgName = "suppliera";
                    KeyPair keyPair = keyPairDAO.getByRefName(orgName, dataDomain);

                    if (keyPair == null) {
                        KeyPair objKeyPair = new KeyPair();
                        objKeyPair.setId(String.valueOf(UUID.randomUUID()));
                        objKeyPair.setDataDomain(dataDomain);
                        objKeyPair.setKeyPairId(orgName);
                        objKeyPair.setRefName(orgName);
                        objKeyPair.setKeySize(1024);
                        objKeyPair.setKeyType("RSA");

                        Password keyPw = passwordDAO.getByRefName("supplierKeyPassword", dataDomain);

                        if (keyPw == null) {
                            keyPw = this.savePassword("password",
                                    "password", "supplierKeyPassword", dataDomain, PasswordType.PrivateKeyEncryption);
                        }


                        ObjectReference pwRef = new ObjectReference();
                        pwRef.setRefName(keyPw.getRefName());
                        pwRef.setDataDomain(dataDomain);
                        pwRef.setType(Password.class.getName());
                        objKeyPair.setPasswordRef(pwRef);

                        objKeyPair = keyPairService.generateKeyPair(objKeyPair, orgPassword);

                        keyPair = keyPairDAO.save(objKeyPair);


                    }

                    // Create KeyPair if it does not exist
                    pemCert = createPublicCertificate(keyPair, "suppliera", dataDomain, orgPassword);


                    Certificate signingCert = new Certificate(pemCert.getBytes());


                    if (LOG.isDebugEnabled()) {
                        LOG.debug("IssueDate:" + signingCert.getEffectiveDate().toString());
                        LOG.debug("ExpireDate:" + signingCert.getExpirationDate().toString());
                        LOG.debug("Subject:" + signingCert.getSubject().toString());
                        LOG.debug("Serial:" + signingCert.getSerialNumber());
                    }

                    supplierSigningCertificate = new PublicCertificate();

                    supplierSigningCertificate.setRefName("SupplierA:SigningCert");
                    supplierSigningCertificate.setDataDomain(dataDomain);
                    supplierSigningCertificate.setPemCertificate(pemCert);
                    ObjectReference keyRef = new ObjectReference();
                    keyRef.setRefName(orgName);
                    keyRef.setDataDomain(dataDomain);
                    keyRef.setType(KeyPair.class.getName());
                    supplierSigningCertificate.setCreationKeyPair(keyRef);
                    supplierSigningCertificate.setCertificateType("SigningCertificate");

                    supplierSigningCertificate = publicCertificateDAO.save(supplierSigningCertificate);


                } else {
                    pemCert = supplierSigningCertificate.getPemCertificate();
                }

                clientConfig.setSigningCertRef(supplierSigningCertReference);


                /********************** Retailer Side of the client Setup   ****************/

                // Now we need our trading partners certificate assuming that the message is encrypted
                // using their certificate's public key.  This would be the retailer's public key
                PublicCertificate retailersPublicEncryptCert = publicCertificateDAO.getByRefName
                        (encryptionCertReference.getRefName(),
                                dataDomain);

                if (retailersPublicEncryptCert == null) {

                    String orgName = "retailera";


                    KeyPair objKeyPair = keyPairDAO.getByRefName(orgName, dataDomain);

                    if (objKeyPair == null) {
                        // normally we would throw here as we need the supplier to supply it.  But given this is a test
                        // we will go a head and create a dummy one for testing purposes.  Note typically you have no
                        // choice but to get the certificate from them as you won't have their private key.
                        objKeyPair = new KeyPair();
                        objKeyPair.setId(String.valueOf(UUID.randomUUID()));
                        objKeyPair.setDataDomain(dataDomain);
                        objKeyPair.setKeyPairId(orgName);
                        objKeyPair.setRefName(orgName);
                        objKeyPair.setKeySize(1024);
                        objKeyPair.setKeyType("RSA");

                        Password keyPw = passwordDAO.getByRefName("retaileraKeyPassword", dataDomain);

                        if (keyPw == null) {
                            keyPw = this.savePassword("password",
                                    "password", "retaileraKeyPassword", dataDomain, PasswordType.PrivateKeyEncryption);
                        }


                        ObjectReference pwRef = new ObjectReference();
                        pwRef.setRefName(keyPw.getRefName());
                        pwRef.setDataDomain(dataDomain);
                        pwRef.setType(Password.class.getName());
                        objKeyPair.setPasswordRef(pwRef);


                        objKeyPair = keyPairService.generateKeyPair(objKeyPair, orgPassword);
                    }


                    pemCert = createPublicCertificate(objKeyPair, orgName, dataDomain, orgPassword);

                    // wipe the private key is we won't have it
                    // TODO actually we need to save this for later as we will need to use it on the server side
                    //objKeyPair.setEncryptedPEMPrivateKey(null);

                    objKeyPair = keyPairDAO.save(objKeyPair);

                    retailersPublicEncryptCert = new PublicCertificate();
                    retailersPublicEncryptCert.setRefName("RetailerA:EncryptionCert");
                    retailersPublicEncryptCert.setDataDomain(dataDomain);
                    retailersPublicEncryptCert.setPemCertificate(pemCert);
                    ObjectReference keyRef = new ObjectReference();
                    keyRef.setRefName(orgName);
                    keyRef.setDataDomain(dataDomain);
                    keyRef.setType(KeyPair.class.getName());
                    retailersPublicEncryptCert.setCreationKeyPair(keyRef);
                    retailersPublicEncryptCert.setCertificateType("EncryptionCertificate");

                    retailersPublicEncryptCert = publicCertificateDAO.save(retailersPublicEncryptCert);


                }


                clientConfig.setEncryptionCertObjectReference(encryptionCertReference);

                MDNOptions mdnOptions = new MDNOptions();
                mdnOptions.setMdmTo("suppliera@mycompanyxyz.com");
                mdnOptions.setSync(true);

                clientConfig.setMdmOptions(mdnOptions);
                clientConfig.setCompressData(false);
                clientConfig = clientConnectionDAO.save(clientConfig);
            }

            /******** Now the Retailer's Server Configuration *****/


            // Ok now we need to set up the retailer's point of view.
            // we need to set up the server configuration.  So same format as the client config
            // AS2 TO| AS2 From
            AS2ServerConnectionConfig serverConfig = serverConnectionConfigDAO.getByRefName("RetailerA|SupplierA",
                    dataDomain);

            if (serverConfig == null) {
                // Set up the server side
                serverConfig = new AS2ServerConnectionConfig();
                serverConfig.setAS2To("RetailerA");
                serverConfig.setAS2From("SupplierA");
                serverConfig.setRefName("RetailerA|SupplierA");
                serverConfig.setDataDomain(dataDomain);

                // We the retailer are the server there for it is our encryption certificate that is used
                // to encrypt the data.  Thus we need to configure our server configuration to use the
                // retailers encryption certificate.  Li ke wise if the retailer is going to sign the data
                // then we need to use our signing certificate.

                serverConfig.setSigningCertRef(supplierSigningCertReference);
                serverConfig.setEncryptionCertObjectReference(encryptionCertReference);

                // The from Organization is the Supplier
                ObjectReference orgRef = new ObjectReference();
                orgRef.setRefName("SupplierA");
                orgRef.setDataDomain(dataDomain);
                orgRef.setType(Organization.class.getName());
                serverConfig.setFromOrgObjectRef(orgRef);


                serverConfig.setMailboxFrom("testAs2@" + clientConfig.getAS2From());
                serverConfig.setMailboxTo("testAS2@" + clientConfig.getAS2To());
                serverConfig.setMailboxRefName("testAs2");

                //ensure the mailbox exists
                Mailbox mb = mailboxDAO.getByRefName("testAs2", dataDomain);
                if (mb == null)
                {
                    mb = new Mailbox();
                    mb.setRefName("testAs2");
                    mb.setMailboxRole(MailboxRole.IN);
                    mb.setDataDomain(dataDomain);
                    mb.setAlias("testAs2");
                    mailboxDAO.save(mb);
                }


                serverConnectionConfigDAO.save(serverConfig);
            }

            String data = "Test123567890123123123123123123";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

            as2Client.sendFile(clientConfig, "test.data", inputStream, String.valueOf(UUID.randomUUID()));


        } finally {
            logout();
        }


    }

}
