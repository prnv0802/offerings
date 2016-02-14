package com.eis.b2bmb.endpts.ssl.compositemanager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.net.ssl.*;
import java.security.*;
import java.util.Arrays;

/**
 * User: mingardia
 * Date: 10/27/13
 * Time: 2:24 PM
 */
public class SSLContextFactory {

    /**
     * Provides the SSL Context which is a composite of the default java one as well as those specified
     * by the keyStore provided.
     *
     * @param keystore - the keystore which will add certificates to the existing java keystore certs
     * @param password - password for the keystore
     * @return The SSLContext you can use to create a socketFactory from
     * @throws KeyManagementException    - KeyManagement Exception
     * @throws NoSuchAlgorithmException  - Bad Algorithm passed
     * @throws UnrecoverableKeyException - Corrupt KeyStore
     * @throws KeyStoreException         - Some other keystore related exception
     */
    public static SSLContext provideSSLContext(KeyStore keystore, char[] password) throws KeyManagementException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

        // Retrieve the default algorithm
        String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

        // Get our custom SunX509 key manager which will use the keystore we pass in as the place
        // to get our custom keys
        X509KeyManager customKeyManager = getKeyManager("SunX509", keystore, password);

        // Get the JVM KeyManager which will contain all the default keys installed in the JVM
        X509KeyManager jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);


        // Get our custom SunX509 TRUST manager with our custom keystore being used to get the Trust anchors
        X509TrustManager customTrustManager = getTrustManager("SunX509", keystore);

        // Get the JVM TRUST manager with the default keystore implementation which should have all the
        // installed trust anchors which come with the JVM
        X509TrustManager jvmTrustManager = getTrustManager(defaultAlgorithm, null);

        // Create the composite arrays
        KeyManager[] keyManagers = {new CompositeX509KeyManager(ImmutableList.of(jvmKeyManager, customKeyManager))};
        TrustManager[] trustManagers = {new CompositeX509TrustManager(ImmutableList.of(jvmTrustManager,
                customTrustManager))};

        // Get the context
        SSLContext context = SSLContext.getInstance("SSL");

        // Initialize our context with our arrays we set up.
        context.init(keyManagers, trustManagers, null);
        return context;
    }

    private static X509KeyManager getKeyManager(String algorithm, KeyStore keystore,
                                                char[] password) throws UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException {
        KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keystore, password);
        return Iterables.getFirst(Iterables.filter(
                Arrays.asList(factory.getKeyManagers()), X509KeyManager.class), null);
    }

    private static X509TrustManager getTrustManager(String algorithm,
                                                    KeyStore keystore) throws NoSuchAlgorithmException,
            KeyStoreException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        return Iterables.getFirst(Iterables.filter(
                Arrays.asList(factory.getTrustManagers()), X509TrustManager.class), null);
    }
}
