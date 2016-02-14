package com.eis.b2bmb.endpts.ssl.compositemanager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Represents an ordered list of {@link X509TrustManager}s with additive trust. If any one of the
 * composed managers trusts a certificate chain, then it is trusted by the composite manager.
 * <p/>
 * This is necessary because of the fine-print on
 * Only the first instance of a particular key and/or trust manager implementation type in the
 * array is used. (For example, only the first javax.net.ssl.X509KeyManager in the array will be used.)
 */
public class CompositeX509TrustManager implements X509TrustManager {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeX509TrustManager.class);

    private final List<X509TrustManager> trustManagers;

    /**
     * Composite trust manager
     *
     * @param trustManagers - the trust managers to aggregate over
     */
    public CompositeX509TrustManager(List<X509TrustManager> trustManagers) {
        this.trustManagers = ImmutableList.copyOf(trustManagers);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing CompositeX509TrustManager");
            LOG.debug(" -- Trust Managers --");

            for (TrustManager t : trustManagers) {
                LOG.debug("-- " + t.getClass().getName() + " --");

                if (t instanceof X509TrustManager) {
                    X509TrustManager xtm = (X509TrustManager) t;


                    for (X509Certificate cert : xtm.getAcceptedIssuers()) {
                        LOG.debug(" X509 Issuer:" + cert.getIssuerDN().getName());
                    }
                }

                LOG.debug("------- END ----");
            }
        }

    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("checkClientTrusted: chain");

            for (X509Certificate cert : chain) {
                LOG.debug("cert Issuer" + cert.getIssuerDN().getName());
                LOG.debug("  cert Subject:" + cert.getSubjectDN().getName());
            }
        }

        for (X509TrustManager trustManager : trustManagers) {
            try {
                trustManager.checkClientTrusted(chain, authType);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found trusting ..");
                }
                return; // someone trusts them. success!
            } catch (CertificateException e) {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("did not find a certification looking for next one");
                }

                // maybe someone else will trust them
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Not found not Trusting ..");
        }
        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkServerTrusted: chain");

            for (X509Certificate cert : chain) {
                LOG.debug("cert Issuer" + cert.getIssuerDN().getName());
                LOG.debug("cert Subject:" + cert.getSubjectDN().getName());
            }
        }

        for (X509TrustManager trustManager : trustManagers) {


            try {
                trustManager.checkServerTrusted(chain, authType);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found trusted chain in trustManager:" + trustManager.getClass().getName() + "  " +
                            "trusting ..");
                }
                return; // someone trusts them. success!
            } catch (CertificateException e) {
                // maybe someone else will trust them
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Not found not Trusting ..");
        }

        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get Acceptable Issuers");
        }

        ImmutableList.Builder certificates = ImmutableList.builder();
        for (X509TrustManager trustManager : trustManagers) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("-- Trust Manager:" + trustManager.getClass().getName());
                X509Certificate[] issuers = trustManager.getAcceptedIssuers();

                for (X509Certificate cert : issuers) {
                    LOG.debug("-- IssuerDN:" + cert.getIssuerDN().getName());
                    LOG.debug("-- SubjectDN:" + cert.getSubjectDN().getName());
                }

                LOG.debug("------ End----");
            }


            certificates.add(trustManager.getAcceptedIssuers());
        }
        return Iterables.toArray(certificates.build(), X509Certificate.class);
    }

}