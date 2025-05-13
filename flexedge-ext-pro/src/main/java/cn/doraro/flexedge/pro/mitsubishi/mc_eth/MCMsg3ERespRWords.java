

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

public class MCMsg3ERespRWords extends MCMsg3EResp {
    private short[] respWords;

    public MCMsg3ERespRWords() {
        this.respWords = null;
    }

    @Override
    protected boolean parseRespDataBin(final byte[] bs) {
        final int wn = (bs.length - 2) / 2;
        this.respWords = new short[wn];
        for (int i = 0; i < wn; ++i) {
            final int sidx = 2 + i * 2;
            final short v = (short) MCMsg.fromBinHexBytes(bs, sidx, 2);
            this.respWords[i] = v;
        }
        return true;
    }

    @Override
    protected boolean parseRespDataAscii(final byte[] bs) {
        final int wn = (bs.length - 4) / 4;
        this.respWords = new short[wn];
        for (int i = 0; i < wn; ++i) {
            final int sidx = 4 + i * 4;
            final short v = (short) MCMsg.fromAsciiHexBytes(bs, sidx, 4);
            this.respWords[i] = v;
        }
        return true;
    }

    public short[] getRespWords() {
        return this.respWords;
    }
}
