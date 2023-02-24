package xly.doip.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import xly.doip.BadDoipException;
import xly.doip.DoipRequestHeadersWithRequestId;
import xly.doip.InDoipMessage;
import xly.doip.InDoipMessageFromJson;
import xly.doip.InDoipSegment;
import xly.doip.util.GsonUtility;

/**
 * An implementation of {@link DoipServerRequest} used internally by {@link DoipServer}.
 */
public class DoipServerRequestImpl implements DoipServerRequest {

    private final InDoipMessage inDoipMessage;
    private final String clientCertId;
    private final PublicKey clientCertPublicKey;
    private final X509Certificate[] clientCertChain;
    private DoipRequestHeadersWithRequestId doipRequestHeaders;
    private InDoipMessage inputFromHeadersJson;

    public DoipServerRequestImpl(InDoipMessage inDoipMessage, String clientCertId, PublicKey clientCertPublicKey, X509Certificate[] clientCertChain) throws IOException {
        this.inDoipMessage = inDoipMessage;
        this.clientCertId = clientCertId;
        this.clientCertPublicKey = clientCertPublicKey;
        this.clientCertChain = clientCertChain;
        try {
            boolean found = inDoipMessage.spliterator().tryAdvance(this::handleFirstSegmentUnchecked);
            if (!found) throw new BadDoipException("no initial segment in request");
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private void handleFirstSegmentUnchecked(InDoipSegment segment) {
        try {
            handleFirstSegment(segment);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void handleFirstSegment(InDoipSegment segment) throws IOException {
        if (!segment.isJson()) {
            throw new BadDoipException("Request initial segment must be JSON");
        }
        JsonElement el = segment.getJson();
        try {
            if (!el.isJsonObject()) {
                throw new BadDoipException("Request initial segment must be a JSON object");
            }
            checkProperties(el.getAsJsonObject());
            doipRequestHeaders = GsonUtility.getGson().fromJson(el, DoipRequestHeadersWithRequestId.class);
        } catch (JsonParseException e) {
            throw new BadDoipException("Error parsing initial JSON of request", e);
        }
        if (doipRequestHeaders.input != null) {
            boolean found = inDoipMessage.spliterator().tryAdvance(nextSegment -> {});
            if (found) throw new BadDoipException("Extra segments after initial JSON with compact input");
            inDoipMessage.close();
        }
    }

    private static final List<String> acceptableRequestProperties;
    static {
        java.lang.reflect.Field[] fields = DoipRequestHeadersWithRequestId.class.getFields();
        acceptableRequestProperties = Stream.of(fields)
        .map(field -> field.getName())
        .collect(Collectors.toList());
    }

    private void checkProperties(JsonObject obj) throws BadDoipException {
        for (String key : obj.keySet()) {
            if (!acceptableRequestProperties.contains(key)) {
                throw new BadDoipException("Unexpected request property " + key);
            }
        }
    }

    public String getRequestId() {
        return doipRequestHeaders.requestId;
    }

    @Override
    public String getClientId() {
        return doipRequestHeaders.clientId;
    }

    @Override
    public String getTargetId() {
        return doipRequestHeaders.targetId;
    }

    @Override
    public String getOperationId() {
        return doipRequestHeaders.operationId;
    }

    @Override
    public JsonObject getAttributes() {
        return doipRequestHeaders.attributes;
    }

    @Override
    public JsonElement getAttribute(String key) {
        if (doipRequestHeaders.attributes == null) return null;
        return doipRequestHeaders.attributes.get(key);
    }

    @Override
    public String getAttributeAsString(String key) {
        if (doipRequestHeaders.attributes == null) return null;
        JsonElement el = doipRequestHeaders.attributes.get(key);
        if (el == null) return null;
        return el.getAsString();
    }

    @Override
    public JsonElement getAuthentication() {
        return doipRequestHeaders.authentication;
    }

    @Override
    public InDoipMessage getInput() {
        JsonElement json = doipRequestHeaders.input;
        if (json != null) {
            if (inputFromHeadersJson != null) return inputFromHeadersJson;
            inputFromHeadersJson = new InDoipMessageFromJson(json);
            return inputFromHeadersJson;
        }
        return inDoipMessage;
    }

    @Override
    public String getConnectionClientId() {
        return clientCertId;
    }

    @Override
    public PublicKey getConnectionPublicKey() {
        return clientCertPublicKey;
    }

    @Override
    public X509Certificate[] getConnectionCertificateChain() {
        return clientCertChain;
    }
}
