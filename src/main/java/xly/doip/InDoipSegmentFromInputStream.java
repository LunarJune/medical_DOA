package xly.doip;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * An implementation of {@link InDoipSegment} produced from in input stream; may be either a JSON segment or a bytes segment.
 */
public class InDoipSegmentFromInputStream implements InDoipSegment {
    private final boolean isJson;
    private final InputStream in;
    private JsonElement json;

    /**
     * Constructs an {@link InDoipSegment} from an input stream.
     *
     * @param isJson whether this will be a JSON segment
     * @param in an input stream supplying the bytes of the segment
     */
    public InDoipSegmentFromInputStream(boolean isJson, InputStream in) {
        this.isJson = isJson;
        this.in = in;
    }

    @Override
    public boolean isJson() {
        return isJson;
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public JsonElement getJson() throws IOException {
        if (!isJson) throw new IllegalStateException("not a JSON segment");
        if (json != null) return json;
        try (InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            json = new JsonParser().parse(isr);
            return json;
        } catch (JsonParseException e) {
            throw new BadDoipException("invalid JSON", e);
        }
    }
}
