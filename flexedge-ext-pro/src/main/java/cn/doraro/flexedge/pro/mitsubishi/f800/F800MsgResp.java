// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.f800;

public class F800MsgResp extends F800Msg {
    byte[] respBS;

    public F800MsgResp() {
        this.respBS = null;
    }

    public static F800MsgResp parseFrom(final byte[] bs) {
        if (bs.length < 6) {
            return null;
        }
        if (bs[0] != 2 || bs[bs.length - 3] != 3) {
            return null;
        }
        final int dlen = bs.length - 6;
        if (dlen != 2 && dlen != 4) {
            return null;
        }
        final int crc = F800Msg.calCRC(bs, 1, dlen + 2);
        final int crc2 = F800Msg.fromAsciiHexBytes(bs, dlen + 4, 2);
        if (crc != crc2) {
            return null;
        }
        final F800MsgResp ret = new F800MsgResp();
        ret.stationNo = F800Msg.fromAsciiHexBytes(bs, 1, 2);
        final byte[] dd = new byte[dlen];
        System.arraycopy(bs, 3, dd, 0, dlen);
        ret.respBS = dd;
        return ret;
    }

    public F800MsgResp asRespBS(final byte[] bs) {
        this.respBS = bs;
        return this;
    }

    public F800MsgResp asRespVal(final int v, final int byte_n) {
        F800Msg.toAsciiHexBytes(v, this.respBS = new byte[byte_n], 0, byte_n);
        return this;
    }

    public Integer getRespVal() {
        final int byten = this.respBS.length;
        if (byten == 2) {
            return F800Msg.fromAsciiHexBytes(this.respBS, 0, 2);
        }
        if (byten == 4) {
            return F800Msg.fromAsciiHexBytes(this.respBS, 0, 4);
        }
        return null;
    }

    public byte[] packTo() {
        final int dlen = this.respBS.length;
        final byte[] rets = new byte[6 + dlen];
        rets[0] = 2;
        F800Msg.toAsciiHexBytes(this.getStationNo(), rets, 1, 2);
        System.arraycopy(this.respBS, 0, rets, 3, dlen);
        final int crc = F800Msg.calCRC(rets, 1, dlen + 2);
        rets[dlen + 3] = 3;
        F800Msg.toAsciiHexBytes(crc, rets, dlen + 4, 2);
        return rets;
    }
}
