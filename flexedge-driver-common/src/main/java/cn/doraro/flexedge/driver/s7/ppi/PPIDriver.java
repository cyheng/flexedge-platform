// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.ConnException;
import java.util.Iterator;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.basic.ValChker;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.Lan;
import java.util.ArrayList;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.util.List;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.DevDriver;

public class PPIDriver extends DevDriver
{
    private static ILogger log;
    protected List<PPIDevItem> ppiDevItems;
    private short masterId;
    private long readTimeout;
    private long cmdInterval;
    private static PPIAddr ppiAddr;
    
    static {
        PPIDriver.log = LoggerManager.getLogger((Class)PPIDriver.class);
        PPIDriver.ppiAddr = new PPIAddr();
    }
    
    public PPIDriver() {
        this.ppiDevItems = null;
        this.masterId = 0;
        this.readTimeout = 3000L;
        this.cmdInterval = 10L;
    }
    
    public String getName() {
        return "s7_200_ppi";
    }
    
    public String getTitle() {
        return "Siemens S7-200";
    }
    
    public short getMasterID() {
        return this.masterId;
    }
    
    public long getReadTimeout() {
        return this.readTimeout;
    }
    
    public long getCmdInterval() {
        return this.cmdInterval;
    }
    
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }
    
    public DevDriver copyMe() {
        return new PPIDriver();
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>)ConnPtStream.class;
    }
    
    public boolean supportDevFinder() {
        return false;
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
        gp = new PropGroup("auto_demotion", lan);
        gp.addPropItem(new PropItem("en", lan, PropItem.PValTP.vt_bool, false, new String[] { "Disabled", "Enabled" }, new Object[] { false, true }, (Object)false));
        gp.addPropItem(new PropItem("dm_tryc", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)3));
        gp.addPropItem(new PropItem("dm_ms", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)10000));
        gp.addPropItem(new PropItem("dm_no_req", lan, PropItem.PValTP.vt_bool, false, new String[] { "Disabled", "Enabled" }, new Object[] { false, true }, (Object)false));
        pgs.add(gp);
        gp = new PropGroup("block_size", lan);
        gp.addPropItem(new PropItem("out_coils", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)32));
        gp.addPropItem(new PropItem("in_coils", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)32));
        gp.addPropItem(new PropItem("internal_reg", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)32));
        gp.addPropItem(new PropItem("holding", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)32));
        pgs.add(gp);
        return pgs;
    }
    
    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class)this.getClass());
        final PropGroup gp = new PropGroup("ppi_spk", lan);
        final PropItem pi = new PropItem("dev_addr", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)1);
        pi.setValChker((ValChker)new ValChker<Number>() {
            public boolean checkVal(final Number v, final StringBuilder failedr) {
                final int vi = v.intValue();
                if (vi >= 1 && vi <= 255) {
                    return true;
                }
                failedr.append("PPI device address must between 1-255");
                return false;
            }
        });
        gp.addPropItem(pi);
        pgs.add(gp);
        return pgs;
    }
    
    public DevAddr getSupportAddr() {
        return PPIDriver.ppiAddr;
    }
    
    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        super.initDriver(failedr);
        final List<UADev> devs = this.getBelongToCh().getDevs();
        final ArrayList<PPIDevItem> mdis = new ArrayList<PPIDevItem>();
        for (final UADev dev : devs) {
            final PPIDevItem mdi = new PPIDevItem(this, dev);
            final StringBuilder devfr = new StringBuilder();
            if (!mdi.init(devfr)) {
                continue;
            }
            mdis.add(mdi);
        }
        this.ppiDevItems = mdis;
        if (this.ppiDevItems.size() <= 0) {
            failedr.append("no ppi cmd inited in driver");
            return false;
        }
        return true;
    }
    
    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
    }
    
    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
    }
    
    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        if (this.ppiDevItems == null) {
            return true;
        }
        final ConnPtStream cpt = (ConnPtStream)this.getBindedConnPt();
        if (cpt == null) {
            return true;
        }
        if (!cpt.isConnReady()) {
            return true;
        }
        try {
            for (final PPIDevItem mdi : this.ppiDevItems) {
                mdi.doCmd(cpt);
            }
        }
        catch (final ConnException se) {
            if (PPIDriver.log.isDebugEnabled()) {
                PPIDriver.log.debug("RT_runInLoop err", (Throwable)se);
            }
            cpt.close();
        }
        catch (final Exception e) {
            if (PPIDriver.log.isErrorEnabled()) {
                PPIDriver.log.debug("RT_runInLoop err", (Throwable)e);
            }
        }
        return true;
    }
    
    private PPIDevItem getDevItem(final UADev dev) {
        for (final PPIDevItem mdi : this.ppiDevItems) {
            if (mdi.getUADev().equals((Object)dev)) {
                return mdi;
            }
        }
        return null;
    }
    
    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        final PPIDevItem mdi = this.getDevItem(dev);
        return mdi != null && mdi.RT_writeVal(da, v);
    }
    
    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        throw new RuntimeException("no impl");
    }
}
