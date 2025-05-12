// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fxnet;

public class FxNetMsgWR extends FxNetMsg {
    static byte[] CMD_BS2;

    static {
        FxNetMsgWR.CMD_BS2 = new byte[]{87, 82};
    }

    short readNum;

    public FxNetMsgWR() {
        this.readNum = -1;
    }

    public FxNetMsgWR asReadNum(final short rn) {
        this.readNum = rn;
        return this;
    }

    public byte[] getCmdBS2() {
        return FxNetMsgWR.CMD_BS2;
    }

    @Override
    public byte[] toBytes() {
        final byte[] bs = new byte[17];
        bs[0] = 5;
        FxNetMsg.toAsciiHexBytes(this.stationCode, bs, 1, 2);
        FxNetMsg.toAsciiHexBytes(this.pcCode, bs, 3, 2);
        bs[5] = FxNetMsgWR.CMD_BS2[0];
        bs[6] = FxNetMsgWR.CMD_BS2[1];
        bs[7] = this.msgWait;
        final byte[] addrbs = this.getStartAddrBS5();
        bs[8] = addrbs[0];
        bs[9] = addrbs[1];
        bs[10] = addrbs[2];
        bs[11] = addrbs[3];
        bs[12] = addrbs[4];
        FxNetMsg.toAsciiHexBytes(this.readNum, bs, 13, 2);
        final int crc = FxNetMsg.calCRC(bs, 1, 14);
        FxNetMsg.toAsciiHexBytes(crc, bs, 15, 2);
        return bs;
    }
}
