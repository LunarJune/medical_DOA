/**********************************************************************\
 © COPYRIGHT 2019 Corporation for National Research Initiatives (CNRI);
                        All rights reserved.

        The HANDLE.NET software is made available subject to the
      Handle.Net Public License Agreement, which may be obtained at
          http://hdl.handle.net/20.1000/112 or hdl:20.1000/112
\**********************************************************************/

package xly.handle.server.replication;

import xly.handle.hdllib.SiteInfo;

public class ReplicationSourceSiteInfo {
    private SiteInfo site;
    private String name;

    public ReplicationSourceSiteInfo(SiteInfo site, String name) {
        this.site = site;
        this.name = name;
    }

    public SiteInfo getSite() {
        return site;
    }

    public String getName() {
        return name;
    }

    public void setSite(SiteInfo site) {
        this.site = site;
    }

    public void setName(String name) {
        this.name = name;
    }
}
