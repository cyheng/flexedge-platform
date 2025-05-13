

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.basic.MemTable;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class S7Block {
    public static final long MAX_Demotion_DELAY = 30000L;
    static ILogger log;

    static {
        S7Block.log = LoggerManager.getLogger("S7_Lib");
    }

    S7MemTp memTp;
    int dbNum;
    String areaKey;
    List<S7Addr> addrs;
    int blockSize;
    long scanInterMS;
    MemTable<MemSeg8> memTb;
    transient HashMap<S7Msg, List<S7Addr>> cmd2addr;
    transient int failedCount;
    private int failedSuccessive;
    private long reqTO;
    private long recvTO;
    private long interReqMs;
    private transient long lastFailedDT;
    private transient int lastFailedCC;
    private transient S7EthDriver ppiDrv;
    private LinkedList<S7MsgWrite> writeCmds;

    public S7Block(final List<S7Addr> addrs, final int block_size, final long scan_inter_ms) {
        this.memTp = null;
        this.dbNum = 0;
        this.areaKey = null;
        this.addrs = null;
        this.blockSize = 32;
        this.scanInterMS = 100L;
        this.memTb = (MemTable<MemSeg8>) new MemTable(8, 131072L);
        this.cmd2addr = new HashMap<S7Msg, List<S7Addr>>();
        this.failedSuccessive = 3;
        this.reqTO = 1000L;
        this.recvTO = 100L;
        this.interReqMs = 20L;
        this.failedCount = 0;
        this.lastFailedDT = -1L;
        this.lastFailedCC = 0;
        this.ppiDrv = null;
        this.writeCmds = new LinkedList<S7MsgWrite>();
        if (addrs == null || addrs.size() <= 0) {
            throw new IllegalArgumentException("addr cannot be emtpy");
        }
        if (block_size <= 0) {
            throw new IllegalArgumentException("block cannot <=0 ");
        }
        final S7Addr addr = addrs.get(0);
        this.memTp = addr.getMemTp();
        this.dbNum = addr.getDBNum();
        this.areaKey = addr.getAreaKey();
        this.addrs = addrs;
        this.blockSize = block_size;
        this.scanInterMS = scan_inter_ms;
    }

    public String getAreaKey() {
        return this.areaKey;
    }

    public void setTimingParam(final long req_to, final long recv_to, final long inter_reqms) {
        this.reqTO = req_to;
        this.recvTO = recv_to;
        this.interReqMs = ((inter_reqms > 20L) ? inter_reqms : 20L);
    }

    public S7MemTp getMemTp() {
        return this.memTp;
    }

    public List<S7Addr> getAddrs() {
        return this.addrs;
    }

    public MemTable<MemSeg8> getMemTable() {
        return this.memTb;
    }

    boolean initCmds(final S7EthDriver drv) {
        this.ppiDrv = drv;
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        S7Msg curcmd = null;
        int cur_reg = -1;
        ArrayList<S7Addr> curaddrs = null;
        for (final S7Addr ma : this.addrs) {
            final int regp = ma.getOffsetBytes();
            if (cur_reg < 0) {
                cur_reg = regp;
                curaddrs = new ArrayList<S7Addr>();
                curaddrs.add(ma);
            } else {
                final int bytelen = regp - cur_reg + 1;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                } else {
                    final S7Addr lastma = curaddrs.get(curaddrs.size() - 1);
                    final int regnum = lastma.getOffsetBytes() - cur_reg + lastma.getValTP().getValByteLen();
                    curcmd = new S7MsgRead().withParam(this.memTp, this.dbNum, cur_reg, regnum).withScanIntervalMS(this.scanInterMS);
                    curcmd.init(drv);
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_reg = regp;
                    curaddrs = new ArrayList<S7Addr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final S7Addr lastma2 = curaddrs.get(curaddrs.size() - 1);
            final int regnum2 = lastma2.getOffsetBytes() - cur_reg + lastma2.getValTP().getValByteLen();
            curcmd = new S7MsgRead().withParam(this.memTp, this.dbNum, cur_reg, regnum2).withScanIntervalMS(this.scanInterMS);
            curcmd.init(drv);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        return true;
    }

    private void setAddrError(final List<S7Addr> addrs) {
        if (addrs == null) {
            return;
        }
        for (final S7Addr ma : addrs) {
            ma.RT_setVal((Object) null);
        }
    }

    private Object getValByAddr(final S7Addr da) {
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

    public boolean setValByAddr(final S7Addr da, final Object v) {
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

    public boolean runCmds(final S7TcpConn conn) throws Exception {
        this.runWriteCmdAndClear(conn);
        final boolean r = this.runReadCmds(conn);
        Thread.sleep(this.interReqMs);
        return r;
    }

    public void runCmdsErr() {
        this.runReadCmdsErr();
    }

    private void transMem2Addrs(final List<S7Addr> addrs) {
        for (final S7Addr ma : addrs) {
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

    private boolean runReadCmds(final S7TcpConn conn) throws Exception {
        boolean ret = true;
        for (final S7Msg mc : this.cmd2addr.keySet()) {
            if (!mc.tickCanRun()) {
                continue;
            }
            final S7MsgRead cmdr = (S7MsgRead) mc;
            Thread.sleep(this.interReqMs);
            final List<S7Addr> addrs = this.cmd2addr.get(mc);
            conn.clearInputStream(50L);
            byte[] retbs = null;
            try {
                cmdr.processByConn(conn);
                retbs = cmdr.getReadRes();
            } catch (final Exception e) {
                if (S7Msg.log.isDebugEnabled()) {
                    S7Msg.log.error("", (Throwable) e);
                }
            }
            if (retbs == null) {
                if (!this.chkSuccessiveFailed(true)) {
                    continue;
                }
                this.setAddrError(addrs);
                ret = false;
            } else {
                final int offsetbs = cmdr.getPos();
                this.memTb.setValBlock((long) offsetbs, retbs.length, retbs, 0);
                this.transMem2Addrs(addrs);
                this.chkSuccessiveFailed(false);
            }
        }
        return ret;
    }

    private boolean runReadCmdsErr() {
        final boolean ret = true;
        for (final S7Msg mc : this.cmd2addr.keySet()) {
            final List<S7Addr> addrs = this.cmd2addr.get(mc);
            this.setAddrError(addrs);
        }
        return ret;
    }

    private void runWriteCmdAndClear(final S7TcpConn conn) throws Exception {
        final int s = this.writeCmds.size();
        if (s <= 0) {
            return;
        }
        final S7Msg[] cmds = new S7Msg[s];
        synchronized (this.writeCmds) {
            for (int i = 0; i < s; ++i) {
                cmds[i] = this.writeCmds.removeFirst();
            }
        }
        for (final S7Msg mc : cmds) {
            if (mc.tickCanRun()) {
                Thread.sleep(this.interReqMs);
                conn.clearInputStream(50L);
                mc.processByConn(conn);
            }
        }
        Thread.sleep(this.interReqMs);
    }

    public boolean setWriteCmdAsyn(final S7Addr addr, final Object v) {
        final S7MsgWrite mc = new S7MsgWrite().withParam(this.memTp, this.dbNum, addr, v);
        mc.init(this.ppiDrv);
        synchronized (this.writeCmds) {
            this.writeCmds.addLast(mc);
        }
        return true;
    }
}
