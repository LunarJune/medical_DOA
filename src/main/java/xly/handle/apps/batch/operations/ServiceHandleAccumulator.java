/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class ServiceHandleAccumulator implements HandleRecordOperationInterface {

    Map<String, List<String>> rootServiceHandlesMap = new HashMap<>();

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        List<HandleValue> servValues = BatchUtil.getValuesOfType(values, "HS_SERV");
        for (HandleValue servValue : servValues) {
            String serviceHandle = servValue.getDataAsString();
            add(serviceHandle, handle);
        }
    }

    private void add(String serviceHandle, String handle) {
        List<String> serviceUsers = rootServiceHandlesMap.get(serviceHandle);
        if (serviceUsers == null) {
            serviceUsers = new ArrayList<>();
            rootServiceHandlesMap.put(serviceHandle, serviceUsers);
        }
        serviceUsers.add(handle);
    }

    public Map<String, List<String>> getRootServiceHandlesMap() {
        return rootServiceHandlesMap;
    }

}
