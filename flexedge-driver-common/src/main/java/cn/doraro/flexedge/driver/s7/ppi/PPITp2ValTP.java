

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;

class PPITp2ValTP {
    static final UAVal.ValTP[] VTP_NOR;
    static final UAVal.ValTP[] VTP_DW_L;
    static final UAVal.ValTP[] VTP_W_S;

    static {
        VTP_NOR = new UAVal.ValTP[]{UAVal.ValTP.vt_bool, UAVal.ValTP.vt_byte, UAVal.ValTP.vt_int16, UAVal.ValTP.vt_uint16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_int64};
        VTP_DW_L = new UAVal.ValTP[]{UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_int64};
        VTP_W_S = new UAVal.ValTP[]{UAVal.ValTP.vt_uint16, UAVal.ValTP.vt_int16};
    }
}
