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
import xly.handle.hdllib.AbstractRequest;
import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.DeleteHandleRequest;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.Util;

public class BatchDeleteHandles {
    private final SiteInfo site;
    private final AuthenticationInfo authInfo;
    private final HandleResolver resolver;

    public BatchDeleteHandles(SiteInfo site, AuthenticationInfo authInfo, HandleResolver resolver) {
        this.site = site;
        this.authInfo = authInfo;
        this.resolver = resolver;
    }

    public List<String> deleteHandles(List<String> handlesToDelete) {
        List<String> fails = new ArrayList<>();
        for (String handle : handlesToDelete) {
            byte[] handleBytes = Util.encodeString(handle);
            AbstractRequest deleteHandleRequest = new DeleteHandleRequest(handleBytes, authInfo);
            AbstractResponse response;
            try {
                response = resolver.sendRequestToSite(deleteHandleRequest, site);
                if (response.responseCode != AbstractMessage.RC_SUCCESS) {
                    fails.add(handle);
                } else {
                    System.out.println("DELETED: " + handle);
                }
            } catch (HandleException e) {
                fails.add(handle);
                e.printStackTrace();
            }
        }
        return fails;
    }
}
