package xly.doip;

import java.util.stream.Stream;

import xly.doip.util.InDoipMessageUtil;

/**
 * A DOIP message to be read as input (for example, a response for the client, or a request for the server).
 * It is a stream or iterable of {@link InDoipSegment}s.  Users must call {@link #close()} when processing is complete.
 *
 * See also {@link InDoipMessageUtil}.
 */
public interface InDoipMessage extends Iterable<InDoipSegment>, AutoCloseable {
    /**
     * Returns an stream over elements of type {@link InDoipSegment}.
     *
     * @return a Stream.
     */
    Stream<InDoipSegment> stream();

    @Override
    void close();
}