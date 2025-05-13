

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.UAVal;

class FxModel_FX3U extends MCModel {
    public FxModel_FX3U() {
        super("fx3u", "FX3U");
        this.setAddrDef(new FxAddrDef("X").asValTpSeg(new FxAddrSeg(128, "Inputs", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, false).EXT_asBaseValStart(36000).asOctal(true).asValBit(true)));
        this.setAddrDef(new FxAddrDef("Y").asValTpSeg(new FxAddrSeg(160, "Outputs", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).EXT_asBaseValStart(35776).EXT_asBaseAddrForceOnOff(24064).asOctal(true).asValBit(true).asBaseAddrForceOnOff(1280)));
        this.setAddrDef(new FxAddrDef("M").asValTpSeg(new FxAddrSeg(256, "Auxiliary Relays", 0, 7679, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).EXT_asBaseValStart(34816).EXT_asBaseAddrForceOnOff(16384).asValBit(true).asBaseAddrForceOnOff(2048)).asValTpSeg(new FxAddrSeg(480, "Special Aux. Relays", 8000, 8511, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).EXT_asBaseValStart(35840).EXT_asBaseAddrForceOnOff(24576).asValBit(true).asBaseValStart(8000).asBaseAddrForceOnOff(3840)));
        this.setAddrDef(new FxAddrDef("S").asValTpSeg(new FxAddrSeg(0, "States", 0, 4095, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).EXT_asBaseValStart(36064).EXT_asBaseAddrForceOnOff(26368).asValBit(true)));
        this.setAddrDef(new FxAddrDef("TS").asValTpSeg(new FxAddrSeg(192, "Timer Contacts", 0, 511, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, false).EXT_asBaseValStart(35936).asValBit(true)));
        this.setAddrDef(new FxAddrDef("CS").asValTpSeg(new FxAddrSeg(448, "Counter Contacts", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, false).EXT_asBaseValStart(35904).asValBit(true)));
        this.setAddrDef(new FxAddrDef("TR").asValTpSeg(new FxAddrSeg(1216, "Timer Reset", 0, 511, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).EXT_asBaseValStart(37632).EXT_asBaseAddrForceOnOff(38912).asValBit(true)));
        this.setAddrDef(new FxAddrDef("CR").asValTpSeg(new FxAddrSeg(1472, "Counter Reset", 0, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_bool}, true).EXT_asBaseValStart(37600).EXT_asBaseAddrForceOnOff(38656).asValBit(true)));
        this.setAddrDef(new FxAddrDef("T").asValTpSeg(new FxAddrSeg(2048, "Timer Value", 0, 511, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true).EXT_asBaseValStart(4096)));
        this.setAddrDef(new FxAddrDef("C").asValTpSeg(new FxAddrSeg(2560, "Counter Value", 0, 199, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true).EXT_asBaseValStart(2560)).asValTpSeg(new FxAddrSeg(3072, "Counter Value 32Bit", 200, 255, 3, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32}, true).EXT_asBaseValStart(3072).asAddrStepInt32(true).asBaseValStart(200)));
        this.setAddrDef(new FxAddrDef("D").asValTpSeg(new FxAddrSeg(4096, "Data Registers", 0, 7999, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true).EXT_asBaseValStart(16384)).asValTpSeg(new FxAddrSeg(4096, "Data Registers", 0, 7998, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}, true).EXT_asBaseValStart(16384).asAddrStepInt32(false)).asValTpSeg(new FxAddrSeg(3584, "Special Data Registers", 8000, 8511, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16}, true).EXT_asBaseValStart(32768).asBaseValStart(8000)).asValTpSeg(new FxAddrSeg(3584, "Special Data Registers", 8000, 8510, 4, new UAVal.ValTP[]{UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_float}, true).EXT_asBaseValStart(32768).asAddrStepInt32(false).asBaseValStart(8000)));
    }
}
