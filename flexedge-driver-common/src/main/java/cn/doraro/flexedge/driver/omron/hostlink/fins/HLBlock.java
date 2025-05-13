

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.basic.MemTable;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.driver.omron.hostlink.HLAddr;
import cn.doraro.flexedge.driver.omron.hostlink.HLAddrSeg;

import java.util.*;

public class HLBlock {
    public static final long MAX_Demotion_DELAY = 30000L;
    static ILogger log;

    static {
        HLBlock.log = LoggerManager.getLogger("HL_Block");
    }

    HLDevItem devItem;
    String prefix;
    List<HLAddr> addrs;
    int blockSize;
    long scanInterMS;
    MemTable<MemSeg8> memTb;
    transient HashMap<HLCmd, List<HLAddr>> cmd2addr;
    transient int failedCount;
    private int failedSuccessive;
    private long reqTO;
    private long interReqMs;
    private transient long lastFailedDT;
    private transient int lastFailedCC;
    private transient long lastWriteFailedDT;
    private transient String lastWriteFailedInf;
    private transient HLFinsDriver driver;
    private transient HLAddrSeg addrSeg;
    private LinkedList<HLCmd> writeCmds;

    HLBlock(final HLDevItem devitem, final HLAddrSeg seg, final List<HLAddr> addrs, final int block_size, final long scan_inter_ms, final int failed_successive) {
        this.devItem = null;
        this.prefix = null;
        this.addrs = null;
        this.blockSize = 32;
        this.scanInterMS = 100L;
        this.memTb = (MemTable<MemSeg8>) new MemTable(8, 131072L);
        this.cmd2addr = new HashMap<HLCmd, List<HLAddr>>();
        this.failedSuccessive = 3;
        this.reqTO = 2000L;
        this.interReqMs = 0L;
        this.failedCount = 0;
        this.lastFailedDT = -1L;
        this.lastFailedCC = 0;
        this.lastWriteFailedDT = -1L;
        this.lastWriteFailedInf = null;
        this.driver = null;
        this.addrSeg = null;
        this.writeCmds = new LinkedList<HLCmd>();
        this.devItem = devitem;
        if (addrs == null || addrs.size() <= 0) {
            throw new IllegalArgumentException("addr cannot be emtpy");
        }
        if (block_size <= 0) {
            throw new IllegalArgumentException("block cannot <=0 ");
        }
        this.addrSeg = seg;
        this.addrs = addrs;
        this.blockSize = block_size;
        this.scanInterMS = scan_inter_ms;
        this.failedSuccessive = failed_successive;
    }

    public void setTimingParam(final long req_to, final long inter_reqms) {
        this.reqTO = req_to;
        this.interReqMs = inter_reqms;
    }

    public List<HLAddr> getAddrs() {
        return this.addrs;
    }

    public MemTable<MemSeg8> getMemTable() {
        return this.memTb;
    }

    public long getReqTimeout() {
        return this.reqTO;
    }

    public long getInterReqMS() {
        return this.interReqMs;
    }

