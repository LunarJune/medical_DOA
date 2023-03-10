package xly.doip;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The initial segment of a properly formed DOIP request, except the requestId which will be generated by processing code.
 * See also {@link DoipResponseHeadersWithRequestId}.
 * Can be serialized or deserialized using Gson.
 */
public class DoipResponseHeaders {

    /**
     * The status code for the response; see {@link DoipConstants} for commonly used status codes.
     */
    public String status;
    /**
     * Attributes to be included with the response.
     */
    public JsonObject attributes;
    /**
     * Output of the operation, when supplied in "compact" form as a single JSON segment.
     * More generally output of the operation is supplied using the remaining segments of the response.
     */
    public JsonElement output;
}
