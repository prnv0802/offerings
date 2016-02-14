package com.eis.b2bmb.endpts.ssl.compositemanager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Compoaite X509 Key Manager
 */
public class CompositeX509KeyManager implements X509KeyManager {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeX509KeyManager.class);

    private final List<X509KeyManager> keyManagers;

    /**
     * Creates a new {@link CompositeX509KeyManager}.
     *
     * @param keyManagers the X509 key managers, ordered with the most-preferred managers first.
     */
    public CompositeX509KeyManager(List<X509KeyManager> keyManagers) {
        this.keyManagers = ImmutableList.copyOf(keyManagers);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Initializing CompositeX509KeyManager");
            LOG.debug(" -- Key Managers --");

            for (KeyManager k : keyManagers)
            {
                LOG.debug("-- " + k.getClass().getName() + " --");

            }
        }
    }


    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("ChooseClientAlais:");

            for (Principal p : issuers)
            {
                LOG.debug("Issuer:" + p.getName());
            }
        }

        for (X509KeyManager keyManager : keyManagers) {
            String alias = keyManager.chooseClientAlias(keyType, issuers, socket);
            if (alias != null) {

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found");
                }
                return alias;
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" Not Found");
        }
        return null;
    }


    @Override
    public  String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        for (X509KeyManager keyManager : keyManagers) {
            String alias = keyManager.chooseServerAlias(keyType, issuers, socket);
            if (alias != null) {
                return alias;
            }
        }
        return null;
    }


    @Override
    public  PrivateKey getPrivateKey(String alias) {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("getPriviateKey:" + alias);
        }

        for (X509KeyManager keyManager : keyManagers) {
            PrivateKey privateKey = keyManager.getPrivateKey(alias);
            if (privateKey != null) {
                return privateKey;
            }
        }
        return null;
    }


    @Override
    public  X509Certificate[] getCertificateChain(String alias) {

        if (LOG.isDebugEnabled())
        {
            LOG.debug("getCertificateChain:" + alias);
        }

        for (X509KeyManager keyManager : keyManagers) {
            X509Certificate[] chain = keyManager.getCertificateChain(alias);
            if (chain != null && chain.length > 0) {
                return chain;
            }
        }
        return null;
    }


    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {

        if (LOG.isDebugEnabled())
        {
            LOG.debug("getClientAliases");
        }

        ImmutableList.Builder aliases = ImmutableList.builder();
        for (X509KeyManager keyManager : keyManagers) {
            aliases.add(keyManager.getClientAliases(keyType, issuers));
        }
        return Iterables.toArray(aliases.build(), String.class);
    }


    @Override
    public  String[] getServerAliases(String keyType, Principal[] issuers) {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("getServerAliases");
        }

        ImmutableList.Builder aliases = ImmutableList.builder();
        for (X509KeyManager keyManager : keyManagers) {
            aliases.add(keyManager.getServerAliases(keyType, issuers));
        }
        return Iterables.toArray(aliases.build(), String.class);
    }



}