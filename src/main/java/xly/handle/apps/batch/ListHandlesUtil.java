/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch;

import java.util.ArrayList;
import java.util.List;

import xly.handle.hdllib.AbstractMessage;
import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.ListHandlesRequest;
import xly.handle.hdllib.ListHandlesResponse;
import xly.handle.hdllib.ResponseMessageCallback;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.Util;

public class ListHandlesUtil {

    private final SiteInfo site;
    private final AuthenticationInfo authInfo;
    private final HandleResolver resolver;

    public ListHandlesUtil(SiteInfo site, AuthenticationInfo authInfo, HandleResolver resolver) {
        this.site = site;
        this.authInfo = authInfo;
        this.resolver = resolver;
    }

    public List<String> getMatchingHandles(HandleRecordFilter filter, String prefix) throws HandleException {
        List<String> result = new ArrayList<>();
        byte[] prefixBytes = Util.encodeString(prefix);
        ListHandlesRequest request = new ListHandlesRequest(prefixBytes, authInfo);
        ListHandlesAccumulator handlesAccumulator = new ListHandlesAccumulator();
        resolver.sendRequestToSite(request, site, handlesAccumulator);

        List<String> allHandles = handlesAccumulator.handlesList;

        int progressCount = 0;
        for (String handle : allHandles) {
            progressCount++;
            if (progressCount % 10 == 0) {
                System.out.println("progressCount: " + progressCount);
            }
            HandleValue[] values = resolver.resolveHandle(handle);
            if (filter.accept(values)) {
                result.add(handle);
                System.out.println(".");
            }

        }
        return result;
    }

    /**
     * Returns all handles under a given prefix
     */
    public List<String> getAllHandles(String prefix) throws HandleException {
        byte[] prefixBytes = Util.encodeString(prefix);
        ListHandlesRequest request = new ListHandlesRequest(prefixBytes, authInfo);
        ListHandlesAccumulator handlesAccumulator = new ListHandlesAccumulator();
        resolver.sendRequestToSite(request, site, handlesAccumulator);
        List<String> allHandles = handlesAccumulator.handlesList;
        return allHandles;
    }

    public static class ListHandlesAccumulator implements ResponseMessageCallback {

        public List<String> handlesList = new ArrayList<>();

        @Override
        public void handleResponse(AbstractResponse response) throws HandleException {
            if (response.responseCode == AbstractMessage.RC_SUCCESS) {
                byte handles[][] = ((ListHandlesResponse) response).handles;
                for (byte[] handleBytes : handles) {
                    String handle = Util.decodeString(handleBytes);
                    handlesList.add(handle);
                }
            }
        }
    }
}
