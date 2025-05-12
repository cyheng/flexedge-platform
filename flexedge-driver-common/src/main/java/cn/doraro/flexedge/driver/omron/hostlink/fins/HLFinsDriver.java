// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.driver.omron.hostlink.HLAddr;
import cn.doraro.flexedge.driver.omron.hostlink.HLDriver;
import cn.doraro.flexedge.driver.omron.hostlink.HLModel;

import java.util.ArrayList;
import java.util.List;

public abstract class HLFinsDriver extends HLDriver {
    private static HLAddr HL_Addr;
    private static UAVal.ValTP[] LIMIT_VTPS;

    static {
        HLFinsDriver.HL_Addr = new HLAddr();
        HLFinsDriver.LIMIT_VTPS = new UAVal.ValTP[]{UAVal.ValTP.vt_bool, UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32};
    }

    protected List<HLDevItem> hlDevItems;
    long readTimeout;
    boolean justOnConn;
    private long cmdInterval;

    public HLFinsDriver() {
        this.readTimeout = 3000L;
        this.cmdInterval = 10L;
        this.hlDevItems = null;
        this.justOnConn = false;
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtStream.class;
    }

    public boolean supportDevFinder() {
        return false;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        gp = new PropGroup("timing", lan);
        gp.addPropItem(new PropItem("req_to", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 2000));
        gp.addPropItem(new PropItem("failed_tryn", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 3));
        gp.addPropItem(new PropItem("inter_req", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 100));
        pgs.add(gp);
        gp = new PropGroup("block_size", lan);
        gp.addPropItem(new PropItem("bit", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 64));
        gp.addPropItem(new PropItem("word", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 32));
        pgs.add(gp);
        return pgs;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    public List<DevDriver.Model> getDevModels() {
        return HLModel.getModelsAll();
    }

    public DevAddr getSupportAddr() {
        return HLFinsDriver.HL_Addr;
    }

    public UAVal.ValTP[] getLimitValTPs(final UADev dev) {
        return HLFinsDriver.LIMIT_VTPS;
    }

    public long getReadTimeout() {
        return this.readTimeout;
    }

    public long getCmdInterval() {
        return this.cmdInterval;
    }

    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        super.initDriver(failedr);
        final List<UADev> devs = this.getBelongToCh().getDevs();
        final ArrayList<HLDevItem> mdis = new ArrayList<HLDevItem>();
        for (final UADev dev : devs) {
            final HLDevItem fdi = new HLDevItem(this, dev, false);
            final StringBuilder devfr = new StringBuilder();
            if (!fdi.init(devfr)) {
                continue;
            }
            mdis.add(fdi);
        }
        this.hlDevItems = mdis;
        if (this.hlDevItems.size() <= 0) {
            failedr.append("no fx cmd inited in driver");
            return false;
        }
        return true;
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        final ConnPtStream cpt = (ConnPtStream) this.getBindedConnPt();
        if (cpt == null) {
            return true;
        }
        if (!cpt.isConnReady()) {
            return true;
        }
        try {
            for (final HLDevItem mdi : this.hlDevItems) {
                final StringBuilder fsb = new StringBuilder();
                if (!mdi.doCmd(cpt, fsb)) {
                    final String warn = String.valueOf(mdi.getUADev().getName()) + " run err: " + fsb.toString();
                    this.RT_fireDrvWarn(warn);
                    if (!HLFinsDriver.log.isWarnEnabled()) {
                        continue;
                    }
                    HLFinsDriver.log.warn(warn);
                }
            }
        } catch (final ConnException se) {
            if (HLFinsDriver.log.isDebugEnabled()) {
                HLFinsDriver.log.debug("RT_runInLoop err", (Throwable) se);
            }
            cpt.close();
        } catch (final Exception e) {
            e.printStackTrace();
            if (HLFinsDriver.log.isErrorEnabled()) {
                HLFinsDriver.log.debug("RT_runInLoop err", (Throwable) e);
            }
        }
        return true;
    }

    private HLDevItem getDevItem(final UADev dev) {
        for (final HLDevItem mdi : this.hlDevItems) {
            if (mdi.getUADev().equals((Object) dev)) {
                return mdi;
            }
        }
        return null;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
        this.justOnConn = true;
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
        this.justOnConn = false;
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        final HLDevItem mdi = this.getDevItem(dev);
        return mdi != null && mdi.RT_writeVal(da, v);
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }
}
