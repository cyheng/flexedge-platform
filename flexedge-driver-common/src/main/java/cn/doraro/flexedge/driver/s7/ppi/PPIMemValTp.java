// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;

public enum PPIMemValTp {
    BIT(1),
    B(2),
    W(4),
    D(6);

    private final short val;

    private PPIMemValTp(final int v) {
        this.val = (short) v;
    }

    public static PPIMemValTp valOf(final char c) {
        switch (c) {
            case 'B': {
                return PPIMemValTp.B;
            }
            case 'W': {
                return PPIMemValTp.W;
            }
            case 'D': {
                return PPIMemValTp.D;
            }
            default: {
                return null;
            }
        }
    }

    public static PPIMemValTp valOf(final short v) {
        switch (v) {
            case 2: {
                return PPIMemValTp.B;
            }
            case 4: {
                return PPIMemValTp.W;
            }
            case 6: {
                return PPIMemValTp.D;
            }
            default: {
                return null;
            }
        }
    }

    public static PPIMemValTp transFromValTp(final UAVal.ValTP vtp) {
        switch (vtp) {
            case vt_bool: {
                return PPIMemValTp.BIT;
            }
            case vt_byte:
            case vt_char: {
                return PPIMemValTp.B;
            }
            case vt_int16:
            case vt_uint16: {
                return PPIMemValTp.W;
            }
            case vt_int32:
            case vt_uint32:
            case vt_float: {
                return PPIMemValTp.D;
            }
            default: {
                return null;
            }
        }
    }

    public short getVal() {
        return this.val;
    }

    public int getByteNum() {
        switch (this.val) {
            case 2: {
                return 1;
            }
            case 4: {
                return 2;
            }
            case 6: {
                return 4;
            }
            default: {
                return 1;
            }
        }
    }

    public int getBitNum() {
        switch (this.val) {
            case 1: {
                return 1;
            }
            case 2: {
                return 8;
            }
            case 4: {
                return 16;
            }
            case 6: {
                return 32;
            }
            default: {
                return 8;
            }
        }
    }
}
