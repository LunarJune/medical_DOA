/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.apps.batch.filters;

import java.util.List;

import xly.handle.hdllib.HandleValue;
import xly.handle.apps.batch.HandleRecordFilter;
import xly.handle.apps.batch.BatchUtil;

public class HasHsNameSpaceFilter implements HandleRecordFilter {

    @Override
    public boolean accept(HandleValue[] values) {
        List<HandleValue> nameSpaceValues = BatchUtil.getValuesOfType(values, "HS_NAMESPACE");
        if (nameSpaceValues.size() != 0) {
            return true;
        }
        return false;
    }

}
