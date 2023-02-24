/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.operations;

import java.util.Map;

import xly.handle.hdllib.AbstractMessage;
import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class AliasAdderOperation implements HandleRecordOperationInterface {

    public Map<String, String> aliasMap;

    public AliasAdderOperation(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        int availableIndex = BatchUtil.lowestAvailableIndex(values);
        String alias = aliasMap.get(handle);
        if (alias == null) {
            throw new HandleException(HandleException.INTERNAL_ERROR, handle + " does not have an alias in map.");
        }
        AbstractResponse response = BatchUtil.addAliasToHandleRecord(handle, alias, availableIndex, resolver, authInfo, site);
        if (response.responseCode != AbstractMessage.RC_SUCCESS) {
            throw new HandleException(HandleException.INTERNAL_ERROR, response.toString());
        }
    }

}
