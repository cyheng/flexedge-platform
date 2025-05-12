// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

public enum S7MemTp {
    I(129, true, true, Tp2ValTP.VTP_NOR, S7ValTp.B),
    Q(130, true, true, Tp2ValTP.VTP_NOR, S7ValTp.B),
    M(131, true, true, Tp2ValTP.VTP_NOR, S7ValTp.B),
    DB(132, true, true, Tp2ValTP.VTP_NOR, S7ValTp.W),
    C(28, false, true, Tp2ValTP.VTP_W_S, S7ValTp.W),
    T(29, false, true, Tp2ValTP.VTP_W_S, S7ValTp.D),
    V(132, true, true, Tp2ValTP.VTP_NOR, S7ValTp.B),
    AI(6, false, false, Tp2ValTP.VTP_W_S, S7ValTp.W),
    AQ(7, false, true, Tp2ValTP.VTP_W_S, S7ValTp.W);

    private final int val;
    private final boolean canBit;
    private final boolean bWrite;
    private final UAVal.ValTP[] valTPs;
    private final S7ValTp defaultVT;

    private S7MemTp(final int v, final boolean hasbit, final boolean bwrite, final UAVal.ValTP[] vtps, final S7ValTp def_svt) {
        this.val = v;
        this.canBit = hasbit;
        this.bWrite = bwrite;
        this.valTPs = vtps;
        this.defaultVT = def_svt;
    }

    public static S7MemTp valOf(final String ss) {
        switch (ss) {
            case "C": {
                return S7MemTp.C;
            }
            case "DB": {
                return S7MemTp.DB;
            }
            case "T": {
                return S7MemTp.T;
            }
            case "I": {
                return S7MemTp.I;
            }
            case "Q": {
                return S7MemTp.Q;
            }
            case "M": {
                return S7MemTp.M;
            }
            case "V": {
                return S7MemTp.V;
            }
            case "AI": {
                return S7MemTp.AI;
            }
            case "AQ": {
                return S7MemTp.AQ;
            }
            default: {
                return null;
            }
        }
    }

    public static S7MemTp parseStrHead(final String ss) {
        if (Convert.isNullOrEmpty(ss)) {
            return null;
        }
        final S7MemTp mt = valOf(ss.substring(0, 1));
        if (mt != null) {
            return mt;
        }
        if (ss.length() <= 1) {
            return null;
        }
        return valOf(ss.substring(0, 2));
    }

    public static S7MemTp valOf(final short v) {
        switch (v) {
            case 129: {
                return S7MemTp.I;
            }
            case 130: {
                return S7MemTp.Q;
            }
            case 131: {
                return S7MemTp.M;
            }
            case 132: {
                return S7MemTp.DB;
            }
            case 28: {
                return S7MemTp.C;
            }
            case 29: {
                return S7MemTp.T;
            }
            case 135: {
                return S7MemTp.V;
            }
            case 6: {
                return S7MemTp.AI;
            }
            case 7: {
                return S7MemTp.AQ;
            }
            default: {
                throw new IllegalArgumentException("invalid S7 Mem Tp val=" + v);
            }
        }
    }

    public int getVal() {
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

    public S7ValTp getDefaultS7ValTp() {
        return this.defaultVT;
    }
}
