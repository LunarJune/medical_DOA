package xly.doip.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import xly.doip.*;
import xly.doip.client.transport.*;
import xly.doip.util.GsonUtility;
import xly.doip.util.InDoipMessageUtil;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DOIP client for performing operations on objects.  The client can be used to perform arbitrary operations on object, and also provides
 * specific methods for the basic DOIP operations.
 * <p>
 * In general handle resolution will be used to find the service information for accessing the object: the
 * target id is resolved, handle values of type DOIPService are references to service ids which are resolved,
 * handle values of type DOIPServiceInfo have service connection information.
 * <p>
 * It is also possible to explicitly supply the service through which the operation is to be performed.
 * <p>
 * The user should call {@link #close()} to release all resources.
 */
public class DoipClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DoipClient.class);

    private static final String DOIP_SERVICE_INFO = "DOIPServiceInfo";
    private static final String TYPE_DOIP_SERVICE_INFO = "0.TYPE/DOIPServiceInfo";
    private static final String DOIP_SERVICE = "DOIPService";
    private static final String TYPE_DOIP_SERVICE = "0.TYPE/DOIPService";

    // connections per service handle
    private static final int MAX_POOL_SIZE = 100;
    private static final int MAX_HOP_COUNT = 20;

    private final Cache<String, ServiceInfoAndPool> serviceHandleToPoolsMap;
    private final Cache<String, String> targetIdToServiceHandleMap;

    private final TransportDoipClient doipClient;
    private final HandleResolver resolver;

    private boolean closed; // guarded by synchronized methods

    /**
     * Constructs a new DoipClient.
     */
    public DoipClient() {
        doipClient = new TransportDoipClient();
        resolver = new HandleResolver();
        serviceHandleToPoolsMap = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .removalListener(new PoolRemovalListener())
                .build();
        targetIdToServiceHandleMap = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    /**
     * Closes all open connections and release all resources.
     */
    @Override
    public synchronized void close() {
        closed = true;
        for (ServiceInfoAndPool serviceInfoAndPool : serviceHandleToPoolsMap.asMap().values()) {
            try {
                serviceInfoAndPool.pool.shutdown();
            } catch (Exception e) {
                logger.warn("Error closing", e);
            }
        }
        try {
            doipClient.close();
        } catch (Exception e) {
            logger.warn("Error closing", e);
        }
    }

    /**
     * Performs an operation, looking up the target's service information by handle resolution.
     * No input (beyond attributes) is provided to the operation.
     *
     * @param targetId    the object on which to perform the operation
     * @param operationId the operation to perform
     * @param authInfo    the authentication to provide
     * @param attributes  the attributes to provide to the operation
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes) throws DoipException {
        DoipRequestHeaders headers = headersFrom(targetId, operationId, authInfo, attributes);
        return performOperation(headers, null);
    }

    /**
     * Performs an operation, looking up the target's service information by handle resolution.
     *
     * @param targetId    the object on which to perform the operation
     * @param operationId the operation to perform
     * @param authInfo    the authentication to provide
     * @param attributes  the attributes to provide to the operation
     * @param input       the input to the operation as a JsonElement
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes, JsonElement input) throws DoipException {
        DoipRequestHeaders headers = headersFrom(targetId, operationId, authInfo, attributes, input);
        return performOperation(headers, null);
    }

    /**
     * Performs an operation, looking up the target's service information by handle resolution.
     *
     * @param targetId    the object on which to perform the operation
     * @param operationId the operation to perform
     * @param authInfo    the authentication to provide
     * @param attributes  the attributes to provide to the operation
     * @param input       the input to the operation as an InDoipMessage
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes, InDoipMessage input) throws DoipException {
        DoipRequestHeaders headers = headersFrom(targetId, operationId, authInfo, attributes);
        return performOperation(headers, input);
    }

    /**
     * Performs an operation at a specified service.
     * No input (beyond attributes) is provided to the operation.
     *
     * @param targetId    the object on which to perform the operation
     * @param operationId the operation to perform
     * @param authInfo    the authentication to provide
     * @param attributes  the attributes to provide to the operation
     * @param serviceInfo the service at which to perform the operation
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes, ServiceInfo serviceInfo) throws DoipException {
        DoipRequestHeaders headers = headersFrom(targetId, operationId, authInfo, attributes);
        return performOperation(headers, null, serviceInfo);
    }

    /**
     * Performs an operation at a specified service.
     *
     * @param targetId    the object on which to perform the operation
     * @param operationId the operation to perform
     * @param authInfo    the authentication to provide
     * @param attributes  the attributes to provide to the operation
     * @param input       the input to the operation as a JsonElement
     * @param serviceInfo the service at which to perform the operation
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes, JsonElement input, ServiceInfo serviceInfo) throws DoipException {
        DoipRequestHeaders headers = headersFrom(targetId, operationId, authInfo, attributes, input);
        return performOperation(headers, null, serviceInfo);
    }

    /**
     * Performs an operation at a specified service.
     *
     * @param targetId    the object on which to perform the operation
     * @param operationId the operation to perform
     * @param authInfo    the authentication to provide
     * @param attributes  the attributes to provide to the operation
     * @param input       the input to the operation as an InDoipMessage
     * @param serviceInfo the service at which to perform the operation
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes, InDoipMessage input, ServiceInfo serviceInfo) throws DoipException {
        DoipRequestHeaders headers = headersFrom(targetId, operationId, authInfo, attributes);
        return performOperation(headers, input, serviceInfo);
    }

    /**
     * Performs an operation, looking up the target's service information by handle resolution.
     *
     * @param headers the content of the initial segment of the request
     * @param input   the input to the operation as an InDoipMessage
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(DoipRequestHeaders headers, InDoipMessage input) throws DoipException {
        return performOperation(headers, input, null);
    }

    /**
     * Performs an operation at a specified service.
     *
     * @param headers     the content of the initial segment of the request
     * @param input       the input to the operation as an InDoipMessage
     * @param serviceInfo the service at which to perform the operation
     * @return the response
     * @throws DoipException
     */
    public DoipClientResponse performOperation(DoipRequestHeaders headers, InDoipMessage input, ServiceInfo serviceInfo) throws DoipException {
        ConnectionAndPool connectionAndPool = connectionAndPoolForOptions(serviceInfo, headers.targetId);
        return performOperationWithConnection(headers, input, connectionAndPool);
    }

    private ConnectionAndPool connectionAndPoolForOptions(ServiceInfo serviceInfo, String targetId) throws DoipException {
        ConnectionAndPool connectionAndPool;
        if (serviceInfo == null) {
            connectionAndPool = getConnectionFor(targetId);
        } else if (serviceInfo.ipAddress != null) {
            ServiceInfoAndPool serviceInfoAndPool = getOrCreatePool(serviceInfo);
            connectionAndPool = new ConnectionAndPool(serviceInfoAndPool.pool);
        } else if (serviceInfo.serviceId != null) {
            connectionAndPool = getConnectionFor(serviceInfo.serviceId);
        } else {
            throw new DoipException("Missing options");
        }
        return connectionAndPool;
    }

    @SuppressWarnings("resource")
    private DoipClientResponse performOperationWithConnection(DoipRequestHeaders headers, InDoipMessage input, ConnectionAndPool connectionAndPool) throws DoipException {
        DoipConnection conn = connectionAndPool.getConnection();
        DoipClientResponse response;
        try {
            if (input != null) {
                response = conn.sendRequest(headers, input);
            } else {
                response = conn.sendCompactRequest(headers);
            }
        } catch (IOException ioe) {
            try {
                connectionAndPool.releaseConnection();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new DoipException(ioe);
        }
        response.setOnClose(() -> {
            try {
                connectionAndPool.releaseConnection();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return response;
    }

    private static DoipRequestHeaders headersFrom(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes, JsonElement input) throws DoipException {
        DoipRequestHeaders headers = new DoipRequestHeaders();
        headers.targetId = targetId;
        headers.operationId = operationId;
        if (authInfo != null) {
            headers.clientId = authInfo.getClientId();
            JsonElement authentication = authInfo.getAuthentication();
            if (authentication != null) {
                headers.authentication = authentication;
            }
        }
        headers.attributes = attributes;
        if (input != null) {
            headers.input = input;
        }
        return headers;
    }

    private static DoipRequestHeaders headersFrom(String targetId, String operationId, AuthenticationInfo authInfo, JsonObject attributes) throws DoipException {
        return headersFrom(targetId, operationId, authInfo, attributes, null);
    }

    /**
     * Creates a digital object at a service.
     *
     * @param dobj        the digital object to create
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the created digital object
     * @throws DoipException
     */
    public DigitalObject create(DigitalObject dobj, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        String targetId;
        if (serviceInfo.serviceId != null) {
            targetId = serviceInfo.serviceId;
        } else {
            throw new DoipException("Missing service id for create");
        }
        try (
                InDoipMessage inMessage = buildCreateOrUpdateMessageFrom(dobj, false);
                DoipClientResponse resp = performOperation(targetId, DoipConstants.OP_CREATE, authInfo, null, inMessage, serviceInfo);
        ) {
            if (resp.getStatus().equals(DoipConstants.STATUS_OK)) {
                try (InDoipMessage in = resp.getOutput()) {
                    DigitalObject resultDo = digitalObjectFromSegments(in);
                    return resultDo;
                }
            } else {
                throw doipExceptionFromDoipResponse(resp);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    /**
     * Updates a digital object.
     *
     * @param dobj     the digital object to update
     * @param authInfo the authentication to provide
     * @return the updated digital object
     * @throws DoipException
     */
    public DigitalObject update(DigitalObject dobj, AuthenticationInfo authInfo) throws DoipException {
        return update(dobj, authInfo, null);
    }

    /**
     * Updates a digital object at a specified service.
     *
     * @param dobj        the digital object to update
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the updated digital object
     * @throws DoipException
     */
    public DigitalObject update(DigitalObject dobj, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        try (
                InDoipMessage inMessage = buildCreateOrUpdateMessageFrom(dobj, true);
                DoipClientResponse resp = performOperation(dobj.id, DoipConstants.OP_UPDATE, authInfo, null, inMessage, serviceInfo);
        ) {
            if (resp.getStatus().equals(DoipConstants.STATUS_OK)) {
                try (InDoipMessage in = resp.getOutput()) {
                    DigitalObject resultDo = digitalObjectFromSegments(in);
                    return resultDo;
                }
            } else {
                throw doipExceptionFromDoipResponse(resp);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    private InDoipMessage buildCreateOrUpdateMessageFrom(DigitalObject dobj, boolean isUpdate) {
        JsonObject dobjJson = GsonUtility.getGson().toJsonTree(dobj).getAsJsonObject();
        List<InDoipSegment> segments = new ArrayList<>();
        InDoipSegment dobjSegment = new InDoipSegmentFromJson(dobjJson);
        segments.add(dobjSegment);
        if (dobj.elements != null) {
            for (Element el : dobj.elements) {
                if (isUpdate && el.in == null) continue;
                JsonObject elementSegmentJson = new JsonObject();
                elementSegmentJson.addProperty("id", el.id);
                InDoipSegment elementHeaderSegment = new InDoipSegmentFromJson(elementSegmentJson);
                segments.add(elementHeaderSegment);
                InDoipSegment elementBytesSegment = new InDoipSegmentFromInputStream(false, el.in);
                segments.add(elementBytesSegment);
            }
        }
        return new InDoipMessageFromCollection(segments);
    }

    /**
     * Retrieves a digital object.
     *
     * @param targetId the id of the object to retrieve
     * @param authInfo the authentication to provide
     * @return the digital object
     * @throws DoipException
     */
    public DigitalObject retrieve(String targetId, AuthenticationInfo authInfo) throws DoipException {
        return retrieve(targetId, false, authInfo, null);
    }

    /**
     * Retrieves a digital object from a specified service.
     *
     * @param targetId    the id of the object to retrieve
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the digital object
     * @throws DoipException
     */
    public DigitalObject retrieve(String targetId, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        return retrieve(targetId, false, authInfo, serviceInfo);
    }

    /**
     * Retrieves a digital object, possibly including all element data.
     *
     * @param targetId           the id of the object to retrieve
     * @param includeElementData if true, include data for all elements
     * @param authInfo           the authentication to provide
     * @return the digital object
     * @throws DoipException
     */
    public DigitalObject retrieve(String targetId, boolean includeElementData, AuthenticationInfo authInfo) throws DoipException {
        return retrieve(targetId, includeElementData, authInfo, null);
    }

    /**
     * Retrieves a digital object from a specified service, possibly including all element data.
     *
     * @param targetId           the id of the object to retrieve
     * @param includeElementData if true, include data for all elements
     * @param authInfo           the authentication to provide
     * @param serviceInfo        the service at which to perform the operation
     * @return the digital object
     * @throws DoipException
     */
    public DigitalObject retrieve(String targetId, boolean includeElementData, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        JsonObject attributes = new JsonObject();
        if (includeElementData) {
            attributes.addProperty("includeElementData", "true");
        }
        try (DoipClientResponse response = performOperation(targetId, DoipConstants.OP_RETRIEVE, authInfo, attributes, serviceInfo)) {
            if (response.getStatus().equals(DoipConstants.STATUS_OK)) {
                try (InDoipMessage in = response.getOutput()) {
                    DigitalObject resultDo = digitalObjectFromSegments(in);
                    return resultDo;
                }
            } else if (response.getStatus().equals(DoipConstants.STATUS_NOT_FOUND)) {
                return null;
            } else {
                throw doipExceptionFromDoipResponse(response);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    /**
     * Deletes a digital object.
     *
     * @param targetId the id of the object to delete
     * @param authInfo the authentication to provide
     * @throws DoipException
     */
    public void delete(String targetId, AuthenticationInfo authInfo) throws DoipException {
        delete(targetId, authInfo, null);
    }

    /**
     * Deletes a digital object from a specified service.
     *
     * @param targetId    the id of the object to delete
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @throws DoipException
     */
    public void delete(String targetId, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        JsonElement input = null;
        try (DoipClientResponse resp = performOperation(targetId, DoipConstants.OP_DELETE, authInfo, null, input, serviceInfo)) {
            if (resp.getStatus().equals(DoipConstants.STATUS_OK)) {
                return;
            } else {
                throw doipExceptionFromDoipResponse(resp);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    /**
     * Lists operations available for a digital object.
     *
     * @param targetId the id of the digital object
     * @param authInfo the authentication to provide
     * @return the list of available operation ids
     * @throws DoipException
     */
    public List<String> listOperations(String targetId, AuthenticationInfo authInfo) throws DoipException {
        return listOperations(targetId, authInfo, null);
    }

    /**
     * Lists operations available for a digital object at a specified service.
     *
     * @param targetId    the id of the digital object
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the list of available operation ids
     * @throws DoipException
     */
    public List<String> listOperations(String targetId, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        JsonElement input = null;
        try (DoipClientResponse resp = performOperation(targetId, DoipConstants.OP_LIST_OPERATIONS, authInfo, null, input, serviceInfo)) {
            if (resp.getStatus().equals(DoipConstants.STATUS_OK)) {
                try (InDoipMessage in = resp.getOutput()) {
                    InDoipSegment firstSegment = InDoipMessageUtil.getFirstSegment(in);
                    if (firstSegment == null) {
                        throw new DoipException("Missing first segment in response");
                    }
                    List<String> results = GsonUtility.getGson().fromJson(firstSegment.getJson(), new TypeToken<List<String>>() {
                    }.getType());
                    return results;
                }
            } else {
                throw doipExceptionFromDoipResponse(resp);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    public static DoipException doipExceptionFromDoipResponse(DoipClientResponse resp) {
        String message = getMessageFromErrorResponse(resp);
        DoipException e = new DoipException(resp.getStatus(), message);
        return e;
    }

    private static String getMessageFromErrorResponse(DoipClientResponse resp) {
        String message = null;
        JsonObject attributes = resp.getAttributes();
        if (attributes != null && attributes.has("message")) {
            message = attributes.get("message").getAsString();
        } else {
            message = "DOIP Error: " + resp.getStatus();
        }
        return message;
    }

    /**
     * Search for digital objects, returning the ids of the results.
     *
     * @param targetId the id of the operation target (generally a DOIP service id)
     * @param query    the query
     * @param params   the query parameters
     * @param authInfo the authentication to provide
     * @return the search results as ids
     * @throws DoipException
     */
    public SearchResults<String> searchIds(String targetId, String query, QueryParams params, AuthenticationInfo authInfo) throws DoipException {
        return searchIds(targetId, query, params, authInfo, null);
    }

    /**
     * Search for digital objects, returning the ids of the results.
     *
     * @param targetId    the id of the operation target (generally a DOIP service id)
     * @param query       the query
     * @param params      the query parameters
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the search results as ids
     * @throws DoipException
     */
    public SearchResults<String> searchIds(String targetId, String query, QueryParams params, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        return searchIdsOrFull("id", String.class, targetId, query, params, authInfo, serviceInfo);
    }

    /**
     * Search for digital objects, returning the full results as digital objects.
     *
     * @param targetId the id of the operation target (generally a DOIP service id)
     * @param query    the query
     * @param params   the query parameters
     * @param authInfo the authentication to provide
     * @return the search results as digital objects
     * @throws DoipException
     */
    public SearchResults<DigitalObject> search(String targetId, String query, QueryParams params, AuthenticationInfo authInfo) throws DoipException {
        return search(targetId, query, params, authInfo, null);
    }

    /**
     * Search for digital objects, returning the full results as digital objects.
     *
     * @param targetId    the id of the operation target (generally a DOIP service id)
     * @param query       the query
     * @param params      the query parameters
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the search results as digital objects
     * @throws DoipException
     */
    public SearchResults<DigitalObject> search(String targetId, String query, QueryParams params, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        return searchIdsOrFull("full", DigitalObject.class, targetId, query, params, authInfo, serviceInfo);
    }

    @SuppressWarnings("resource")
    private <T> SearchResults<T> searchIdsOrFull(String type, Class<T> klass, String targetId, String query, QueryParams params, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        DoipClientResponse resp = null;
        try {
            JsonObject attributes = getSearchAttributes(type, query, params);
            resp = performOperation(targetId, DoipConstants.OP_SEARCH, authInfo, attributes, serviceInfo);
            if (resp.getStatus().equals(DoipConstants.STATUS_OK)) {
                return new DoipSearchResults<>(resp, klass);
            } else {
                throw doipExceptionFromDoipResponse(resp);
            }
        } catch (Exception e) {
            closeQuietly(resp);
            if (e instanceof DoipException) throw (DoipException) e;
            throw new DoipException(e);
        }
    }

    private static JsonObject getSearchAttributes(String type, String query, QueryParams params) {
        if (params == null) params = QueryParams.DEFAULT;
        JsonObject attributes = new JsonObject();
        attributes.addProperty("query", query);
        attributes.addProperty("pageNum", params.getPageNumber());
        attributes.addProperty("pageSize", params.getPageSize());
        if (type == null) {
            type = "full";
        }
        attributes.addProperty("type", type);
        if (params.getSortFields() != null) {
            String sortFields = sortFieldsToString(params.getSortFields());
            attributes.addProperty("sortFields", sortFields);
        }
        return attributes;
    }

    private static String sortFieldsToString(List<SortField> sortFields) {
        if (sortFields != null && !sortFields.isEmpty()) {
            List<String> sortFieldsForTransport = new ArrayList<>(sortFields.size());
            for (SortField sortField : sortFields) {
                if (sortField.isReverse()) sortFieldsForTransport.add(sortField.getName() + " DESC");
                else sortFieldsForTransport.add(sortField.getName());
            }
            if (!sortFieldsForTransport.isEmpty()) {
                return String.join(",", sortFieldsForTransport);
            }
        }
        return null;
    }

    /**
     * Performs the "hello" operation.
     *
     * @param targetId the id of the operation target (generally a DOIP service id)
     * @param authInfo the authentication to provide
     * @return the result of the hello operation as a service info digital object
     * @throws DoipException
     */
    public DigitalObject hello(String targetId, AuthenticationInfo authInfo) throws DoipException {
        return hello(targetId, authInfo, null);
    }

    /**
     * Performs the "hello" operation at a specified service.
     *
     * @param targetId    the id of the operation target (generally a DOIP service id)
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return the result of the hello operation as a service info digital object
     * @throws DoipException
     */
    public DigitalObject hello(String targetId, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        JsonElement input = null;
        try (DoipClientResponse response = performOperation(targetId, DoipConstants.OP_HELLO, authInfo, null, input, serviceInfo)) {
            if (response.getStatus().equals(DoipConstants.STATUS_OK)) {
                try (InDoipMessage in = response.getOutput()) {
                    DigitalObject resultDo = digitalObjectFromSegments(in);
                    return resultDo;
                }
            } else {
                throw doipExceptionFromDoipResponse(response);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    public DoipClientResponse getLHS(String targetId, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        try (DoipClientResponse response = performOperation(targetId, DoipConstants.OP_GETLHS, authInfo, null, (JsonElement) null, serviceInfo)) {
            if (response.getStatus().equals(DoipConstants.STATUS_OK)) {
                return response;
            } else {
                throw doipExceptionFromDoipResponse(response);
            }
        } catch (DoipException e) {
            throw e;
        } catch (Exception e) {
            throw new DoipException(e);
        }
    }

    /**
     * Retrieves an element from a digital object.
     *
     * @param targetId  the id of the digital object
     * @param elementId the id of the element
     * @param authInfo  the authentication to provide
     * @return an input stream with the bytes of the element
     * @throws DoipException
     */
    public InputStream retrieveElement(String targetId, String elementId, AuthenticationInfo authInfo) throws DoipException {
        return retrieveElement(targetId, elementId, authInfo, null);
    }

    /**
     * Retrieves an element from a digital object at a specified service.
     *
     * @param targetId    the id of the digital object
     * @param elementId   the id of the element
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return an input stream with the bytes of the element
     * @throws DoipException
     */
    @SuppressWarnings("resource")
    public InputStream retrieveElement(String targetId, String elementId, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("element", elementId);
        DoipClientResponse response = null;
        try {
            response = performOperation(targetId, DoipConstants.OP_RETRIEVE, authInfo, attributes, serviceInfo);
            if (response.getStatus().equals(DoipConstants.STATUS_OK)) {
                InDoipMessage in = response.getOutput();
                InDoipSegment firstSegment = InDoipMessageUtil.getFirstSegment(in);
                if (firstSegment == null) {
                    throw new DoipException("Missing first segment");
                }
                return getElementInputStreamWithCorrectClose(firstSegment, response);
            } else {
                throw doipExceptionFromDoipResponse(response);
            }
        } catch (Exception e) {
            closeQuietly(response);
            if (e instanceof DoipException) throw (DoipException) e;
            throw new DoipException(e);
        }
    }

    /**
     * Retrieves a byte range of an element from a digital object at a specified service.
     * Either start or end may be null.
     * If neither are null, all bytes from start to end, inclusive, with the first byte of the element being numbered 0, are returned.
     * If both are null, the entire element is returned.
     * If only end is null, all bytes from start to the end of the element are returned.
     * If only start is null, end indicates how many bytes to return from the end of the element. For example, if start is null and end is 500, the
     * last 500 bytes of the element are returned.
     *
     * @param targetId    the id of the digital object
     * @param elementId   the id of the element
     * @param start       the start byte of the desired range, or null (indicates that the number of bytes given by end should be retrieved from the end of the element)
     * @param end         the end byte of the desired range, or null (indicates the range should extend to the end of the element)
     * @param authInfo    the authentication to provide
     * @param serviceInfo the service at which to perform the operation
     * @return an input stream with the bytes of the element from start to end inclusive
     * @throws DoipException
     */
    @SuppressWarnings("resource")
    public InputStream retrievePartialElement(String targetId, String elementId, Long start, Long end, AuthenticationInfo authInfo, ServiceInfo serviceInfo) throws DoipException {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("element", elementId);
        JsonObject range = new JsonObject();
        range.addProperty("start", start);
        range.addProperty("end", end);
        attributes.add("range", range);
        DoipClientResponse response = null;
        try {
            response = performOperation(targetId, DoipConstants.OP_RETRIEVE, authInfo, attributes, serviceInfo);
            if (response.getStatus().equals(DoipConstants.STATUS_OK)) {
                InDoipMessage in = response.getOutput();
                InDoipSegment firstSegment = InDoipMessageUtil.getFirstSegment(in);
                if (firstSegment == null) {
                    throw new DoipException("Missing first segment");
                }
                return getElementInputStreamWithCorrectClose(firstSegment, response);
            } else {
                throw doipExceptionFromDoipResponse(response);
            }
        } catch (Exception e) {
            closeQuietly(response);
            if (e instanceof DoipException) throw (DoipException) e;
            throw new DoipException(e);
        }
    }

    private static InputStream getElementInputStreamWithCorrectClose(InDoipSegment doipSegment, DoipClientResponse response) {
        return new DelegatedCloseableInputStream(doipSegment.getInputStream(), () -> closeQuietly(response));
    }

    private static void closeQuietly(DoipClientResponse response) {
        if (response != null) try {
            response.close();
        } catch (Exception ex) {
        }
    }

    /**
     * 从InDoipMessage中，解析出各个element，放入DigitalObject中
     */
    private DigitalObject digitalObjectFromSegments(InDoipMessage input) throws IOException, DoipException {
        InDoipSegment firstSegment = InDoipMessageUtil.getFirstSegment(input);
        if (firstSegment == null) {
            throw new BadDoipException("Missing input");
        }
        DigitalObject digitalObject = GsonUtility.getGson().fromJson(firstSegment.getJson(), DigitalObject.class);

        if (digitalObject.elements != null) {
            Map<String, Element> elements = new HashMap<>();
            for (Element el : digitalObject.elements) {
                elements.put(el.id, el);
            }
            Iterator<InDoipSegment> segments = input.iterator();
            while (segments.hasNext()) {
                InDoipSegment headerSegment = segments.next();
                String elementId;
                try {
                    elementId = headerSegment.getJson().getAsJsonObject().get("id").getAsString();
                } catch (Exception e) {
                    throw new DoipException("Unexpected element header");
                }
                if (!segments.hasNext()) {
                    throw new DoipException("Unexpected end of input");
                }
                InDoipSegment elementBytesSegment = segments.next();
                Element el = elements.get(elementId);
                if (el == null) {
                    throw new DoipException("No such element " + elementId);
                }
                el.in = persistInputStream(elementBytesSegment.getInputStream());
            }
        } else {
            if (!InDoipMessageUtil.isEmpty(input)) {
                throw new DoipException("Unexpected input segments");
            }
        }
        return digitalObject;
    }

    private static ByteArrayInputStream persistInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) > 0) {
            bout.write(buf, 0, r);
        }
        return new ByteArrayInputStream(bout.toByteArray());
    }

    private ConnectionAndPool getConnectionFor(String targetId) throws DoipException {
        String serviceHandle = targetIdToServiceHandleMap.getIfPresent(targetId);
        ServiceInfoAndPool serviceInfoAndPool = null;
        if (serviceHandle == null) {
            serviceInfoAndPool = getServiceInfoAndPoolFor(targetId);
            targetIdToServiceHandleMap.put(targetId, serviceInfoAndPool.serviceInfo.serviceId);
        } else {
            serviceInfoAndPool = serviceHandleToPoolsMap.getIfPresent(serviceHandle);
            if (serviceInfoAndPool == null) {
                serviceInfoAndPool = getServiceInfoAndPoolFor(serviceHandle);
            }
        }
        ConnectionAndPool result = new ConnectionAndPool(serviceInfoAndPool.pool);
        return result;
    }

    private ServiceInfoAndPool getServiceInfoAndPoolFor(String handle) throws DoipException {
        try {
            ServiceInfo serviceInfo = getServiceInfoFor(handle, 0);
            if (serviceInfo == null) {
                throw new DoipException("DOIPServiceInfo not found for " + handle);
            }
            ServiceInfoAndPool serviceInfoAndPool = getOrCreatePool(serviceInfo);
            return serviceInfoAndPool;
        } catch (HandleException he) {
            throw new DoipException(he);
        }
    }

    private synchronized ServiceInfoAndPool getOrCreatePool(ServiceInfo serviceInfo) {
        if (closed) throw new IllegalStateException("closed");
        ServiceInfoAndPool serviceInfoAndPool = serviceHandleToPoolsMap.getIfPresent(serviceInfo.serviceId);
        if (serviceInfoAndPool == null) {
            DoipConnectionPool pool = new DoipConnectionPool(MAX_POOL_SIZE, doipClient, connectionOptionsForServiceInfo(serviceInfo));
            serviceInfoAndPool = new ServiceInfoAndPool(serviceInfo, pool);
            serviceHandleToPoolsMap.put(serviceInfo.serviceId, serviceInfoAndPool);
        }
        return serviceInfoAndPool;
    }

    private ConnectionOptions connectionOptionsForServiceInfo(ServiceInfo serviceInfo) {
        ConnectionOptions res = new ConnectionOptions();
        res.serverId = serviceInfo.serviceId;
        res.address = serviceInfo.ipAddress;
        res.port = serviceInfo.port;
        if (serviceInfo.publicKey != null) {
            res.trustedServerPublicKeys = Collections.singletonList(serviceInfo.publicKey);
        }
        return res;
    }

    /**
     * 封装ServiceInfo和DoipConnectionPool
     */
    private static class ServiceInfoAndPool {
        public final ServiceInfo serviceInfo;
        public final DoipConnectionPool pool;

        public ServiceInfoAndPool(ServiceInfo serviceInfo, DoipConnectionPool pool) {
            this.serviceInfo = serviceInfo;
            this.pool = pool;
        }
    }

    private static class PoolRemovalListener implements RemovalListener<String, ServiceInfoAndPool> {

        @Override
        public void onRemoval(RemovalNotification<String, ServiceInfoAndPool> notification) {
            ServiceInfoAndPool serviceInfoAndPool = notification.getValue();
            serviceInfoAndPool.pool.shutdown();
        }
    }

    private ServiceInfo getServiceInfoFor(String handle, int hopCount) throws HandleException {
        HandleValue[] values = resolver.resolveHandle(handle, new String[]{DOIP_SERVICE, TYPE_DOIP_SERVICE, DOIP_SERVICE_INFO, TYPE_DOIP_SERVICE_INFO}, null);
        for (HandleValue value : values) {
            String type = value.getTypeAsString();
            if (DOIP_SERVICE_INFO.equals(type) || TYPE_DOIP_SERVICE_INFO.equals(type)) {
                String json = value.getDataAsString();
                DigitalObject dobj = GsonUtility.getGson().fromJson(json, DigitalObject.class);
                ServiceInfo result = GsonUtility.getGson().fromJson(dobj.attributes, ServiceInfo.class);
                result.serviceId = handle;
                return result;
            } else if (DOIP_SERVICE.equals(type) || TYPE_DOIP_SERVICE.equals(type)) {
                String doipServiceHandle = value.getDataAsString();
                if (hopCount >= MAX_HOP_COUNT) {
                    return null;
                }
                return getServiceInfoFor(doipServiceHandle, hopCount + 1);
            }
        }
        return null;
    }
}
