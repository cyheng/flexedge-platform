// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.Lan;
import java.util.ArrayList;
import cn.doraro.flexedge.core.basic.PropGroup;
import java.util.List;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.DevDriver;

public class HLFinsDriverNet extends HLFinsDriver
{
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }
    
    public DevDriver copyMe() {
        return new HLFinsDriverNet();
    }
    
    public String getName() {
        return "hostlink_fins_net";
    }
    
    public String getTitle() {
        return "Hostlink FINS Network(TCP)";
    }
    
    @Override
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class)this.getClass());
        final PropGroup gp = new PropGroup("fins_net", lan);
        PropItem pi = new PropItem("sna", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)0);
        gp.addPropItem(pi);
        pi = new PropItem("sa1", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)1);
        gp.addPropItem(pi);
        pi = new PropItem("dev_id_ip", lan, PropItem.PValTP.vt_str, false, (String[])null, (Object[])null, (Object)"");
        gp.addPropItem(pi);
        pi = new PropItem("dna", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)0);
        gp.addPropItem(pi);
        pi = new PropItem("da1", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)0);
        gp.addPropItem(pi);
        pi = new PropItem("da2", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)0);
        gp.addPropItem(pi);
        pgs.add(gp);
        return pgs;
    }
}
