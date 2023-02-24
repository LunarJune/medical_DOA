/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import java.util.List;

import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.Util;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class ReplaceHsNamespaceOperation implements HandleRecordOperationInterface {

    private final byte[] bytes;

    public ReplaceHsNamespaceOperation(String replacementNamespaceString) {
        bytes = Util.encodeString(replacementNamespaceString);
    }

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        List<HandleValue> existingNamespaceValues = BatchUtil.getValuesOfType(values, "HS_NAMESPACE");
        if (existingNamespaceValues.size() != 1) {
            throw new HandleException(HandleException.INTERNAL_ERROR, "Handle does not have exactly one HS_NAMESPACE");
        }
        HandleValue existingNamespaceValue = existingNamespaceValues.get(0);
        existingNamespaceValue.setData(bytes);
        AbstractResponse response = BatchUtil.modifyHandleValue(handle, existingNamespaceValue, resolver, authInfo, site);
        BatchUtil.throwIfNotSuccess(response);
    }
}
