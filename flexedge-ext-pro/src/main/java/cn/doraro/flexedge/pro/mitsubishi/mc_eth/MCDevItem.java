

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDef;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.conn.ConnPtStream;

import java.util.*;

public class MCDevItem {
    MCEthDriver fxDrv;
    short readNum;
    boolean commFmtAscii;
    HashMap<MCAddrSeg, MCBlock> seg2block;
    private transient UADev uaDev;
    private transient DevDef devDef;
    private transient List<MCAddr> fxAddrs;

    public MCDevItem(final MCEthDriver drv, final UADev dev) {
        this.fxDrv = null;
        this.uaDev = null;
        this.devDef = null;
        this.readNum = 1;
        this.commFmtAscii = false;
        this.seg2block = new HashMap<MCAddrSeg, MCBlock>();
        this.fxAddrs = new ArrayList<MCAddr>();
        this.fxDrv = drv;
        this.uaDev = dev;
    }

    public UADev getUADev() {
        return this.uaDev;
    }

    public List<MCBlock> listMCBlock() {
        final ArrayList<MCBlock> rets = new ArrayList<MCBlock>(this.seg2block.size());
        rets.addAll(this.seg2block.values());
        return rets;
    }

    boolean init(final StringBuilder failedr) {
        final List<DevAddr> addrs = this.uaDev.listTagsAddrAll();
        if (addrs == null || addrs.size() <= 0) {
            failedr.append("no access addresses found");
            return false;
        }
        final List<MCAddr> tmpads = new ArrayList<MCAddr>();
        for (final DevAddr d : addrs) {
            tmpads.add((MCAddr) d);
        }
        this.fxAddrs = tmpads;
        int blocksize = this.uaDev.getOrDefaultPropValueInt("block_size", "blk_bytes", 64);
        if (blocksize <= 0) {
            blocksize = 64;
        }
        final long reqto = this.uaDev.getOrDefaultPropValueLong("timing", "req_to", 100L);
        final long recvto = this.uaDev.getOrDefaultPropValueLong("timing", "recv_to", 200L);
        final long inter_ms = this.uaDev.getOrDefaultPropValueLong("timing", "inter_req", 100L);
        final String comm_fmt = this.uaDev.getBelongToCh().getOrDefaultPropValueStr("mceth_comm", "comm_fmt", "bin");
        this.commFmtAscii = "ascii".equals(comm_fmt);
        final MCModel fx_m = (MCModel) this.uaDev.getDrvDevModel();
        for (final String prefix : fx_m.listPrefix()) {
            final List<MCAddr> fxaddrs = this.filterAndSortAddrs(prefix);
            if (fxaddrs != null) {
                if (fxaddrs.size() <= 0) {
                    continue;
                }
                final HashMap<MCAddrSeg, List<MCAddr>> seg2addrs = fx_m.filterAndSortAddrs(prefix, fxaddrs);
                if (seg2addrs == null) {
                    continue;
                }
                if (seg2addrs.size() <= 0) {
                    continue;
                }
                final MCCode code = MCCode.getCodeBySymbol(prefix);
                if (code == null) {
                    continue;
                }
                for (final Map.Entry<MCAddrSeg, List<MCAddr>> seg2ads : seg2addrs.entrySet()) {
                    final MCBlock blk = new MCBlock(code, seg2ads.getKey(), seg2ads.getValue(), blocksize, inter_ms);
                    blk.setTimingParam(reqto, recvto, inter_ms);
                    if (blk.initCmds(this.fxDrv, this)) {
                        this.seg2block.put(seg2ads.getKey(), blk);
                    }
                }
            }
        }
        return true;
    }

    private List<MCAddr> filterAndSortAddrs(final String prefix) {
        final ArrayList<MCAddr> r = new ArrayList<MCAddr>();
        for (final MCAddr ma : this.fxAddrs) {
            if (prefix.equals(ma.getPrefix())) {
                r.add(ma);
            }
        }
        Collections.sort(r);
        return r;
    }

    boolean doCmd(final ConnPtStream ep) throws Exception {
        for (final MCBlock blk : this.seg2block.values()) {
            blk.runCmds((IConnEndPoint) ep);
        }
        return true;
    }

    long getLastReadOkDT() {
        long ret = -1L;
        for (final MCBlock blk : this.seg2block.values()) {
            final long dt = blk.getLastReadOkDT();
            if (dt <= 0L) {
                continue;
            }
            if (dt <= ret) {
                continue;
            }
            ret = dt;
        }
        return ret;
    }

    void doCmdError(final String errinf) {
        for (final MCBlock blk : this.seg2block.values()) {
            blk.runReadCmdsErr(errinf);
        }
    }

    public boolean RT_writeVal(final DevAddr da, final Object v) {
        final MCAddr ma = (MCAddr) da;
        final MCBlock blk = this.seg2block.get(ma.addrSeg);
        return blk != null && blk.setWriteCmdAsyn(ma, v);
    }
}
