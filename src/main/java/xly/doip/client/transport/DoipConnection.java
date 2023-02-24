package xly.doip.client.transport;

import xly.doip.DoipRequestHeaders;
import xly.doip.InDoipMessage;

import java.io.IOException;

/**
 * A connection with a DOIP server.
 *
 * The user can send requests using the {@link #sendCompactRequest(DoipRequestHeaders)}, {@link #sendRequest(DoipRequestHeaders, InDoipMessage)},
 * and {@link #sendRequestToExchange(DoipRequestHeaders)} methods.  The user should call {@link #close()} when done (except
 * when obtained from a pool, in which case {@link DoipConnectionPool#release(DoipConnection)} should be called instead).
 */
public interface DoipConnection extends AutoCloseable {
    /**
     * Returns true if the connection is closed.
     *
     * @return true if the connection is closed
     */
    boolean isClosed();

    /**
     * Sends a "compact" request consisting of a single JSON segment, with the request "input" embedded in the initial segment.
     *
     * @param request the single-segment "compact" request to be sent
     * @return the response
     * @throws IOException
     */
    DoipClientResponse sendCompactRequest(DoipRequestHeaders request) throws IOException;

    /**
     * Sends a request with the specified initial segment, and subsequent segments (the request "input") read
     * from the supplied InDoipMessage.
     *
     * @param request the initial segment of the request (except the requestId which will be generated automatically)
     * @param in the remaining segments of the request
     * @return the response
     * @throws IOException
     */
    DoipClientResponse sendRequest(DoipRequestHeaders request, InDoipMessage in) throws IOException;

    /**
     * Sends a request using a specified initial segment, and providing a {@link DoipExchange} which allows
     * writing additional segments as well as reading segments from the response.
     *
     * @param request the initial segment of the request (except the requestId which will be generated automatically)
     * @return an instance of {@link DoipExchange} which allows writing additional segments as well as reading segments from the response
     * @throws IOException
     */
    DoipExchange sendRequestToExchange(DoipRequestHeaders request) throws IOException;

    @Override
    void close();
}
