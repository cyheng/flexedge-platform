

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;

public enum PPIMemTp {
    S(4, true, true, PPITp2ValTP.VTP_NOR),
    SM(5, true, true, PPITp2ValTP.VTP_NOR),
    AI(6, false, false, PPITp2ValTP.VTP_W_S),
    AQ(7, false, true, PPITp2ValTP.VTP_W_S),
    C(30, false, true, PPITp2ValTP.VTP_W_S),
    HC(32, false, false, PPITp2ValTP.VTP_DW_L),
    T(31, false, true, PPITp2ValTP.VTP_DW_L),
    I(129, true, true, PPITp2ValTP.VTP_NOR),
    Q(130, true, true, PPITp2ValTP.VTP_NOR),
    M(131, true, true, PPITp2ValTP.VTP_NOR),
    V(132, true, true, PPITp2ValTP.VTP_NOR);

    private final short val;
    private final boolean canBit;
    private final boolean bWrite;
    private final UAVal.ValTP[] valTPs;

    private PPIMemTp(final int v, final boolean hasbit, final boolean bwrite, final UAVal.ValTP[] vtps) {
        this.val = (short) v;
        this.canBit = hasbit;
        this.bWrite = bwrite;
        this.valTPs = vtps;
    }

    public static PPIMemTp valOf(final String ss) {
        switch (ss) {
            case "S": {
                return PPIMemTp.S;
            }
            case "SM": {
                return PPIMemTp.SM;
            }
            case "AI": {
                return PPIMemTp.AI;
            }
            case "AQ": {
                return PPIMemTp.AQ;
            }
            case "C": {
                return PPIMemTp.C;
            }
            case "HC": {
                return PPIMemTp.HC;
            }
            case "T": {
                return PPIMemTp.T;
            }
            case "I": {
                return PPIMemTp.I;
            }
            case "Q": {
                return PPIMemTp.Q;
            }
            case "M": {
                return PPIMemTp.M;
            }
            case "V": {
                return PPIMemTp.V;
            }
            default: {
                return null;
            }
        }
    }

    public static PPIMemTp valOf(final short v) {
        switch (v) {
            case 4: {
                return PPIMemTp.S;
            }
            case 5: {
                return PPIMemTp.SM;
            }
            case 6: {
                return PPIMemTp.AI;
            }
            case 7: {
                return PPIMemTp.AQ;
            }
            case 30: {
                return PPIMemTp.C;
            }
            case 32: {
                return PPIMemTp.HC;
            }
            case 31: {
                return PPIMemTp.T;
            }
            case 129: {
                return PPIMemTp.I;
            }
            case 130: {
                return PPIMemTp.Q;
            }
            case 131: {
                return PPIMemTp.M;
            }
            case 132: {
                return PPIMemTp.V;
            }
            default: {
                throw new IllegalArgumentException("invalid PPI Mem Tp val=" + v);
            }
        }
    }

    public short getVal() {
        return this.val;
    }

    public boolean hasBit() {
        return this.canBit;
    }

    public boolean canWrite() {
        return this.bWrite;
    }

    public UAVal.ValTP[] getFitValTPs() {
        return this.valTPs;
    }
}
