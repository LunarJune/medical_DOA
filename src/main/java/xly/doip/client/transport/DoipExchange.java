package xly.doip.client.transport;

import java.io.IOException;

import xly.doip.OutDoipMessage;

/**
 * An object representing both the outgoing request input, and the response,
 * of a DOIP connection.
 * This allows the writing of segments to the request input to be interspersed with
 * reading segments from the response.
 */
public interface DoipExchange extends AutoCloseable {

    /**
     * Returns the response from the server.  This method will block until the initial segment of the response is available; in general
     * it may be necessary to write some or all request input using {@link #getRequestOutgoingMessage()} before this method
     * will return.
     *
     * @return the response from the server
     * @throws IOException
     */
    DoipClientResponse getResponse() throws IOException;

    /**
     * Returns an instance of {@link OutDoipMessage} to which segments of the request input can be written.
     *
     * @return an instance of {@link OutDoipMessage} to which segments of the request input can be written
     */
    OutDoipMessage getRequestOutgoingMessage();

    @Override
    void close();
}
