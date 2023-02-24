package xly.doip.server;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import xly.doip.InDoipMessage;

/**
 * Interface for an incoming DOIP request to be handled by a {@link DoipProcessor}.
 */
public interface DoipServerRequest {

    /**
     * Returns the client id given by the initial segment of the request.
     */
    String getClientId();

    /**
     * Returns the target id (the object on which to perform the operation) given by the initial segment of the request.
     */
    String getTargetId();

    /**
     * Returns the operation id (the operation to perform) given by the initial segment of the request.
     */
    String getOperationId();

    /**
     * Returns the attributes given by the initial segment of the request.
     */
    JsonObject getAttributes();

    /**
     * Returns a single attribute from the attributes given by the initial segment of the request.
     *
     * @param key the attribute to retrieve
     */
    JsonElement getAttribute(String key);

    /**
     * Returns as a String a single attribute from the attributes given by the initial segment of the request.
     *
     * @param key the attribute to retrieve
     */
    String getAttributeAsString(String key);

    /**
     * Returns the authentication information given by the initial segment of the request.
     */
    JsonElement getAuthentication();

    /**
     * Returns the input of the request.  In the case of a "compact" single-segment request, this will
     * be a single JSON segment corresponding to the "input" property of the single-segment request.
     * Otherwise it will be all remaining segments of the request after the initial segment.
     */
    InDoipMessage getInput();

    /**
     * If the client is using a TLS client-side certificate, this returns the client id from the certificate.
     */
    String getConnectionClientId();

    /**
     * If the client is using a TLS client-side certificate, this returns the public key from the certificate.
     */
    PublicKey getConnectionPublicKey();

    /**
     * If the client is using a TLS client-side certificate, this returns the supplied certificate chain.
     */
    X509Certificate[] getConnectionCertificateChain();
}
