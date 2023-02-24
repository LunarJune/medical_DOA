package xly.doip.server;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import xly.doip.OutDoipMessage;

/**
 * Interface for an outgoing DOIP response to be populated by a {@link DoipProcessor}.
 * The initial segment of the response is a JSON segment with a status code and attributes.
 * The {@link DoipProcessor} can set those until the initial segment is "committed", that is,
 * sent to the client.  The initial segment can be committed manually with a call to {@link #commit()},
 * and will be committed automatically when either {@link #writeCompactOutput(JsonElement)} or
 * {@link #getOutput()} is called.
 */
public interface DoipServerResponse {

    /**
     * Sets the status code in the initial segment of the response.
     *
     * @param status the status code
     */
    void setStatus(String status);

    /**
     * Set a single attribute in the initial segment of the response.
     *
     * @param key the attribute to set
     * @param value the value of the attribute to be set
     */
    void setAttribute(String key, JsonElement value);

    /**
     * Set (as a String) a single attribute in the initial segment of the response.
     *
     * @param key the attribute to set
     * @param value the value of the attribute to be set
     */
    void setAttribute(String key, String value);

    /**
     * Set the entire collection of attributes in the initial segment of the response.
     *
     * @param attributes the new value of the entire attributes collection
     */
    void setAttributes(JsonObject attributes);

    /**
     * Ensure that the initial segment has been sent to the client.
     *
     * @throws IOException
     */
    void commit() throws IOException;

    /**
     * Write a "compact" single-segment output.  The output is supplied as an "output"
     * property in the JSON of the single segment sent to the client.
     *
     * @param output the output JSON
     * @throws IOException
     */
    void writeCompactOutput(JsonElement output) throws IOException;

    /**
     * Get an {@link OutDoipMessage} for writing output segments to the client.
     * This is used for a non-compact output containing multiple segments.
     * This will commit the initial segment, so it will no longer be possible
     * to change the status code or attributes.
     *
     * @return an OutDoipMessage for writing output segments.
     * @throws IOException
     */
    OutDoipMessage getOutput() throws IOException;
}
