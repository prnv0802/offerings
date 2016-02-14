package com.eis.b2bmb.jca;

import com.eis.b2bmb.endpts.ssl.compositemanager.SSLContextFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;
import java.security.cert.CertificateException;

import static junit.framework.Assert.assertNotNull;


/**
 * User: mingardia
 * Date: 10/27/13
 * Time: 2:20 PM
 */
public class TestCompositeX509 {

    private static final Logger LOG = LoggerFactory.getLogger(TestCompositeX509.class);

    @Test
     public void testProvider() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        LOG.debug("------------- BEGIN test Provider ---------");

        LOG.debug(" -- Before add Provider");

         Provider p = Security.getProvider("SSIT");

         Provider[] providers = Security.getProviders();

         for (Provider ip : providers)
         {
              LOG.debug("Provider:" + ip.getName());
         }

         Security.addProvider(new SmartShipItProvider());

         LOG.debug("--");
         providers = Security.getProviders();

         LOG.debug("Default Type: " + KeyStore.getDefaultType());

         for (Provider ip : providers)
         {
             LOG.debug("Provider:" + ip.getName());
         }

         KeyStore keyStore = KeyStore.getInstance("SSIT");
         keyStore.load(null);

         keyStore.containsAlias("something");

         assertNotNull(keyStore);


        LOG.debug("------------- END test Provider ---------");


     }

     @Test
     public void testSSLWithCustomKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {

         LOG.debug("------------- BEGIN test SSLWithCustomKeyStore ---------");

         Provider p = Security.getProvider("SSIT");

         if (p == null)
         {
             Security.addProvider(new SmartShipItProvider());
         }

         KeyStore keyStore = KeyStore.getInstance("SSIT");
         keyStore.load(null);

         SSLContext context = SSLContextFactory.provideSSLContext(keyStore, "test".toCharArray());


         // Create a trust manager that does not validate certificate chains
        /* TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
             public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                 return null;
             }
             public void checkClientTrusted(X509Certificate[] certs, String authType) {
             }
             public void checkServerTrusted(X509Certificate[] certs, String authType) {
             }
         } };   */
         // Install the all-trusting trust manager
         //final SSLContext sc = SSLContext.getInstance("SSL");
         //sc.init(null, trustAllCerts, new java.security.SecureRandom());

        // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

         HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

         // Create all-trusting host refName verifier
         HostnameVerifier allHostsValid = new HostnameVerifier() {
             public boolean verify(String hostname, SSLSession session) {
                 return true;
             }
         };

         // Install the all-trusting host verifier
         HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

         URL url = new URL("https://www.google.com");
         URLConnection con = url.openConnection();
         final Reader reader = new InputStreamReader(con.getInputStream());
         final BufferedReader br = new BufferedReader(reader);
         String line = "";
         while ((line = br.readLine()) != null) {
            // System.out.println(line);
         }
         br.close();



         LOG.debug("------------- END test SSLWithCustomKeyStore ---------");
     }


}


