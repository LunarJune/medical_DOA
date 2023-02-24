package xly.doip.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which will run a supplied runnable when the input stream is closed.
 */
public class DelegatedCloseableInputStream extends FilterInputStream {
    private final Runnable closeFunction;

    /**
     * Constructs an input stream which wraps the supplied input stream and runs the supplied runnable when the input stream is closed.
     *
     * @param in the input stream to wrap
     * @param closeFunction the runnable to run when the input stream is closed
     */
    public DelegatedCloseableInputStream(InputStream in, Runnable closeFunction) {
        super(in);
        this.closeFunction = closeFunction;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.closeFunction.run();
    }
}
