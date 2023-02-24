package xly.doip.client;

import xly.doip.client.transport.DoipConnection;
import xly.doip.client.transport.DoipConnectionPool;

/**
 * Used internally by {@link DoipClient} to manage pools of connections.
 */
public class ConnectionAndPool {
    private final DoipConnection connection;
    private final DoipConnectionPool pool;

    public ConnectionAndPool(DoipConnectionPool pool) {
        this.connection = pool.get();
        this.pool = pool;
    }

    public void releaseConnection() throws InterruptedException {
        pool.release(connection);
    }

    public DoipConnection getConnection() { return connection; }
}
