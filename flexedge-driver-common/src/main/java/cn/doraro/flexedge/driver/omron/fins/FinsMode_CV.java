// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.UAVal;

class FinsMode_CV extends FinsMode
{
    protected FinsMode_CV() {
        this.setAreaCode(new AreaCode("CIO", "CIO Bit Area", UAVal.ValTP.vt_bool, 0));
        this.setAreaCode(new AreaCode("A", "Auxiliary Bit Area", UAVal.ValTP.vt_bool, 0));
        this.setAreaCode(new AreaCode("CIO", "CIO Area", UAVal.ValTP.vt_int16, 128));
        this.setAreaCode(new AreaCode("A", "Auxiliary Area", UAVal.ValTP.vt_int16, 128));
        this.setAreaCode(new AreaCode("TS", "Timer Completion Flag (Status)", UAVal.ValTP.vt_bool, 1));
        this.setAreaCode(new AreaCode("CS", "Counter Completion Flag (Status)", UAVal.ValTP.vt_bool, 1));
        this.setAreaCode(new AreaCode("T", "Timer Area", UAVal.ValTP.vt_int16, 129));
        this.setAreaCode(new AreaCode("C", "Counter Area", UAVal.ValTP.vt_int16, 129));
        this.setAreaCode(new AreaCode("D", "Data Memory Area", UAVal.ValTP.vt_int16, 130));
        this.setAreaCode(new AreaCode("DR", "Data Register", UAVal.ValTP.vt_int16, 156));
        for (int i = 0; i <= 7; ++i) {
            final String str_i = (i < 10) ? ("0" + i) : ("" + i);
            this.setAreaCode(new AreaCode("E" + str_i + ":", "Expansion Data Memory", UAVal.ValTP.vt_int16, 144 + i));
        }
    }
}
