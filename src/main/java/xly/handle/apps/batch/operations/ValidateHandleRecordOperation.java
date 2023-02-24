/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleRecord;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.trust.HandleRecordTrustVerifier;
import xly.handle.apps.batch.HandleRecordOperationInterface;

public class ValidateHandleRecordOperation implements HandleRecordOperationInterface {

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {

        HandleRecordTrustVerifier handleRecordTrustVerifier = new HandleRecordTrustVerifier(resolver);
        handleRecordTrustVerifier.setThrowing(true);
        HandleRecord handleRecord = new HandleRecord(handle, values);
        boolean validates = handleRecordTrustVerifier.validateHandleRecord(handleRecord);

        if (!validates) {
            throw new HandleException(HandleException.SECURITY_ALERT);
        }
    }

}
