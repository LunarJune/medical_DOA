package xly.doip.util.tls;

import java.net.*;

import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

/**
 * This object is used to provide a private key for authentication to the other side
 * of a secure socket connection.
 */
public class AutoSelfSignedKeyManager extends X509ExtendedKeyManager {
    private static final String ALIAS = "ALIAS";
    private volatile X509Certificate myCert = null;
    private X509Certificate[] configuredChain = null;
    private final String id;
    private final PrivateKey privKey;
    private final PublicKey publicKey;

    /**
     * Constructs a key manager corresponding to the supplied certificate chain and private key.
     *
     * @param id the identifier of the entity
     * @param chain the certificate chain of the entity
     * @param privKey the private key of the entity
     */
    public AutoSelfSignedKeyManager(String id, X509Certificate[] chain, PrivateKey privKey) {
        if (chain == null || chain.length == 0) throw new IllegalArgumentException("Empty X509Certificate chain");
        this.id = id;
        this.configuredChain = chain;
        this.myCert = chain[0];
        this.privKey = privKey;
        this.publicKey = myCert.getPublicKey();
    }

    /**
     * Constructs a key manager corresponding to the supplied certificate and private key.
     *
     * @param id the identifier of the entity
     * @param cert the certificate of the entity
     * @param privKey the private key of the entity
     */
    public AutoSelfSignedKeyManager(String id, X509Certificate cert, PrivateKey privKey) {
        this.id = id;
        this.myCert = cert;
        this.privKey = privKey;
        this.publicKey = cert.getPublicKey();
    }

    /**
     * Constructs a key manager corresponding the the supplied keypair.  A self-signed certificate
     * will be generated with the appropriate id.
     *
     * @param id the identifier of the entity
     * @param pubKey the public key of the entity
     * @param privKey the private key of the entity
     */
    public AutoSelfSignedKeyManager(String id, PublicKey pubKey, PrivateKey privKey) {
        this.id = id;
        this.privKey = privKey;
        this.publicKey = pubKey;
    }

    /**
     * Constructs a key manager for the supplied identity with a newly minted keypair.
     *
     * @param id the identifier of the entity
     * @throws Exception if there is any issue minting a keypair
     */
    public AutoSelfSignedKeyManager(String id) throws Exception {
        this.id = id;
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keys = kpg.generateKeyPair();
        this.privKey = keys.getPrivate();
        this.publicKey = keys.getPublic();
    }

    /**
     * Returns the certificate (supplied or generated).
     *
     * @return the certificate
     */
    public X509Certificate getCertificate() {
        if (myCert == null) {
            synchronized (this) {
                if (myCert == null) {
                    try {
                        myCert = X509CertificateGenerator.generate(id, publicKey, privKey);
                    } catch (Exception e) {
                        System.err.println("Error generating certificate");
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return myCert;
    }

    @Override
    public String chooseClientAlias(String keyTypes[], Principal issuers[], Socket socket) {
        return ALIAS;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return ALIAS;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return new String[] { ALIAS };
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        if (configuredChain != null) {
            return configuredChain;
        } else if (getCertificate() != null) {
            return new X509Certificate[] { getCertificate() };
        } else {
            return new X509Certificate[0];
        }
    }

    @Override
    public String[] getClientAliases(String keyType, Principal issuers[]) {
        return new String[] { ALIAS };
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return privKey;
    }

    @Override
    public String chooseEngineClientAlias(String[] as, Principal[] aprincipal, SSLEngine sslengine) {
        return chooseClientAlias(as, aprincipal, null);
    }

    @Override
    public String chooseEngineServerAlias(String s, Principal[] aprincipal, SSLEngine sslengine) {
        return chooseServerAlias(s, aprincipal, null);
    }
}
