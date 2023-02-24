/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.server.servletcontainer.support;

import xly.handle.hdllib.AbstractRequest;
import xly.handle.hdllib.AuthenticationInfo;
import xly.handle.hdllib.ChallengeResponse;
import xly.handle.hdllib.HandleException;
import xly.handle.hdllib.Util;

public class PreAuthenticatedAuthenticationInfo extends AuthenticationInfo {
    public static final byte[] AUTH_TYPE = Util.encodeString(PreAuthenticatedAuthenticationInfo.class.getName());

    private final byte[] userIdHandle;
    private final int userIdIndex;

    public PreAuthenticatedAuthenticationInfo(byte[] userIdHandle, int userIdIndex) {
        this.userIdHandle = userIdHandle;
        this.userIdIndex = userIdIndex;
    }

    @Override
    public byte[] getAuthType() {
        return AUTH_TYPE;
    }

    @Override
    public byte[] authenticate(ChallengeResponse challenge, AbstractRequest request) throws HandleException {
        throw new HandleException(HandleException.INTERNAL_ERROR, getClass().getName() + ".authenticate() should not be called");
    }

    @Override
    public byte[] getUserIdHandle() {
        return userIdHandle;
    }

    @Override
    public int getUserIdIndex() {
        return userIdIndex;
    }

}
