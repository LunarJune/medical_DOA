package xly.doip;

import java.io.IOException;
import java.io.InputStream;

import com.google.gson.JsonElement;

/**
 * A segment of a DOIP message to be read as input (see {@link InDoipMessage}).
 */
public interface InDoipSegment {

    /**
     * Returns true if this is a JSON segment, false if this is a bytes segment.
     *
     * @return true if this is a JSON segment, otherwise false
     */
    boolean isJson();

    /**
     * Returns an input stream, for either a JSON segment or a bytes segment.
     *
     * @return an input stream containing the bytes from the segment (including a serialization of a JSON segment)
     */
    InputStream getInputStream();

    /**
     * Returns the JSON of a JSON segment
     *
     * @return the JSON of a JSON segment
     * @throws IOException if there is a JSON parsing error
     * @throws IllegalStateException if this is not a JSON segment
     */
    JsonElement getJson() throws IOException;
}