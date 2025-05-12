// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.driver.mitsubishi.Addr;

public class FxAddr extends Addr {
    MCModel fxModel;
    transient FxAddrDef addrDef;
    transient FxAddrSeg addrSeg;

    public FxAddr() {
        this.addrDef = null;
        this.addrSeg = null;
    }

    FxAddr(final String addr_str, final UAVal.ValTP vtp, final MCModel fx_m, final String prefix, final int addr_num, final boolean b_valbit, final int digit_num, final boolean b_oct) {
        super(addr_str, vtp, prefix, addr_num, b_valbit, digit_num, b_oct);
        this.addrDef = null;
        this.addrSeg = null;
        this.fxModel = fx_m;
    }

    FxAddr asDef(final FxAddrDef addr_def, final FxAddrSeg seg) {
        this.addrDef = addr_def;
        this.addrSeg = seg;
        this.bWritable = this.addrSeg.isWritable();
        return this;
    }

    @Override
    public int getBytesInBase() {
        return this.addrSeg.calBytesInBase(this.addrNum);
    }
}
