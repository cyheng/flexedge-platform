// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.UAVal;

class FinsMode_CS extends FinsMode
{
    protected FinsMode_CS(final boolean cj2) {
        this.setAreaCode(new AreaCode("CIO", "CIO Bit Area", UAVal.ValTP.vt_bool, 48));
        this.setAreaCode(new AreaCode("W", "Work Area Bit Area", UAVal.ValTP.vt_bool, 49));
        this.setAreaCode(new AreaCode("H", "Holding Bit Area", UAVal.ValTP.vt_bool, 50));
        this.setAreaCode(new AreaCode("A", "Auxiliary Bit Area", UAVal.ValTP.vt_bool, 51));
        this.setAreaCode(new AreaCode("CIO", "CIO Area", UAVal.ValTP.vt_int16, 176));
        this.setAreaCode(new AreaCode("W", "Work Area", UAVal.ValTP.vt_int16, 177));
        this.setAreaCode(new AreaCode("H", "Holding Area", UAVal.ValTP.vt_int16, 178));
        this.setAreaCode(new AreaCode("A", "Auxiliary Area", UAVal.ValTP.vt_int16, 179));
        this.setAreaCode(new AreaCode("TS", "Timer Completion Flag (Status)", UAVal.ValTP.vt_bool, 9));
        this.setAreaCode(new AreaCode("CS", "Counter Completion Flag (Status)", UAVal.ValTP.vt_bool, 9));
        this.setAreaCode(new AreaCode("T", "Timer Area", UAVal.ValTP.vt_int16, 137));
        this.setAreaCode(new AreaCode("C", "Counter Area", UAVal.ValTP.vt_int16, 137));
        this.setAreaCode(new AreaCode("D", "Data Memory Area", UAVal.ValTP.vt_bool, 2));
        this.setAreaCode(new AreaCode("D", "Data Memory Area", UAVal.ValTP.vt_int16, 130));
        this.setAreaCode(new AreaCode("DR", "Data Register", UAVal.ValTP.vt_int16, 188));
        for (int i = 0; i <= 15; ++i) {
            final String str_i = (i < 10) ? ("0" + i) : ("" + i);
            this.setAreaCode(new AreaCode("E" + str_i + ":", "Expansion Data Memory", UAVal.ValTP.vt_bool, 32 + i));
            if (cj2) {
                this.setAreaCode(new AreaCode("E" + str_i + ":", "Expansion Data Memory", UAVal.ValTP.vt_int16, 80 + i));
            }
            else {
                this.setAreaCode(new AreaCode("E" + str_i + ":", "Expansion Data Memory", UAVal.ValTP.vt_int16, 160 + i));
            }
        }
        for (int i = 16; i <= 24; ++i) {
            final String str_i = (i < 10) ? ("0" + i) : ("" + i);
            this.setAreaCode(new AreaCode("E" + str_i + ":", "Expansion Data Memory", UAVal.ValTP.vt_bool, 224 + i));
            this.setAreaCode(new AreaCode("E" + str_i + ":", "Expansion Data Memory", UAVal.ValTP.vt_int16, 96 + i));
        }
        this.setAreaCode(new AreaCode("IR", "Index Register", UAVal.ValTP.vt_int32, 220));
    }
}
