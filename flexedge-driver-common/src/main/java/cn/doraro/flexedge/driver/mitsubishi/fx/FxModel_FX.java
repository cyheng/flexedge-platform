

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.UAVal;

class FxModel_FX extends MCModel {
    public FxModel_FX() {
        super("fx", "FX");
        this.setAddrDef(new FxAddrDef("S").asValTpSeg(new FxAddrSeg(0, "States", 0, 999, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).asValBit(true).asBaseAddrForceOnOff(0)));
        this.setAddrDef(new FxAddrDef("X").asValTpSeg(new FxAddrSeg(128, "Inputs", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, false).asOctal(true).asValBit(true)));
        this.setAddrDef(new FxAddrDef("Y").asValTpSeg(new FxAddrSeg(160, "Outputs", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).asOctal(true).asValBit(true).asBaseAddrForceOnOff(1280)));
        this.setAddrDef(new FxAddrDef("M").asValTpSeg(new FxAddrSeg(256, "Auxiliary Relays", 0, 1535, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).asValBit(true).asBaseAddrForceOnOff(2048)).asValTpSeg(new FxAddrSeg(480, "Special Aux. Relays", 8000, 8255, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).asValBit(true).asBaseValStart(8000).asBaseAddrForceOnOff(3840)));
        this.setAddrDef(new FxAddrDef("TS").asValTpSeg(new FxAddrSeg(192, "Timer Contacts", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, false).asValBit(true)));
        this.setAddrDef(new FxAddrDef("CS").asValTpSeg(new FxAddrSeg(448, "Counter Contacts", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, false).asValBit(true)));
        this.setAddrDef(new FxAddrDef("TR").asValTpSeg(new FxAddrSeg(1216, "Timer Reset", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).asValBit(true).asBaseAddrForceOnOff(1536)));
        this.setAddrDef(new FxAddrDef("CR").asValTpSeg(new FxAddrSeg(1472, "Counter Reset", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).asValBit(true).asBaseAddrForceOnOff(3584)));
        this.setAddrDef(new FxAddrDef("T").asValTpSeg(new FxAddrSeg(2048, "Timer Value", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true)));
        this.setAddrDef(new FxAddrDef("C").asValTpSeg(new FxAddrSeg(2560, "Counter Value", 0, 199, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true)).asValTpSeg(new FxAddrSeg(3072, "Counter Value 32Bit", 200, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32}, true).asAddrStepInt32(true).asBaseValStart(200)));
        this.setAddrDef(new FxAddrDef("D").asValTpSeg(new FxAddrSeg(4096, "Data Registers", 0, 999, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true)).asValTpSeg(new FxAddrSeg(4096, "Data Registers", 0, 998, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}, true).asAddrStepInt32(false)).asValTpSeg(new FxAddrSeg(3584, "Special Data Registers", 8000, 8255, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true).asBaseValStart(8000)).asValTpSeg(new FxAddrSeg(3584, "Special Data Registers", 8000, 8254, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}, true).asAddrStepInt32(false).asBaseValStart(8000)));
    }
}
