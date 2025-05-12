// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.util.Convert;

import java.io.IOException;
import java.io.InputStream;

public class PPIMsgRespR extends PPIMsgResp {
    short da;
    short sa;
    short fc;
    int byteNum;
    int bitNum;
    byte[] respBS;

    public PPIMsgRespR(final byte[] bs) {
        this.sa = 0;
        this.fc = 8;
        this.respBS = null;
    }

    public static PPIMsgRespR parseFromBS(final byte[] bs, final StringBuilder failedr) {
        final PPIMsgRespR ret = new PPIMsgRespR(bs);
        ret.da = (short) (bs[4] & 0xFF);
        ret.sa = (short) (bs[5] & 0xFF);
        if (ret.fc != (short) (bs[6] & 0xFF)) {
            failedr.append("fucntion code err");
            return null;
        }
        int byte_num = (bs[16] & 0xFF) - 4;
        if (byte_num < 0) {
            failedr.append("read bytes number is <=0");
            return null;
        }
        if (byte_num == 0 && bs[21] != 255) {
            ret.byteNum = 0;
            ret.bitNum = -1;
            byte_num = (bs[21] & 0xFF);
            System.arraycopy(bs, 22, ret.respBS = new byte[byte_num], 0, byte_num);
            return ret;
        }
        if (byte_num <= 0) {
            failedr.append("read bytes number is <=0");
            return null;
        }
        if (bs[22] != 4) {
            failedr.append("not read bytes");
            return null;
        }
        int bit_num = (bs[23] & 0xFF) << 8;
        bit_num += (bs[24] & 0xFF);
        ret.byteNum = byte_num;
        ret.bitNum = bit_num;
        System.arraycopy(bs, 25, ret.respBS = new byte[byte_num], 0, byte_num);
        return ret;
    }

    public static PPIMsgRespR parseFromStream(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        final byte[] bs = PPIMsg.readFromStream(inputs, timeout);
        if (bs == null) {
            failedr.append("read ppi msg err");
            return null;
        }
        if (bs.length == 1) {
            return null;
        }
        if (PPIMsgRespR.log.isTraceEnabled()) {
            final String tmps = Convert.byteArray2HexStr(bs, " ");
            PPIMsgRespR.log.trace("resp <-" + tmps);
        }
        return parseFromBS(bs, failedr);
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

    public int getByteNum() {
        return this.byteNum;
    }

    public int getBitNum() {
        return this.bitNum;
    }

    @Override
    public byte[] getRetData() {
        return this.respBS;
    }

    @Override
    public byte[] toBytes() {
        return null;
    }

    @Override
    public String toString() {
        return this.sa + "->" + this.da + " [" + Convert.byteArray2HexStr(this.respBS, " ") + "]";
    }
}
