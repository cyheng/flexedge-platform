// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import java.util.Collection;
import java.util.Iterator;
import cn.doraro.flexedge.core.UAVal;
import java.util.List;
import java.util.ArrayList;
import cn.doraro.flexedge.core.util.ILang;
import cn.doraro.flexedge.core.DevAddr;

public class S7AddrDef implements DevAddr.IAddrDef, ILang
{
    String prefix;
    String title;
    ArrayList<S7AddrSeg> segs;
    
    public S7AddrDef(final String prefix, final String title) {
        this.prefix = null;
        this.segs = new ArrayList<S7AddrSeg>();
        this.prefix = prefix;
        this.title = title;
    }
    
    public S7AddrDef asValTpSeg(final S7AddrSeg seg) {
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
    
    public List<S7AddrSeg> getSegs() {
        return this.segs;
    }
    
    public List<S7AddrSeg> findSegs(final UAVal.ValTP vtp) {
        final ArrayList<S7AddrSeg> rets = new ArrayList<S7AddrSeg>();
        for (final S7AddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp)) {
                rets.add(seg);
            }
        }
        return rets;
    }
    
    public S7AddrSeg findSeg(final UAVal.ValTP vtp, final String addr) {
        for (final S7AddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp) && seg.matchAddr(addr) != null) {
                return seg;
            }
        }
        return null;
    }
    
    public S7AddrSeg findSeg(final S7Addr fxaddr) {
        final UAVal.ValTP vtp = fxaddr.getValTP();
        if (vtp == null) {
            return null;
        }
        for (final S7AddrSeg seg : this.segs) {
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
        rets.addAll((Collection<? extends DevAddr.IAddrDefSeg>)this.segs);
        return rets;
    }
}
