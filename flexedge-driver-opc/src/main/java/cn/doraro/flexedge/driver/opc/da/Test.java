// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.da;

import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;

import java.util.Collection;
import java.util.concurrent.Executors;

public class Test {
    public static void main(final String[] args) throws Exception {
        final ConnectionInformation ci = new ConnectionInformation();
        ci.setHost("localhost");
        ci.setDomain("");
        ci.setUser("zzj");
        ci.setPassword("zhijun1090");
        ci.setClsid("7BC0CC8E-482C-47CA-ABDC-0FE7F9C6E729");
        final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
        server.connect();
        final ServerList serverList = new ServerList("localhost", "zzj", "zhijun1090", "");
        final Collection<ClassDetails> classDetails = serverList.listServersWithDetails(new Category[]{Categories.OPCDAServer10, Categories.OPCDAServer20, Categories.OPCDAServer30}, new Category[0]);
        for (final ClassDetails cds : classDetails) {
            System.out.println(String.valueOf(cds.getProgId()) + "=" + cds.getDescription());
        }
    }
}
