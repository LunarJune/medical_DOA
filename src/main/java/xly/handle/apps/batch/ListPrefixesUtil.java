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
import xly.handle.hdllib.Common;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.ListNAsRequest;
import xly.handle.hdllib.ListNAsResponse;
import xly.handle.hdllib.ResponseMessageCallback;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.Util;

public class ListPrefixesUtil {
    private final SiteInfo site;
    private final AuthenticationInfo authInfo;
    private final HandleResolver resolver;

    public ListPrefixesUtil(SiteInfo site, AuthenticationInfo authInfo, HandleResolver resolver) {
        this.site = site;
        this.authInfo = authInfo;
        this.resolver = resolver;
    }

    public List<String> getAllPrefixes() throws HandleException {
        ListNAsRequest request = new ListNAsRequest(Common.BLANK_HANDLE, authInfo);
        ListHandlesAccumulator handlesAccumulator = new ListHandlesAccumulator();
        AbstractResponse response = resolver.sendRequestToSite(request, site, handlesAccumulator);
        BatchUtil.throwIfNotSuccess(response);
        List<String> allPrefixes = handlesAccumulator.handlesList;
        return allPrefixes;
    }

    public static class ListHandlesAccumulator implements ResponseMessageCallback {

        public List<String> handlesList = new ArrayList<>();

        @Override
        public void handleResponse(AbstractResponse response) throws HandleException {
            if (response.responseCode == AbstractMessage.RC_SUCCESS) {
                byte handles[][] = ((ListNAsResponse) response).handles;
                for (byte[] handleBytes : handles) {
                    String handle = Util.decodeString(handleBytes);
                    handlesList.add(handle);
                }
            }
        }
    }
}
