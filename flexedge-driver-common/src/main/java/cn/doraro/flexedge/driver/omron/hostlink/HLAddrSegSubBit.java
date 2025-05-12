// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.UAVal;

class HLAddrSegSubBit extends HLAddrSeg
{
    HLAddrSegSubBit(final HLAddrSeg seg) {
        super(seg.title, seg.valStart, seg.valEnd, seg.digitNum, new UAVal.ValTP[] { UAVal.ValTP.vt_bool });
        this.belongTo = seg.belongTo;
    }
    
    @Override
    public String getRangeFrom() {
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valStart, this.digitNum) + ".xx";
    }
    
    @Override
    public String getRangeTo() {
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valEnd, this.digitNum) + ".xx";
    }
    
    public String getRangeStr() {
        return super.getRangeStr() + " [00-xx-15]";
    }
}
