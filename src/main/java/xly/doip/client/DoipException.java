package xly.doip.client;

/**
 * An exception thrown by {@link DoipClient} when something goes wrong such as an unexpected status code returned for an operation.
 */
public class DoipException extends Exception {

    private final String statusCode;

    public DoipException(String message) {
        super(message);
        this.statusCode = null;
    }

    public DoipException(String statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public DoipException(Throwable cause) {
        super(cause);
        this.statusCode = null;
    }

    public DoipException(String statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Gets the status code returned by the operation, if available.
     *
     * @return the status code returned by the operation, if available
     */
    public String getStatusCode() { return statusCode; }
}
