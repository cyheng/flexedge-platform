

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.util.Convert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PPIMsgRespRTC extends PPIMsgResp {
    PPIMemTp memTp;
    short da;
    short sa;
    short fc;
    ArrayList<TItem> t_items;
    ArrayList<CItem> c_items;
    private byte[] retData;

    public PPIMsgRespRTC(final PPIMemTp mtp) {
        this.sa = 0;
        this.fc = 8;
        this.t_items = null;
        this.c_items = null;
        this.retData = null;
        this.memTp = mtp;
    }

    public static PPIMsgRespRTC parseFromBS(final PPIMemTp memtp, final byte[] bs, final StringBuilder failedr) {
        final PPIMsgRespRTC ret = new PPIMsgRespRTC(memtp);
        ret.da = (short) (bs[4] & 0xFF);
        ret.sa = (short) (bs[5] & 0xFF);
        if (ret.fc != (short) (bs[6] & 0xFF)) {
            failedr.append("fucntion code err");
            return null;
        }
        if (bs[22] != 9) {
            failedr.append("not read bytes");
            return null;
        }
        int retnum = bs[24] & 0xFF;
        if (memtp == PPIMemTp.T) {
            retnum /= 5;
            ret.t_items = new ArrayList<TItem>(retnum);
            ret.retData = new byte[retnum * 4];
            for (int i = 0; i < retnum; ++i) {
                final TItem ti = new TItem();
                ti.st = (bs[25 + i * 5] & 0xFF);
                long v = (long) (bs[25 + i * 5 + 1] << 24) & -1L;
                v += (bs[25 + i * 5 + 2] << 16 & 0xFFFFFF);
                v += (bs[25 + i * 5 + 3] << 8 & 0xFFFF);
                v += (bs[25 + i * 5 + 4] & 0xFF);
                ti.val = v;
                System.arraycopy(bs, 25 + i * 5 + 1, ret.retData, i * 4, 4);
                ret.t_items.add(ti);
            }
        } else {
            if (memtp != PPIMemTp.C) {
                return null;
            }
            retnum /= 3;
            ret.c_items = new ArrayList<CItem>(retnum);
            ret.retData = new byte[retnum * 2];
            for (int i = 0; i < retnum; ++i) {
                final CItem ti2 = new CItem();
                ti2.st = (bs[25 + i * 5] & 0xFF);
                int v2 = bs[25 + i * 5 + 1] << 8 & 0xFFFF;
                v2 += (bs[25 + i * 5 + 2] & 0xFF);
                ti2.val = v2;
                ret.retData[i * 4] = bs[25 + i * 5 + 1];
                ret.retData[i * 4 + 1] = bs[25 + i * 5 + 2];
                ret.c_items.add(ti2);
            }
        }
        return ret;
    }

    public static PPIMsgRespRTC parseFromStream(final PPIMemTp memtp, final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        final byte[] bs = PPIMsg.readFromStream(inputs, timeout);
        if (bs == null) {
            failedr.append("read ppi msg err");
            return null;
        }
        if (bs.length == 1) {
            return null;
        }
        if (PPIMsgRespRTC.log.isTraceEnabled()) {
            final String tmps = Convert.byteArray2HexStr(bs, " ");
            PPIMsgRespRTC.log.trace("resp <-" + tmps);
        }
        return parseFromBS(memtp, bs, failedr);
    }

    @Override
    protected short getStartD() {
        return 104;
    }

    public short getDestAddr() {
        return this.da;
    }

    public short getSorAddr() {
        return this.sa;
    }

    @Override
    public byte[] toBytes() {
        return null;
    }

    @Override
    public byte[] getRetData() {
        return this.retData;
    }

    @Override
    public String toString() {
        String r = this.sa + "->" + this.da + " [";
        if (this.memTp == PPIMemTp.T) {
            for (final TItem ti : this.t_items) {
                r = r + ti.val + " ";
            }
        } else if (this.memTp == PPIMemTp.C) {
            for (final CItem ti2 : this.c_items) {
                r = r + ti2.val + " ";
            }
        }
        r += "]";
        return r;
    }

    public static class TItem {
        public int st;
        public long val;

        public TItem() {
            this.st = -1;
            this.val = -1L;
        }
    }

    public static class CItem {
        public int st;
        public int val;

        public CItem() {
            this.st = -1;
            this.val = -1;
        }
    }
}
