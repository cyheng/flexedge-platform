

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.util.Convert;

import java.util.List;

public class MCMsg3EReqRRWords extends MCMsg3EReq {
    private List<RRItem> wordRRItems;
    private List<RRItem> dwordRRItems;

    public MCMsg3EReqRRWords() {
        this.wordRRItems = null;
        this.dwordRRItems = null;
    }

    @Override
    public int getCmd() {
        return 1027;
    }

    @Override
    public int getCmdSub() {
        return 0;
    }

    public MCMsg3EReqRRWords asRRItems(final List<RRItem> word_rris, final List<RRItem> dword_rris) {
        if (word_rris != null) {
            if (word_rris.size() > 255) {
                throw new IllegalArgumentException("word item cannot bigger than 255");
            }
            for (final RRItem rri : word_rris) {
                if (rri.code.binVal_Q_L == null) {
                    throw new IllegalArgumentException("MCCode[" + rri.code.symbol + "] has no binVal in Q/L");
                }
                if (Convert.isNullOrEmpty(rri.code.asciiVal_Q_L)) {
                    throw new IllegalArgumentException("MCCode[" + rri.code.symbol + "] has no asciiVal in Q/L");
                }
            }
        }
        if (dword_rris != null) {
            if (dword_rris.size() > 255) {
                throw new IllegalArgumentException("dword item cannot bigger than 255");
            }
            for (final RRItem rri : dword_rris) {
                if (rri.code.binVal_Q_L == null) {
                    throw new IllegalArgumentException("MCCode[" + rri.code.symbol + "] has no binVal in Q/L");
                }
                if (Convert.isNullOrEmpty(rri.code.asciiVal_Q_L)) {
                    throw new IllegalArgumentException("MCCode[" + rri.code.symbol + "] has no asciiVal in Q/L");
                }
            }
        }
        this.wordRRItems = word_rris;
        this.dwordRRItems = dword_rris;
        return this;
    }

    @Override
    public int calRespLenAscii() {
        int w_n = 0;
        if (this.wordRRItems != null) {
            w_n = this.wordRRItems.size();
        }
        int dw_n = 0;
        if (this.dwordRRItems != null) {
            dw_n = this.dwordRRItems.size();
        }
        return w_n * 4 + dw_n * 8;
    }

    @Override
    public int calRespLenBin() {
        int w_n = 0;
        if (this.wordRRItems != null) {
            w_n = this.wordRRItems.size();
        }
        int dw_n = 0;
        if (this.dwordRRItems != null) {
            dw_n = this.dwordRRItems.size();
        }
        return w_n * 2 + dw_n * 4;
    }

    @Override
    protected byte[] packDataAscii() throws Exception {
        int w_n = 0;
        if (this.wordRRItems != null) {
            w_n = this.wordRRItems.size();
        }
        int dw_n = 0;
        if (this.dwordRRItems != null) {
            dw_n = this.dwordRRItems.size();
        }
        final int bs_len = 2 + w_n * 8 + dw_n * 8;
        final byte[] bs = new byte[bs_len];
        bs[0] = (byte) w_n;
        bs[1] = (byte) dw_n;
        for (int i = 0; i < w_n; ++i) {
            final RRItem rri = this.wordRRItems.get(i);
            final int sidx = 2 + i * 8;
            final String cc = rri.code.asciiVal_Q_L;
            bs[sidx] = (byte) cc.charAt(0);
            bs[sidx + 1] = (byte) cc.charAt(1);
            MCMsg.toAsciiHexBytes(rri.addr, bs, sidx + 2, 6);
        }
        for (int i = 0; i < dw_n; ++i) {
            final RRItem rri = this.dwordRRItems.get(i);
            final int sidx = 2 + w_n * 8 + i * 8;
            final String cc = rri.code.asciiVal_Q_L;
            bs[sidx] = (byte) cc.charAt(0);
            bs[sidx + 1] = (byte) cc.charAt(1);
            MCMsg.toAsciiHexBytes(rri.addr, bs, sidx + 2, 6);
        }
        return bs;
    }

    @Override
    protected byte[] packDataBin() throws Exception {
        int w_n = 0;
        if (this.wordRRItems != null) {
            w_n = this.wordRRItems.size();
        }
        int dw_n = 0;
        if (this.dwordRRItems != null) {
            dw_n = this.dwordRRItems.size();
        }
        final int bs_len = 2 + w_n * 4 + dw_n * 4;
        final byte[] bs = new byte[bs_len];
        bs[0] = (byte) w_n;
        bs[1] = (byte) dw_n;
        for (int i = 0; i < w_n; ++i) {
            final RRItem rri = this.wordRRItems.get(i);
            final int sidx = 2 + i * 4;
            MCMsg.toBinHexBytes(rri.addr, bs, sidx, 3);
            bs[sidx + 3] = (byte) (int) rri.code.binVal_Q_L;
        }
        for (int i = 0; i < dw_n; ++i) {
            final RRItem rri = this.dwordRRItems.get(i);
            final int sidx = 2 + w_n * 4 + i * 4;
            MCMsg.toBinHexBytes(rri.addr, bs, sidx, 3);
            bs[sidx + 3] = (byte) (int) rri.code.binVal_Q_L;
        }
        return bs;
    }
}
