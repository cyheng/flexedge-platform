

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.basic.MemTable;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PPIBlock {
    public static final long MAX_Demotion_DELAY = 30000L;
    static ILogger log;

    static {
        PPIBlock.log = LoggerManager.getLogger("PPI_Lib");
    }

    int devId;
    PPIMemTp memTp;
    List<PPIAddr> addrs;
    int blockSize;
    long scanInterMS;
    MemTable<MemSeg8> memTb;
    transient HashMap<PPICmd, List<PPIAddr>> cmd2addr;
    transient int failedCount;
    private int failedSuccessive;
    private long reqTO;
    private long recvTO;
    private long interReqMs;
    private transient long lastFailedDT;
    private transient int lastFailedCC;
    private transient PPIDriver ppiDrv;
    private LinkedList<PPICmd> writeCmds;

    public PPIBlock(final int devid, final PPIMemTp memtp, final List<PPIAddr> addrs, final int block_size, final long scan_inter_ms) {
        this.devId = 1;
        this.memTp = null;
        this.addrs = null;
        this.blockSize = 32;
        this.scanInterMS = 100L;
        this.memTb = (MemTable<MemSeg8>) new MemTable(8, 131072L);
        this.cmd2addr = new HashMap<PPICmd, List<PPIAddr>>();
        this.failedSuccessive = 3;
        this.reqTO = 1000L;
        this.recvTO = 100L;
        this.interReqMs = 0L;
        this.failedCount = 0;
        this.lastFailedDT = -1L;
        this.lastFailedCC = 0;
        this.ppiDrv = null;
        this.writeCmds = new LinkedList<PPICmd>();
        this.devId = devid;
        if (addrs == null || addrs.size() <= 0) {
            throw new IllegalArgumentException("addr cannot be emtpy");
        }
        if (block_size <= 0) {
            throw new IllegalArgumentException("block cannot <=0 ");
        }
        this.memTp = memtp;
        this.addrs = addrs;
        this.blockSize = block_size;
        this.scanInterMS = scan_inter_ms;
    }

    public void setTimingParam(final long req_to, final long recv_to, final long inter_reqms) {
        this.reqTO = req_to;
        this.recvTO = recv_to;
        this.interReqMs = inter_reqms;
    }

    public PPIMemTp getMemTp() {
        return this.memTp;
    }

    public List<PPIAddr> getAddrs() {
        return this.addrs;
    }

    public MemTable<MemSeg8> getMemTable() {
        return this.memTb;
    }

    boolean initCmds(final PPIDriver drv) {
        this.ppiDrv = drv;
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        PPICmd curcmd = null;
        int cur_reg = -1;
        ArrayList<PPIAddr> curaddrs = null;
        for (final PPIAddr ma : this.addrs) {
            final int regp = ma.getOffsetBytes();
            if (cur_reg < 0) {
                cur_reg = regp;
                curaddrs = new ArrayList<PPIAddr>();
                curaddrs.add(ma);
            } else {
                final int bytelen = regp - cur_reg + 1;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                } else {
                    final PPIAddr lastma = curaddrs.get(curaddrs.size() - 1);
                    final int regnum = lastma.getOffsetBytes() - cur_reg + lastma.getValTP().getValByteLen();
                    curcmd = new PPICmdR((short) this.devId, this.memTp, cur_reg, (short) regnum).withScanIntervalMS(this.scanInterMS);
                    curcmd.initCmd(drv);
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_reg = regp;
                    curaddrs = new ArrayList<PPIAddr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final PPIAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            final int regnum2 = lastma2.getOffsetBytes() - cur_reg + lastma2.getValTP().getValByteLen();
            curcmd = new PPICmdR((short) this.devId, this.memTp, cur_reg, (short) regnum2).withScanIntervalMS(this.scanInterMS);
            curcmd.initCmd(drv);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final PPICmd mc : this.cmd2addr.keySet()) {
            mc.withRecvTimeout(this.reqTO).withRecvEndTimeout(this.recvTO);
            if (PPIBlock.log.isDebugEnabled()) {
                PPIBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }

    private void setAddrError(final List<PPIAddr> addrs) {
        if (addrs == null) {
            return;
        }
        for (final PPIAddr ma : addrs) {
            ma.RT_setVal((Object) null);
        }
    }

    private Object getValByAddr(final PPIAddr da) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return null;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            final int regp = da.getOffsetBytes();
            final int inbit = da.getInBits();
            final int vv = this.memTb.getValNumber(UAVal.ValTP.vt_byte, (long) regp, ByteOrder.LittleEndian).intValue();
            return (vv & 1 << inbit) > 0;
        }
        if (vt.isNumberVT()) {
            return this.memTb.getValNumber(vt, (long) da.getOffsetBytes(), ByteOrder.LittleEndian);
        }
        return null;
    }

    public boolean setValByAddr(final PPIAddr da, final Object v) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return false;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            boolean bv = false;
            if (v instanceof Boolean) {
                bv = (boolean) v;
            } else {
                if (!(v instanceof Number)) {
                    return false;
                }
                bv = (((Number) v).doubleValue() > 0.0);
            }
            this.memTb.setValBool((long) da.getOffsetBytes(), da.getInBits(), bv);
            return true;
        }
        if (!vt.isNumberVT()) {
            return false;
        }
        if (!(v instanceof Number)) {
            return false;
        }
        this.memTb.setValNumber(vt, (long) da.getOffsetBytes(), (Number) v);
        return true;
    }

    public boolean runCmds(final IConnEndPoint ep) throws Exception {
        this.runWriteCmdAndClear(ep);
        return this.runReadCmds(ep);
    }

    public void runCmdsErr() {
        this.runReadCmdsErr();
    }

    private void transMem2Addrs(final List<PPIAddr> addrs) {
        for (final PPIAddr ma : addrs) {
            final Object ov = this.getValByAddr(ma);
            if (ov != null) {
                ma.RT_setVal(ov);
            }
        }
    }

    private boolean chkSuccessiveFailed(final boolean bfailed) {
        if (!bfailed) {
            this.failedCount = 0;
            this.lastFailedDT = -1L;
            this.lastFailedCC = 0;
            return false;
        }
        this.lastFailedDT = System.currentTimeMillis();
        ++this.lastFailedCC;
        if (this.lastFailedCC > 3600) {
            this.lastFailedCC = 3600;
        }
        ++this.failedCount;
        if (this.failedCount >= this.failedSuccessive) {
            this.failedCount = this.failedSuccessive;
            return true;
        }
        return false;
    }

    public boolean checkDemotionCanRun() {
        if (this.lastFailedCC <= 0) {
            return true;
        }
        long dur_dt = this.lastFailedCC * 1000;
        if (dur_dt > 30000L) {
            dur_dt = 30000L;
        }
        return System.currentTimeMillis() - this.lastFailedDT >= dur_dt;
    }

    private boolean runReadCmds(final IConnEndPoint ep) throws Exception {
        boolean ret = true;
        for (final PPICmd mc : this.cmd2addr.keySet()) {
            if (!mc.tickCanRun()) {
                continue;
            }
            final PPICmdR cmdr = (PPICmdR) mc;
            Thread.sleep(this.interReqMs);
            final List<PPIAddr> addrs = this.cmd2addr.get(mc);
            cmdr.doCmd(ep.getInputStream(), ep.getOutputStream());
            final PPIMsgResp resp = cmdr.getResp();
            final PPIMsgReq req = cmdr.getReq();
            byte[] retbs = null;
            if (resp == null) {
                continue;
            }
            retbs = resp.getRetData();
            final int offsetbs = req.getRetOffsetBytes();
            if (retbs == null) {
                if (!this.chkSuccessiveFailed(true)) {
                    continue;
                }
                this.setAddrError(addrs);
                ret = false;
            } else {
                this.memTb.setValBlock((long) offsetbs, retbs.length, retbs, 0);
                this.transMem2Addrs(addrs);
                this.chkSuccessiveFailed(false);
            }
        }
        return ret;
    }

    private boolean runReadCmdsErr() {
        final boolean ret = true;
        for (final PPICmd mc : this.cmd2addr.keySet()) {
            final List<PPIAddr> addrs = this.cmd2addr.get(mc);
            this.setAddrError(addrs);
        }
        return ret;
    }

    private void runWriteCmdAndClear(final IConnEndPoint ep) throws Exception {
        final int s = this.writeCmds.size();
        if (s <= 0) {
            return;
        }
        final PPICmd[] cmds = new PPICmd[s];
        synchronized (this.writeCmds) {
            for (int i = 0; i < s; ++i) {
                cmds[i] = this.writeCmds.removeFirst();
            }
        }
        for (final PPICmd mc : cmds) {
            if (mc.tickCanRun()) {
                Thread.sleep(this.interReqMs);
                mc.doCmd(ep.getInputStream(), ep.getOutputStream());
            }
        }
    }

    public boolean setWriteCmdAsyn(final PPIAddr addr, final Object v) {
        final PPICmdW mc = new PPICmdW((short) this.devId, this.memTp, addr, v);
        mc.initCmd(this.ppiDrv);
        synchronized (this.writeCmds) {
            this.writeCmds.addLast(mc);
        }
        return true;
    }
}
