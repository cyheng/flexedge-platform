// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.msgnet.MNBase;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.msgnet.MNNet;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.msgnet.MNNode;
import java.util.HashSet;
import cn.doraro.flexedge.core.util.xmldata.DataTranserJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.List;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.util.ArrayList;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.msgnet.IMNRunner;
import cn.doraro.flexedge.core.msgnet.MNModule;

public class MSBus_M extends MNModule implements IMNRunner
{
    static ILogger log;
    ArrayList<SlaveDev> devs;
    transient ArrayList<SlaveCP> cps;
    Thread RT_th;
    boolean RT_bRun;
    boolean RT_bValid;
    ArrayList<Integer> limitIds;
    Runnable RT_runner;
    
    static {
        MSBus_M.log = LoggerManager.getLogger((Class)MSBus_M.class);
    }
    
    public MSBus_M() {
        this.devs = new ArrayList<SlaveDev>();
        this.cps = new ArrayList<SlaveCP>();
        this.RT_th = null;
        this.RT_bRun = false;
        this.RT_bValid = true;
        this.limitIds = null;
        this.RT_runner = new Runnable() {
            @Override
            public void run() {
                try {
                    MSBus_M.this.RT_run();
                }
                finally {
                    MSBus_M.this.RT_th = null;
                    MSBus_M.this.RT_bRun = false;
                    MSBus_M.this.RT_stop();
                }
                MSBus_M.this.RT_th = null;
                MSBus_M.this.RT_bRun = false;
                MSBus_M.this.RT_stop();
            }
        };
    }
    
    public String getTP() {
        return "modbus_slave_bus";
    }
    
    public String getTPTitle() {
        return "Modbus Slave Bus";
    }
    
    public String getColor() {
        return "#24acf2";
    }
    
    public String getIcon() {
        return "PK_bus";
    }
    
    public List<SlaveDev> listDevItems() {
        return this.devs;
    }
    
    public SlaveDev getDev(final String id) {
        for (final SlaveDev d : this.devs) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }
    
    public SlaveDev getDevByName(final String n) {
        for (final SlaveDev d : this.devs) {
            if (n.equals(d.getName())) {
                return d;
            }
        }
        return null;
    }
    
    public String getPmTitle() {
        final List<SlaveDev> devs = this.listDevItems();
        if (devs == null || devs.size() <= 0) {
            return "No devices set";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("Device ");
        for (final SlaveDev sd : devs) {
            sb.append(" ").append(sd.getDevAddr());
        }
        return sb.toString();
    }
    
    public boolean isParamReady(final StringBuilder failedr) {
        if (this.cps == null || this.cps.size() <= 0) {
            failedr.append("no connection be set");
            return false;
        }
        for (final SlaveCP scp : this.cps) {
            if (!scp.isValid(failedr)) {
                return false;
            }
        }
        return true;
    }
    
    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        JSONArray jarr = new JSONArray();
        for (final SlaveDev dev : this.devs) {
            final JSONObject tmpjo = DataTranserJSON.extractJSONFromObj((Object)dev);
            jarr.put((Object)tmpjo);
        }
        jo.put("devs", (Object)jarr);
        jarr = new JSONArray();
        for (final SlaveCP cp : this.cps) {
            final JSONObject tmpjo = cp.toJO();
            jarr.put((Object)tmpjo);
        }
        jo.put("cps", (Object)jarr);
        return jo;
    }
    
