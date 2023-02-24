/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.server.servletcontainer.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import xly.handle.hdllib.AbstractMessage;
import xly.handle.hdllib.AbstractRequest;
import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.GsonUtility;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.ListHandlesResponse;
import xly.handle.hdllib.ListNAsResponse;
import xly.handle.hdllib.ResponseMessageCallback;
import xly.handle.hdllib.Util;

import com.google.gson.JsonObject;

class ListCallback implements ResponseMessageCallback {
    int page;
    int pageSize;
    final List<String> handles = new ArrayList<>();
    int totalCount;
    AbstractResponse unexpectedResponse;

    @Override
    public void handleResponse(AbstractResponse resp) throws HandleException {
        if (resp instanceof ListHandlesResponse) {
            handleListResponse(((ListHandlesResponse) resp).handles);
        } else if (resp instanceof ListNAsResponse) {
            handleListResponse(((ListNAsResponse) resp).handles);
        } else {
            unexpectedResponse = resp;
        }
    }

    private void handleListResponse(byte[][] responseHandles) {
        if (page >= 0 && pageSize >= 0) {
            if (pageSize == 0 || totalCount >= (page + 1) * pageSize || totalCount + responseHandles.length <= page * pageSize) {
                totalCount += responseHandles.length;
                return;
            }
        }
        for (byte[] handle : responseHandles) {
            if (page >= 0 && pageSize >= 0) {
                if (pageSize == 0 || totalCount >= (page + 1) * pageSize || totalCount + 1 <= page * pageSize) {
                    totalCount += 1;
                    continue;
                }
            }
            String handleString = Util.decodeString(handle);
            handles.add(handleString);
            totalCount += 1;
        }
    }

    void processListHandlesResponse(HttpServletRequest servletReq, HttpServletResponse servletResp, AbstractRequest listReq, @SuppressWarnings("hiding") List<String> handles, @SuppressWarnings("hiding") int totalCount) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("responseCode", Integer.valueOf(AbstractMessage.RC_SUCCESS));
        if (listReq.opCode == AbstractMessage.OC_LIST_HANDLES) json.addProperty("prefix", Util.decodeString(listReq.handle));
        json.addProperty("totalCount", String.valueOf(totalCount));
        if (page >= 0 && pageSize >= 0) {
            json.addProperty("page", page);
            json.addProperty("pageSize", pageSize);
        }
        String property;
        if (listReq.opCode == AbstractMessage.OC_LIST_HOMED_NAS) {
            property = "prefixes";
        } else {
            property = "handles";
        }
        json.add(property, GsonUtility.getGson().toJsonTree(handles));
        BaseHandleRequestProcessingServlet.processResponse(servletReq, servletResp, HttpServletResponse.SC_OK, json);
    }
}
