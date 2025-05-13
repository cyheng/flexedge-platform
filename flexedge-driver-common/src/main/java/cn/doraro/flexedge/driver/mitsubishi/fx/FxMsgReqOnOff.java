

package cn.doraro.flexedge.driver.mitsubishi.fx;

public class FxMsgReqOnOff extends FxMsg {
    int baseAddr;
    int startAddr;
    boolean bOn;

    public FxMsgReqOnOff() {
        this.startAddr = 0;
    }

    public FxMsgReqOnOff asOnOrOff(final boolean b_on) {
        this.bOn = b_on;
        return this;
    }

    public FxMsgReqOnOff asStartAddr(final int baseaddr, final int startaddr) {
        this.baseAddr = baseaddr;
        this.startAddr = startaddr;
        return this;
    }

    @Override
    public byte[] toBytes() {
        if (!this.bExt) {
            return this.toBytes_7_8();
        }
        final int n = 10;
        final byte[] bs = new byte[n];
        bs[0] = 2;
        bs[1] = 69;
        if (this.bOn) {
            bs[2] = 55;
        } else {
            bs[2] = 56;
        }
        final int addr = this.baseAddr + this.startAddr;
        final int l = (byte) (addr & 0xFF);
        final int h = (byte) (addr >> 8 & 0xFF);
        FxMsg.toAsciiHexBytes(l, bs, 3, 2);
        FxMsg.toAsciiHexBytes(h, bs, 5, 2);
        bs[7] = 3;
        final int crc = FxMsg.calCRC(bs, 1, 7);
        FxMsg.toAsciiHexBytes(crc, bs, 8, 2);
        return bs;
    }

    public byte[] toBytes_7_8() {
        final int n = 9;
        final byte[] bs = new byte[n];
        bs[0] = 2;
        if (this.bOn) {
            bs[1] = 55;
        } else {
            bs[1] = 56;
        }
        FxMsg.toAsciiHexBytes(this.baseAddr + this.startAddr, bs, 2, 4);
        bs[6] = 3;
        final int crc = FxMsg.calCRC(bs, 1, 6);
        FxMsg.toAsciiHexBytes(crc, bs, 7, 2);
        return bs;
    }

    public int getRetOffsetBytes() {
        return this.startAddr;
    }
}
