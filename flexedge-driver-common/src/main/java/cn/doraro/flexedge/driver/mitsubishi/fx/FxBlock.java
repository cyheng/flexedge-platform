// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.UAVal;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.basic.MemTable;
import java.util.List;
import cn.doraro.flexedge.core.util.logger.ILogger;

public class FxBlock
{
    public static final long MAX_Demotion_DELAY = 30000L;
    static ILogger log;
    String prefix;
    List<FxAddr> addrs;
    int blockSize;
    long scanInterMS;
    MemTable<MemSeg8> memTb;
    transient HashMap<FxCmd, List<FxAddr>> cmd2addr;
    private int failedSuccessive;
    private long reqTO;
    private long recvTO;
    private long interReqMs;
    transient int failedCount;
    private transient long lastFailedDT;
    private transient int lastFailedCC;
    private transient long lastWriteFailedDT;
    private transient String lastWriteFailedInf;
    private transient FxDriver fxDrv;
    private transient FxAddrSeg fxSeg;
    private LinkedList<FxCmd> writeCmds;
    
    FxBlock(final FxAddrSeg seg, final List<FxAddr> addrs, final int block_size, final long scan_inter_ms) {
        this.prefix = null;
        this.addrs = null;
        this.blockSize = 32;
        this.scanInterMS = 100L;
        this.memTb = (MemTable<MemSeg8>)new MemTable(8, 131072L);
        this.cmd2addr = new HashMap<FxCmd, List<FxAddr>>();
        this.failedSuccessive = 3;
        this.reqTO = 1000L;
        this.recvTO = 100L;
        this.interReqMs = 0L;
        this.failedCount = 0;
        this.lastFailedDT = -1L;
        this.lastFailedCC = 0;
        this.lastWriteFailedDT = -1L;
        this.lastWriteFailedInf = null;
        this.fxDrv = null;
        this.fxSeg = null;
        this.writeCmds = new LinkedList<FxCmd>();
        if (addrs == null || addrs.size() <= 0) {
            throw new IllegalArgumentException("addr cannot be emtpy");
        }
        if (block_size <= 0) {
            throw new IllegalArgumentException("block cannot <=0 ");
        }
        this.fxSeg = seg;
        this.addrs = addrs;
        this.blockSize = block_size;
        this.scanInterMS = scan_inter_ms;
    }
    
    public void setTimingParam(final long req_to, final long recv_to, final long inter_reqms) {
        this.reqTO = req_to;
        this.recvTO = recv_to;
        this.interReqMs = inter_reqms;
    }
    
    public List<FxAddr> getAddrs() {
        return this.addrs;
    }
    
    public MemTable<MemSeg8> getMemTable() {
        return this.memTb;
    }
    
    boolean initCmds(final FxDriver drv) {
        this.fxDrv = drv;
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        final FxAddr fxaddr = this.addrs.get(0);
        int base_addr = -1;
        if (this.fxSeg.isExtCmd()) {
            base_addr = fxaddr.addrSeg.extBaseValStart;
        }
        else {
            base_addr = fxaddr.addrSeg.getBaseAddr();
        }
        if (base_addr < 0) {
            return false;
        }
        FxCmd curcmd = null;
        int cur_reg = -1;
        ArrayList<FxAddr> curaddrs = null;
        for (final FxAddr ma : this.addrs) {
            final int regp = ma.getBytesInBase();
            if (cur_reg < 0) {
                cur_reg = regp;
                curaddrs = new ArrayList<FxAddr>();
                curaddrs.add(ma);
            }
            else {
                final int bytelen = regp - cur_reg + 1;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                }
                else {
                    final FxAddr lastma = curaddrs.get(curaddrs.size() - 1);
                    final int regnum = lastma.getBytesInBase() - cur_reg + lastma.getValTP().getValByteLen();
                    curcmd = new FxCmdR(base_addr, cur_reg, regnum).withScanIntervalMS(this.scanInterMS);
                    curcmd.initCmd(drv, this.fxSeg.isExtCmd());
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_reg = regp;
                    curaddrs = new ArrayList<FxAddr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final FxAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            final int regnum2 = lastma2.getBytesInBase() - cur_reg + lastma2.getValTP().getValByteLen();
            curcmd = new FxCmdR(base_addr, cur_reg, regnum2).withScanIntervalMS(this.scanInterMS);
            curcmd.initCmd(drv, this.fxSeg.isExtCmd());
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final FxCmd mc : this.cmd2addr.keySet()) {
            mc.withRecvTimeout(this.reqTO).withRecvEndTimeout(this.recvTO);
            if (FxBlock.log.isDebugEnabled()) {
                FxBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }
    
    private void setAddrError(final List<FxAddr> addrs) {
        if (addrs == null) {
            return;
        }
        for (final FxAddr ma : addrs) {
            ma.RT_setVal((Object)null);
        }
    }
    
    private Object getValByAddr(final FxAddr da) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return null;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            final int regp = da.getBytesInBase();
            final int inbit = da.getInBits();
            final int vv = this.memTb.getValNumber(UAVal.ValTP.vt_byte, (long)regp, ByteOrder.LittleEndian).intValue();
            return (vv & 1 << inbit) > 0;
        }
        if (vt.isNumberVT()) {
            final Number nbv = this.memTb.getValNumber(vt, (long)da.getBytesInBase(), ByteOrder.BigEndian);
            return nbv;
        }
        return null;
    }
    
    public byte[] transValToBytesByAddr(final FxAddr da, final Object v, final StringBuilder failedr) {
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
            intv = ((Number)v).intValue();
        }
        else {
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
            DataUtil.shortToBytes((short)intv, rets, 0, ByteOrder.BigEndian);
            return rets;
        }
        failedr.append("valtp is not 2 or 4");
        return null;
    }
    
