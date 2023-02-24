/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.server.servletcontainer.support;

import java.net.InetAddress;

import xly.handle.hdllib.AbstractMessage;
import xly.handle.hdllib.AbstractRequest;
import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.ResponseMessageCallback;
import xly.handle.hdllib.Util;
import xly.handle.server.servletcontainer.HandleServerInterface;

public class LoggingResponseMessageCallbackWrapper implements ResponseMessageCallback {
    public static final String ACCESS_TYPE_PREFIX = "HTTP:";

    private final HandleServerInterface handleServer;
    private final boolean logAccesses;
    private final AbstractRequest currentRequest;
    private final InetAddress inetAddress;
    private final ResponseMessageCallback callback;
    private final String accessType;
    private final long recvTime;

    public LoggingResponseMessageCallbackWrapper(ResponseMessageCallback callback, HandleServerInterface handleServer, boolean logAccesses, AbstractRequest currentRequest, InetAddress inetAddress, String accessType) {
        this.callback = callback;
        this.handleServer = handleServer;
        this.logAccesses = logAccesses;
        this.currentRequest = currentRequest;
        this.inetAddress = inetAddress;
        this.accessType = accessType;
        this.recvTime = System.currentTimeMillis();
    }

    @Override
    public void handleResponse(AbstractResponse response) throws HandleException {
        if (logAccesses) {
            if (currentRequest != null) {
                long respTime = System.currentTimeMillis() - recvTime;
                handleServer.logAccess(ACCESS_TYPE_PREFIX + accessType, inetAddress, currentRequest.opCode, (response != null ? response.responseCode : AbstractMessage.RC_ERROR), Util.getAccessLogString(currentRequest, response), respTime);
            }
        }
        callback.handleResponse(response);
    }

}
