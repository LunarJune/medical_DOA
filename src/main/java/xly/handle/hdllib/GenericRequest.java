/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.hdllib;

public class GenericRequest extends AbstractRequest {
    public GenericRequest(byte handle[], int opCode, AuthenticationInfo authInfo) {
        super(handle, opCode, authInfo);
    }
}
