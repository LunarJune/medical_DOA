package xly.doip.server;

import java.io.IOException;
import java.io.Writer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import xly.doip.DoipConstants;
import xly.doip.DoipResponseHeadersWithRequestId;
import xly.doip.OutDoipMessage;
import xly.doip.util.GsonUtility;

/**
 * An implementation of {@link DoipServerResponse} used internally by {@link DoipServer}.
 */
public class DoipServerResponseImpl implements DoipServerResponse {

    private final OutDoipMessage outDoipMessage;
    private final String requestId;
    private String status = DoipConstants.STATUS_OK;
    private JsonObject attributes;
    private boolean wroteCompactOutput;
    private boolean committed;

    public DoipServerResponseImpl(String requestId, OutDoipMessage outDoipMessage) {
        this.requestId = requestId;
        this.outDoipMessage = outDoipMessage;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setAttribute(String key, JsonElement value) {
        if (this.attributes == null) this.attributes = new JsonObject();
        this.attributes.add(key, value);
    }

    @Override
    public void setAttribute(String key, String value) {
        if (this.attributes == null) this.attributes = new JsonObject();
        this.attributes.addProperty(key, value);
    }

    @Override
    public void setAttributes(JsonObject attributes) {
        this.attributes = attributes;
    }

    @Override
    public void commit() throws IOException {
        if (wroteCompactOutput || committed) return;
        committed = true;
        writeInitialSegment(null);
    }

    protected void writeInitialSegment(JsonElement output) throws IOException {
        DoipResponseHeadersWithRequestId segment = new DoipResponseHeadersWithRequestId();
        segment.requestId = requestId;
        segment.status = status;
        segment.attributes = attributes;
        segment.output = output;
        try (Writer writer = outDoipMessage.getJsonWriter()) {
            GsonUtility.getGson().toJson(segment, writer);
        } catch (JsonParseException e) {
            throw new IOException("Error writing initial segment", e);
        }
    }

    @Override
    public void writeCompactOutput(JsonElement output) throws IOException {
        if (wroteCompactOutput) throw new IllegalStateException("already wrote compact output");
        if (committed) throw new IllegalStateException("already committed");
        wroteCompactOutput = true;
        writeInitialSegment(output);
        outDoipMessage.close();
    }

    @Override
    public OutDoipMessage getOutput() throws IOException {
        if (wroteCompactOutput) throw new IllegalStateException("already wrote compact output");
        if (!committed) {
            committed = true;
            writeInitialSegment(null);
        }
        return outDoipMessage;
    }

}
