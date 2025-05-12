// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.DevAddr;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import cn.doraro.flexedge.core.DevDef;
import cn.doraro.flexedge.core.UADev;

public class PPIDevItem
{
    PPIDriver ppiDrv;
    private transient UADev uaDev;
    private transient DevDef devDef;
    short readNum;
    HashMap<PPIMemTp, PPIBlock> tp2block;
    private transient List<PPIAddr> ppiAddrs;
    
    public PPIDevItem(final PPIDriver drv, final UADev dev) {
        this.ppiDrv = null;
        this.uaDev = null;
        this.devDef = null;
        this.readNum = 1;
        this.tp2block = new HashMap<PPIMemTp, PPIBlock>();
        this.ppiAddrs = new ArrayList<PPIAddr>();
        this.ppiDrv = drv;
        this.uaDev = dev;
    }
    
    UADev getUADev() {
        return this.uaDev;
    }
    
    private List<PPIAddr> filterAndSortAddrs(final PPIMemTp tp) {
        final ArrayList<PPIAddr> r = new ArrayList<PPIAddr>();
        for (final PPIAddr ma : this.ppiAddrs) {
            if (ma.getMemTp() == tp) {
                r.add(ma);
            }
        }
        Collections.sort(r);
        return r;
    }
    
    boolean init(final StringBuilder failedr) {
        final List<DevAddr> addrs = this.uaDev.listTagsAddrAll();
        if (addrs == null || addrs.size() <= 0) {
            failedr.append("no access addresses found");
            return false;
        }
        final List<PPIAddr> tmpads = new ArrayList<PPIAddr>();
        for (final DevAddr d : addrs) {
            tmpads.add((PPIAddr)d);
        }
        this.ppiAddrs = tmpads;
        final short devid = (short)this.uaDev.getOrDefaultPropValueLong("ppi_spk", "dev_addr", 1L);
        final int failAfterSuccessive = this.uaDev.getOrDefaultPropValueInt("timing", "failed_tryn", 3);
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
        PPIMemTp[] values;
        for (int length = (values = PPIMemTp.values()).length, i = 0; i < length; ++i) {
            final PPIMemTp mtp = values[i];
            final List<PPIAddr> ppiaddrs = this.filterAndSortAddrs(mtp);
            if (ppiaddrs != null) {
                if (ppiaddrs.size() > 0) {
                    final PPIBlock blk = new PPIBlock(devid, mtp, ppiaddrs, blocksize, inter_ms);
                    blk.setTimingParam(reqto, recvto, inter_ms);
                    if (blk.initCmds(this.ppiDrv)) {
                        this.tp2block.put(mtp, blk);
                    }
                }
            }
        }
        return true;
    }
    
    boolean doCmd(final ConnPtStream ep) throws Exception {
        for (final PPIBlock blk : this.tp2block.values()) {
            blk.runCmds((IConnEndPoint)ep);
        }
        return true;
    }
    
    public boolean RT_writeVal(final DevAddr da, final Object v) {
        final PPIAddr ma = (PPIAddr)da;
        final PPIBlock blk = this.tp2block.get(ma.getMemTp());
        return blk != null && blk.setWriteCmdAsyn(ma, v);
    }
}
