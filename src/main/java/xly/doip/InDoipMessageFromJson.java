package xly.doip;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;

/**
 * An implementation of {@link InDoipMessage} constructed by supplying a single {@code JsonElement}, which will lead to
 * a single JSON segment in the message.
 */
public class InDoipMessageFromJson implements InDoipMessage {

    private final Spliterator<InDoipSegment> spliterator;

    /**
     * Constructs an {@link InDoipMessage} with a single JSON segment containing the supplied JSON.
     *
     * @param json the JSON of the single segment of the message
     */
    public InDoipMessageFromJson(JsonElement json) {
        InDoipSegment segment = new InDoipSegmentFromJson(json);
        List<InDoipSegment> segments = Collections.singletonList(segment);
        this.spliterator = Spliterators.spliterator(segments, Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }

    @Override
    public Iterator<InDoipSegment> iterator() {
        return Spliterators.iterator(spliterator);
    }

    @Override
    public Spliterator<InDoipSegment> spliterator() {
        return spliterator;
    }

    @Override
    public Stream<InDoipSegment> stream() {
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * A no-op for this implementation.
     */
    @Override
    public void close() {
        // no-op
    }

}
