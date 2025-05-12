// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import kotlin.NotImplementedError;

class HLModel_C200H extends HLModel {
    public HLModel_C200H() {
        super("c200h", "C200H");
        this.setAddrDef(new HLAddrDef("AR").asValTpSeg(new HLAddrSeg("AR", 0, 27, 2, new UAVal.ValTP[]{UAVal.ValTP.vt_bool, UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("AR", 0, 26, 2, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
    }

    @Override
    public FinsMode getFinsMode() {
        throw new NotImplementedError();
    }
}
