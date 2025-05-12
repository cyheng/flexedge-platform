// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.f800;

import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.UADev;

import java.util.ArrayList;
import java.util.List;

public class RespSnifferDev {
    RespSnifferDriver driver;
    int stationNo;
    ArrayList<F800MsgReq> reqs;
    transient ArrayList<F800MsgResp> parsedResps;
    transient ArrayList<RespSnifferCmd> cmds;

    public RespSnifferDev(final RespSnifferDriver drv, final int station_no) {
        this.reqs = new ArrayList<F800MsgReq>();
        this.parsedResps = null;
        this.cmds = null;
        this.driver = drv;
        this.stationNo = station_no;
    }

    public RespSnifferDriver getDriver() {
        return this.driver;
    }

    public int getStationNo() {
        return this.stationNo;
    }

    public List<F800MsgReq> listReqs() {
        return this.reqs;
    }

    public List<RespSnifferCmd> listCmds() {
        if (this.cmds != null) {
            return this.cmds;
        }
        if (this.reqs == null) {
            return null;
        }
        final ArrayList<RespSnifferCmd> ss = new ArrayList<RespSnifferCmd>();
        for (final F800MsgReq req : this.reqs) {
            final RespSnifferCmd cmd = RespSnifferCmd.fromReq(this, req);
            ss.add(cmd);
        }
        return this.cmds = ss;
    }

    public void reconstructDevTree() throws Exception {
        final UACh ch = this.getDriver().getBelongToCh();
        if (ch == null) {
            return;
        }
        final String devn = "bp_" + this.stationNo;
        UADev dev = ch.getDevByName(devn);
        if (dev == null) {
            dev = ch.addDev(devn, "\u53d8\u9891\u5668" + this.stationNo, "", (String) null, (String) null, (String) null);
        }
        final List<RespSnifferCmd> cmds = this.listCmds();
        if (cmds != null) {
            for (final RespSnifferCmd cmd : cmds) {
                cmd.reconstructTags(dev);
            }
        }
    }

    public UADev getUADev() {
        final UACh ch = this.getDriver().getBelongToCh();
        if (ch == null) {
            return null;
        }
        final String devn = "bp_" + this.stationNo;
        return ch.getDevByName(devn);
    }

    public void RT_updateDevice(final UACh ch) {
        final UADev dev = ch.getDevByName("bp_" + this.stationNo);
        if (dev == null) {
            return;
        }
        final List<RespSnifferCmd> cmds = this.listCmds();
        if (cmds != null) {
            for (int cn = cmds.size(), i = 0; i < cn; ++i) {
                final RespSnifferCmd cmd = cmds.get(i);
                final F800MsgResp resp = this.parsedResps.get(i);
                cmd.RT_injectResp(resp);
            }
        }
    }
}
