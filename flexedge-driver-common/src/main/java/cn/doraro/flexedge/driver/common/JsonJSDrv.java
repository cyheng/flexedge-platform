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
import cn.doraro.flexedge.core.conn.ConnPtMSGNor;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevDriver;

public class JsonJSDrv extends DevDriver
{
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }
    
    public DevDriver copyMe() {
        return new JsonJSDrv();
    }
    
    public String getName() {
        return "json_js";
    }
    
    public String getTitle() {
        return "Json JS";
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>)ConnPtMSGNor.class;
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
