package xly.doip.client;

import com.google.gson.JsonObject;

/**
 * An {@link AuthenticationInfo} which provides a username and a password.
 */
public class PasswordAuthenticationInfo implements AuthenticationInfo {

    private final String username;
    private final String password;
    private final String asUserId;

    public PasswordAuthenticationInfo(String username, String password) {
        this(username, password, null);
    }

    public PasswordAuthenticationInfo(String username, String password, String asUserId) {
        this.username = username;
        this.password = password;
        this.asUserId = asUserId;
    }

    @Override
    public String getClientId() {
        return null;
    }

    @Override
    public JsonObject getAuthentication() {
        JsonObject authentication = new JsonObject();
        authentication.addProperty("username", username);
        authentication.addProperty("password", password);
        if (asUserId != null) {
            authentication.addProperty("asUserId", asUserId);
        }
        return authentication;
    }
}
