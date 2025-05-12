// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.ConnException;
import java.io.IOException;
import java.util.Iterator;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.UAVal;
import java.util.ArrayList;
import java.util.List;
import cn.doraro.flexedge.core.DevDriver;

public class FxDriver extends DevDriver
{
    protected List<FxDevItem> fxDevItems;
    long readTimeout;
    private long cmdInterval;
    static ArrayList<DevDriver.Model> models;
    private static FxAddr FX_Addr;
    private static UAVal.ValTP[] LIMIT_VTPS;
    boolean justOnConn;
    
    static {
        (FxDriver.models = new ArrayList<DevDriver.Model>()).add(new FxModel_FX());
        FxDriver.models.add(new FxModel_FX0());
        FxDriver.models.add(new FxModel_FX0N());
        FxDriver.models.add(new FxModel_FX2N());
        FxDriver.models.add(new FxModel_FX3U());
        FxDriver.FX_Addr = new FxAddr();
        FxDriver.LIMIT_VTPS = new UAVal.ValTP[] { UAVal.ValTP.vt_bool, UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32 };
    }
    
    public FxDriver() {
        this.fxDevItems = null;
        this.readTimeout = 3000L;
        this.cmdInterval = 10L;
        this.justOnConn = false;
    }
    
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }
    
    public DevDriver copyMe() {
        return new FxDriver();
    }
    
    public String getName() {
        return "mitsubishi_fx";
    }
    
    public String getTitle() {
        return "Mitsubishi FX";
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>)ConnPtStream.class;
    }
    
    public boolean supportDevFinder() {
        return false;
    }
    
    public List<DevDriver.Model> getDevModels() {
        return FxDriver.models;
    }
    
    public List<PropGroup> getPropGroupsForDevDef() {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class)this.getClass());
        gp = new PropGroup("timing", lan);
        gp.addPropItem(new PropItem("req_to", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)1000));
        gp.addPropItem(new PropItem("failed_tryn", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)3));
        gp.addPropItem(new PropItem("recv_to", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)200));
        gp.addPropItem(new PropItem("inter_req", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)0));
        pgs.add(gp);
        gp = new PropGroup("block_size", lan);
        gp.addPropItem(new PropItem("out_coils", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)32));
        gp.addPropItem(new PropItem("reg", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)32));
        pgs.add(gp);
        return pgs;
    }
    
    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }
    
    public DevAddr getSupportAddr() {
        return FxDriver.FX_Addr;
    }
    
    public UAVal.ValTP[] getLimitValTPs(final UADev dev) {
        return FxDriver.LIMIT_VTPS;
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
        final ArrayList<FxDevItem> mdis = new ArrayList<FxDevItem>();
        for (final UADev dev : devs) {
            final FxDevItem fdi = new FxDevItem(this, dev);
            final StringBuilder devfr = new StringBuilder();
            if (!fdi.init(devfr)) {
                continue;
            }
            mdis.add(fdi);
        }
        this.fxDevItems = mdis;
        if (this.fxDevItems.size() <= 0) {
            failedr.append("no fx cmd inited in driver");
            return false;
        }
        return true;
    }
    
    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
        this.justOnConn = true;
    }
    
    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
        this.justOnConn = false;
    }
    
    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        final ConnPtStream cpt = (ConnPtStream)this.getBindedConnPt();
        if (cpt == null) {
            return true;
        }
        if (!cpt.isConnReady()) {
            return true;
        }
        try {
            if (this.justOnConn) {
                this.justOnConn = false;
                final boolean b_ack = FxCmd.checkDevReady(cpt.getInputStream(), cpt.getOutputStream(), 5000L);
                if (!b_ack) {
                    throw new IOException("check dev not acked");
                }
                return true;
            }
            else {
                for (final FxDevItem mdi : this.fxDevItems) {
                    mdi.doCmd(cpt);
                }
            }
        }
        catch (final ConnException se) {
            if (FxDriver.log.isDebugEnabled()) {
                FxDriver.log.debug("RT_runInLoop err", (Throwable)se);
            }
            cpt.close();
        }
        catch (final Exception e) {
            if (FxDriver.log.isErrorEnabled()) {
                FxDriver.log.debug("RT_runInLoop err", (Throwable)e);
            }
        }
        return true;
    }
    
    private FxDevItem getDevItem(final UADev dev) {
        for (final FxDevItem mdi : this.fxDevItems) {
            if (mdi.getUADev().equals((Object)dev)) {
                return mdi;
            }
        }
        return null;
    }
    
    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        final FxDevItem mdi = this.getDevItem(dev);
        return mdi != null && mdi.RT_writeVal(da, v);
    }
    
    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        throw new RuntimeException("no impl");
    }
}
