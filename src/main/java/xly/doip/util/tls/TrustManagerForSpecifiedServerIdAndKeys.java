package xly.doip.util.tls;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509TrustManager;

/**
 * A trust manager which expects the server certificate to both have the
 * specified identitifier, and to have one of a list of specified public keys.
 */
public class TrustManagerForSpecifiedServerIdAndKeys implements X509TrustManager {
    private final String id;
    private final List<PublicKey> keys;

    /**
     * Constructs a  trust manager which expects the server certificate to both have the
     * specified identitifier, and to have one of a list of specified public keys.
     *
     * @param id the expected server identifier
     * @param keys the list of permissible server public keys
     */
    public TrustManagerForSpecifiedServerIdAndKeys(String id, List<PublicKey> keys) {
        this.id = id;
        this.keys = keys;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain == null || chain.length == 0) throw new IllegalArgumentException("null or empty certificate chain");
        authenticate(chain[0]);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain == null || chain.length == 0) throw new IllegalArgumentException("null or empty certificate chain");
        authenticate(chain[0]);
    }

    private void authenticate(X509Certificate cert) throws CertificateException {
        try {
            String certId = X509IdParser.parseIdentityHandle(cert);
            if (certId == null) throw new CertificateException("Could not parse identity from server certificate");
            if (!certId.equals(id)) {
                throw new CertificateException("Unable to validate X509 certificate, id does not match expected id");
            }
            PublicKey certKey = cert.getPublicKey();
            if (keys == null || keys.contains(certKey)) return;
            throw new CertificateException("Unable to validate X509 certificate, public key does not match any of expected public keys");
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            throw new CertificateException("Exception validating X509 certificate", e);
        }
    }
}
