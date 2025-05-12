// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.f800;

public class F800MsgReq extends F800Msg {
    int cmdCode;

    public F800MsgReq() {
        this.cmdCode = 0;
    }

    public static F800MsgReq parseFrom(final byte[] bs) {
        if (bs.length != 7) {
            return null;
        }
        if (bs[0] != 5) {
            return null;
        }
        final int crc = F800Msg.calCRC(bs, 1, 4);
        final int crc2 = F800Msg.fromAsciiHexBytes(bs, 5, 2);
        if (crc != crc2) {
            return null;
        }
        final F800MsgReq ret = new F800MsgReq();
        ret.stationNo = F800Msg.fromAsciiHexBytes(bs, 1, 2);
        ret.cmdCode = F800Msg.fromAsciiHexBytes(bs, 3, 2);
        return ret;
    }

    public F800MsgReq asCmdCode(final int cc) {
        this.cmdCode = cc;
        return this;
    }

    public int getCmdCode() {
        return this.cmdCode;
    }

    public byte[] packTo() {
        final byte[] rets = new byte[7];
        rets[0] = 5;
        F800Msg.toAsciiHexBytes(this.getStationNo(), rets, 1, 2);
        F800Msg.toAsciiHexBytes(this.getCmdCode(), rets, 3, 2);
        final int crc = F800Msg.calCRC(rets, 1, 4);
        F800Msg.toAsciiHexBytes(crc, rets, 5, 2);
        return rets;
    }
}
