package xly.doip.client.transport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xly.doip.BadDoipException;
import xly.doip.DoipRequestHeaders;
import xly.doip.DoipRequestHeadersWithRequestId;
import xly.doip.DoipResponseHeadersWithRequestId;
import xly.doip.InDoipMessage;
import xly.doip.InDoipMessageImpl;
import xly.doip.InDoipSegment;
import xly.doip.OutDoipMessage;
import xly.doip.OutDoipMessageImpl;
import xly.doip.util.GsonUtility;

/**
 * An implementation of {@link DoipConnection}. Generally, instances of DoipConnection should be obtained
 * using the methods of {@link TransportDoipClient} or {@link DoipConnectionPool}.
 *
 * The user can send requests using the {@link #sendCompactRequest(DoipRequestHeaders)}, {@link #sendRequest(DoipRequestHeaders, InDoipMessage)},
 * and {@link #sendRequestToExchange(DoipRequestHeaders)} methods.  The user should call {@link #close()} when done (except
 * when obtained from a pool, in which case {@link DoipConnectionPool#release(DoipConnection)} should be called instead).
 */
public class DoipConnectionImpl implements DoipConnection {
    private static final Logger logger = LoggerFactory.getLogger(DoipConnectionImpl.class);

    private static final AtomicInteger connectionCount = new AtomicInteger(1);

    private final Socket socket;
    private final Semaphore outputLock = new Semaphore(1, true);
    private final ConcurrentMap<String, CompletableFuture<DoipClientResponse>> outstandingRequests = new ConcurrentHashMap<>();
    private volatile CountDownLatch requestWaitLatch = new CountDownLatch(1);
    private final ExecutorService execServ;

    private DoipResponseHeadersWithRequestId initialSegment;
    private CompletableFuture<?> responseReadingCompleter;
    private volatile boolean isClosed;

    /**
     * Establishes a DoipConnection using the specified Socket.  Generally, instances of DoipConnection should be obtained
     * using the methods of {@link TransportDoipClient}.
     *
     * @param socket
     */
    public DoipConnectionImpl(Socket socket) {
        this.socket = socket;
        this.execServ = Executors.newSingleThreadExecutor(r -> new Thread(r, "doip-connection-monitor-" + connectionCount.getAndIncrement()));
        this.execServ.submit(this::monitor);
    }

