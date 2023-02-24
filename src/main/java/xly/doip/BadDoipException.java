package xly.doip;

import java.io.IOException;

/**
 * Exception which indicates a malformed DOIP message.
 */
public class BadDoipException extends IOException {

    public BadDoipException(String message) {
        super(message);
    }

    public BadDoipException(Throwable cause) {
        super(cause);
    }

    public BadDoipException(String message, Throwable cause) {
        super(message, cause);
    }

}
