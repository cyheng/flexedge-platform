// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDef;
import cn.doraro.flexedge.core.UADev;

import java.util.*;

public class S7DevItem {
    S7EthDriver s7Drv;
    short readNum;
    HashMap<String, S7Block> tp2block;
    private transient UADev uaDev;
    private transient DevDef devDef;
    private transient List<S7Addr> s7Addrs;

    public S7DevItem(final S7EthDriver drv, final UADev dev) {
        this.s7Drv = null;
        this.uaDev = null;
        this.devDef = null;
        this.readNum = 1;
        this.tp2block = new HashMap<String, S7Block>();
        this.s7Addrs = new ArrayList<S7Addr>();
        this.s7Drv = drv;
        this.uaDev = dev;
    }

    UADev getUADev() {
        return this.uaDev;
    }

    private HashSet<Integer> searchAddrsDBNums() {
        final HashSet<Integer> rets = new HashSet<Integer>();
        for (final S7Addr ma : this.s7Addrs) {
            if (ma.getMemTp() == S7MemTp.DB) {
                final int dbn = ma.getDBNum();
                rets.add(dbn);
            }
        }
        return rets;
    }

    private List<S7Addr> filterAndSortAddrs(final S7MemTp tp, final int db_num) {
        final ArrayList<S7Addr> r = new ArrayList<S7Addr>();
        for (final S7Addr ma : this.s7Addrs) {
            if (ma.chkSameArea(tp, db_num)) {
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
        final List<S7Addr> tmpads = new ArrayList<S7Addr>();
        for (final DevAddr d : addrs) {
            tmpads.add((S7Addr) d);
        }
        this.s7Addrs = tmpads;
        final short devid = (short) this.uaDev.getOrDefaultPropValueLong("ppi_spk", "dev_addr", 1L);
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
        for (final Integer dbnum : this.searchAddrsDBNums()) {
            this.initAddrs(S7MemTp.DB, dbnum);
        }
        S7MemTp[] values;
        for (int length = (values = S7MemTp.values()).length, i = 0; i < length; ++i) {
            final S7MemTp mtp = values[i];
            if (mtp != S7MemTp.DB) {
                this.initAddrs(mtp, 0);
            }
        }
        return true;
    }

    private void initAddrs(final S7MemTp mtp, final int dbnum) {
        final List<S7Addr> addrs = this.filterAndSortAddrs(mtp, dbnum);
        if (addrs == null || addrs.size() <= 0) {
            return;
        }
        final String areakey = addrs.get(0).getAreaKey();
        final S7Block blk = new S7Block(addrs, 32, 200L);
        if (blk.initCmds(this.s7Drv)) {
            this.tp2block.put(areakey, blk);
        }
    }

    boolean doCmd(final S7TcpConn conn) {
        for (final S7Block blk : this.tp2block.values()) {
            try {
                blk.runCmds(conn);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean RT_writeVal(final DevAddr da, final Object v) {
        final S7Addr ma = (S7Addr) da;
        final S7Block blk = this.tp2block.get(ma.getAreaKey());
        return blk != null && blk.setWriteCmdAsyn(ma, v);
    }
}
