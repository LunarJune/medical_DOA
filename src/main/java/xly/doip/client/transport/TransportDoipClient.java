package xly.doip.client.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import xly.doip.util.tls.AllTrustingTrustManager;
import xly.doip.util.tls.AutoSelfSignedKeyManager;
import xly.doip.util.tls.TlsProtocolAndCipherSuiteConfigurationUtil;
import xly.doip.util.tls.TrustManagerForSpecifiedServerIdAndKeys;

/**
 * A DOIP client close to the transport protocol.  It can connect to DOIP servers yielding instances of {@link DoipConnection} which are
 * used to communicate with the server.  The user can call {@link #close()} to close all connections.
 */
public class TransportDoipClient implements AutoCloseable {

    private static final int DEFAULT_TIMEOUT_MS = 60_000;

    private final AtomicLong counter = new AtomicLong();
    private final ConcurrentMap<Long, DoipConnection> openConnections = new ConcurrentHashMap<>();
    private volatile boolean closed;

    /**
     * Connects to a server by specifying an IP address and port, using default timeouts of one minute, trusting any provided server certificate,
     * and not supplying a client certificate.
     *
     * @param address
     * @param port
     * @return a DOIP connection for communicating with the specified server
     * @throws IOException
     */
    public DoipConnection connect(InetAddress address, int port) throws IOException {
        return connect(address, port, DEFAULT_TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Connects to a server by specifying an IP address and port, using the specified timeouts, trusting any provided server certificate,
     * and not supplying a client certificate.
     *
     * @param address
     * @param port
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return a DOIP connection for communicating with the specified server
     * @throws IOException
     */
    public DoipConnection connect(InetAddress address, int port, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        return connect(address, port, null, null, connectTimeoutMs, readTimeoutMs);
    }

    /**
     * Connect to a server using the specified connection options.
     *
     * @param options
     * @return a DOIP connection for communicating with the specified server
     * @throws IOException
     */
    public DoipConnection connect(ConnectionOptions options) throws IOException {
        InetAddress address = InetAddress.getByName(options.address);
        int port = options.port;
        int connectTimeoutMs = options.connectTimeoutMs == null ? DEFAULT_TIMEOUT_MS : options.connectTimeoutMs;
        if (connectTimeoutMs < 0) connectTimeoutMs = DEFAULT_TIMEOUT_MS;
        int readTimeoutMs = options.readTimeoutMs == null ? DEFAULT_TIMEOUT_MS : options.connectTimeoutMs;
        if (readTimeoutMs < 0) readTimeoutMs = DEFAULT_TIMEOUT_MS;
        X509TrustManager serverTrustManager = null;
        /*TODO*/
//        if (options.serverId != null) {
//            serverTrustManager = new TrustManagerForSpecifiedServerIdAndKeys(options.serverId, options.trustedServerPublicKeys);
//        }
        X509KeyManager clientKeyManager = null;
//        if (options.clientId != null) {
//            clientKeyManager = new AutoSelfSignedKeyManager(options.clientId, options.clientPublicKey, options.clientPrivateKey);
//        }
        return connect(address, port, serverTrustManager, clientKeyManager, connectTimeoutMs, readTimeoutMs);
    }

    /**
     * Connects to a server by specifying an IP address and port, using the specified timeouts, trusting server certificates according to
     * the specified trust manager, and providing a client certificate using the specified key manager.
     *
     * @param address
     * @param port
     * @param serverTrustManager a trust manager defining which server certificates to trust; if null trust every server
     * @param clientKeyManager a key manager defining which client certificate to provide; if null provide no client certificate
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return a DOIP connection for communicating with the specified server
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public DoipConnection connect(InetAddress address, int port, X509TrustManager serverTrustManager, X509KeyManager clientKeyManager, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        if (closed) throw new IOException("closed");
        /*TODO*/
//        SSLContext sslContext = getSSLContext(serverTrustManager, clientKeyManager);
//        SocketFactory socketFactory = sslContext.getSocketFactory();
//        Socket socket = socketFactory.createSocket();
//        TlsProtocolAndCipherSuiteConfigurationUtil.configureEnabledProtocolsAndCipherSuites(socket);
        Socket socket = new Socket();
        socket.setSoTimeout(readTimeoutMs);
        socket.connect(new InetSocketAddress(address, port), connectTimeoutMs);
        long count = counter.getAndIncrement();
        DoipConnection res = new DoipConnectionImpl(socket) {
            @Override
            public void close() {
                super.close();
                openConnections.remove(count);
            }
        };
        openConnections.put(count, res);
        return res;
    }

    private SSLContext getSSLContext(X509TrustManager serverTrustManager, X509KeyManager clientKeyManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            if (serverTrustManager == null) {
                serverTrustManager = new AllTrustingTrustManager();
            }
            TrustManager[] tms = new TrustManager[] { serverTrustManager };
            KeyManager[] kms = null;
            if (clientKeyManager != null) {
                kms = new KeyManager[] { clientKeyManager };
            }
            sslContext.init(kms, tms, null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Closes all connections.
     */
    @Override
    public void close() {
        closed = true;
        boolean found = true;
        while (found) {
            found = false;
            Iterator<DoipConnection> iter = openConnections.values().iterator();
            while (iter.hasNext()) {
                found = true;
                iter.next().close();
            }
        }
    }
}
