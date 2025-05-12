// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDef;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.conn.ConnPtStream;

import java.util.*;

public class FxDevItem {
    FxDriver fxDrv;
    short readNum;
    HashMap<FxAddrSeg, FxBlock> seg2block;
    private transient UADev uaDev;
    private transient DevDef devDef;
    private transient List<FxAddr> fxAddrs;

    public FxDevItem(final FxDriver drv, final UADev dev) {
        this.fxDrv = null;
        this.uaDev = null;
        this.devDef = null;
        this.readNum = 1;
        this.seg2block = new HashMap<FxAddrSeg, FxBlock>();
        this.fxAddrs = new ArrayList<FxAddr>();
        this.fxDrv = drv;
        this.uaDev = dev;
    }

    UADev getUADev() {
        return this.uaDev;
    }

    boolean init(final StringBuilder failedr) {
        final List<DevAddr> addrs = this.uaDev.listTagsAddrAll();
        if (addrs == null || addrs.size() <= 0) {
            failedr.append("no access addresses found");
            return false;
        }
        final List<FxAddr> tmpads = new ArrayList<FxAddr>();
        for (final DevAddr d : addrs) {
            tmpads.add((FxAddr) d);
        }
        this.fxAddrs = tmpads;
        int blocksize = 32;
        if (this.devDef != null) {
            blocksize = this.devDef.getOrDefaultPropValueInt("block_size", "out_coils", 32);
        }
        if (blocksize <= 0) {
            blocksize = 32;
        }
        final long reqto = this.uaDev.getOrDefaultPropValueLong("timing", "req_to", 100L);
        final long recvto = this.uaDev.getOrDefaultPropValueLong("timing", "recv_to", 200L);
        final long inter_ms = this.uaDev.getOrDefaultPropValueLong("timing", "inter_req", 100L);
        final MCModel fx_m = (MCModel) this.uaDev.getDrvDevModel();
        for (final String prefix : fx_m.listPrefix()) {
            final List<FxAddr> fxaddrs = this.filterAndSortAddrs(prefix);
            if (fxaddrs != null) {
                if (fxaddrs.size() <= 0) {
                    continue;
                }
                final HashMap<FxAddrSeg, List<FxAddr>> seg2addrs = fx_m.filterAndSortAddrs(prefix, fxaddrs);
                if (seg2addrs == null) {
                    continue;
                }
                if (seg2addrs.size() <= 0) {
                    continue;
                }
                for (final Map.Entry<FxAddrSeg, List<FxAddr>> seg2ads : seg2addrs.entrySet()) {
                    final FxBlock blk = new FxBlock(seg2ads.getKey(), seg2ads.getValue(), blocksize, inter_ms);
                    blk.setTimingParam(reqto, recvto, inter_ms);
                    if (blk.initCmds(this.fxDrv)) {
                        this.seg2block.put(seg2ads.getKey(), blk);
                    }
                }
            }
        }
        return true;
    }

    private List<FxAddr> filterAndSortAddrs(final String prefix) {
        final ArrayList<FxAddr> r = new ArrayList<FxAddr>();
        for (final FxAddr ma : this.fxAddrs) {
            if (prefix.equals(ma.getPrefix())) {
                r.add(ma);
            }
        }
        Collections.sort(r);
        return r;
    }

    boolean doCmd(final ConnPtStream ep) throws Exception {
        for (final FxBlock blk : this.seg2block.values()) {
            blk.runCmds((IConnEndPoint) ep);
        }
        return true;
    }

    public boolean RT_writeVal(final DevAddr da, final Object v) {
        final FxAddr ma = (FxAddr) da;
        final FxBlock blk = this.seg2block.get(ma.addrSeg);
        return blk != null && blk.setWriteCmdAsyn(ma, v);
    }
}
