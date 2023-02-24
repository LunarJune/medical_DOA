package xly.doip.server;

import java.io.IOException;

import com.google.gson.JsonObject;

/**
 * A DoipProcessor encapsulates the request-processing logic of a DOIP server.
 * When a request comes to the listener of a {@link DoipServer}, the {@link #process(DoipServerRequest, DoipServerResponse)}
 * method of the DoipProcessor will be called with an object representing the incoming request
 * and an object representing the outgoing response.  The request object may be inspected and the response object
 * may be populated to provide the response.  When the process method returns the server will close the request
 * and response objects as necessary, while generally leaving the socket open for other requests.
 */
public interface DoipProcessor {
    /**
     * Initializes the DoipProcessor according to the supplied configuration object.
     * This method is called by the {@link DoipServer} when the DoipServer is constructed
     * using {@link DoipServer#DoipServer(DoipServerConfig)}; the configuration must
     * supply a DoipProcessor class name (via {@link DoipServerConfig#processorClass});
     * the configuration object for the DoipProcessor is given by {@link DoipServerConfig#processorConfig}.
     *
     * @param config a configuration JSON object
     */
    default void init(@SuppressWarnings("unused") JsonObject config) { }

    /**
     * Process a request and provide a response.  The supplied request object may be inspected
     * for the details of the request, and the supplied response object may be called to
     * populate the response.  It is not necessary to close these objects in this method.
     *
     * @param req the request object
     * @param resp the response object
     * @throws IOException if something goes wrong reading or writing, which will cause the DoipServer to end the connection
     */
    void process(DoipServerRequest req, DoipServerResponse resp) throws IOException;

    /**
     * Shuts down the DoipProcessor.  This method may be overridden to clean up any resources used by the DoipProcessor instance.
     * It will be called by the {@link DoipServer} if the DoipServer was constructed using {@link DoipServer#DoipServer(DoipServerConfig)}.
     */
    default void shutdown() { }
}
