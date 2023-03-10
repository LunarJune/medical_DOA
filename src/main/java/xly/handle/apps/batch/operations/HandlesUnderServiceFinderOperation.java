/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import java.util.ArrayList;
import java.util.List;

import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class HandlesUnderServiceFinderOperation implements HandleRecordOperationInterface {

    public List<String> resultHandles;
    public String serviceHandle;

    public HandlesUnderServiceFinderOperation(String serviceHandle) {
        this.serviceHandle = serviceHandle;
        resultHandles = new ArrayList<>();
    }

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        if (containsSpecifiedServiceHandle(values)) {
            resultHandles.add(handle);
        }
    }

    private boolean containsSpecifiedServiceHandle(HandleValue[] values) {
        List<HandleValue> hsServValues = BatchUtil.getValuesOfType(values, "HS_SERV");
        for (HandleValue value : hsServValues) {
            String data = value.getDataAsString();
            String upCaseData = data.toUpperCase();
            if (serviceHandle.equals(upCaseData)) {
                return true;
            }
        }
        return false;
    }

}
