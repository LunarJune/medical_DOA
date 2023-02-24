package xly.doip.client.transport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import xly.doip.DoipResponseHeaders;
import xly.doip.InDoipMessage;
import xly.doip.InDoipMessageFromJson;

/**
 * A response from a server to the client.
 */
public class DoipClientResponse implements AutoCloseable {

    private final DoipResponseHeaders initialSegment;
    private final InDoipMessage in;
    private Runnable onClose;

    /**
     * Constructs the response with the given initial segment, and further segments to be read from the supplied {@link InDoipMessage}.
     *
     * @param initialSegment the contents of the initial segment of the response
     * @param in further segments fo the response
     */
    public DoipClientResponse(DoipResponseHeaders initialSegment, InDoipMessage in) {
        this.initialSegment = initialSegment;
        this.in = in;
    }

    /**
     * Sets a callback which will be run after closing the response.
     *
     * @param onClose the callback to be run after closing the response
     */
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    /**
     * Returns the status code from the initial segment of the response.
     *
     * @return the status code from the initial segment of the response
     */
    public String getStatus() {
        return initialSegment.status;
    }

    /**
     * Returns the attributes from the initial segment of the response.
     *
     * @return the attributes from the initial segment of the response
     */
    public JsonObject getAttributes() {
        return initialSegment.attributes;
    }

    /**
     * Returns a specified attribute from the initial segment of the response.
     *
     * @param key the property name of the attribute to retrieve
     * @return the value of the specified attribute
     */
    public JsonElement getAttribute(String key) {
        return initialSegment.attributes.get(key);
    }

    /**
     * Returns a specified attribute from the initial segment of the response, as a String.
     *
     * @param key the property name of the attribute to retrieve
     * @return the value as a String of the specified attribute
     */
    public String getAttributeAsString(String key) {
        return initialSegment.attributes.get(key).getAsString();
    }

    /**
     * Returns the "output" of the response as an {@link InDoipMessage} for the client to read from.
     * This can either be a single JSON segment, in the case
     * of a "compact" response fully contained in the initial segment, or in general it can be
     * all segments following the initial segment.
     *
     * @return
     */
    public InDoipMessage getOutput() {
        if (initialSegment.output != null) {
            return new InDoipMessageFromJson(initialSegment.output);
        } else {
            return in;
        }
    }

    /**
     * Closes the response "output" and runs a callback if one was specified using {@link #setOnClose(Runnable)}.
     */
    @Override
    public void close() throws Exception {
        if (in != null) in.close();
        if (onClose != null) onClose.run();
    }
}
