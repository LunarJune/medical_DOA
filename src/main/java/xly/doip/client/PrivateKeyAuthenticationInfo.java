package xly.doip.client;

import com.google.gson.JsonObject;
import xly.handle.hdllib.trust.JsonWebSignature;
import xly.handle.hdllib.trust.JsonWebSignatureFactory;
import xly.handle.hdllib.trust.TrustException;
import org.apache.commons.codec.binary.Hex;

import java.security.PrivateKey;
import java.security.SecureRandom;

/**
 * An {@link AuthenticationInfo} which provides an identity and a JWT signed by that entity's private key.
 */
public class PrivateKeyAuthenticationInfo implements AuthenticationInfo {

    private final String clientId;
    private final PrivateKey privateKey;
    private final SecureRandom random;
    private final String asUserId;

    public PrivateKeyAuthenticationInfo(String clientId, PrivateKey privateKey) {
        this(clientId, privateKey, null);
    }

    public PrivateKeyAuthenticationInfo(String clientId, PrivateKey privateKey, String asUserId) {
        this.clientId = clientId;
        this.privateKey = privateKey;
        this.random = new SecureRandom();
        this.asUserId = asUserId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public JsonObject getAuthentication() throws DoipException {
        JsonObject authentication = new JsonObject();
        try {
            String token = createBearerToken(privateKey, clientId);
            authentication.addProperty("token", token);
            if (asUserId != null) {
                authentication.addProperty("asUserId", asUserId);
            }
            return authentication;
        } catch (TrustException te) {
            throw new DoipException(te);
        }
    }

    private String createBearerToken(@SuppressWarnings("hiding") PrivateKey privateKey, String iss) throws TrustException {
        long nowSeconds = System.currentTimeMillis() / 1000L;
        JsonObject claims = new JsonObject();
        claims.addProperty("iss", iss);
        claims.addProperty("sub", iss);
        claims.addProperty("jti", generateJti());
        claims.addProperty("iat", nowSeconds);
        claims.addProperty("exp", nowSeconds + 600);
        String claimsJson = claims.toString();
        JsonWebSignature jwt;
        jwt = JsonWebSignatureFactory.getInstance().create(claimsJson, privateKey);
        return jwt.serialize();
    }

    private String generateJti() {
        byte bytes[] = new byte[10];
        random.nextBytes(bytes);
        String hex = Hex.encodeHexString(bytes);
        return hex;
    }
}
