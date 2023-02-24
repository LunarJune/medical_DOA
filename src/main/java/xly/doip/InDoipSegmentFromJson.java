package xly.doip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;

/**
 * An implementation of {@link InDoipSegment} produced from supplied JSON, which is always a JSON segment.
 */
public class InDoipSegmentFromJson implements InDoipSegment {

    private final JsonElement json;

    /**
     * Constructs an {@link InDoipSegment} JSON segment from the supplied JSON.
     *
     * @param json the JSON for the JSON segment
     */
    public InDoipSegmentFromJson(JsonElement json) {
        this.json = json;
    }

    @Override
    public boolean isJson() {
        return true;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JsonElement getJson() {
        return json;
    }

}
