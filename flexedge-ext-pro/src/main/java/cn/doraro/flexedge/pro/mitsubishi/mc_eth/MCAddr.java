// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.UAVal;

public class MCAddr extends MCAddrComm {
    MCModel fxModel;
    transient MCAddrDef addrDef;
    transient MCAddrSeg addrSeg;

    public MCAddr() {
        this.addrDef = null;
        this.addrSeg = null;
    }

    MCAddr(final String addr_str, final UAVal.ValTP vtp, final MCModel fx_m, final String prefix, final int addr_num, final int digit_num, final int bit_num) {
        super(addr_str, vtp, prefix, addr_num, digit_num, bit_num);
        this.addrDef = null;
        this.addrSeg = null;
        this.fxModel = fx_m;
    }

    MCAddr asDef(final MCAddrDef addr_def, final MCAddrSeg seg) {
        this.addrDef = addr_def;
        this.addrSeg = seg;
        this.bWritable = this.addrSeg.isWritable();
        return this;
    }
}