    protected boolean initCmds(final HLFinsDriver drv) {
        this.driver = drv;
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        final HLAddr fxaddr = this.addrs.get(0);
        HLCmd curcmd = null;
        int cur_reg = -1;
        ArrayList<HLAddr> curaddrs = null;
        final boolean bbit_only = this.addrSeg.isValBitOnly();
        for (final HLAddr ma : this.addrs) {
            final int regp = ma.getAddrNum();
            if (cur_reg < 0) {
                cur_reg = regp;
                curaddrs = new ArrayList<HLAddr>();
                curaddrs.add(ma);
            } else {
                final int bytelen = regp - cur_reg + 1;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                } else {
                    final HLAddr lastma = curaddrs.get(curaddrs.size() - 1);
                    int blen = lastma.getValTP().getValByteLen() - 2;
                    if (blen < 0) {
                        blen = 0;
                    }
                    final int regnum = lastma.getAddrNum() - cur_reg + 1 + blen;
                    curcmd = new HLFinsCmdMemR(cur_reg, regnum, bbit_only).withScanIntervalMS(this.scanInterMS);
                    curcmd.initCmd(drv, this);
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_reg = regp;
                    curaddrs = new ArrayList<HLAddr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final HLAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            int blen2 = lastma2.getValTP().getValByteLen() - 2;
            if (blen2 < 0) {
                blen2 = 0;
            }
            final int regnum2 = lastma2.getAddrNum() - cur_reg + 1 + blen2;
            curcmd = new HLFinsCmdMemR(cur_reg, regnum2, bbit_only).withScanIntervalMS(this.scanInterMS);
            curcmd.initCmd(drv, this);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final HLCmd mc : this.cmd2addr.keySet()) {
            mc.withRecvTimeout(this.reqTO, this.failedSuccessive);
            if (HLBlock.log.isDebugEnabled()) {
                HLBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }

    private void setAddrError(final List<HLAddr> addrs) {
        if (addrs == null) {
            return;
        }
        for (final HLAddr ma : addrs) {
            ma.RT_setVal((Object) null);
        }
    }

    private Object getValByAddr(final HLAddr da) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return null;
        }
        final int regp = da.getAddrNum() * 2;
        final int inbit = da.getBitNum();
        if (vt == UAVal.ValTP.vt_bool) {
            if (inbit >= 0) {
                final int vv = this.memTb.getValNumber(UAVal.ValTP.vt_int16, (long) regp, ByteOrder.LittleEndian).intValue();
                return (vv & 1 << inbit) > 0;
            }
            if (this.addrSeg.isValBitOnly()) {
                final byte bv = this.memTb.getValNumber(UAVal.ValTP.vt_byte, (long) da.getAddrNum()).byteValue();
                return bv != 0;
            }
        } else if (vt.isNumberVT()) {
            final Number nbv = this.memTb.getValNumber(vt, (long) regp, ByteOrder.LittleEndian);
            return nbv;
        }
        return null;
    }

    public byte[] transValToBytesByAddr(final HLAddr da, final Object v, final StringBuilder failedr) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            failedr.append("no valtp in addr");
            return null;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            failedr.append("vt_bool is not supported");
            return null;
        }
        if (!vt.isNumberVT()) {
            failedr.append("valtp is not number");
            return null;
        }
        int intv;
        if (v instanceof Number) {
            intv = ((Number) v).intValue();
        } else {
            if (!(v instanceof String)) {
                failedr.append("invalid val,it must be number");
                return null;
            }
            intv = Integer.parseInt("" + v);
        }
        final int blen = vt.getValByteLen();
        if (blen == 4) {
            final byte[] rets = new byte[4];
            DataUtil.intToBytes(intv, rets, 0, ByteOrder.BigEndian);
            return rets;
        }
        if (blen == 2) {
            final byte[] rets = new byte[2];
            DataUtil.shortToBytes((short) intv, rets, 0, ByteOrder.BigEndian);
            return rets;
        }
        failedr.append("valtp is not 2 or 4");
        return null;
    }

    public List<Short> transValToWordsByAddr(final HLAddr da, final Object v, final StringBuilder failedr) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            failedr.append("no valtp in addr");
            return null;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            failedr.append("vt_bool is not supported");
            return null;
        }
        if (!vt.isNumberVT()) {
            failedr.append("valtp is not number");
            return null;
        }
        int intv;
        if (v instanceof Number) {
            intv = ((Number) v).intValue();
        } else {
            if (!(v instanceof String)) {
                failedr.append("invalid val,it must be number");
                return null;
            }
            intv = Integer.parseInt("" + v);
        }
        final int blen = vt.getValByteLen();
        if (blen == 4) {
            final short hv = (short) (intv >> 16 & 0xFFFF);
            final short lv = (short) (intv & 0xFFFF);
            return Arrays.asList(hv, lv);
        }
        if (blen == 2) {
            return Arrays.asList((short) intv);
        }
        failedr.append("valtp is not 2 or 4");
        return null;
    }

    public boolean runCmds(final IConnEndPoint ep, final StringBuilder failedr) throws Exception {
        this.runWriteCmdAndClear(ep);
        return this.runReadCmds(ep, failedr);
    }

    public void runCmdsErr() {
        this.runReadCmdsErr();
    }

    private void transMem2Addrs(final List<HLAddr> addrs) {
        for (final HLAddr ma : addrs) {
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

    private boolean runReadCmds(final IConnEndPoint ep, final StringBuilder failedr) throws Exception {
        boolean ret = true;
        final boolean bbit_only = this.addrSeg.isValBitOnly();
        for (final HLCmd mc : this.cmd2addr.keySet()) {
            if (!mc.tickCanRun()) {
                continue;
            }
            final HLFinsCmdMemR cmdr = (HLFinsCmdMemR) mc;
            Thread.sleep(this.interReqMs);
            final List<HLAddr> addrs = this.cmd2addr.get(mc);
            final boolean cmdres = cmdr.doCmd(ep.getInputStream(), ep.getOutputStream(), failedr);
            final HLFinsRespMemR resp = cmdr.getResp();
            final HLFinsReqMemR req = cmdr.getReq();
            byte[] retbs = null;
            retbs = null;
            if (cmdres && resp != null) {
                retbs = resp.getReturnBytes();
            }
            if (retbs == null) {
                if (!this.chkSuccessiveFailed(true)) {
                    continue;
                }
                this.setAddrError(addrs);
                ret = false;
            } else {
                final int offsetbs = req.getBeginAddr();
                if (bbit_only) {
                    for (int i = 0; i < retbs.length; ++i) {
                        final byte b = retbs[i];
                        this.memTb.setValNumber(UAVal.ValTP.vt_byte, (long) (offsetbs + i), (Number) b);
                    }
                } else {
                    this.memTb.setValBlock((long) (offsetbs * 2), retbs.length, retbs, 0);
                }
                this.transMem2Addrs(addrs);
                this.chkSuccessiveFailed(false);
            }
        }
        return ret;
    }

    private boolean runReadCmdsErr() {
        final boolean ret = true;
        for (final HLCmd mc : this.cmd2addr.keySet()) {
            final List<HLAddr> addrs = this.cmd2addr.get(mc);
            this.setAddrError(addrs);
        }
        return ret;
    }

    private void runWriteCmdAndClear(final IConnEndPoint ep) throws Exception {
        final int s = this.writeCmds.size();
        if (s <= 0) {
            return;
        }
        final HLCmd[] cmds = new HLCmd[s];
        synchronized (this.writeCmds) {
            for (int i = 0; i < s; ++i) {
                cmds[i] = this.writeCmds.removeFirst();
            }
        }
        for (final HLCmd mc : cmds) {
            if (mc.tickCanRun()) {
                Thread.sleep(this.interReqMs);
                final StringBuilder failedr = new StringBuilder();
                final boolean res = mc.doCmd(ep.getInputStream(), ep.getOutputStream(), failedr);
                if (!res) {
                    HLBlock.log.error(failedr.toString());
                }
                if (mc instanceof HLFinsCmdMemW) {
                    final HLFinsCmdMemW fcw = (HLFinsCmdMemW) mc;
                    if (!fcw.isAck()) {
                        HLBlock.log.error(fcw.toString() + " is not ack");
                    } else if (HLBlock.log.isDebugEnabled()) {
                        HLBlock.log.debug(fcw.toString() + " is run ok (ack=true)");
                    }
                }
            }
        }
    }

    public boolean setWriteCmdAsyn(final HLAddr fxaddr, final Object v) {
        if (!this.addrSeg.matchAddr(fxaddr)) {
            return false;
        }
        HLFinsCmdMemW fxcmd = null;
        if (fxaddr.isBitVal()) {
            boolean bv;
            if (v instanceof Boolean) {
                bv = (boolean) v;
            } else if (v instanceof Number) {
                bv = (((Number) v).intValue() > 0);
            } else {
                if (!(v instanceof String)) {
                    return false;
                }
                bv = ("true".equalsIgnoreCase((String) v) || "1".equalsIgnoreCase((String) v));
            }
            fxcmd = new HLFinsCmdMemW();
            fxcmd.withScanIntervalMS(this.scanInterMS);
            if (this.addrSeg.isValBitOnly()) {
                fxcmd.asBitOnlyVals(fxaddr.getAddrNum(), Arrays.asList(bv));
            } else {
                fxcmd.asBitVals(fxaddr.getAddrNum(), fxaddr.getBitNum(), Arrays.asList(bv));
            }
        } else {
            final StringBuilder failedr = new StringBuilder();
            final List<Short> ws = this.transValToWordsByAddr(fxaddr, v, failedr);
            if (ws == null) {
                this.lastWriteFailedDT = System.currentTimeMillis();
                this.lastWriteFailedInf = failedr.toString();
                return false;
            }
            fxcmd = new HLFinsCmdMemW();
            fxcmd.withScanIntervalMS(this.scanInterMS);
            fxcmd.asWordVals(fxaddr.getAddrNum(), ws);
        }
        fxcmd.initCmd(this.driver, this);
        synchronized (this.writeCmds) {
            this.writeCmds.addLast(fxcmd);
        }
        return true;
    }

    public long getLastWriteFailedDT() {
        return this.lastWriteFailedDT;
    }

    public String getLastWriteFailedInf() {
        return this.lastWriteFailedInf;
    }
}
