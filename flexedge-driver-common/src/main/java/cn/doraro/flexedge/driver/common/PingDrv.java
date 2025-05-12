// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.UAUtil;
import cn.doraro.flexedge.core.DevAddrFix;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.Lan;
import java.util.ArrayList;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.basic.PropGroup;
import java.util.List;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.DevDriver;

public class PingDrv extends DevDriver
{
    public DevDriver copyMe() {
        return new PingDrv();
    }
    
    public String getName() {
        return "ping";
    }
    
    public String getTitle() {
        return "Ping";
    }
    
    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        final ArrayList<PropGroup> rets = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class)this.getClass());
        final PropGroup pg = new PropGroup("ping", lan);
        pg.addPropItem(new PropItem("timeout", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)3000));
        pg.addPropItem(new PropItem("chk_inter", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)10000));
        rets.add(pg);
        return rets;
    }
    
    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }
    
    public DevAddr getSupportAddr() {
        return (DevAddr)new DevAddrFix();
    }
    
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        switch (groupn) {
            case "ping": {
                if ("timeout".contentEquals(itemn) && !UAUtil.chkPropValInt(strv, Long.valueOf(1000L), (Long)null, Boolean.valueOf(true), failedr)) {
                    return false;
                }
                if ("chk_inter".contentEquals(itemn) && !UAUtil.chkPropValInt(strv, Long.valueOf(100L), (Long)null, Boolean.valueOf(true), failedr)) {
                    return false;
                }
                break;
            }
            default:
                break;
        }
        return true;
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return null;
    }
    
    public boolean supportDevFinder() {
        return false;
    }
    
    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
    }
    
    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
    }
    
    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        return false;
    }
    
    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        return false;
    }
    
    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }
}