    protected void setParamJO(final JSONObject jo) {
        JSONArray jarr = jo.optJSONArray("devs");
        final ArrayList<SlaveDev> devs = new ArrayList<SlaveDev>();
        if (jarr != null) {
            for (int n = jarr.length(), i = 0; i < n; ++i) {
                final JSONObject tmpjo = jarr.getJSONObject(i);
                try {
                    final SlaveDev dev = new SlaveDev();
                    DataTranserJSON.injectJSONToObj((Object)dev, tmpjo);
                    devs.add(dev);
                }
                catch (final Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        this.devs = devs;
        jarr = jo.optJSONArray("cps");
        final ArrayList<SlaveCP> cps = new ArrayList<SlaveCP>();
        if (jarr != null) {
            for (int n2 = jarr.length(), j = 0; j < n2; ++j) {
                final JSONObject tmpjo2 = jarr.getJSONObject(j);
                final SlaveCP scp = SlaveCP.fromJO(this, tmpjo2);
                if (scp == null) {
                    MSBus_M.log.warn("failed load SlaveCP ");
                }
                else {
                    cps.add(scp);
                }
            }
        }
        this.cps = cps;
    }
    
    public void checkAfterSetParam() {
        try {
            this.updateDevNodes();
        }
        catch (final Exception ee) {
            ee.printStackTrace();
        }
    }
    
    private void updateDevNodes() throws Exception {
        final List<MNNode> nodes = this.getRelatedNodes();
        final float me_x = this.getX();
        final float me_y = this.getY();
        final HashSet<String> upmids = new HashSet<String>();
        final HashSet<String> mwids = new HashSet<String>();
        if (this.devs != null) {
            for (final SlaveDev dev : this.devs) {
                upmids.add(dev.getId());
                mwids.add(dev.getId());
            }
        }
        final MNNet net = this.getBelongTo();
        boolean bdirty = false;
        if (nodes != null && nodes.size() > 0) {
            for (final MNNode n : nodes) {
                if (n instanceof MSUpdateMem_NE) {
                    final MSUpdateMem_NE num = (MSUpdateMem_NE)n;
                    if (Convert.isNullOrEmpty(num.devId) || !upmids.contains(num.devId)) {
                        net.delNodeById(n.getId(), false);
                        bdirty = true;
                    }
                    else {
                        upmids.remove(num.devId);
                    }
                }
                if (n instanceof MSOnMasterWrite_NS) {
                    final MSOnMasterWrite_NS nnn = (MSOnMasterWrite_NS)n;
                    if (Convert.isNullOrEmpty(nnn.devId) || !mwids.contains(nnn.devId)) {
                        net.delNodeById(n.getId(), false);
                        bdirty = true;
                    }
                    else {
                        mwids.remove(nnn.devId);
                    }
                }
            }
        }
        final MNNode sup_um = this.getSupportedNodeByTP("ms_up_mem");
        final MNNode sup_mw = this.getSupportedNodeByTP("ms_on_master_w");
        int newcc = 0;
        for (final String addid : upmids) {
            ++newcc;
            final SlaveDev dev2 = this.getDev(addid);
            final MSUpdateMem_NE newn = (MSUpdateMem_NE)net.createNewNodeInModule((MNModule)this, sup_um, me_x - 100.0f, me_y - 20.0f - 33 * newcc, (String)null, false);
            newn.devId = addid;
            newn.setTitle(String.valueOf(dev2.getTitle()) + "[" + dev2.getDevAddr() + "]");
            bdirty = true;
        }
        newcc = 0;
        for (final String addid : mwids) {
            ++newcc;
            final SlaveDev dev2 = this.getDev(addid);
            final MSOnMasterWrite_NS newn2 = (MSOnMasterWrite_NS)net.createNewNodeInModule((MNModule)this, sup_mw, me_x + 200.0f, me_y - 20.0f - 53 * newcc, (String)null, false);
            newn2.devId = addid;
            newn2.setTitle(String.valueOf(dev2.getTitle()) + "[" + dev2.getDevAddr() + "]");
            bdirty = true;
        }
        if (bdirty) {
            net.save();
        }
    }
    
    public boolean RT_isBusValid() {
        return this.RT_bValid;
    }
    
    void RT_setBusValid(final boolean bvalid) {
        this.RT_bValid = bvalid;
    }
    
    private void RT_run() {
        if (this.cps == null || this.cps.size() <= 0) {
            return;
        }
        final List<SlaveDev> devs = this.listDevItems();
        if (devs == null || devs.size() <= 0) {
            return;
        }
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        for (final SlaveDev dev : devs) {
            dev.belongTo = this;
            dev.RT_init();
            ids.add(dev.getDevAddr());
        }
        this.limitIds = ids;
        for (final SlaveCP scp : this.cps) {
            if (!scp.isEnable()) {
                continue;
            }
            scp.RT_init();
        }
        for (final SlaveDev dev : this.devs) {
            dev.RT_init();
        }
        while (this.RT_bRun) {
            for (final SlaveDev dev : this.devs) {
                dev.RT_readBindTags();
            }
            for (final SlaveCP scp : this.cps) {
                if (!scp.isEnable()) {
                    continue;
                }
                scp.RT_runInLoop();
                this.UTIL_sleep(50L);
            }
        }
    }
    
    public synchronized boolean RT_start(final StringBuilder failedr) {
        if (this.RT_bRun) {
            return true;
        }
        this.RT_bRun = true;
        (this.RT_th = new Thread(this.RT_runner)).start();
        return true;
    }
    
    public synchronized void RT_stop() {
        final Thread th = this.RT_th;
        if (th != null) {
            th.interrupt();
        }
        this.RT_th = null;
        this.RT_bRun = false;
        if (this.cps != null) {
            for (final SlaveCP scp : this.cps) {
                scp.RT_stop();
            }
        }
    }
    
    public boolean RT_isRunning() {
        return this.RT_th != null;
    }
    
    public boolean RT_isSuspendedInRun(final StringBuilder reson) {
        return false;
    }
    
    public boolean RT_runnerEnabled() {
        return true;
    }
    
    public boolean RT_runnerStartInner() {
        return false;
    }
    
    void RT_setMemData(final String devid, final JSONObject jo) {
        final SlaveDev sdev = this.getDev(devid);
        if (sdev == null) {
            return;
        }
        for (final String k : jo.keySet()) {
            final Object ob = jo.opt(k);
            if (ob == null) {
                continue;
            }
            sdev.RT_setVarVal(k, ob);
        }
    }
    
    void RT_onMasterWriteVar(final SlaveDevSeg seg, final JSONObject name_vars) {
        final SlaveDev dev = seg.belongTo;
        final String devid = dev.getId();
        for (final MNNode n : this.getRelatedNodes()) {
            if (!(n instanceof MSOnMasterWrite_NS)) {
                continue;
            }
            final MSOnMasterWrite_NS mw = (MSOnMasterWrite_NS)n;
            if (devid.equals(mw.devId)) {
                mw.RT_fireVarWriteOut(name_vars);
            }
        }
    }
    
    void RT_onMasterWriteTag(final SlaveDevSeg seg, final JSONObject tag_val) {
        final SlaveDev dev = seg.belongTo;
        final String devid = dev.getId();
        for (final MNNode n : this.getRelatedNodes()) {
            if (!(n instanceof MSOnMasterWrite_NS)) {
                continue;
            }
            final MSOnMasterWrite_NS mw = (MSOnMasterWrite_NS)n;
            if (devid.equals(mw.devId)) {
                mw.RT_fireTagWriteOut(tag_val);
            }
        }
    }
    
    void RT_onMasterWriteBindTag(final SlaveDevSeg seg, final SlaveBindTag bt, final Object wval) {
        final UATag tag = bt.getTag();
        if (tag == null) {
            return;
        }
        tag.RT_writeVal(wval);
    }
    
    protected void RT_renderDiv(final List<MNBase.DivBlk> divblks) {
        final StringBuilder divsb = new StringBuilder();
        divsb.append("<div class=\"rt_blk\" style='position:relative;height0:90%;'><div style='background-color:#aaaaaa;white-space: nowrap;'>Connections</div>");
        for (final SlaveCP scp : this.cps) {
            final String ss = scp.RT_getRunInf();
            divsb.append(scp.getConnTitle()).append(" [").append(scp.proto).append("]").append(scp.isEnable() ? "<span style='color:green'>Enabled</span>" : "<span style='color:red'>Disabled</span>").append("<div style='position:relative;left:20px;'>").append(ss).append("</div>");
        }
        divsb.append("</div>");
        divblks.add(new MNBase.DivBlk("ms_bus", divsb.toString()));
        super.RT_renderDiv((List)divblks);
    }
}
