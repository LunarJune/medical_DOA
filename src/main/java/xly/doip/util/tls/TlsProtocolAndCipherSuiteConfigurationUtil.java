package xly.doip.util.tls;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for restricting TLS connections to use appropriate protocol versions and cipher suites.
 */
public class TlsProtocolAndCipherSuiteConfigurationUtil {

    /**
     * A list of TLS protocols both provided by the JVM and considered sufficiently strong.
     */
    public static final String[] ENABLED_PROTOCOLS;

    /**
     * A list of TLS cipher suites both provided by the JVM and considered sufficiently strong.
     */
    public static final String[] ENABLED_CIPHER_SUITES;

    // prefer GCM to CBC; SHA2 to SHA1; ECDHE to DHE; AES256 to AES128
    private static final String[] DESIRED_CIPHER_SUITES = {
            "TLS_AES_256_GCM_SHA384", // TLS 1.3
            "TLS_AES_128_GCM_SHA256", // TLS 1.3
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
    };

    static {
        SSLContext context = getAllTrustingClientSSLContext();
        String[] supportedCipherSuitesArray = context.getSupportedSSLParameters().getCipherSuites();
        List<String> supportedCipherSuites = java.util.Arrays.asList(supportedCipherSuitesArray);
        List<String> enabledCipherSuites = new ArrayList<>(java.util.Arrays.asList(DESIRED_CIPHER_SUITES));
        enabledCipherSuites.retainAll(supportedCipherSuites);
        ENABLED_CIPHER_SUITES = enabledCipherSuites.toArray(new String[0]);
    }

    static {
        SSLContext context = getAllTrustingClientSSLContext();
        String[] supportedProtocols = context.getSupportedSSLParameters().getProtocols();
        List<String> enabledProtocols = new ArrayList<>();
        for (String protocol : supportedProtocols) {
            if (protocol.startsWith("SSL")) continue;
            if ("TLSv1.2".compareTo(protocol) > 0) continue;
            enabledProtocols.add(protocol);
        }
        ENABLED_PROTOCOLS = enabledProtocols.toArray(new String[0]);
    }

    /**
     * Configures an {@code SSLSocket} to use the appropriate protocols and cipher suites.
     * If the input is not an {@code SSLSocket} no action is taken.
     *
     * @param s a {@code Socket}
     * @return the input {@code Socket}
     */
    public static Socket configureEnabledProtocolsAndCipherSuites(Socket s) {
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).setEnabledProtocols(TlsProtocolAndCipherSuiteConfigurationUtil.ENABLED_PROTOCOLS);
            ((SSLSocket) s).setEnabledCipherSuites(TlsProtocolAndCipherSuiteConfigurationUtil.ENABLED_CIPHER_SUITES);
        }
        return s;
    }

    /**
     * Configures an {@code SSLServerSocket} to use the appropriate protocols and cipher suites.
     * If the input is not an {@code SSLServerSocket} no action is taken.
     *
     * @param s a {@code ServerSocket}
     * @return the input {@code ServerSocket}
     */
    public static ServerSocket configureEnabledProtocolsAndCipherSuites(ServerSocket s) {
        if (s instanceof SSLServerSocket) {
            ((SSLServerSocket) s).setEnabledProtocols(TlsProtocolAndCipherSuiteConfigurationUtil.ENABLED_PROTOCOLS);
            ((SSLServerSocket) s).setEnabledCipherSuites(TlsProtocolAndCipherSuiteConfigurationUtil.ENABLED_CIPHER_SUITES);
        }
        return s;
    }

    /**
     * Returns an {@code SSLContext} which trusts all server certificates.
     *
     * @return an {@code SSLContext} which trusts all server certificates
     */
    public static SSLContext getAllTrustingClientSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] tm = new TrustManager[] { new AllTrustingTrustManager() };
            sslContext.init(null, tm, null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * A wrapper for an {@code SSLSocketFactory} which ensures that
     * all created sockets will use appropriate protocols and cipher suites.
     */
    public static class SocketFactoryWrapper extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        public SocketFactoryWrapper(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public Socket createSocket() throws IOException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createSocket());
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createSocket(address, port, localAddress, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createSocket(host, port));
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    /**
     * A wrapper for an {@code SSLServerSocketFactory} which ensures that
     * all created server sockets will use appropriate protocols and cipher suites.
     */
    public static class ServerSocketFactoryWrapper extends SSLServerSocketFactory {
        private final SSLServerSocketFactory delegate;

        public ServerSocketFactoryWrapper(SSLServerSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public ServerSocket createServerSocket() throws IOException {
            return configureEnabledProtocolsAndCipherSuites(delegate.createServerSocket());
        }

        @Override
        public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
            return delegate.createServerSocket(port, backlog, ifAddress);
        }

        @Override
        public ServerSocket createServerSocket(int port, int backlog) throws IOException {
            return delegate.createServerSocket(port, backlog);
        }

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            return delegate.createServerSocket(port);
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
