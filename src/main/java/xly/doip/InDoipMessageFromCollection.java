package xly.doip;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An implementation of {@link InDoipMessage} constructed by supplying an explicit collection of {@link InDoipSegment}s.
 */
public class InDoipMessageFromCollection implements InDoipMessage {

    private final Spliterator<InDoipSegment> spliterator;

    /**
     * Constructs an {@link InDoipMessage} whose segments are those in the supplied collection.
     *
     * @param segments the segments of the message
     */
    public InDoipMessageFromCollection(Collection<InDoipSegment> segments) {
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
