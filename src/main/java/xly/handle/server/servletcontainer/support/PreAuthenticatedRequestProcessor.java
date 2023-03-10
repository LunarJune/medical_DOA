/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.server.servletcontainer.support;

import java.net.InetAddress;

import xly.handle.hdllib.AbstractRequest;
import xly.handle.hdllib.AbstractRequestProcessor;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.ResponseMessageCallback;
import xly.handle.server.servletcontainer.HandleServerInterface;

public class PreAuthenticatedRequestProcessor extends AbstractRequestProcessor {
    private final HandleServerInterface handleServer;
    private final String accessType;

    // if accessType == null, do not log every response
    public PreAuthenticatedRequestProcessor(HandleServerInterface handleServer, String accessType) {
        this.handleServer = handleServer;
        this.accessType = accessType;
    }

    @Override
    public void processRequest(AbstractRequest req, InetAddress caller, ResponseMessageCallback callback) throws HandleException {
        ResponseMessageCallback callbackWrapper = callback;
        if (accessType != null && caller != null) {
            callbackWrapper = new LoggingResponseMessageCallbackWrapper(callback, handleServer, this.handleServer.logHttpAccesses(), req, caller, accessType);
        }
        handleServer.processPreAuthenticatedRequest(req, callbackWrapper);
    }

}
