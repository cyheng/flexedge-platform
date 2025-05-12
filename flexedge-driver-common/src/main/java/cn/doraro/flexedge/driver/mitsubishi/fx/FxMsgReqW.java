// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

public class FxMsgReqW extends FxMsg
{
    int baseAddr;
    int startAddr;
    int byteNum;
    byte[] byteVals;
    
    public FxMsgReqW() {
        this.startAddr = 0;
        this.byteNum = -1;
        this.byteVals = null;
    }
    
    public FxMsgReqW asStartAddr(final int baseaddr, final int startaddr) {
        this.baseAddr = baseaddr;
        this.startAddr = startaddr;
        return this;
    }
    
    public FxMsgReqW asBytesVal(final byte[] bs) {
        if (bs.length >= 64) {
            throw new IllegalArgumentException("reg num cannot big than 0x40");
        }
        this.byteNum = bs.length;
        this.byteVals = bs;
        return this;
    }
    
    @Override
    public byte[] toBytes() {
        if (!this.bExt) {
            return this.toBytes_31();
        }
        final int n = 13 + this.byteNum * 2;
        final byte[] bs = new byte[n];
        bs[0] = 2;
        bs[1] = 69;
        bs[2] = 49;
        bs[3] = 48;
        FxMsg.toAsciiHexBytes(this.baseAddr + this.startAddr, bs, 4, 4);
        FxMsg.toAsciiHexBytes(this.byteNum, bs, 8, 2);
        for (int i = 0; i < this.byteNum; ++i) {
            FxMsg.toAsciiHexBytes(this.byteVals[i], bs, 10 + i * 2, 2);
        }
        bs[n - 3] = 3;
        final int crc = FxMsg.calCRC(bs, 1, n - 3);
        FxMsg.toAsciiHexBytes(crc, bs, n - 2, 2);
        return bs;
    }
    
    public byte[] toBytes_31() {
        final int n = 11 + this.byteNum * 2;
        final byte[] bs = new byte[n];
        bs[0] = 2;
        bs[1] = 49;
        FxMsg.toAsciiHexBytes(this.baseAddr + this.startAddr, bs, 2, 4);
        FxMsg.toAsciiHexBytes(this.byteNum, bs, 6, 2);
        for (int i = 0; i < this.byteNum; ++i) {
            FxMsg.toAsciiHexBytes(this.byteVals[i], bs, 8 + i * 2, 2);
        }
        bs[n - 3] = 3;
        final int crc = FxMsg.calCRC(bs, 1, n - 3);
        FxMsg.toAsciiHexBytes(crc, bs, n - 2, 2);
        return bs;
    }
    
    public int getRetOffsetBytes() {
        return this.startAddr;
    }
}
