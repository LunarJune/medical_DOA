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

public class HsAdminModifierOperation implements HandleRecordOperationInterface {

    public String oldAdmin;
    public int oldAdminIndex;
    public String replacementAdmin;
    public int replacementAdminIndex;

    public HsAdminModifierOperation(String oldAdmin, int oldAdminIndex, String replacementAdmin, int replacementAdminIndex) {
        this.oldAdmin = oldAdmin;
        this.oldAdminIndex = oldAdminIndex;
        this.replacementAdmin = replacementAdmin;
        this.replacementAdminIndex = replacementAdminIndex;
    }

    @Override
    public void process(String handle, HandleValue[] values, HandleResolver resolver, AuthenticationInfo authInfo, SiteInfo site) throws HandleException {
        List<HandleValue> hsAdmins = BatchUtil.getValuesOfType(values, "HS_ADMIN");
        if (hsAdmins.size() != 1) {
            throw new HandleException(0, handle + " has does not have exactly 1 admin value.");
        }
        HandleValue hsAdmin = hsAdmins.get(0);
        int indexOfThisHsAdminValue = hsAdmin.getIndex();

        AdminRecord adminRecord = Encoder.decodeAdminRecord(hsAdmin.getData(), 0);

        byte[] currentAdminIdBytes = adminRecord.adminId;
        String currentAdmin = Util.decodeString(currentAdminIdBytes);

        int currentAdminIndex = adminRecord.adminIdIndex;

        if (!oldAdmin.equals(currentAdmin) || oldAdminIndex != currentAdminIndex) {
            return;
            //throw new HandleException(0, handle + " does not have correct old admin to perform operation.");
        }

        adminRecord.adminIdIndex = replacementAdminIndex;
        adminRecord.adminId = Util.encodeString(replacementAdmin);

        byte[] replacementBytes = Encoder.encodeAdminRecord(adminRecord);

        HandleValue replacementHandleValue = new HandleValue();

        replacementHandleValue.setIndex(indexOfThisHsAdminValue);
        replacementHandleValue.setType(Common.ADMIN_TYPE);
        replacementHandleValue.setData(replacementBytes);

        AbstractResponse response = BatchUtil.modifyHandleValue(handle, replacementHandleValue, resolver, authInfo, site);
        if (response.responseCode != AbstractMessage.RC_SUCCESS) {
            throw new HandleException(HandleException.INTERNAL_ERROR, response.toString());
        }
    }

}
