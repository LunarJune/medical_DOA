package xly.doip.client;

import com.google.gson.JsonElement;

/**
 * An interface for classes which provide authentication information to {@link DoipClient}.
 */
public interface AuthenticationInfo {

    /**
     * Returns the clientId to be supplied with the initial segment of DOIP requests.
     *
     * @return the clientId to be supplied with the initial segment of DOIP requests
     */
    String getClientId();

    /**
     * Returns the authentication property to be supplied with the initial segment of DOIP requests.
     *
     * @return the authentication property to be supplied with the initial segment of DOIP requests
     * @throws DoipException
     */
    JsonElement getAuthentication() throws DoipException;
}
