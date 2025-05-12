// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.UATag;
import java.util.Iterator;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.util.ArrayList;
import cn.doraro.flexedge.core.UAVal;
import java.util.List;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.DevDriver;

public abstract class MCEthDriver extends DevDriver
{
    protected static ILogger log;
    protected List<MCDevItem> devItems;
    long readTimeout;
    private long cmdInterval;
    private static MCAddr MC_Addr;
    private static UAVal.ValTP[] LIMIT_VTPS;
    static ArrayList<DevDriver.Model> models;
    
    static {
        MCEthDriver.log = LoggerManager.getLogger((Class)MCEthDriver.class);
        MCEthDriver.MC_Addr = new MCAddr();
        MCEthDriver.LIMIT_VTPS = new UAVal.ValTP[] { UAVal.ValTP.vt_bool, UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float };
        (MCEthDriver.models = new ArrayList<DevDriver.Model>()).add(new MCModel_Q());
    }
    
    public MCEthDriver() {
        this.devItems = null;
        this.readTimeout = 3000L;
        this.cmdInterval = 10L;
    }
    
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
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
        gp = new PropGroup("block_size", lan);
        gp.addPropItem(new PropItem("blk_bytes", lan, PropItem.PValTP.vt_int, false, (String[])null, (Object[])null, (Object)64));
        pgs.add(gp);
        return pgs;
    }
    
    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class)this.getClass());
        gp = new PropGroup("mceth_comm", lan);
        gp.addPropItem(new PropItem("comm_fmt", lan, PropItem.PValTP.vt_str, false, new String[] { "bin", "ascii" }, new Object[] { "bin", "ascii" }, (Object)"bin"));
        pgs.add(gp);
        return pgs;
    }
    
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }
    
    public DevAddr getSupportAddr() {
        return MCEthDriver.MC_Addr;
    }
    
    public UAVal.ValTP[] getLimitValTPs(final UADev dev) {
        return MCEthDriver.LIMIT_VTPS;
    }
    
    public long getReadTimeout() {
        return this.readTimeout;
    }
    
    public long getCmdInterval() {
        return this.cmdInterval;
    }
    
    public List<DevDriver.Model> getDevModels() {
        return MCEthDriver.models;
    }
    
    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        super.initDriver(failedr);
        this.devItems = this.listMCDevItem();
        if (this.devItems.size() <= 0) {
            failedr.append("no fx cmd inited in driver");
            return false;
        }
        return true;
    }
    
    public List<MCDevItem> listMCDevItem() {
        final List<UADev> devs = this.getBelongToCh().getDevs();
        final ArrayList<MCDevItem> mdis = new ArrayList<MCDevItem>();
        for (final UADev dev : devs) {
            final MCDevItem fdi = new MCDevItem(this, dev);
            final StringBuilder devfr = new StringBuilder();
            if (!fdi.init(devfr)) {
                continue;
            }
            mdis.add(fdi);
        }
        return mdis;
    }
    
    private MCDevItem getDevItem(final UADev dev) {
        for (final MCDevItem mdi : this.devItems) {
            if (mdi.getUADev().equals((Object)dev)) {
                return mdi;
            }
        }
        return null;
    }
    
    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        final MCDevItem mdi = this.getDevItem(dev);
        return mdi != null && mdi.RT_writeVal(da, v);
    }
    
    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        throw new RuntimeException("no impl");
    }
}
