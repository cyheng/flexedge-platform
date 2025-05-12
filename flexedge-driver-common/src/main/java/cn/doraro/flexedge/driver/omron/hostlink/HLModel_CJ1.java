// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;

class HLModel_CJ1 extends HLModel {
    public HLModel_CJ1() {
        super("cj1", "CJ1,CP1");
        this.setAddrDef(new HLAddrDef("A").asValTpSeg(new HLAddrSeg("R", 0, 447, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(false).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("R", 0, 446, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(false).asHasSubBit(false)).asValTpSeg(new HLAddrSeg("RW", 448, 959, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("RW", 448, 958, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("CIO").asValTpSeg(new HLAddrSeg("IO", 0, 6143, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("IO", 0, 6142, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("C").asValTpSeg(new HLAddrSeg("IO", 0, 4095, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("CS").asValTpSeg(new HLAddrSeg("IO", 0, 4095, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("D").asValTpSeg(new HLAddrSeg("IO", 0, 32767, 5, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("IO", 0, 32766, 5, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("DR").asValTpSeg(new HLAddrSeg("IO", 0, 15, 2, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(false)).asValTpSeg(new HLAddrSeg("IO", 0, 14, 2, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("H").asValTpSeg(new HLAddrSeg("IO", 0, 1535, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("IO", 0, 1534, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("IR").asValTpSeg(new HLAddrSeg("IO", 0, 15, 2, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("TK").asValTpSeg(new HLAddrSeg("IO", 0, 31, 2, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}).asWrite(false).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("T").asValTpSeg(new HLAddrSeg("IO", 0, 4095, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("TS").asValTpSeg(new HLAddrSeg("IO", 0, 4095, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}).asWrite(true).asHasSubBit(false)));
        this.setAddrDef(new HLAddrDef("W").asValTpSeg(new HLAddrSeg("IO", 0, 511, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}).asWrite(true).asHasSubBit(true)).asValTpSeg(new HLAddrSeg("IO", 0, 510, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}).asWrite(true).asHasSubBit(false)));
    }

    @Override
    public FinsMode getFinsMode() {
        return FinsMode.getMode_CS_CJ1();
    }
}
