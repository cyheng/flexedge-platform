// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.basic.MemTable;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.driver.common.ModbusAddr;
import cn.doraro.flexedge.driver.common.modbus.sniffer.SnifferCmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ModbusBlock {
    public static final long MAX_Demotion_DELAY = 30000L;
    static ILogger log;

    static {
        ModbusBlock.log = LoggerManager.getLogger("Modbus_Lib");
    }

    int devId;
    short addrTp;
    List<ModbusAddr> addrs;
    int blockSize;
    long scanInterMS;
    MemTable<MemSeg8> memTb;
    transient HashMap<ModbusCmd, List<ModbusAddr>> cmd2addr;
    transient int failedCount;
    transient ModbusCmd.Protocol modbusProtocal;
    private int failedSuccessive;
    private long reqTO;
    private long recvTO;
    private long interReqMs;
    private boolean fw_low32;
    private transient long lastFailedDT;
    private transient int lastFailedCC;
    private transient long lastReadOkDT;
    private LinkedList<ModbusCmd> writeCmds;

    public ModbusBlock(final int devid, final short addrtp, final List<ModbusAddr> addrs, final int block_size, final long scan_inter_ms, final int failed_successive) {
        this.devId = 1;
        this.addrTp = -1;
        this.addrs = null;
        this.blockSize = 32;
        this.scanInterMS = 100L;
        this.memTb = (MemTable<MemSeg8>) new MemTable(8, 131072L);
        this.cmd2addr = new HashMap<ModbusCmd, List<ModbusAddr>>();
        this.failedSuccessive = 3;
        this.reqTO = 1000L;
        this.recvTO = 100L;
        this.interReqMs = 0L;
        this.failedCount = 0;
        this.fw_low32 = true;
        this.modbusProtocal = ModbusCmd.Protocol.rtu;
        this.lastFailedDT = -1L;
        this.lastFailedCC = 0;
        this.lastReadOkDT = -1L;
        this.writeCmds = new LinkedList<ModbusCmd>();
        this.devId = devid;
        if (addrs == null || addrs.size() <= 0) {
            throw new IllegalArgumentException("addr cannot be emtpy");
        }
        if (block_size <= 0) {
            throw new IllegalArgumentException("block cannot <=0 ");
        }
        this.addrTp = addrtp;
        this.addrs = addrs;
        this.blockSize = block_size;
        this.scanInterMS = scan_inter_ms;
        this.failedSuccessive = failed_successive;
    }

    public void setTimingParam(final long req_to, final long recv_to, final long inter_reqms) {
        this.reqTO = req_to;
        this.recvTO = recv_to;
        this.interReqMs = inter_reqms;
    }

    public ModbusBlock asFirstWordLowIn32Bit(final boolean b) {
        this.fw_low32 = b;
        return this;
    }

    public short getAddrTp() {
        return this.addrTp;
    }

    private boolean isBitCmd() {
        switch (this.addrTp) {
            case 48:
            case 49: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private short getFC() {
        switch (this.addrTp) {
            case 49: {
                return 2;
            }
            case 48: {
                return 1;
            }
            case 51: {
                return 4;
            }
            case 52: {
                return 3;
            }
            default: {
                return -1;
            }
        }
    }

    public List<ModbusAddr> getAddrs() {
        return this.addrs;
    }

    public MemTable<MemSeg8> getMemTable() {
        return this.memTb;
    }

    public boolean initReadCmds() {
        if (this.isBitCmd()) {
            return this.initReadCmdsBit();
        }
        return this.initReadCmdsWord();
    }

    private boolean initReadCmdsBit() {
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        ModbusCmd curcmd = null;
        int cur_reg = -1;
        ArrayList<ModbusAddr> curaddrs = null;
        for (final ModbusAddr ma : this.addrs) {
            final int regp = ma.getRegPos();
            if (cur_reg < 0) {
                cur_reg = regp;
                curaddrs = new ArrayList<ModbusAddr>();
                curaddrs.add(ma);
            } else if (ma.getRegPos() <= cur_reg + this.blockSize) {
                curaddrs.add(ma);
            } else {
                final ModbusAddr lastma = curaddrs.get(curaddrs.size() - 1);
                curcmd = new ModbusCmdReadBits(this.getFC(), this.scanInterMS, this.devId, cur_reg, lastma.getRegPos() - cur_reg + 1);
                this.cmd2addr.put(curcmd, curaddrs);
                cur_reg = regp;
                curaddrs = new ArrayList<ModbusAddr>();
                curaddrs.add(ma);
            }
        }
        if (curaddrs.size() > 0) {
            final ModbusAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            curcmd = new ModbusCmdReadBits(this.getFC(), this.scanInterMS, this.devId, cur_reg, lastma2.getRegPos() - cur_reg + 1);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final ModbusCmd mc : this.cmd2addr.keySet()) {
            mc.setRecvTimeout(this.reqTO);
            mc.setRecvEndTimeout(this.recvTO);
            if (ModbusBlock.log.isDebugEnabled()) {
                ModbusBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }

    private boolean initReadCmdsWord() {
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        ModbusCmd curcmd = null;
        int cur_reg = -1;
        ArrayList<ModbusAddr> curaddrs = null;
        for (final ModbusAddr ma : this.addrs) {
            final int regp = ma.getRegPos();
            if (cur_reg < 0) {
                cur_reg = regp;
                curaddrs = new ArrayList<ModbusAddr>();
                curaddrs.add(ma);
            } else {
                final int bytelen = (regp - cur_reg) * 2 + 2;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                } else {
                    final ModbusAddr lastma = curaddrs.get(curaddrs.size() - 1);
                    int regnum = (lastma.getRegPos() - cur_reg) * 2 + lastma.getValTP().getValByteLen();
                    regnum = regnum / 2 + regnum % 2;
                    curcmd = new ModbusCmdReadWords(this.getFC(), this.scanInterMS, this.devId, cur_reg, regnum);
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_reg = regp;
                    curaddrs = new ArrayList<ModbusAddr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final ModbusAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            int regnum2 = (lastma2.getRegPos() - cur_reg) * 2 + lastma2.getValTP().getValByteLen();
            regnum2 = regnum2 / 2 + regnum2 % 2;
            curcmd = new ModbusCmdReadWords(this.getFC(), this.scanInterMS, this.devId, cur_reg, regnum2);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final ModbusCmd mc : this.cmd2addr.keySet()) {
            mc.setRecvTimeout(this.reqTO);
            mc.setRecvEndTimeout(this.recvTO);
            if (ModbusBlock.log.isDebugEnabled()) {
                ModbusBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }

    public void initAsSlave() {
        final boolean bbit = this.isBitCmd();
        for (final ModbusAddr ma : this.addrs) {
            final int regpos = ma.getRegPos();
            final int endpos = ma.getRegEnd();
            final int n = (endpos - regpos) / 2;
            if (bbit) {
                this.memTb.setValBool((long) (regpos / 8), regpos % 8, false);
            } else {
                for (int k = 0; k < n; ++k) {
                    this.memTb.setValNumber(UAVal.ValTP.vt_int16, (long) ((regpos + k) * 2), (Number) 0);
                }
            }
        }
    }

    public void setModbusProtocal(final ModbusCmd.Protocol p) {
        this.modbusProtocal = p;
        for (final ModbusCmd mc : this.cmd2addr.keySet()) {
            mc.setProtocol(p);
        }
    }

    private void setAddrError(final List<ModbusAddr> addrs) {
        if (addrs == null) {
            return;
        }
        for (final ModbusAddr ma : addrs) {
            ma.RT_setValErr();
        }
    }

    private Object getValByAddr(final ModbusAddr da) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return null;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            final int regp = da.getRegPos();
            return this.memTb.getValBool((long) (regp / 8), regp % 8);
        }
        if (!vt.isNumberVT()) {
            return null;
        }
        if (this.fw_low32) {
            return this.memTb.getValNumber(vt, (long) (da.getRegPos() * 2), ByteOrder.ModbusWord);
        }
        return this.memTb.getValNumber(vt, (long) (da.getRegPos() * 2), ByteOrder.LittleEndian);
    }

    public boolean setValByAddr(final ModbusAddr da, final Object v) {
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
            this.memTb.setValBool((long) da.getRegPos(), da.getBitPos(), bv);
            return true;
        }
        if (!vt.isNumberVT()) {
            return false;
        }
        if (!(v instanceof Number)) {
            return false;
        }
        this.memTb.setValNumber(vt, (long) da.getRegPos(), (Number) v);
        return true;
    }

    public boolean runCmds(final IConnEndPoint ep) throws Exception {
        this.runWriteCmdAndClear(ep);
        return this.runReadCmds(ep);
    }

    public void runCmdsErr() {
        this.runReadCmdsErr();
    }

    private void transMem2Addrs(final List<ModbusAddr> addrs) {
        for (final ModbusAddr ma : addrs) {
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

    public void onSnifferCmd(final SnifferCmd sc) {
        if (this.devId != sc.getDevId()) {
            return;
        }
        for (final ModbusCmd mc : this.cmd2addr.keySet()) {
            final List<ModbusAddr> addrs = this.cmd2addr.get(mc);
            this.onSnifferCmd(sc, mc, addrs);
        }
    }

    private void onSnifferCmd(final SnifferCmd sc, final ModbusCmd mc, final List<ModbusAddr> addrs) {
        for (final ModbusAddr maddr : addrs) {
            final Object ov = sc.getValByAddr(maddr);
            if (ov == null) {
                continue;
            }
            maddr.RT_setVal(ov);
        }
    }

    private boolean runReadCmds(final IConnEndPoint ep) throws Exception {
        boolean ret = true;
        for (final ModbusCmd mc : this.cmd2addr.keySet()) {
            if (!mc.tickCanRun()) {
                continue;
            }
            Thread.sleep(this.interReqMs);
            final List<ModbusAddr> addrs = this.cmd2addr.get(mc);
            mc.doCmd(ep.getOutputStream(), ep.getInputStream());
            if (mc instanceof ModbusCmdReadBits) {
                final ModbusCmdReadBits mcb = (ModbusCmdReadBits) mc;
                final boolean[] bvs = mcb.getRetVals();
                if (bvs == null) {
                    if (!this.chkSuccessiveFailed(true)) {
                        continue;
                    }
                    this.setAddrError(addrs);
                    ret = false;
                } else {
                    final int regpos = mcb.getRegAddr();
                    for (int i = 0; i < bvs.length; ++i) {
                        final boolean bv = bvs[i];
                        this.memTb.setValBool((long) ((regpos + i) / 8), (regpos + i) % 8, bv);
                    }
                    this.transMem2Addrs(addrs);
                    this.chkSuccessiveFailed(false);
                    this.lastReadOkDT = System.currentTimeMillis();
                }
            } else {
                if (!(mc instanceof ModbusCmdReadWords)) {
                    continue;
                }
                final ModbusCmdReadWords mcw = (ModbusCmdReadWords) mc;
                final int[] rvs = mcw.getRetVals();
                if (rvs == null) {
                    if (!this.chkSuccessiveFailed(true)) {
                        continue;
                    }
                    this.setAddrError(addrs);
                    ret = false;
                } else {
                    final int regpos = mcw.getRegAddr();
                    for (int i = 0; i < rvs.length; ++i) {
                        final int rv = rvs[i];
                        this.memTb.setValNumber(UAVal.ValTP.vt_int16, (long) ((regpos + i) * 2), (Number) rv);
                    }
                    this.transMem2Addrs(addrs);
                    this.chkSuccessiveFailed(false);
                    this.lastReadOkDT = System.currentTimeMillis();
                }
            }
        }
        return ret;
    }

    public long getLastReadOkDT() {
        return this.lastReadOkDT;
    }

    private boolean runReadCmdsErr() {
        final boolean ret = true;
        for (final ModbusCmd mc : this.cmd2addr.keySet()) {
            final List<ModbusAddr> addrs = this.cmd2addr.get(mc);
            this.setAddrError(addrs);
        }
        return ret;
    }

    private void runWriteCmdAndClear(final IConnEndPoint ep) throws Exception {
        final int s = this.writeCmds.size();
        if (s <= 0) {
            return;
        }
        final ModbusCmd[] cmds = new ModbusCmd[s];
        synchronized (this.writeCmds) {
            for (int i = 0; i < s; ++i) {
                cmds[i] = this.writeCmds.removeFirst();
            }
        }
        for (final ModbusCmd mc : cmds) {
            if (mc.tickCanRun()) {
                Thread.sleep(this.interReqMs);
                mc.doCmd(ep.getOutputStream(), ep.getInputStream());
            }
        }
    }

    public boolean setWriteCmdAsyn(final ModbusAddr ma, final Object v) {
        ModbusCmd mc = null;
        switch (ma.getAddrTp()) {
            case 48: {
                final boolean[] bvs = {(boolean) v};
                mc = new ModbusCmdWriteBit(this.scanInterMS, this.devId, ma.getRegPos(), (boolean) v);
                mc.setRecvTimeout(this.reqTO);
                mc.setRecvEndTimeout(this.recvTO);
                break;
            }
            case 52: {
                if (!(v instanceof Number)) {
                    return false;
                }
                final Number nv = (Number) v;
                final UAVal.ValTP vt = ma.getValTP();
                final int dlen = vt.getValByteLen() / 2;
                if (dlen < 1) {
                    return false;
                }
                int[] vals = null;
                switch (vt) {
                    case vt_int16:
                    case vt_uint16: {
                        vals = new int[]{nv.shortValue()};
                        break;
                    }
                    case vt_int32:
                    case vt_uint32: {
                        vals = new int[2];
                        final int intv = nv.intValue();
                        vals[1] = (intv >> 16 & 0xFFFF);
                        vals[0] = (intv & 0xFFFF);
                        break;
                    }
                    case vt_int64:
                    case vt_uint64: {
                        vals = new int[4];
                        final long longv = nv.longValue();
                        vals[3] = (int) (longv >> 48 & 0xFFFFL);
                        vals[2] = (int) (longv >> 32 & 0xFFFFL);
                        vals[1] = (int) (longv >> 16 & 0xFFFFL);
                        vals[0] = (int) (longv & 0xFFFFL);
                        break;
                    }
                    case vt_float: {
                        vals = new int[2];
                        final int intv = Float.floatToIntBits(nv.floatValue());
                        vals[0] = (intv >> 16 & 0xFFFF);
                        vals[1] = (intv & 0xFFFF);
                        break;
                    }
                    case vt_double: {
                        vals = new int[4];
                        final long longv = Double.doubleToLongBits(nv.doubleValue());
                        vals[0] = (int) (longv >> 48 & 0xFFFFL);
                        vals[1] = (int) (longv >> 32 & 0xFFFFL);
                        vals[2] = (int) (longv >> 16 & 0xFFFFL);
                        vals[3] = (int) (longv & 0xFFFFL);
                        break;
                    }
                    default: {
                        return false;
                    }
                }
                if (vals.length == 1) {
                    mc = new ModbusCmdWriteWord(this.scanInterMS, this.devId, ma.getRegPos(), vals[0]);
                } else {
                    mc = new ModbusCmdWriteWords(this.scanInterMS, this.devId, ma.getRegPos(), vals);
                }
                mc.setRecvTimeout(this.reqTO);
                mc.setRecvEndTimeout(this.recvTO);
                break;
            }
            default: {
                return false;
            }
        }
        mc.setProtocol(this.modbusProtocal);
        synchronized (this.writeCmds) {
            this.writeCmds.addLast(mc);
        }
        return true;
    }
}
