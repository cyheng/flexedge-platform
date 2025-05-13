

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.basic.MemTable;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;

import java.util.*;

public class MCBlock {
    public static final long MAX_Demotion_DELAY = 30000L;
    static ILogger log;

    static {
        MCBlock.log = LoggerManager.getLogger((Class) MCBlock.class);
    }

    String prefix;
    MCCode code;
    List<MCAddr> addrs;
    int blockSize;
    long scanInterMS;
    MemTable<MemSeg8> memTb;
    transient HashMap<MCCmd, List<MCAddr>> cmd2addr;
    transient int failedCount;
    private int failedSuccessive;
    private long reqTO;
    private long recvTO;
    private long interReqMs;
    private transient long lastFailedDT;
    private transient int lastFailedCC;
    private transient long lastWriteFailedDT;
    private transient String lastWriteFailedInf;
    private transient MCEthDriver fxDrv;
    private transient MCDevItem devItem;
    private transient MCAddrSeg fxSeg;
    private transient long lastReadOkDT;
    private LinkedList<MCCmd> writeCmds;

    MCBlock(final MCCode code, final MCAddrSeg seg, final List<MCAddr> addrs, final int block_size, final long scan_inter_ms) {
        this.prefix = null;
        this.code = null;
        this.addrs = null;
        this.blockSize = 128;
        this.scanInterMS = 100L;
        this.memTb = (MemTable<MemSeg8>) new MemTable(8, 131072L);
        this.cmd2addr = new HashMap<MCCmd, List<MCAddr>>();
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
        this.devItem = null;
        this.fxSeg = null;
        this.lastReadOkDT = -1L;
        this.writeCmds = new LinkedList<MCCmd>();
        if (addrs == null || addrs.size() <= 0) {
            throw new IllegalArgumentException("addr cannot be emtpy");
        }
        if (block_size <= 0) {
            throw new IllegalArgumentException("block cannot <=0 ");
        }
        this.code = code;
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

    public MCCode getMCCode() {
        return this.code;
    }

    public List<MCAddr> getAddrs() {
        return this.addrs;
    }

    public MemTable<MemSeg8> getMemTable() {
        return this.memTb;
    }

    public Map<MCCmd, List<MCAddr>> getCmd2AddrMap() {
        return this.cmd2addr;
    }

    boolean initCmds(final MCEthDriver drv, final MCDevItem devitem) {
        this.fxDrv = drv;
        this.devItem = devitem;
        if (this.addrs == null || this.addrs.size() <= 0) {
            return false;
        }
        if (this.code.tp == MCCode.TP.bit) {
            return this.initCmdsBit();
        }
        return this.initCmdsWord();
    }

    private boolean initCmdsBit() {
        MCCmd curcmd = null;
        int cur_bit_idx = -1;
        ArrayList<MCAddr> curaddrs = null;
        for (final MCAddr ma : this.addrs) {
            final int bitidx = ma.getAddrIdx();
            if (cur_bit_idx < 0) {
                cur_bit_idx = bitidx / 16 * 16;
                curaddrs = new ArrayList<MCAddr>();
                curaddrs.add(ma);
            } else {
                int d_bit = bitidx - cur_bit_idx;
                final int bytelen = d_bit / 16 * 2 + ((d_bit % 16 > 0) ? 2 : 0) + 2;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                } else {
                    final MCAddr lastma = curaddrs.get(curaddrs.size() - 1);
                    final int last_bit_idx = lastma.getAddrIdx() + lastma.getValTP().getValByteLen() * 8;
                    d_bit = last_bit_idx - cur_bit_idx;
                    final int word_regnum = d_bit / 16 + ((d_bit % 16 > 0) ? 1 : 0) + 1;
                    curcmd = new MCCmdR(this.code, cur_bit_idx, word_regnum, this.devItem.commFmtAscii).withScanIntervalMS(this.scanInterMS).withRecvTimeout(this.recvTO);
                    curcmd.initCmd(this.fxDrv);
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_bit_idx = bitidx / 16 * 16;
                    curaddrs = new ArrayList<MCAddr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final MCAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            final int last_bit_idx2 = lastma2.getAddrIdx() + lastma2.getValTP().getValByteLen() * 8;
            final int d_bit2 = last_bit_idx2 - cur_bit_idx;
            final int word_regnum2 = d_bit2 / 16 + ((d_bit2 % 16 > 0) ? 1 : 0) + 1;
            curcmd = new MCCmdR(this.code, cur_bit_idx, word_regnum2, this.devItem.commFmtAscii).withScanIntervalMS(this.scanInterMS).withRecvTimeout(this.recvTO);
            curcmd.initCmd(this.fxDrv);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final MCCmd mc : this.cmd2addr.keySet()) {
            mc.withRecvTimeout(this.reqTO).withRecvEndTimeout(this.recvTO);
            if (MCBlock.log.isDebugEnabled()) {
                MCBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }

    private boolean initCmdsWord() {
        MCCmd curcmd = null;
        int cur_idx = -1;
        ArrayList<MCAddr> curaddrs = null;
        for (final MCAddr ma : this.addrs) {
            final int idx = ma.getAddrIdx();
            if (cur_idx < 0) {
                cur_idx = idx;
                curaddrs = new ArrayList<MCAddr>();
                curaddrs.add(ma);
            } else {
                int d_wd = idx - cur_idx;
                final int bytelen = d_wd * 2 + 2;
                if (bytelen <= this.blockSize) {
                    curaddrs.add(ma);
                } else {
                    final MCAddr lastma = curaddrs.get(curaddrs.size() - 1);
                    final int last_idx = lastma.getAddrIdx() + lastma.getValTP().getValByteLen() / 2 - 1;
                    d_wd = last_idx - cur_idx;
                    final int word_regnum = d_wd + 1;
                    curcmd = new MCCmdR(this.code, cur_idx, word_regnum, this.devItem.commFmtAscii).withScanIntervalMS(this.scanInterMS).withRecvTimeout(this.recvTO);
                    curcmd.initCmd(this.fxDrv);
                    this.cmd2addr.put(curcmd, curaddrs);
                    cur_idx = idx;
                    curaddrs = new ArrayList<MCAddr>();
                    curaddrs.add(ma);
                }
            }
        }
        if (curaddrs.size() > 0) {
            final MCAddr lastma2 = curaddrs.get(curaddrs.size() - 1);
            final int last_idx2 = lastma2.getAddrIdx() + lastma2.getValTP().getValByteLen() / 2;
            final int d_wd2 = last_idx2 - cur_idx;
            final int word_regnum2 = d_wd2 + 1;
            curcmd = new MCCmdR(this.code, cur_idx, word_regnum2, this.devItem.commFmtAscii).withScanIntervalMS(this.scanInterMS).withRecvTimeout(this.recvTO);
            curcmd.initCmd(this.fxDrv);
            this.cmd2addr.put(curcmd, curaddrs);
        }
        for (final MCCmd mc : this.cmd2addr.keySet()) {
            mc.withRecvTimeout(this.reqTO).withRecvEndTimeout(this.recvTO);
            if (MCBlock.log.isDebugEnabled()) {
                MCBlock.log.debug("init modbus cmd=" + mc);
            }
        }
        return true;
    }

    private void setAddrError(final List<MCAddr> addrs, final String errinf) {
        if (addrs == null) {
            return;
        }
        for (final MCAddr ma : addrs) {
            ma.RT_setValErr(errinf);
        }
    }

    private Object getValByAddr(final MCAddr da) {
        final UAVal.ValTP vt = da.getValTP();
        if (vt == null) {
            return null;
        }
        if (this.code.isBitTp()) {
            if (vt == UAVal.ValTP.vt_bool) {
                final int addr_idx = da.getAddrIdx();
                final int byten = addr_idx / 8;
                final int inbit = addr_idx % 8;
                final int vv = this.memTb.getValNumber(UAVal.ValTP.vt_byte, (long) byten, ByteOrder.BigEndian).intValue();
                if ((vv & 1 << inbit) > 0) {
                    return true;
                }
                return false;
            } else if (vt.isNumberVT()) {
                final int addr_idx = da.getAddrIdx();
                final int byten = addr_idx / 8;
                final int inbit = addr_idx % 8;
                switch (vt) {
                    case vt_byte: {
                        short sv = this.memTb.getValNumber(UAVal.ValTP.vt_int16, (long) byten, ByteOrder.BigEndian).shortValue();
                        sv >>>= (short) inbit;
                        sv &= 0xFF;
                        return (byte) sv;
                    }
                    case vt_char: {
                        short sv = this.memTb.getValNumber(UAVal.ValTP.vt_int16, (long) byten, ByteOrder.BigEndian).shortValue();
                        sv >>>= (short) inbit;
                        sv &= 0xFF;
                        return sv;
                    }
                    case vt_int16: {
                        int iv = this.memTb.getValNumber(UAVal.ValTP.vt_int32, (long) byten, ByteOrder.BigEndian).intValue();
                        iv >>>= inbit;
                        iv &= 0xFFFF;
                        return (short) iv;
                    }
                    case vt_uint16: {
                        int iv = this.memTb.getValNumber(UAVal.ValTP.vt_int32, (long) byten, ByteOrder.BigEndian).intValue();
                        iv >>>= inbit;
                        iv &= 0xFFFF;
                        return iv;
                    }
                    case vt_int32: {
                        int lv1 = this.memTb.getValNumber(UAVal.ValTP.vt_int32, (long) byten, ByteOrder.BigEndian).shortValue();
                        lv1 &= 0xFFFF;
                        int lv2 = this.memTb.getValNumber(UAVal.ValTP.vt_int32, (long) (byten + 2), ByteOrder.BigEndian).shortValue();
                        lv2 = (lv1 & 0xFFFF);
                        long lv3 = lv2;
                        lv3 <<= 16;
                        lv3 += lv2;
                        lv3 >>>= inbit;
                        lv3 &= -1L;
                        return (int) lv3;
                    }
                    case vt_uint32: {
                        int lv1 = this.memTb.getValNumber(UAVal.ValTP.vt_int32, (long) byten, ByteOrder.BigEndian).shortValue();
                        lv1 &= 0xFFFF;
                        int lv2 = this.memTb.getValNumber(UAVal.ValTP.vt_int32, (long) (byten + 2), ByteOrder.BigEndian).shortValue();
                        lv2 &= 0xFFFF;
                        long lv3 = lv2;
                        lv3 <<= 16;
                        lv3 += lv1;
                        lv3 >>>= inbit;
                        lv3 &= -1L;
                        return lv3;
                    }
                    default: {
                        return null;
                    }
                }
            }
        } else if (vt.isNumberVT()) {
            final int addr_idx = da.getAddrIdx();
            final Number nbv = this.memTb.getValNumber(vt, (long) (addr_idx * 2), ByteOrder.BigEndian);
            final int bitn = da.getBitNum();
            if (bitn >= 0) {
                final int intv = nbv.intValue();
                final boolean bitv = (intv & 1 << bitn) != 0x0;
                return UAVal.transStr2ObjVal(vt, bitv ? "1" : "0");
            }
            return nbv;
        }
        return null;
    }

    public byte[] transValToBytesByAddr(final MCAddr da, final Object v, final StringBuilder failedr) {
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
            intv = Integer.parseInt(new StringBuilder().append(v).toString());
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

    public boolean runCmds(final IConnEndPoint ep) throws Exception {
        this.runWriteCmdAndClear(ep);
        return this.runReadCmds(ep);
    }

    private void transMem2Addrs(final List<MCAddr> addrs) {
        for (final MCAddr ma : addrs) {
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
        for (final MCCmd mc : this.cmd2addr.keySet()) {
            if (!mc.tickCanRun()) {
                continue;
            }
            final MCCmdR cmdr = (MCCmdR) mc;
            Thread.sleep(this.interReqMs);
            final List<MCAddr> addrs = this.cmd2addr.get(mc);
            cmdr.doCmd((ConnPtStream) ep, ep.getInputStream(), ep.getOutputStream());
            final MCMsg3ERespRWords resp = cmdr.getResp();
            if (resp == null) {
                continue;
            }
            final MCMsg3EReqRWords req = cmdr.getReq();
            if (this.code.tp == MCCode.TP.bit) {
                if (!this.onReqRespBit(addrs, req, resp)) {
                    ret = false;
                } else {
                    this.lastReadOkDT = System.currentTimeMillis();
                }
            } else {
                if (this.code.tp != MCCode.TP.word) {
                    continue;
                }
                if (!this.onReqRespWord(addrs, req, resp)) {
                    ret = false;
                } else {
                    this.lastReadOkDT = System.currentTimeMillis();
                }
            }
        }
        return ret;
    }

    long getLastReadOkDT() {
        return this.lastReadOkDT;
    }

    private boolean onReqRespBit(final List<MCAddr> addrs, final MCMsg3EReqRWords req, final MCMsg3ERespRWords resp) {
        short[] retws = null;
        retws = resp.getRespWords();
        if (!resp.isReadOk() || retws == null) {
            if (this.chkSuccessiveFailed(true)) {
                this.setAddrError(addrs, resp.getReadErr());
            }
            return false;
        }
        final int startaddr = req.getStartAddr() / 8;
        for (int n = retws.length, i = 0; i < n; ++i) {
            final short v = retws[i];
            this.memTb.setValNumber(UAVal.ValTP.vt_int16, (long) (startaddr + i * 2), (Number) v, ByteOrder.BigEndian);
        }
        this.transMem2Addrs(addrs);
        this.chkSuccessiveFailed(false);
        return true;
    }

    private boolean onReqRespWord(final List<MCAddr> addrs, final MCMsg3EReqRWords req, final MCMsg3ERespRWords resp) {
        short[] retws = null;
        int a = 0;
        a = 1;
        retws = resp.getRespWords();
        if (!resp.isReadOk() || retws == null) {
            if (this.chkSuccessiveFailed(true)) {
                this.setAddrError(addrs, resp.getReadErr());
            }
            return false;
        }
        final int startaddr = req.getStartAddr() * 2;
        for (int n = retws.length, i = 0; i < n; ++i) {
            final short v = retws[i];
            this.memTb.setValNumber(UAVal.ValTP.vt_int16, (long) (startaddr + i * 2), (Number) v, ByteOrder.BigEndian);
        }
        this.transMem2Addrs(addrs);
        this.chkSuccessiveFailed(false);
        return true;
    }

    boolean runReadCmdsErr(final String errinf) {
        final boolean ret = true;
        for (final MCCmd mc : this.cmd2addr.keySet()) {
            final List<MCAddr> addrs = this.cmd2addr.get(mc);
            this.setAddrError(addrs, errinf);
        }
        return ret;
    }

    private void runWriteCmdAndClear(final IConnEndPoint ep) throws Exception {
        final int s = this.writeCmds.size();
        if (s <= 0) {
            return;
        }
        final MCCmd[] cmds = new MCCmd[s];
        int i;
        synchronized(this.writeCmds) {
            i = 0;

            while (i < s) {

                cmds[i] = (MCCmd) this.writeCmds.removeFirst();
                ++i;
            }
        }
        MCCmd[] array;
        for (int length = (array = cmds).length, j = 0; j < length; ++j) {
            final MCCmd mc = array[j];
            if (mc.tickCanRun()) {
                Thread.sleep(this.interReqMs);
                mc.doCmd((ConnPtStream) ep, ep.getInputStream(), ep.getOutputStream());
                if (mc instanceof MCCmdWBits) {
                    final MCCmdWBits fcw = (MCCmdWBits) mc;
                    if (!mc.isRespOk()) {
                        MCBlock.log.error(String.valueOf(fcw.toString()) + " is not ack");
                    } else if (MCBlock.log.isDebugEnabled()) {
                        MCBlock.log.debug(String.valueOf(fcw.toString()) + " is run ok (ack=true)");
                    }
                }
            }
        }
    }

    public boolean setWriteCmdAsyn(final MCAddr fxaddr, final Object v) {
        if (v == null) {
            return false;
        }
        if (!this.fxSeg.matchAddr(fxaddr)) {
            return false;
        }
        MCCmd fxcmd = null;
        if (this.code.isBitTp() && fxaddr.getValTP() == UAVal.ValTP.vt_bool) {
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
            fxcmd = this.setWriteCmdAsynBitBool(fxaddr.getAddrIdx(), bv);
        }
        if (fxcmd == null) {
            if (!(v instanceof Number)) {
                return false;
            }
            final Number nv = (Number) v;
            final UAVal.ValTP vtp = fxaddr.getValTP();
            switch (vtp) {
                case vt_int16:
                case vt_uint16: {
                    fxcmd = this.setWriteCmdAsynWords(fxaddr.getAddrIdx(), fxaddr.getValTP(), new short[]{nv.shortValue()});
                    break;
                }
                case vt_int32:
                case vt_uint32: {
                    final int intv = nv.intValue();
                    final short[] vs = {(short) (intv & 0xFFFF), (short) (intv >> 16 & 0xFFFF)};
                    fxcmd = this.setWriteCmdAsynWords(fxaddr.getAddrIdx(), fxaddr.getValTP(), vs);
                    break;
                }
                case vt_float: {
                    final float fv = nv.floatValue();
                    final byte[] fbs = DataUtil.floatToBytes(fv, ByteOrder.BigEndian);
                    short sv0 = (short) (fbs[1] & 0xFF);
                    sv0 <<= 8;
                    sv0 |= (short) (fbs[0] & 0xFF);
                    short sv2 = (short) (fbs[3] & 0xFF);
                    sv2 <<= 8;
                    sv2 |= (short) (fbs[2] & 0xFF);
                    final short[] vs = {sv0, sv2};
                    fxcmd = this.setWriteCmdAsynWords(fxaddr.getAddrIdx(), fxaddr.getValTP(), vs);
                    break;
                }
                default: {
                    return false;
                }
            }
        }
        if (fxcmd == null) {
            return false;
        }
        synchronized(this.writeCmds) {
            this.writeCmds.addLast(fxcmd);
            return true;
        }

    }

    private MCCmd setWriteCmdAsynBitBool(final int startidx, final boolean bv) {
        final MCCmdWBits cmd = new MCCmdWBits(this.code, startidx, new boolean[]{bv}, this.devItem.commFmtAscii);
        cmd.withRecvTimeout(this.recvTO);
        cmd.initCmd(this.fxDrv);
        return cmd;
    }

    private MCCmd setWriteCmdAsynWords(final int startidx, final UAVal.ValTP vtp, final short[] vals) {
        final MCCmdWWords cmd = new MCCmdWWords(this.code, startidx, vals, this.devItem.commFmtAscii);
        cmd.withRecvTimeout(this.recvTO);
        cmd.initCmd(this.fxDrv);
        return cmd;
    }

    public long getLastWriteFailedDT() {
        return this.lastWriteFailedDT;
    }

    public String getLastWriteFailedInf() {
        return this.lastWriteFailedInf;
    }
}
