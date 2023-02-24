package xly.doip.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * A Java representation of a Digital Object.
 */
public class DigitalObject {

    /**
     * The identifier of the object.
     */
    public String id;

    /**
     * The type of the object.
     */
    public String type;

    /**
     * The attributes of the object.
     */
    public JsonObject attributes;

    /**
     * The elements of the object.
     */
    public List<Element> elements;

    /**
     * A convenience method that sets an attribute on the object.
     *
     * @param name the name of the attribute
     * @param att the value to set
     */
    public synchronized void setAttribute(String name, JsonElement att) {
        if (attributes == null) {
            attributes = new JsonObject();
        }
        attributes.add(name, att);
    }

    /**
     * A convenience method that sets an attribute on the object.
     *
     * @param name the name of the attribute
     * @param att the value to set as a String
     */
    public synchronized void setAttribute(String name, String att) {
        if (attributes == null) {
            attributes = new JsonObject();
        }
        attributes.addProperty(name, att);
    }
}
