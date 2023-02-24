package xly.doip.client;

import com.google.gson.JsonObject;

/**
 * An {@link AuthenticationInfo} which supplies a specified token for a specified clientId.
 */
public class TokenAuthenticationInfo implements AuthenticationInfo {

    private final String clientId;
    private final String token;
    private final String asUserId;

    public TokenAuthenticationInfo(String clientId, String token) {
        this(clientId, token, null);
    }

    public TokenAuthenticationInfo(String clientId, String token, String asUserId) {
        this.clientId = clientId;
        this.token = token;
        this.asUserId = asUserId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public JsonObject getAuthentication() {
        JsonObject authentication = new JsonObject();
        authentication.addProperty("token", token);
        if (asUserId != null) {
            authentication.addProperty("asUserId", asUserId);
        }
        return authentication;
    }
}
