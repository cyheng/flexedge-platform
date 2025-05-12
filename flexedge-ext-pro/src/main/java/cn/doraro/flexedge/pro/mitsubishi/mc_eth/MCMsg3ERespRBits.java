// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

public class MCMsg3ERespRBits extends MCMsg3EResp {
    private boolean[] respBits;

    public MCMsg3ERespRBits() {
        this.respBits = null;
    }

    public boolean[] getRespBits() {
        return this.respBits;
    }

    @Override
    protected boolean parseRespDataBin(final byte[] bs) {
        final int wn = (bs.length - 2) * 2;
        this.respBits = new boolean[wn];
        for (int i = 2; i < bs.length; ++i) {
            final int sidx = (i - 2) * 2;
            this.respBits[sidx] = ((bs[i] & 0x10) > 0);
            if (sidx + 1 < this.respBits.length) {
                this.respBits[sidx + 1] = ((bs[i] & 0x1) > 0);
            }
        }
        return true;
    }

    @Override
    protected boolean parseRespDataAscii(final byte[] bs) {
        final int wn = bs.length - 4;
        this.respBits = new boolean[wn];
        for (int i = 0; i < wn; ++i) {
            this.respBits[i] = (bs[4 + i] == 49);
        }
        return true;
    }
}
