package xly.doip.client;

import java.security.PublicKey;

/**
 * A representation of a service identifier together with the DOIPServiceInfo needed for connecting to the DOIP service.
 */
public class ServiceInfo {

    /**
     * The service identifier.  A ServiceInfo provided to methods of {@link DoipClient} may have only a serviceId in which case
     * the client will resolve the identifier to find the actual service info.
     */
    public String serviceId;

    /**
     * Currently unused.
     */
    public String serviceName;

    /**
     * The IP address to connect to.
     */
    public String ipAddress;

    /**
     * The port to connect to.
     */
    public int port;

    /**
     * The protocol; currently assumed to be TCP.
     */
    public String protocol;

    /**
     * The protocol version; currently assume to be DOIPv2.
     */
    public String protocolVersion;

    /**
     * The public key of the service (serialized in JWK format).
     */
    public PublicKey publicKey;

    /**
     * Constructs a ServiceInfo with all fields unpopulated.
     */
    public ServiceInfo() {}

    /**
     * Constructs a ServiceInfo with only a service identifier.  If supplied to methods of {@link DoipClient},
     * the client will resolve the identifier to find the service information.
     *
     * @param serviceId the service identifier
     */
    public ServiceInfo(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Constructs a ServiceInfo with a service identifier, an IP address, and a port.  With no public key, the server
     * certificate will not be checked.
     *
     * @param serviceId the service identifier
     * @param ipAddress the IP address
     * @param port the port
     */
    public ServiceInfo(String serviceId, String ipAddress, int port) {
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Constructs a ServiceInfo with a service identifier, an IP address, a port, and the expecte
     * public key in the server certificate.
     *
     * @param serviceId the service identifier
     * @param ipAddress the IP address
     * @param port the port
     * @param publicKey the public key of the server
     */
    public ServiceInfo(String serviceId, String ipAddress, int port, PublicKey publicKey) {
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.publicKey = publicKey;
    }
}
