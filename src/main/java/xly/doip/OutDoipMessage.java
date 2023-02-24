package xly.doip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import com.google.gson.JsonElement;

/**
 * A DOIP message to be written as output (for example, a request from the client, or a response from the server).
 * Users must call {@link #close()} when processing is complete.
 */
public interface OutDoipMessage extends AutoCloseable {

    /**
     * Writes a JSON segment into the outgoing message.
     *
     * @param json the json to be written
     * @throws IOException
     */
    void writeJson(JsonElement json) throws IOException;

    /**
     * Writes a JSON segment into the outgoing message.
     *
     * @param json the json to be written
     * @throws IOException
     */
    void writeJson(String json) throws IOException;

    /**
     * Writes a JSON segment into the outgoing message.
     *
     * @param json the json to be written
     * @throws IOException
     */
    void writeJson(byte[] json) throws IOException;

    /**
     * Returns a {@code Writer} that can be used to write to a JSON segment.
     *
     * @return a {@code Writer} that can be used to write to a JSON segment
     */
    Writer getJsonWriter() throws IOException;

    /**
     * Writes a bytes segment into the outgoing message.
     *
     * @param bytes the bytes the be written
     * @throws IOException
     */
    void writeBytes(byte[] bytes) throws IOException;

    /**
     * Writes a bytes segment into the outgoing message.
     *
     * @param in an input stream from which bytes will be read and written to the outgoing bytes segment
     * @throws IOException
     */
    void writeBytes(InputStream in) throws IOException;

    /**
     * Returns an {@code OutputStream} that can be used to write to a bytes segment.
     *
     * @return an {@code OutputStream} that can be used to write to a bytes segment
     * @throws IOException
     */
    OutputStream getBytesOutputStream() throws IOException;

    @Override
    void close() throws IOException;
}