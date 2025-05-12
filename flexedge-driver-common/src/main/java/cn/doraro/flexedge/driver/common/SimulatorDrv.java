// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;

import java.util.List;

public class SimulatorDrv extends DevDriver {
    private static DevAddr supAddr;

    static {
        SimulatorDrv.supAddr = new SimulatorAddr();
    }

    public String getName() {
        return "simulator";
    }

    public String getTitle() {
        return "Simulator";
    }

    public DevDriver copyMe() {
        final SimulatorDrv r = new SimulatorDrv();
        return r;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }

    public DevAddr getSupportAddr() {
        return SimulatorDrv.supAddr;
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
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

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
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
