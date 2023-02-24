package xly.doip.server;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;

import com.google.gson.JsonObject;

/**
 * Configuration for a {@link DoipServer}.
 */
public class DoipServerConfig {

    /**
     * IP address to which to bind the listener.
     */
    public String listenAddress;

    /**
     * Port to which to bind the listener.
     */
    public int port;

    /**
     * Backlog to be used for the TCP listener (defaults to 50).
     */
    public int backlog = 50;

    /**
     * Idle time in ms for an open DOIP connection while waiting for another request (defaults to 5 minutes).
     */
    public int maxIdleTimeMillis = 5*60*1000;

    /**
     * Number of request-processing threads (defaults to 200).
     */
    public int numThreads = 200;

    /**
     * Class name of a {@link DoipProcessor}, which will be automatically instantiated, initialized, and later shut down
     * by a {@link DoipServer} when constructed using {@link DoipServer#DoipServer(DoipServerConfig)}.
     */
    public String processorClass;

    /**
     * JSON used to initialize an automatically constructed {@link DoipProcessor} (see {{@link #processorClass}).
     */
    public JsonObject processorConfig;

    /**
     * TLS configuration for a {@link DoipServer}.
     */
    public TlsConfig tlsConfig;

    /**
     * TLS configuration for a {@link DoipServer}.
     */
    public static class TlsConfig {
        /**
         * Identifier for the server, which will be included in any automatically generated server certificate.
         * If only id is present, a keypair will be minted on server startup.
         * If {@link #certificateChain} is provided this is ignored.
         */
        public String id;

        /**
         * Public key for the server.  If {@link #certificateChain} is provided this is ignored.  Otherwise
         * a self-signed certificate using this public key is automatically generated.
         */
        public PublicKey publicKey;

        /**
         * Private key for the server.  Required if {@link #publicKey} or {@link #certificateChain} are present.
         */
        public PrivateKey privateKey;

        /**
         * Certificate chain for the server.
         */
        public X509Certificate[] certificateChain;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(certificateChain);
            result = prime * result + Objects.hash(id, privateKey, publicKey);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TlsConfig other = (TlsConfig) obj;
            return Arrays.equals(certificateChain, other.certificateChain) && Objects.equals(id, other.id) && Objects.equals(privateKey, other.privateKey) && Objects.equals(publicKey, other.publicKey);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(backlog, listenAddress, maxIdleTimeMillis, numThreads, port, processorClass, processorConfig, tlsConfig);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DoipServerConfig other = (DoipServerConfig) obj;
        return backlog == other.backlog && Objects.equals(listenAddress, other.listenAddress) && maxIdleTimeMillis == other.maxIdleTimeMillis && numThreads == other.numThreads && port == other.port
            && Objects.equals(processorClass, other.processorClass) && Objects.equals(processorConfig, other.processorConfig) && Objects.equals(tlsConfig, other.tlsConfig);
    }
}
