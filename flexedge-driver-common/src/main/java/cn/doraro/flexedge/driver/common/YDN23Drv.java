// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.basic.PropGroup;
import java.util.List;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevDriver;

public class YDN23Drv extends DevDriver
{
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
    }
    
    public DevDriver copyMe() {
        return new YDN23Drv();
    }
    
    public String getName() {
        return "ydn23";
    }
    
    public String getTitle() {
        return "YDN23(YD/T 1363)";
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return null;
    }
    
    public boolean supportDevFinder() {
        return false;
    }
    
    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }
    
    public DevAddr getSupportAddr() {
        return null;
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
