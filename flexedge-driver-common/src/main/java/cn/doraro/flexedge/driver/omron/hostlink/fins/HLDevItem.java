// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import cn.doraro.flexedge.driver.omron.hostlink.HLModel;
import cn.doraro.flexedge.core.DevAddr;
import java.util.ArrayList;
import cn.doraro.flexedge.driver.omron.hostlink.HLAddr;
import java.util.List;
import cn.doraro.flexedge.driver.omron.hostlink.HLAddrSeg;
import java.util.HashMap;
import cn.doraro.flexedge.core.DevDef;
import cn.doraro.flexedge.core.UADev;

public class HLDevItem
{
    HLFinsDriver driver;
    private transient UADev uaDev;
    private transient DevDef devDef;
    short readNum;
    HashMap<HLAddrSeg, HLBlock> seg2block;
    private transient List<HLAddr> fxAddrs;
    boolean bNetOrSerial;
    
    public HLDevItem(final HLFinsDriver drv, final UADev dev, final boolean b_net_or_serial) {
        this.driver = null;
        this.uaDev = null;
        this.devDef = null;
        this.readNum = 1;
        this.seg2block = new HashMap<HLAddrSeg, HLBlock>();
        this.fxAddrs = new ArrayList<HLAddr>();
        this.bNetOrSerial = true;
        this.driver = drv;
        this.uaDev = dev;
        this.bNetOrSerial = b_net_or_serial;
    }
    
    UADev getUADev() {
        return this.uaDev;
    }
    
    public boolean init(final StringBuilder failedr) {
        final List<DevAddr> addrs = this.uaDev.listTagsAddrAll();
        if (addrs == null || addrs.size() <= 0) {
            failedr.append("no access addresses found");
            return false;
        }
        final List<HLAddr> tmpads = new ArrayList<HLAddr>();
        for (final DevAddr d : addrs) {
            tmpads.add((HLAddr)d);
        }
        this.fxAddrs = tmpads;
        int blocksize_bit = 64;
        int blocksize_word = 32;
        if (this.devDef != null) {
            blocksize_word = this.devDef.getOrDefaultPropValueInt("block_size", "word", 32);
            blocksize_bit = this.devDef.getOrDefaultPropValueInt("block_size", "bit", 64);
        }
        if (blocksize_bit <= 0) {
            blocksize_bit = 64;
        }
        if (blocksize_word <= 0) {
            blocksize_word = 32;
        }
        final long reqto = this.uaDev.getOrDefaultPropValueLong("timing", "req_to", 100L);
        final int failed_tryn = this.uaDev.getOrDefaultPropValueInt("timing", "failed_tryn", 3);
        final long inter_ms = this.uaDev.getOrDefaultPropValueLong("timing", "inter_req", 100L);
        final HLModel fx_m = (HLModel)this.uaDev.getDrvDevModel();
        for (final String prefix : fx_m.listPrefix()) {
            final List<HLAddr> fxaddrs = this.filterAndSortAddrs(prefix);
            if (fxaddrs != null) {
                if (fxaddrs.size() <= 0) {
                    continue;
                }
                final HashMap<HLAddrSeg, List<HLAddr>> seg2addrs = fx_m.filterAndSortAddrs(prefix, fxaddrs);
                if (seg2addrs == null) {
                    continue;
                }
                if (seg2addrs.size() <= 0) {
                    continue;
                }
                for (final Map.Entry<HLAddrSeg, List<HLAddr>> seg2ads : seg2addrs.entrySet()) {
                    final HLAddrSeg seg = seg2ads.getKey();
                    HLBlock blk = null;
                    if (seg.isValBitOnly()) {
                        blk = new HLBlock(this, seg, seg2ads.getValue(), blocksize_bit, inter_ms, failed_tryn);
                    }
                    else {
                        blk = new HLBlock(this, seg, seg2ads.getValue(), blocksize_word, inter_ms, failed_tryn);
                    }
                    blk.prefix = prefix;
                    blk.setTimingParam(reqto, inter_ms);
                    if (blk.initCmds(this.driver)) {
                        this.seg2block.put(seg2ads.getKey(), blk);
                    }
                }
            }
        }
        return true;
    }
    
    private List<HLAddr> filterAndSortAddrs(final String prefix) {
        final ArrayList<HLAddr> r = new ArrayList<HLAddr>();
        for (final HLAddr ma : this.fxAddrs) {
            if (prefix.equals(ma.getPrefix())) {
                r.add(ma);
            }
        }
        Collections.sort(r);
        return r;
    }
    
    public boolean doCmd(final ConnPtStream ep, final StringBuilder failedr) throws Exception {
        for (final HLBlock blk : this.seg2block.values()) {
            if (!blk.runCmds((IConnEndPoint)ep, failedr)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean RT_writeVal(final DevAddr da, final Object v) {
        final HLAddr ma = (HLAddr)da;
        final HLBlock blk = this.seg2block.get(ma.getAddrSeg());
        return blk != null && blk.setWriteCmdAsyn(ma, v);
    }
}
