package xly.doip;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The initial segment of a properly formed DOIP request, except the requestId which will be generated by processing code.
 * See also {@link DoipRequestHeadersWithRequestId}.
 * Can be serialized or deserialized using Gson.
 */
public class DoipRequestHeaders {

    /**
     * The id of the calling entity.
     */
    public String clientId;
    /**
     * The id of the object on which the operation is to be performed.
     */
    public String targetId;
    /**
     * The id of the operation to be performed.
     */
    public String operationId;
    /**
     * Attributes supplied to the operation.
     */
    public JsonObject attributes;
    /**
     * Authentication information; the structure depends on the authentication method used.
     */
    public JsonElement authentication;
    /**
     * Input supplied to the operation, when supplied in "compact" form as a single JSON segment.
     * More generally input to the operation is supplied using the remaining segments of the request.
     */
    public JsonElement input;
}
