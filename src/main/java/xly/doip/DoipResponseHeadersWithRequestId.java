package xly.doip;

/**
 * The initial segment of a properly formed DOIP response, including the requestId.
 * Generally it should suffice to produce a {@link DoipResponseHeaders} and allow processing code to
 * automatically generate the requestId.
 * Can be serialized or deserialized using Gson.
 */
public class DoipResponseHeadersWithRequestId extends DoipResponseHeaders {

    public String requestId;
}
