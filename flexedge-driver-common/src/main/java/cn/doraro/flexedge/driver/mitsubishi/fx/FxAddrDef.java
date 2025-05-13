

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.ILang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FxAddrDef implements DevAddr.IAddrDef, ILang {
    String prefix;
    ArrayList<FxAddrSeg> segs;

    public FxAddrDef(final String prefix) {
        this.prefix = null;
        this.segs = new ArrayList<FxAddrSeg>();
        this.prefix = prefix;
    }

    public FxAddrDef asValTpSeg(final FxAddrSeg seg) {
        seg.belongTo = this;
        this.segs.add(seg);
        return this;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List<FxAddrSeg> getSegs() {
        return this.segs;
    }

    public List<FxAddrSeg> findSegs(final UAVal.ValTP vtp) {
        final ArrayList<FxAddrSeg> rets = new ArrayList<FxAddrSeg>();
        for (final FxAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp)) {
                rets.add(seg);
            }
        }
        return rets;
    }

    public FxAddrSeg findSeg(final UAVal.ValTP vtp, final String addr) {
        for (final FxAddrSeg seg : this.segs) {
            if (seg.matchValTP(vtp) && seg.matchAddr(addr) != null) {
                return seg;
            }
        }
        return null;
    }

    public FxAddrSeg findSeg(final FxAddr fxaddr) {
        final UAVal.ValTP vtp = fxaddr.getValTP();
        if (vtp == null) {
            return null;
        }
        for (final FxAddrSeg seg : this.segs) {
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
