package xly.doip.client;

import com.google.gson.JsonObject;

import java.io.InputStream;

/**
 * A Java representation of an element of a Digital Object.
 */
public class Element {

    /**
     * The id of the element.
     */
    public String id;

    /**
     * The size of the element.  May be null when the size is unknown.
     */
    public Long length;

    /**
     * The type of the element.
     */
    public String type;

    /**
     * The attributes of the element.
     */
    public JsonObject attributes;

    /**
     * The bytes of the element, as an {@code InputStream}.
     */
    public transient InputStream in;
}
