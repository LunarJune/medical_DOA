package xly.doip.client;

import com.google.gson.stream.JsonReader;
import xly.doip.BadDoipException;
import xly.doip.InDoipMessage;
import xly.doip.InDoipSegment;
import xly.doip.client.transport.DoipClientResponse;
import xly.doip.util.InDoipMessageUtil;
import net.handle.hdllib.GsonUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Used internally by {@link DoipClient} to produce search results
 *
 * @param <T> either String for searchIds or DigitalObject for full search
 */
public class DoipSearchResults<T> implements SearchResults<T> {
    private final DoipClientResponse resp;
    private final InDoipMessage in;
    private final JsonReader jsonReader;
    private final Class<T> klass;
    private final int size;

    private boolean closed = false;

    public DoipSearchResults(DoipClientResponse resp, Class<T> klass) throws IOException {
        this.klass = klass;
        this.resp = resp;
        in = resp.getOutput();
        InDoipSegment firstSegment = InDoipMessageUtil.getFirstSegment(in);
        if (firstSegment == null) {
            throw new BadDoipException("Missing input");
        }
        InputStream inputStream = firstSegment.getInputStream();
        @SuppressWarnings("resource")
        InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
        this.jsonReader = new JsonReader(isr);
        jsonReader.beginObject();
        @SuppressWarnings("hiding")
        int size = -1;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if ("size".equals(name)) {
                size = jsonReader.nextInt();
            } else if ("results".equals(name)) {
                jsonReader.beginArray();
                break;
            } else {
                jsonReader.nextString();
            }
        }
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new DoipMessageIterator();
    }

    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;
            if (in != null) try { in.close(); } catch (Exception e) {}
            if (resp != null) try { resp.close(); } catch (Exception e) {}
        }
    }

    private class DoipMessageIterator implements Iterator<T> {
        private Boolean hasNextResult;

        @Override
        public boolean hasNext() {
            if (hasNextResult != null) return hasNextResult.booleanValue();
            if (closed) throw new IllegalStateException("Already closed");
            try {
                boolean res = jsonReader.hasNext();
                hasNextResult = res;
                if (res == false) {
                    close();
                }
                return res;
            } catch (IOException e) {
                throw new RuntimeException(new DoipException(e));
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (hasNextResult == null) hasNext();
            if (!hasNextResult) throw new NoSuchElementException();
            hasNextResult = null;
            try {
                if (klass == DigitalObject.class) {
                    DigitalObject dobj = GsonUtility.getGson().fromJson(jsonReader, DigitalObject.class);
                    return (T) dobj;
                } else if (klass == String.class) {
                    String handle = jsonReader.nextString();
                    return (T) handle;
                } else {
                    throw new AssertionError("Unexpected class " + klass);
                }
            } catch (Exception e) {
                throw new RuntimeException(new DoipException(e));
            }
        }
    }
}
