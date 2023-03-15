package xly.doip.client;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A representation of search results from {@link DoipClient} search operations.
 * Provides either ids (an {@code Iterable} or {@code Stream} of String objects) or
 * Digital Objects (an {@code Iterable} or {@code Stream} of DigitalObject objects).
 *
 * @param <T> either String for searchIds or DigitalObject for full search
 */
public interface SearchResults<T> extends Iterable<T>, AutoCloseable {

    /**
     * Returns the full number of results across all pages, or -1 if the number is not available
     */
    int size();

    /**
     * Returns an {@code Iterator} of the search results (either String or DigitalObject).
     */
    @Override
    Iterator<T> iterator();

    /**
     * Closes the search results, releasing the connection to the server.
     */
    @Override
    void close();

    @Override
    default Spliterator<T> spliterator() {
        int characteristics = Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;
        if (size() >= 0) {
            return Spliterators.spliterator(iterator(), size(), characteristics);
        } else {
            return Spliterators.spliteratorUnknownSize(iterator(), characteristics);
        }
    }

    /**
     * Returns an {@code Stream} of the search results (either String or DigitalObject).
     */
    default Stream<T> stream() {
        Stream<T> stream = StreamSupport.stream(spliterator(), false);
        return stream.onClose(this::close);
    }

    /**
     * Returns a potentially parallel {@code Stream} of the search results (either String or DigitalObject).
     */
    default Stream<T> parallelStream() {
        Stream<T> stream = StreamSupport.stream(spliterator(), true);
        return stream.onClose(this::close);
    }
}
