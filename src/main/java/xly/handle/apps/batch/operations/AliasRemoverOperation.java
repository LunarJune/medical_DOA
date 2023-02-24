/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import java.util.List;

import xly.handle.hdllib.AbstractMessage;
import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class AliasRemoverOperation implements HandleRecordOperationInterface {

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        List<HandleValue> aliases = BatchUtil.getValuesOfType(values, "HS_ALIAS");

        if (aliases.size() > 1) {
            throw new HandleException(HandleException.INTERNAL_ERROR, handle + " has more than one alias");
        }
        if (aliases.size() == 0) {
            return;
        }

        HandleValue alias = aliases.get(0);
        AbstractResponse response = BatchUtil.removeValueRequest(handle, alias, resolver, authInfo, site);
        if (response.responseCode != AbstractMessage.RC_SUCCESS) {
            throw new HandleException(HandleException.INTERNAL_ERROR, response.toString());
        }

    }

}
