// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.cmode;

import cn.doraro.flexedge.core.util.ILang;

public class CMCmd implements ILang {
    public static String ENDCODE_NOR;
    public static String ENDCODE_FCS_ERR;

    static {
        CMCmd.ENDCODE_NOR = "00";
        CMCmd.ENDCODE_FCS_ERR = "13";
    }

    private String getEndCodeTitle(final String endc) {
        return this.g("encode_" + endc, "");
    }
}