    public boolean setValByAddr(final FxAddr da, final Object v) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return false;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            boolean bv = false;
            if (v instanceof Boolean) {
                bv = (boolean)v;
            }
            else {
                if (!(v instanceof Number)) {
                    return false;
                }
                bv = (((Number)v).doubleValue() > 0.0);
            }
            this.memTb.setValBool((long)da.getBytesInBase(), da.getInBits(), bv);
            return true;
        }
        if (!vt.isNumberVT()) {
            return false;
        }
        if (!(v instanceof Number)) {
            return false;
        }
        this.memTb.setValNumber(vt, (long)da.getBytesInBase(), (Number)v);
        return true;
    }
    
    public boolean runCmds(final IConnEndPoint ep) throws Exception {
        this.runWriteCmdAndClear(ep);
        return this.runReadCmds(ep);
    }
    
    public void runCmdsErr() {
        this.runReadCmdsErr();
    }
    
    private void transMem2Addrs(final List<FxAddr> addrs) {
        for (final FxAddr ma : addrs) {
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
        for (final FxCmd mc : this.cmd2addr.keySet()) {
            if (!mc.tickCanRun()) {
                continue;
            }
            final FxCmdR cmdr = (FxCmdR)mc;
            Thread.sleep(this.interReqMs);
            final List<FxAddr> addrs = this.cmd2addr.get(mc);
            cmdr.doCmd(ep.getInputStream(), ep.getOutputStream());
            final FxMsgRespR resp = cmdr.getResp();
            final FxMsgReqR req = cmdr.getReq();
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
            }
            else {
                this.memTb.setValBlock((long)offsetbs, retbs.length, retbs, 0);
                this.transMem2Addrs(addrs);
                this.chkSuccessiveFailed(false);
            }
        }
        return ret;
    }
    
    private boolean runReadCmdsErr() {
        final boolean ret = true;
        for (final FxCmd mc : this.cmd2addr.keySet()) {
            final List<FxAddr> addrs = this.cmd2addr.get(mc);
            this.setAddrError(addrs);
        }
        return ret;
    }
    
    private void runWriteCmdAndClear(final IConnEndPoint ep) throws Exception {
        final int s = this.writeCmds.size();
        if (s <= 0) {
            return;
        }
        final FxCmd[] cmds = new FxCmd[s];
        synchronized (this.writeCmds) {
            for (int i = 0; i < s; ++i) {
                cmds[i] = this.writeCmds.removeFirst();
            }
        }
        for (final FxCmd mc : cmds) {
            if (mc.tickCanRun()) {
                Thread.sleep(this.interReqMs);
                mc.doCmd(ep.getInputStream(), ep.getOutputStream());
                if (mc instanceof FxCmdW) {
                    final FxCmdW fcw = (FxCmdW)mc;
                    if (!fcw.isAck()) {
                        FxBlock.log.error(fcw.toString() + " is not ack");
                    }
                    else if (FxBlock.log.isDebugEnabled()) {
                        FxBlock.log.debug(fcw.toString() + " is run ok (ack=true)");
                    }
                }
            }
        }
    }
    
    public boolean setWriteCmdAsyn(final FxAddr fxaddr, final Object v) {
        if (!this.fxSeg.matchAddr(fxaddr)) {
            return false;
        }
        FxCmd fxcmd = null;
        if (this.fxSeg.isValBit()) {
            int base_addr = -1;
            if (this.fxSeg.isExtCmd()) {
                base_addr = fxaddr.addrSeg.extBaseAddrForceOnOff;
            }
            else {
                base_addr = fxaddr.addrSeg.baseAddrForceOnOff;
            }
            if (base_addr < 0) {
                return false;
            }
            boolean bv;
            if (v instanceof Boolean) {
                bv = (boolean)v;
            }
            else if (v instanceof Number) {
                bv = (((Number)v).intValue() > 0);
            }
            else {
                if (!(v instanceof String)) {
                    return false;
                }
                bv = ("true".equalsIgnoreCase((String)v) || "1".equalsIgnoreCase((String)v));
            }
            final int addrn = fxaddr.getAddrNum();
            fxcmd = new FxCmdOnOff(base_addr, addrn, bv);
        }
        else {
            int base_addr = -1;
            if (this.fxSeg.isExtCmd()) {
                base_addr = fxaddr.addrSeg.extBaseValStart;
            }
            else {
                base_addr = fxaddr.addrSeg.getBaseAddr();
            }
            if (base_addr < 0) {
                return false;
            }
            final int regp = fxaddr.getBytesInBase();
            final StringBuilder failedr = new StringBuilder();
            final byte[] bs = this.transValToBytesByAddr(fxaddr, v, failedr);
            if (bs == null) {
                this.lastWriteFailedDT = System.currentTimeMillis();
                this.lastWriteFailedInf = failedr.toString();
                return false;
            }
            fxcmd = new FxCmdW(base_addr, regp, bs);
        }
        fxcmd.initCmd(this.fxDrv, this.fxSeg.isExtCmd());
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
    
    static {
        FxBlock.log = LoggerManager.getLogger("Fx_Block");
    }
}
