// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.UAVal;

public enum S7ValTp {
    X(1, UAVal.ValTP.vt_bool, 1),
    B(2, UAVal.ValTP.vt_uint8, 1),
    C(3, UAVal.ValTP.vt_byte, 1),
    W(4, UAVal.ValTP.vt_uint16, 2),
    D(5, UAVal.ValTP.vt_uint32, 4),
    DATE(6, UAVal.ValTP.vt_str, 2),
    DI(7, UAVal.ValTP.vt_int32, 4),
    DT(8, UAVal.ValTP.vt_date, 8),
    I(9, UAVal.ValTP.vt_int16, 2),
    REAL(10, UAVal.ValTP.vt_float, 4),
    STRING(11, UAVal.ValTP.vt_str, -1),
    T(12, UAVal.ValTP.vt_str, 4),
    TOD(13, UAVal.ValTP.vt_str, 4);

    private final int val;
    private final UAVal.ValTP valTP;
    private final int byteNum;

    private S7ValTp(final int v, final UAVal.ValTP vtp, final int byte_num) {
        this.val = v;
        this.valTP = vtp;
        this.byteNum = byte_num;
    }

    public static S7ValTp valOf(final String nn) {
        final String upperCase = nn.toUpperCase();
        switch (upperCase) {
            case "X": {
                return S7ValTp.X;
            }
            case "B":
            case "BYTE": {
                return S7ValTp.B;
            }
            case "C":
            case "CHAR": {
                return S7ValTp.C;
            }
            case "W": {
                return S7ValTp.W;
            }
            case "D":
            case "DWORD": {
                return S7ValTp.D;
            }
            case "DATE": {
                return S7ValTp.DATE;
            }
            case "DI":
            case "DINT": {
                return S7ValTp.DI;
            }
            case "DT": {
                return S7ValTp.DT;
            }
            case "I":
            case "INT": {
                return S7ValTp.I;
            }
            case "REAL": {
                return S7ValTp.REAL;
            }
            case "T":
            case "TIME": {
                return S7ValTp.T;
            }
            case "TOD": {
                return S7ValTp.TOD;
            }
            default: {
                return null;
            }
        }
    }

    public static S7ValTp valOf(final int v) {
        switch (v) {
            case 1: {
                return S7ValTp.X;
            }
            case 2: {
                return S7ValTp.B;
            }
            case 3: {
                return S7ValTp.C;
            }
            case 4: {
                return S7ValTp.W;
            }
            case 5: {
                return S7ValTp.D;
            }
            case 6: {
                return S7ValTp.DATE;
            }
            case 7: {
                return S7ValTp.DI;
            }
            case 8: {
                return S7ValTp.DT;
            }
            case 9: {
                return S7ValTp.I;
            }
            case 10: {
                return S7ValTp.REAL;
            }
            case 11: {
                return S7ValTp.STRING;
            }
            case 12: {
                return S7ValTp.T;
            }
            case 13: {
                return S7ValTp.TOD;
            }
            default: {
                return null;
            }
        }
    }

    public int getVal() {
        return this.val;
    }

    public UAVal.ValTP getValTP() {
        return this.valTP;
    }

    public int getByteNum() {
        return this.byteNum;
    }
}
