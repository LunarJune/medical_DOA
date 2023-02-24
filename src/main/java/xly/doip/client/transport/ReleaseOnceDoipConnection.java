package xly.doip.client.transport;

import xly.doip.DoipRequestHeaders;
import xly.doip.InDoipMessage;
import xly.doip.client.transport.DoipClientResponse;
import xly.doip.client.transport.DoipConnection;
import xly.doip.client.transport.DoipExchange;

import java.io.IOException;

/**
 * An implementation of {@link DoipConnection} which is used by {@link DoipConnectionPool} and ensures correct
 * behavior for releasing connections back into the pool. Generally, instances should be obtained
 * from a {@link DoipConnectionPool} and then released using {@link DoipConnectionPool#release(DoipConnection)}.
 */
public class ReleaseOnceDoipConnection implements DoipConnection {

    private final DoipConnection conn;
    private volatile boolean isReleased = false;

    public ReleaseOnceDoipConnection(DoipConnection conn) {
        this.conn = conn;
    }

    public synchronized void release() {
        if (isReleased) {
            return;
        } else {
            isReleased = true;
        }
    }

    DoipConnection getConnection() {
        return conn;
    }

    @Override
    public boolean isClosed() {
        return conn.isClosed();
    }

    @Override
    public DoipClientResponse sendCompactRequest(DoipRequestHeaders request) throws IOException {
        if (isReleased) {
            throw new IllegalStateException("Attempt to use released connection");
        }
        return conn.sendCompactRequest(request);
    }

    @Override
    public DoipClientResponse sendRequest(DoipRequestHeaders request, InDoipMessage in) throws IOException {
        if (isReleased) {
            throw new IllegalStateException("Attempt to use released connection");
        }
        return conn.sendRequest(request, in);
    }

    @Override
    public DoipExchange sendRequestToExchange(DoipRequestHeaders request) throws IOException {
        if (isReleased) {
            throw new IllegalStateException("Attempt to use released connection");
        }
        return conn.sendRequestToExchange(request);
    }

    @Override
    public void close() {
        conn.close();
    }
}
