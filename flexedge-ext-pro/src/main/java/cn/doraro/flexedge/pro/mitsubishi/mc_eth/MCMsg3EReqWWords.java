// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

public class MCMsg3EReqWWords extends MCMsg3EReq {
    MCCode code;
    int startAddr;
    short[] wordVals;

    public MCMsg3EReqWWords() {
        this.code = null;
        this.wordVals = null;
    }

    @Override
    public int getCmd() {
        return 5121;
    }

    @Override
    public int getCmdSub() {
        return 0;
    }

    @Override
    public int calRespLenAscii() {
        return 4;
    }

    @Override
    public int calRespLenBin() {
        return 2;
    }

    public MCMsg3EReqWWords asWritePM(final MCCode code, final int startaddr, final short[] wordvals) {
        this.code = code;
        this.startAddr = startaddr;
        this.wordVals = wordvals;
        return this;
    }

    @Override
    protected byte[] packDataAscii() throws Exception {
        final byte[] bs = new byte[12 + this.wordVals.length * 4];
        bs[0] = (byte) this.code.asciiVal_Q_L.charAt(0);
        bs[1] = (byte) this.code.asciiVal_Q_L.charAt(1);
        MCMsg.toAsciiHexBytes(this.startAddr, bs, 2, 6);
        MCMsg.toAsciiHexBytes(this.wordVals.length, bs, 8, 4);
        for (int i = 0; i < this.wordVals.length; ++i) {
            MCMsg.toAsciiHexBytes(this.wordVals[i], bs, 12 + i * 4, 4);
        }
        return bs;
    }

    @Override
    protected byte[] packDataBin() throws Exception {
        final int byten = this.wordVals.length * 2;
        final byte[] bs = new byte[6 + byten];
        MCMsg.toBinHexBytes(this.startAddr, bs, 0, 3);
        bs[3] = (byte) (int) this.code.binVal_Q_L;
        MCMsg.toBinHexBytes(this.wordVals.length, bs, 4, 2);
        for (int i = 0; i < this.wordVals.length; ++i) {
            final short w = this.wordVals[i];
            bs[6 + i * 2 + 1] = (byte) (w >> 8 & 0xFF);
            bs[6 + i * 2] = (byte) w;
        }
        return bs;
    }
}
