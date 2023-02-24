package xly.doip.client.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import xly.doip.client.DoipClient;

/**
 * Used internally by {@link DoipClient} to manage pools of connections.
 */
public class DoipConnectionPool {

    private final List<DoipConnection> allActiveConnections;
    private final BlockingQueue<DoipConnection> pool;
    private final TransportDoipClient client;
    private final int maxSize;
    private final ConnectionOptions connectionOptions;
    private volatile boolean isShutdown = false;

    public DoipConnectionPool(int maxSize, TransportDoipClient client, ConnectionOptions connectionOptions) {
        this.maxSize = maxSize;
        this.pool = new LinkedBlockingQueue<>();
        this.client = client;
        this.connectionOptions = connectionOptions;
        this.allActiveConnections = new ArrayList<>();
    }

    private DoipConnection getNewDoipConnection() throws IOException {
        return client.connect(connectionOptions);
    }

    public void release(DoipConnection connection) throws InterruptedException {
        if (connection instanceof ReleaseOnceDoipConnection) {
            ReleaseOnceDoipConnection releaseOnceDoipConnection = (ReleaseOnceDoipConnection)connection;
            if (isShutdown) {
                connection.close();
            } else {
                releaseOnceDoipConnection.release();
                pool.put(releaseOnceDoipConnection.getConnection());
            }
        } else {
            throw new IllegalStateException("Attempt to release underlying connection");
        }
    }

    @SuppressWarnings("resource")
    public DoipConnection get() {
        if (isShutdown) {
            return null;
        }
        try {
            DoipConnection connection = pool.poll();
            synchronized(this) {
                if (connection == null && allActiveConnections.size() < maxSize) {
                    connection = getNewDoipConnection();
                    allActiveConnections.add(connection);
                    return new ReleaseOnceDoipConnection(connection);
                }
            }
            if (connection == null) {
                connection = pool.take();
            }
            if (isGood(connection)) {
                return new ReleaseOnceDoipConnection(connection);
            } else {
                DoipConnection newConnection;
                synchronized(this) {
                    allActiveConnections.remove(connection);
                    newConnection = getNewDoipConnection();
                    allActiveConnections.add(newConnection);
                }
                try { connection.close(); } catch (Exception e) {}
                return new ReleaseOnceDoipConnection(newConnection);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isGood(DoipConnection connection) {
        return !connection.isClosed();
    }

    public void shutdown() {
        if (!isShutdown) {
            for (DoipConnection connection : pool) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
            isShutdown = true;
        }
    }
}
