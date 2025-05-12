// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fxnet;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;

import java.util.List;

public class FxNetDriver extends DevDriver {
    private static FxNetAddr FX_NET_ADDR;

    static {
        FxNetDriver.FX_NET_ADDR = new FxNetAddr();
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    public DevDriver copyMe() {
        return new FxNetDriver();
    }

    public String getName() {
        return "mitsubishi_fxnet";
    }

    public String getTitle() {
        return "Mitsubishi FX Net";
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
        return FxNetDriver.FX_NET_ADDR;
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
