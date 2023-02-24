package xly.doip.util;

import java.io.IOException;
import java.io.UncheckedIOException;

import xly.doip.InDoipMessage;
import xly.doip.InDoipSegment;

/**
 * Utility methods for {@link InDoipMessage}.
 */
public class InDoipMessageUtil {

    /**
     * Returns the first segment of the supplied {@link InDoipMessage}, or null
     * if it is empty.
     *
     * @param in an {@link InDoipMessage}
     * @return the first segment of the supplied {@link InDoipMessage}, or null if it is empty
     * @throws IOException
     */
    public static InDoipSegment getFirstSegment(InDoipMessage in) throws IOException {
        try {
            for (InDoipSegment segment : in) {
                return segment;
            }
            return null;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Returns true is the supplied {@link InDoipMessage} is empty (has no segments),
     * otherwise false.
     *
     * @param in an {@link InDoipMessage}
     * @return true is the supplied {@link InDoipMessage} is empty (has no segments), otherwise false
     * @throws IOException
     */
    public static boolean isEmpty(InDoipMessage in) throws IOException {
        try {
            for (@SuppressWarnings("unused") InDoipSegment segment : in) {
                return false;
            }
            return true;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
