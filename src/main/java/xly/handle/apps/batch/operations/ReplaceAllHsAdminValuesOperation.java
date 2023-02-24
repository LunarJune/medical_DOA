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

import xly.handle.hdllib.AbstractResponse;
import xly.handle.hdllib.AdminRecord;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.Common;
import xly.handle.hdllib.Encoder;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.HandleResolver;
import xly.handle.hdllib.HandleValue;
import xly.handle.hdllib.SiteInfo;
import xly.handle.hdllib.Util;
import xly.handle.apps.batch.HandleRecordOperationInterface;
import xly.handle.apps.batch.BatchUtil;

public class ReplaceAllHsAdminValuesOperation implements HandleRecordOperationInterface {

    public String replacementAdmin;
    public int replacementAdminIndex;

    public ReplaceAllHsAdminValuesOperation(String replacementAdmin, int replacementAdminIndex) {
        this.replacementAdmin = replacementAdmin;
        this.replacementAdminIndex = replacementAdminIndex;
    }

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        List<Integer> indicesToRemoveList = new ArrayList<>();
        for (HandleValue value : values) {
            String type = value.getTypeAsString();
            if ("HS_ADMIN".equals(type)) {
                indicesToRemoveList.add(value.getIndex());
            }
        }
        int[] indicesToRemove = toIntArray(indicesToRemoveList);
        BatchUtil.removeValuesAtIndices(handle, indicesToRemove, resolver, authInfo, site);

        HandleValue newAdminValue = new HandleValue();

        newAdminValue.setIndex(100);
        newAdminValue.setType(Common.ADMIN_TYPE);
        newAdminValue.setData(Encoder.encodeAdminRecord(new AdminRecord(Util.encodeString(replacementAdmin), replacementAdminIndex, true, // addHandle
            true, // deleteHandle
            true, // addNA
            true, // deleteNA
            true, // readValue
            true, // modifyValue
            true, // removeValue
            true, // addValue
            true, // modifyAdmin
            true, // removeAdmin
            true, // addAdmin
            true // listHandles
        )));

        AbstractResponse response = BatchUtil.addHandleValue(handle, newAdminValue, resolver, authInfo, site);
        BatchUtil.throwIfNotSuccess(response);
    }

    private int[] toIntArray(List<Integer> integerList) {
        int[] ints = new int[integerList.size()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = integerList.get(i);
        }
        return ints;
    }
}
