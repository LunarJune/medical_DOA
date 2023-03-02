package xly.doip.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import xly.doip.BadDoipException;
import xly.doip.DoipConstants;
import xly.doip.DoipResponseHeadersWithRequestId;
import xly.doip.InDoipMessage;
import xly.doip.InDoipMessageImpl;
import xly.doip.OutDoipMessageImpl;
import xly.doip.server.DoipServerConfig.TlsConfig;
import xly.doip.util.GsonUtility;
import xly.doip.util.tls.AllTrustingTrustManager;
import xly.doip.util.tls.AutoSelfSignedKeyManager;
import xly.doip.util.tls.TlsProtocolAndCipherSuiteConfigurationUtil;
import xly.doip.util.tls.X509IdParser;

/**
 * A DOIP server.  It is constructed via a {@link DoipServerConfig} and a {@link DoipProcessor} which
 * determines request-handling logic.  The DoipProcessor can be automatically instantiated and managed
 * if not provided to the server on construction in which case the DoipServerConfig must specify the
 * class name of the DoipProcessor.
 * <p>
 * The DOIP server will set up a listener according to the DoipServerConfig, and when requests
 * come in, will pass them to the DoipProcessor to populate the response.
 */
public class DoipServer {
    private static final Logger logger = LoggerFactory.getLogger(DoipServer.class);

    private static final AtomicInteger serverCount = new AtomicInteger(1);

    private final DoipServerConfig config;
    private final boolean willShutdownDoipProcessorLifecycle;
    private ServerSocket serverSocket;
    private DoipProcessor doipProcessor;
    private ExecutorService execServ;
    private int port;

    private volatile boolean keepServing;
    private final ConcurrentMap<Long, Socket> activeSockets = new ConcurrentHashMap<>();

    /**
     * Constructs a DoipServer.  The provided configuration must specify a {@link DoipProcessor} class name via
     * {@link DoipServerConfig#processorClass} which will be used to instantiate a DoipProcessor when
     * the server's {@link #init()} method is called.
     * The DoipProcessor will be initialized using {@link DoipServerConfig#processorConfig} and will
     * be shut down along with the server when the server's {@link #shutdown()} method is called.
     *
     * @param config the server configuration object
     */
    public DoipServer(DoipServerConfig config) {
        this.config = config;
        this.willShutdownDoipProcessorLifecycle = true;
        this.port = config.port; // if 0 will change later
    }

    /**
     * Constructs a DoipServer with a previously instantiated {@link DoipProcessor}.
     * The {@link DoipServerConfig} is used only to determine the properties of the listener.
     * The DoipServer does not call the {@link DoipProcessor#init(JsonObject)} or
     * {@link DoipProcessor#shutdown()} methods.
     *
     * @param config        the server configuration object (used for listener properties only)
     * @param doipProcessor a DoipProcessor instance used to handle requests
     */
    public DoipServer(DoipServerConfig config, DoipProcessor doipProcessor) {
        this.config = config;
        this.doipProcessor = doipProcessor;
        this.willShutdownDoipProcessorLifecycle = false;
        this.port = config.port; // if 0 will change later
    }

    /**
     * Initializes the server listener and thread pool and begins serving requests.
     * If the {@link DoipProcessor} was not provided at construction, it will be instantiated
     * and initialized.
     *
     * @throws Exception
     */
    public void init() throws Exception {
        if (doipProcessor == null) {
            doipProcessor = (DoipProcessor) Class.forName(config.processorClass).newInstance();
            doipProcessor.init(config.processorConfig);
        }
        initServerSocket();
        AtomicInteger threadCount = new AtomicInteger(1);
        int thisServerCount = serverCount.getAndIncrement();
        execServ = Executors.newFixedThreadPool(config.numThreads, r -> new Thread(r, "doip-server-" + thisServerCount + "-" + threadCount.getAndIncrement()));
        keepServing = true;
        new Thread(this::serveRequests, "DOIP-Socket-Accept-Thread").start();
    }

    public int getPort() {
        return port;
    }

