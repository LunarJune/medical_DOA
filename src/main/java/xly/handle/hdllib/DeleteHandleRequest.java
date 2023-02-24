/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.hdllib;

/******************************************************************************
 * Request used to delete an existing handle.
 ******************************************************************************/

public class DeleteHandleRequest extends AbstractRequest {

    public DeleteHandleRequest(byte handle[], AuthenticationInfo authInfo) {
        super(handle, AbstractMessage.OC_DELETE_HANDLE, authInfo);
        this.isAdminRequest = true;
    }

}
