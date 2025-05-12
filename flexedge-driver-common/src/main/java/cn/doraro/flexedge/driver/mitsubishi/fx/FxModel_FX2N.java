// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.UAVal;

class FxModel_FX2N extends MCModel
{
    public FxModel_FX2N() {
        super("fx2n", "FX2N");
        this.setAddrDef(new FxAddrDef("S").asValTpSeg(new FxAddrSeg(0, "States", 0, 999, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, true).EXT_asBaseValStart(640).EXT_asBaseAddrForceOnOff(5120).asValBit(true)));
        this.setAddrDef(new FxAddrDef("X").asValTpSeg(new FxAddrSeg(128, "Inputs", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, false).EXT_asBaseValStart(576).asOctal(true).asValBit(true)));
        this.setAddrDef(new FxAddrDef("Y").asValTpSeg(new FxAddrSeg(160, "Outputs", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, true).EXT_asBaseValStart(384).EXT_asBaseAddrForceOnOff(3072).asOctal(true).asValBit(true).asBaseAddrForceOnOff(1280)));
        this.setAddrDef(new FxAddrDef("M").asValTpSeg(new FxAddrSeg(256, "Auxiliary Relays", 0, 3071, 4, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, true).EXT_asBaseValStart(0).EXT_asBaseAddrForceOnOff(0).asValBit(true).asBaseAddrForceOnOff(2048)).asValTpSeg(new FxAddrSeg(480, "Special Aux. Relays", 8000, 8255, 4, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, true).EXT_asBaseValStart(304).EXT_asBaseAddrForceOnOff(11584).asValBit(true).asBaseValStart(8000).asBaseAddrForceOnOff(3840)));
        this.setAddrDef(new FxAddrDef("TS").asValTpSeg(new FxAddrSeg(192, "Timer Contacts", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, false).EXT_asBaseValStart(512).asValBit(true)));
        this.setAddrDef(new FxAddrDef("CS").asValTpSeg(new FxAddrSeg(448, "Counter Contacts", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, false).EXT_asBaseValStart(480).asValBit(true)));
        this.setAddrDef(new FxAddrDef("TR").asValTpSeg(new FxAddrSeg(1216, "Timer Reset", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, true).EXT_asBaseValStart(1792).EXT_asBaseAddrForceOnOff(38912).asValBit(true)));
        this.setAddrDef(new FxAddrDef("CR").asValTpSeg(new FxAddrSeg(1472, "Counter Reset", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_bool }, true).EXT_asBaseValStart(1760).EXT_asBaseAddrForceOnOff(38656).asValBit(true)));
        this.setAddrDef(new FxAddrDef("T").asValTpSeg(new FxAddrSeg(2048, "Timer Value", 0, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16 }, true).EXT_asBaseValStart(4096)));
        this.setAddrDef(new FxAddrDef("C").asValTpSeg(new FxAddrSeg(2560, "Counter Value", 0, 199, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16 }, true).EXT_asBaseValStart(2560)).asValTpSeg(new FxAddrSeg(3072, "Counter Value 32Bit", 200, 255, 3, new UAVal.ValTP[] { UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32 }, true).EXT_asBaseValStart(3072).asAddrStepInt32(true).asBaseValStart(200)));
        this.setAddrDef(new FxAddrDef("D").asValTpSeg(new FxAddrSeg(4096, "Data Registers", 0, 7999, 4, new UAVal.ValTP[] { UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16 }, true).EXT_asBaseValStart(16384)).asValTpSeg(new FxAddrSeg(4096, "Data Registers", 0, 7998, 4, new UAVal.ValTP[] { UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float }, true).EXT_asBaseValStart(16384).asAddrStepInt32(false)).asValTpSeg(new FxAddrSeg(3584, "Special Data Registers", 8000, 8255, 4, new UAVal.ValTP[] { UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16 }, true).EXT_asBaseValStart(3584).asBaseValStart(8000)).asValTpSeg(new FxAddrSeg(3584, "Special Data Registers", 8000, 8254, 4, new UAVal.ValTP[] { UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float }, true).EXT_asBaseValStart(3584).asAddrStepInt32(false).asBaseValStart(8000)));
    }
}
