package xly.doip;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;

/**
 * An implementation of {@link OutDoipMessage} which writes a serialized DOIP message into an {@code OutputStream}.
 */
public class OutDoipMessageImpl implements OutDoipMessage {

    private static final byte[] SEGMENT_TERMINATOR = { '\n', '#', '\n' };
    private static final byte[] EMPTY_SEGMENT = { '#', '\n' };

    private final OutputStream out;
    private boolean isClosed;
    private Closeable openCloseable;

    /**
     * Constructs an {@link OutDoipMessage} which will write a serialized DOIP message into the supplied {@code OutputStream}.
     *
     * @param out the stream into which to write the serialized DOIP message
     */
    public OutDoipMessageImpl(OutputStream out) {
        this.out = out;
    }

    @Override
    public void writeJson(JsonElement json) throws IOException {
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        writeJson(json.toString());
    }

    @Override
    public void writeJson(String json) throws IOException {
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        writeJson(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeJson(byte[] json) throws IOException {
        if (json == null) throw new NullPointerException();
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        out.write(json);
        out.write(SEGMENT_TERMINATOR);
        out.flush();
    }

    @Override
    public Writer getJsonWriter() {
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        Writer writer = new JsonSegmentWriter(out);
        openCloseable = writer;
        return new BufferedWriter(writer);
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        if (bytes == null) throw new NullPointerException();
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        out.write('@');
        writeChunkString(bytes.length);
        out.write(bytes);
        out.write(SEGMENT_TERMINATOR);
        out.flush();
    }

    @Override
    public void writeBytes(InputStream in) throws IOException {
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        out.write('@');
        byte[] bytes = new byte[8192];
        int r;
        while ((r = in.read(bytes)) > 0) {
            writeChunkString(r);
            out.write(bytes, 0, r);
        }
        out.write(SEGMENT_TERMINATOR);
        out.flush();
    }

    private void writeChunkString(int size) throws IOException {
        String chunkString = "\n" + size + "\n";
        out.write(chunkString.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public OutputStream getBytesOutputStream() throws IOException {
        if (isClosed) throw new IllegalStateException("closed");
        if (openCloseable != null) throw new IllegalStateException("already opened segment output stream or writer");
        out.write('@');
        OutputStream output = new BytesSegmentOutputStream();
        openCloseable = output;
        return new BufferedOutputStream(output);
    }

    /**
     * Closes the particular segment currently being written, if any
     *
     * @throws IOException
     */
    public void closeSegmentOutput() throws IOException {
        if (openCloseable != null) {
            openCloseable.close();
        }
    }

    @Override
    public void close() throws IOException {
        if (isClosed) return;
        if (openCloseable != null) {
            openCloseable.close();
        }
        isClosed = true;
        out.write(EMPTY_SEGMENT);
        out.flush();
    }

    private class JsonSegmentWriter extends OutputStreamWriter {
        private JsonSegmentWriter(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int c) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.write(c);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.write(cbuf, off, len);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.write(str, off, len);
        }

        @Override
        public void flush() throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.flush();
        }

        @Override
        public void write(char[] cbuf) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.write(cbuf);
        }

        @Override
        public void write(String str) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.write(str);
        }

        @Override
        public Writer append(CharSequence csq) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            return super.append(csq);
        }

        @Override
        public Writer append(CharSequence csq, int start, int end) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            return super.append(csq, start, end);
        }

        @Override
        public Writer append(char c) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            return super.append(c);
        }

        @Override
        public void close() throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            flush();
            out.write(SEGMENT_TERMINATOR);
            out.flush();
            openCloseable = null;
        }
    }

    private class BytesSegmentOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            writeChunkString(1);
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (b == null) throw new NullPointerException();
            if (isClosed) throw new IllegalStateException("closed");
            if (b.length == 0) return;
            writeChunkString(b.length);
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) throw new NullPointerException();
            if (isClosed) throw new IllegalStateException("closed");
            if (len == 0) return;
            writeChunkString(len);
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            super.flush();
        }

        @Override
        public void close() throws IOException {
            if (isClosed) throw new IllegalStateException("closed");
            flush();
            out.write(SEGMENT_TERMINATOR);
            out.flush();
            super.close();
            openCloseable = null;
        }
    }
}