    @SuppressWarnings("resource")
    private void monitor() {
        try {
            PushbackInputStream in = new PushbackInputStream(new BufferedInputStream(socket.getInputStream()));
            int ch;
            while (waitForRequest() && (ch = in.read()) > -1) {
                if (isClosed) return;
                in.unread(ch);
                InDoipMessageImpl inDoipMessage = new InDoipMessageImpl(in);
                boolean found = inDoipMessage.spliterator().tryAdvance(this::handleInitialSegment);
                if (found) {
                    CompletableFuture<DoipClientResponse> responseFuture = outstandingRequests.remove(initialSegment.requestId);
                    if (outstandingRequests.isEmpty()) {
                        requestWaitLatch = new CountDownLatch(1);
                    }
                    if (responseFuture == null) {
                        throw new BadDoipException("No request " + initialSegment.requestId);
                    }
                    responseReadingCompleter = new CompletableFuture<>();
                    inDoipMessage.setCompleter(responseReadingCompleter);
                    responseFuture.complete(new DoipClientResponse(initialSegment, inDoipMessage));
                    if (isClosed) return;
                    responseReadingCompleter.join();
                    responseReadingCompleter = null;
                } else {
                    throw new BadDoipException("empty response received");
                }
            }
        } catch (Exception e) {
            if (isClosed) return;
            if (e instanceof CompletionException || e instanceof UncheckedIOException) {
                logger.error("Error in DOIP response stream", e.getCause());
            } else if (!(e instanceof CancellationException)) {
                logger.error("Error in DOIP response stream", e);
            }
            try {
                closeWithoutWaiting();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    private boolean waitForRequest() {
        if (outstandingRequests.isEmpty()) {
            try {
                requestWaitLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (isClosed) return false;
        return true;
    }

    private void handleInitialSegment(InDoipSegment segment) {
        try {
            if (!segment.isJson()) {
                throw new BadDoipException("expected JSON segment");
            }
            initialSegment = GsonUtility.getGson().fromJson(segment.getJson(), DoipResponseHeadersWithRequestId.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public DoipClientResponse sendCompactRequest(DoipRequestHeaders request) throws IOException {
        if (isClosed) throw new IOException("closed");
        DoipRequestHeadersWithRequestId requestWithRequestId = new DoipRequestHeadersWithRequestId(request);
        String requestId = UUID.randomUUID().toString();
        requestWithRequestId.requestId = requestId;
        CompletableFuture<DoipClientResponse> completer = new CompletableFuture<>();
        outstandingRequests.put(requestId, completer);
        requestWaitLatch.countDown();
        try {
            outputLock.acquire();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (isClosed) throw new IOException("closed");
        try (OutDoipMessageImpl outDoipMessage = new OutDoipMessageImpl(new BufferedOutputStream(socket.getOutputStream()))) {
            outDoipMessage.writeJson(GsonUtility.getGson().toJson(requestWithRequestId));
        } finally {
            outputLock.release();
        }
        try {
            return completer.join();
        } catch (Exception e) {
            unwrapAndThrow(e);
            throw e;
        }
    }

    @Override
    public DoipClientResponse sendRequest(DoipRequestHeaders request, InDoipMessage in) throws IOException {
        if (isClosed) throw new IOException("closed");
        DoipRequestHeadersWithRequestId requestWithRequestId = new DoipRequestHeadersWithRequestId(request);
        String requestId = UUID.randomUUID().toString();
        requestWithRequestId.requestId = requestId;
        CompletableFuture<DoipClientResponse> completer = new CompletableFuture<>();
        outstandingRequests.put(requestId, completer);
        requestWaitLatch.countDown();
        try {
            outputLock.acquire();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (isClosed) throw new IOException("closed");
        try (OutDoipMessageImpl outDoipMessage = new OutDoipMessageImpl(new BufferedOutputStream(socket.getOutputStream()))) {
            outDoipMessage.writeJson(GsonUtility.getGson().toJson(requestWithRequestId));
            for (InDoipSegment segment : in) {
                if (isClosed) throw new IOException("closed");
                if (segment.isJson()) {
                    outDoipMessage.writeJson(segment.getJson());
                } else {
                    outDoipMessage.writeBytes(segment.getInputStream());
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        } finally {
            outputLock.release();
        }
        try {
            return completer.join();
        } catch (Exception e) {
            unwrapAndThrow(e);
            throw e;
        }
    }

    @Override
    public DoipExchange sendRequestToExchange(DoipRequestHeaders request) throws IOException {
        if (isClosed) throw new IOException("closed");
        DoipRequestHeadersWithRequestId requestWithRequestId = new DoipRequestHeadersWithRequestId(request);
        String requestId = UUID.randomUUID().toString();
        requestWithRequestId.requestId = requestId;
        CompletableFuture<DoipClientResponse> completer = new CompletableFuture<>();
        outstandingRequests.put(requestId, completer);
        requestWaitLatch.countDown();
        try {
            outputLock.acquire();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (isClosed) throw new IOException("closed");
        OutDoipMessageImpl outDoipMessage = new OutDoipMessageImpl(new BufferedOutputStream(socket.getOutputStream())) {
            @Override
            public void close() throws IOException {
                super.close();
                outputLock.release();
            }
        };
        outDoipMessage.writeJson(GsonUtility.getGson().toJson(requestWithRequestId));
        return new DoipExchange() {

            @Override
            public DoipClientResponse getResponse() throws IOException {
                if (isClosed) throw new IOException("closed");
                try {
                    return completer.join();
                } catch (Exception e) {
                    unwrapAndThrow(e);
                    throw e;
                }
            }

            @Override
            public OutDoipMessage getRequestOutgoingMessage() {
                return outDoipMessage;
            }

            @Override
            public void close() {
                try {
                    outDoipMessage.close();
                } catch (Exception e) {
                    logger.warn("Error closing", e);
                }
                try {
                    getResponse().close();
                } catch (Exception e) {
                    logger.warn("Error closing", e);
                }
            }
        };
    }

    @Override
    public void close() {
        closeWithoutWaiting();
        try {
            execServ.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("Error closing", e);
        }
    }

    private void closeWithoutWaiting() {
        isClosed = true;
        for (CompletableFuture<?> future : outstandingRequests.values()) {
            future.cancel(false);
        }
        try {
            socket.close();
        } catch (Exception e) {
            logger.warn("Error closing", e);
        }
        CompletableFuture<?> completer = responseReadingCompleter;
        if (completer != null) {
            completer.cancel(false);
        }
        requestWaitLatch.countDown();
        try {
            execServ.shutdown();
        } catch (Exception e) {
            logger.warn("Error closing", e);
        }
        for (CompletableFuture<?> future : outstandingRequests.values()) {
            future.cancel(false);
        }
    }

    private void unwrapAndThrow(Exception e) throws IOException {
        if (e instanceof CompletionException) {
            if (e.getCause() instanceof Exception) {
                unwrapAndThrow((Exception) e.getCause());
            } else if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else {
                throw (CompletionException) e;
            }
        }
        if (e instanceof UncheckedIOException) {
            unwrapAndThrow(((UncheckedIOException)e).getCause());
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        throw new AssertionError(e);
    }
}
