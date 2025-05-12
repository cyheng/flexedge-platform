// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

public class MCMsg3EReqWBits extends MCMsg3EReq {
    MCCode code;
    int startAddr;
    boolean[] bitVals;

    public MCMsg3EReqWBits() {
        this.code = null;
        this.bitVals = null;
    }

    @Override
    public int getCmd() {
        return 5121;
    }

    @Override
    public int getCmdSub() {
        return 1;
    }

    @Override
    public int calRespLenAscii() {
        return 4;
    }

    @Override
    public int calRespLenBin() {
        return 2;
    }

    public MCMsg3EReqWBits asWritePM(final MCCode code, final int startaddr, final boolean[] bitvals) {
        this.code = code;
        this.startAddr = startaddr;
        this.bitVals = bitvals;
        return this;
    }

    @Override
    protected byte[] packDataAscii() throws Exception {
        final byte[] bs = new byte[12 + this.bitVals.length];
        bs[0] = (byte) this.code.asciiVal_Q_L.charAt(0);
        bs[1] = (byte) this.code.asciiVal_Q_L.charAt(1);
        MCMsg.toAsciiHexBytes(this.startAddr, bs, 2, 6);
        MCMsg.toAsciiHexBytes(this.bitVals.length, bs, 8, 4);
        for (int i = 0; i < this.bitVals.length; ++i) {
            bs[12 + i] = (byte) (this.bitVals[i] ? 49 : 48);
        }
        return bs;
    }

    @Override
    protected byte[] packDataBin() throws Exception {
        final int byten = this.bitVals.length / 2 + ((this.bitVals.length % 2 > 0) ? 1 : 0);
        final byte[] bs = new byte[6 + byten];
        MCMsg.toBinHexBytes(this.startAddr, bs, 0, 3);
        bs[3] = (byte) (int) this.code.binVal_Q_L;
        MCMsg.toBinHexBytes(this.bitVals.length, bs, 4, 2);
        for (int i = 0; i < byten; ++i) {
            final boolean b0 = this.bitVals[i * 2];
            boolean b2 = false;
            if (i * 2 + 1 < this.bitVals.length) {
                b2 = this.bitVals[i * 2 + 1];
            }
            short bv = 0;
            if (b0) {
                bv += 16;
            }
            if (b2) {
                ++bv;
            }
            bs[6 + i] = (byte) bv;
        }
        return bs;
    }
}
