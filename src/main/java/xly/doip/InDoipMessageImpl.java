package xly.doip;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An implementation of {@link InDoipMessage} which reads a serialized DOIP message from an {@code InputStream}.
 */
public class InDoipMessageImpl implements InDoipMessage {
    private final PushbackInputStream in;
    private final SpliteratorImpl spliterator;
    private InDoipSegment curr;
    private boolean isClosed;
    private BadDoipException terminalException;
    private CompletableFuture<?> completer;

    /**
     * Constructs an {@link InDoipMessage} using the serialized DOIP message from the supplied {@code InputStream}.
     *
     * @param in the input stream from which to read the serialized DOIP message
     */
    public InDoipMessageImpl(InputStream in) {
        if (in instanceof PushbackInputStream) {
            this.in = (PushbackInputStream)in;
        } else {
            this.in = new PushbackInputStream(in);
        }
        this.spliterator = new SpliteratorImpl();
    }

    /**
     * Sets a {@code CompletableFuture} which will be completed when the message is fully processed.
     * It will be completed normally with {@code null} when all segments are read; it will be
     * completed exceptionally if the DOIP message is malformed.
     *
     * @param completer the future to complete when the message is fully processed
     */
    public void setCompleter(CompletableFuture<?> completer) {
        this.completer = completer;
    }

    /**
     * If the DOIP message was malformed, retrieves a {@link BadDoipException} indicating how; otherwise returns null.
     *
     * @return an instance of {@link BadDoipException} which indicates how the DOIP message was malformed, or null
     */
    public BadDoipException getTerminalException() {
        return terminalException;
    }

    @Override
    public Iterator<InDoipSegment> iterator() {
        return Spliterators.iterator(spliterator);
    }

    /**
     * Reads all remaining segments (unless the DOIP message is malformed, in which case it returns immediately).
     */
    @Override
    public void close() {
        if (terminalException != null) return;
        while (!isClosed) spliterator.tryAdvance(x -> {});
    }

    @Override
    public Spliterator<InDoipSegment> spliterator() {
        return spliterator;
    }

    @Override
    public Stream<InDoipSegment> stream() {
        Stream<InDoipSegment> stream = StreamSupport.stream(spliterator, false);
        return stream.onClose(this::close);
    }

    private BadDoipException terminalException(String message) {
        terminalException = new BadDoipException(message);
        if (completer != null) completer.completeExceptionally(terminalException);
        return terminalException;
    }

    private void skipToNewline() throws IOException {
        int ch;
        while (true) {
            ch = in.read();
            if (ch == -1) throw terminalException("end of input before newline");
            if (ch == '\n') return;
            if (ch == ' ' || ch == '\t' || ch == '\r') continue;
            throw terminalException("expected whitespace until newline");
        }
    }

    private class SpliteratorImpl extends AbstractSpliterator<InDoipSegment> {
        public SpliteratorImpl() {
            super(Long.MAX_VALUE, Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
        }

        @Override
        @SuppressWarnings("resource")
        public boolean tryAdvance(Consumer<? super InDoipSegment> action) {
            if (terminalException != null) throw new UncheckedIOException(terminalException);
            if (isClosed) return false;
            try {
                while (curr != null && curr.getInputStream().skip(Long.MAX_VALUE) > 0) { }
                int ch = in.read();
                if (ch == -1) {
                    throw terminalException("end of input before terminal empty segment");
                }
                if (ch == '#') {
                    skipToNewline();
                    isClosed = true;
                    if (completer != null) completer.complete(null);
                    return false;
                }
                if (ch == '@') {
                    skipToNewline();
                    curr = new InDoipSegmentFromInputStream(false, new ChunkedBytesInputStream());
                } else {
                    in.unread(ch);
                    curr = new InDoipSegmentFromInputStream(true, new HashTerminatedInputStream());
                }
                action.accept(curr);
                return true;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private class ChunkedBytesInputStream extends InputStream {
        int currentSize = -1;
        boolean isDone;

        @Override
        public int read() throws IOException {
            if (terminalException != null) throw terminalException;
            if (isDone) return -1;
            if (currentSize > 0) {
                int res = in.read();
                currentSize--;
                if (res < 0) throw terminalException("end of input while reading chunk");
                return res;
            }
            if (currentSize == 0) {
                skipToNewline();
                currentSize = -1;
            }
            int ch = in.read();
            if (ch == '#') {
                skipToNewline();
                curr = null;
                isDone = true;
                return -1;
            }
            if (ch == '0') throw terminalException("zero at start of chunk size");
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (ch == -1) throw terminalException("end of input reading chunk size");
                if (ch == '\n' || ch == ' ' || ch == '\t' || ch == '\r') {
                    if (sb.length() == 0) throw terminalException("missing chunk size");
                    currentSize = Integer.parseInt(sb.toString());
                    if (currentSize <= 0) throw terminalException("overlong chunk size");
                    if (ch != '\n') skipToNewline();
                    return read();
                }
                if (ch < '0' || ch > '9') throw terminalException("unexpected character in chunk size");
                sb.append((char)ch);
                if (sb.length() >= String.valueOf(Integer.MAX_VALUE).length()) throw terminalException("overlong chunk size");
                ch = in.read();
            }
        }

        @Override
        public void close() throws IOException {
            while (currentSize > 0) skip(Long.MAX_VALUE);
            super.close();
        }
    }

    private class HashTerminatedInputStream extends InputStream {
        boolean sawNewline = false;
        boolean isDone;

        @Override
        public int read() throws IOException {
            if (terminalException != null) throw terminalException;
            if (isDone) return -1;
            int ch = in.read();
            if (ch == -1) throw terminalException("end of input reading JSON segment");
            if (ch == '#' && sawNewline) {
                skipToNewline();
                curr = null;
                isDone = true;
                return -1;
            }
            if (ch == '\n') sawNewline = true;
            else sawNewline = false;
            return ch;
        }

        @Override
        public void close() throws IOException {
            while (!isDone) skip(Long.MAX_VALUE);
            super.close();
        }
    }
}
