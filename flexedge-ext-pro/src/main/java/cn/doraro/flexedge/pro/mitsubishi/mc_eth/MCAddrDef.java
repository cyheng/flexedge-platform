// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.ILang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MCAddrDef implements DevAddr.IAddrDef, ILang {
    String prefix;
    String title;
    ArrayList<MCAddrSeg> segs;

    public MCAddrDef(final String prefix, final String title) {
        this.prefix = null;
        this.segs = new ArrayList<MCAddrSeg>();
        this.prefix = prefix;
        this.title = title;
    }

    public MCAddrDef asValTpSeg(final MCAddrSeg seg) {
        seg.belongTo = this;
        this.segs.add(seg);
        return this;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getTitle() {
        return this.title;
    }

    public List<MCAddrSeg> getSegs() {
        return this.segs;
    }

    public List<MCAddrSeg> findSegs(final UAVal.ValTP vtp) {
        final ArrayList<MCAddrSeg> rets = new ArrayList<MCAddrSeg>();
        for (final MCAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp)) {
                rets.add(seg);
            }
        }
        return rets;
    }

    public MCAddrSeg findSeg(final UAVal.ValTP vtp, final String addr) {
        for (final MCAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp) && seg.matchAddr(addr) != null) {
                return seg;
            }
        }
        return null;
    }

    public MCAddrSeg findSeg(final MCAddr fxaddr) {
        final UAVal.ValTP vtp = fxaddr.getValTP();
        if (vtp == null) {
            return null;
        }
        for (final MCAddrSeg seg : this.segs) {
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
        return rets;
    }
}