    private void initServerSocket() throws KeyManagementException, IOException, UnknownHostException {
        /*TODO*/
//        String ephemeralDHKeySize = System.getProperty("jdk.tls.ephemeralDHKeySize");
//        if (ephemeralDHKeySize == null) {
//            System.setProperty("jdk.tls.ephemeralDHKeySize", "2048");
//        }
//        SSLContext sslContext = getServerSSLContext(config.tlsConfig);
//        SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
//        serverSocket = serverSocketFactory.createServerSocket();
//        ((SSLServerSocket) serverSocket).setWantClientAuth(true);
//        TlsProtocolAndCipherSuiteConfigurationUtil.configureEnabledProtocolsAndCipherSuites(serverSocket);
        serverSocket = new ServerSocket();
        if (config.listenAddress == null) serverSocket.bind(new InetSocketAddress(config.port), config.backlog);
        else
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(config.listenAddress), config.port), config.backlog);
        this.port = serverSocket.getLocalPort();
    }

    private static SSLContext getServerSSLContext(TlsConfig tlsConfig) throws KeyManagementException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManager km;
            try {
                if (tlsConfig == null) {
                    km = new AutoSelfSignedKeyManager(null);
                } else if (tlsConfig.certificateChain != null) {
                    km = new AutoSelfSignedKeyManager(null, tlsConfig.certificateChain, tlsConfig.privateKey);
                } else if (tlsConfig.publicKey != null) {
                    km = new AutoSelfSignedKeyManager(tlsConfig.id, tlsConfig.publicKey, tlsConfig.privateKey);
                } else {
                    km = new AutoSelfSignedKeyManager(tlsConfig.id);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            KeyManager[] kms = new KeyManager[]{km};
            // trust all client certificates; pass the information about the client on to the processor to decide
            TrustManager[] tms = new TrustManager[]{new AllTrustingTrustManager()};
            sslContext.init(kms, tms, null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private void serveRequests() {
        while (keepServing) {
            try {
                @SuppressWarnings("resource")
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(config.maxIdleTimeMillis);
                execServ.execute(() -> handle(socket));
            } catch (Exception e) {
                if (keepServing) {
                    logger.error("Exception accepting request", e);
                }
            }
        }
    }

    private void handle(Socket socket) {
        activeSockets.put(Thread.currentThread().getId(), socket);
        try {
            if (keepServing) {
                handleMessagesThrowing(socket);
            }
        } catch (Exception e) {
            // ignore
        } finally {
            activeSockets.remove(Thread.currentThread().getId());
        }
        try {
            socket.close();
        } catch (Exception e) {
            // ignore
        }
    }

    @SuppressWarnings("resource")
    private void handleMessagesThrowing(Socket socket) throws IOException {
        PushbackInputStream in = new PushbackInputStream(new BufferedInputStream(socket.getInputStream()));
        /*TODO*/
//        int n;
//        // 读取文件，并将内容转换为字符输出
//        while ((n = in.read()) != -1){
//            System.out.print((char)n);
//        }
        int ch;
        while ((ch = in.read()) > -1) {
            in.unread(ch);
            InDoipMessage inDoipMessage = new InDoipMessageImpl(in);
            OutDoipMessageImpl outDoipMessage = new OutDoipMessageImpl(new BufferedOutputStream(socket.getOutputStream()));
            String requestId = null;
            try {
                // get cert for each message in order to support TLS renegotiation to change client id?
//                X509Certificate[] clientCertChain = getClientCertChain(socket);
//                String clientCertId = X509IdParser.parseIdentityHandle(clientCertChain);
//                PublicKey clientCertPublicKey = null;
//                if (clientCertChain != null && clientCertChain.length > 0) {
//                    clientCertPublicKey = clientCertChain[0].getPublicKey();
//                }
//                DoipServerRequestImpl req = new DoipServerRequestImpl(inDoipMessage, clientCertId, clientCertPublicKey, clientCertChain);
                /*TODO*/
                DoipServerRequestImpl req = new DoipServerRequestImpl(inDoipMessage, "", null, null);
                requestId = req.getRequestId();
                DoipServerResponseImpl resp = new DoipServerResponseImpl(requestId, outDoipMessage);
                try {
                    doipProcessor.process(req, resp);
                    resp.commit();
                    outDoipMessage.close();
                    inDoipMessage.close();
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            } catch (BadDoipException e) {
                outDoipMessage.closeSegmentOutput();
                writeBadDoipException(requestId, socket.getOutputStream(), e.getMessage());
                throw e;
            } catch (SocketTimeoutException e) {
                outDoipMessage.closeSegmentOutput();
                writeBadDoipException(requestId, socket.getOutputStream(), e.getMessage());
                throw e;
            } catch (Exception e) {
                if (keepServing) {
                    logger.warn("Exception handling message", e);
                }
                outDoipMessage.closeSegmentOutput();
                writeServerException(requestId, socket.getOutputStream(), "An unexpected server error occurred");
                throw e;
            }
        }
    }

    private X509Certificate[] getClientCertChain(Socket socket) {
        if (!(socket instanceof SSLSocket)) return null;
        try {
            Certificate[] certs = ((SSLSocket) socket).getSession().getPeerCertificates();
            if (certs == null || certs.length == 0) return null;
            X509Certificate[] res = new X509Certificate[certs.length];
            for (int i = 0; i < certs.length; i++) {
                if (!(certs[i] instanceof X509Certificate)) return null;
                res[i] = (X509Certificate) certs[i];
            }
            return res;
        } catch (SSLPeerUnverifiedException e) {
            return null;
        }
    }

    private void writeBadDoipException(String requestId, OutputStream out, String message) throws IOException {
        DoipResponseHeadersWithRequestId segment = new DoipResponseHeadersWithRequestId();
        segment.requestId = requestId;
        segment.status = DoipConstants.STATUS_BAD_REQUEST;
        segment.attributes = new JsonObject();
        segment.attributes.addProperty("message", message);
        String resp = GsonUtility.getGson().toJson(segment);
        resp += "\n#\n#\n";
        out.write(resp.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void writeServerException(String requestId, OutputStream out, String message) throws IOException {
        DoipResponseHeadersWithRequestId segment = new DoipResponseHeadersWithRequestId();
        segment.requestId = requestId;
        segment.status = DoipConstants.STATUS_ERROR;
        segment.attributes = new JsonObject();
        segment.attributes.addProperty("message", message);
        String resp = GsonUtility.getGson().toJson(segment);
        resp += "\n#\n#\n";
        out.write(resp.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Shuts down the server listener and thread pool.
     * If the {@link DoipProcessor} was not provided at construction but was instead instantiated
     * by {@link #init()}, it will be shut down here.
     *
     * @throws Exception
     */
    public void shutdown() {
        keepServing = false;
        try {
            execServ.shutdown();
        } catch (Exception e) {
            logger.error("Shutdown error", e);
        }
        try {
            serverSocket.close();
        } catch (Exception e) {
            logger.error("Shutdown error", e);
        }
        for (Socket socket : activeSockets.values()) {
            try {
                socket.close();
            } catch (Exception e) {
                logger.error("Shutdown error", e);
            }
        }
        try {
            execServ.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            logger.error("Shutdown error", e);
        }
        if (willShutdownDoipProcessorLifecycle) {
            try {
                doipProcessor.shutdown();
            } catch (Exception e) {
                logger.error("Shutdown error", e);
            }
        }
    }
}
