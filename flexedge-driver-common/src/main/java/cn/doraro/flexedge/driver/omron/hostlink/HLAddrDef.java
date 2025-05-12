// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.ILang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HLAddrDef implements DevAddr.IAddrDef, ILang {
    String prefix;
    ArrayList<HLAddrSeg> segs;

    public HLAddrDef(final String prefix) {
        this.prefix = null;
        this.segs = new ArrayList<HLAddrSeg>();
        this.prefix = prefix;
    }

    public HLAddrDef asValTpSeg(final HLAddrSeg seg) {
        seg.belongTo = this;
        this.segs.add(seg);
        return this;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List<HLAddrSeg> getSegs() {
        return this.segs;
    }

    public List<HLAddrSeg> findSegs(final UAVal.ValTP vtp) {
        final ArrayList<HLAddrSeg> rets = new ArrayList<HLAddrSeg>();
        for (final HLAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp)) {
                rets.add(seg);
            }
        }
        return rets;
    }

    public HLAddrSeg findSeg(final UAVal.ValTP vtp, String addr) {
        final int k = addr.indexOf(46);
        String bit_str = null;
        if (k > 0) {
            bit_str = addr.substring(k + 1);
            addr = addr.substring(0, k);
        }
        final int addr_n = Convert.parseToInt32(addr, -1);
        if (addr_n < 0) {
            return null;
        }
        final int bit_n = Convert.parseToInt32(bit_str, -1);
        for (final HLAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp) && seg.matchAddr(addr_n, bit_n)) {
                return seg;
            }
        }
        return null;
    }

    public HLAddrSeg findSeg(final HLAddr fxaddr) {
        final UAVal.ValTP vtp = fxaddr.getValTP();
        if (vtp == null) {
            return null;
        }
        for (final HLAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp) && seg.matchAddr(fxaddr)) {
                return seg;
            }
        }
        return null;
    }

    public String getDefTypeForDoc() {
        return this.g("deftp_" + this.prefix);
    }

    public List<DevAddr.IAddrDefSeg> getSegsForDoc() {
        final ArrayList<DevAddr.IAddrDefSeg> rets = new ArrayList<DevAddr.IAddrDefSeg>();
        rets.addAll((Collection<? extends DevAddr.IAddrDefSeg>) this.segs);
        for (final HLAddrSeg seg : this.segs) {
            if (seg.isHasBit()) {
                rets.add((DevAddr.IAddrDefSeg) new HLAddrSegSubBit(seg));
            }
        }
        return rets;
    }
}
