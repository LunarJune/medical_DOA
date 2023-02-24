package xly.doip.client.transport;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * Options for a connection to a DOIP server.
 */
public class ConnectionOptions {

    /**
     * The IP address of the DOIP server.
     */
    public String address;

    /**
     * The port of the DOIP server.
     */
    public int port;

    /**
     * Connection timeout in ms.
     */
    public Integer connectTimeoutMs;

    /**
     * Read timeout in ms.
     */
    public Integer readTimeoutMs;

    /**
     * The identifier of the server; if present {@link #trustedServerPublicKeys} should also be provided.
     */
    public String serverId;

    /**
     * A list of public keys that will be trusted for this server.
     */
    public List<PublicKey> trustedServerPublicKeys;

    /**
     * The identifier for the client; if present, will be used along with {@link #clientPublicKey} and {@link #clientPrivateKey}
     * to produce a client-side TLS certificate to be used for the connection.
     */
    public String clientId;
    public PublicKey clientPublicKey;
    public PrivateKey clientPrivateKey;
}
