// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

public class FxMsgReqR extends FxMsg
{
    int baseAddr;
    int startAddr;
    int byteNum;
    
    public FxMsgReqR() {
        this.startAddr = 0;
        this.byteNum = 5;
    }
    
    public FxMsgReqR asStartAddr(final int baseaddr, final int startaddr) {
        this.baseAddr = baseaddr;
        this.startAddr = startaddr;
        return this;
    }
    
    public FxMsgReqR asByteNum(final int num) {
        if (num >= 64) {
            throw new IllegalArgumentException("reg num cannot big than 0x40");
        }
        this.byteNum = num;
        return this;
    }
    
    @Override
    public byte[] toBytes() {
        if (!this.bExt) {
            return this.toBytes_30();
        }
        final int n = 13;
        final byte[] bs = new byte[n];
        bs[0] = 2;
        bs[1] = 69;
        bs[2] = (bs[3] = 48);
        FxMsg.toAsciiHexBytes(this.baseAddr + this.startAddr, bs, 4, 4);
        FxMsg.toAsciiHexBytes(this.byteNum, bs, 8, 2);
        bs[10] = 3;
        final int crc = FxMsg.calCRC(bs, 1, 10);
        FxMsg.toAsciiHexBytes(crc, bs, 11, 2);
        return bs;
    }
    
    public byte[] toBytes_30() {
        final int n = 11;
        final byte[] bs = new byte[n];
        bs[0] = 2;
        bs[1] = 48;
        FxMsg.toAsciiHexBytes(this.baseAddr + this.startAddr, bs, 2, 4);
        FxMsg.toAsciiHexBytes(this.byteNum, bs, 6, 2);
        bs[8] = 3;
        final int crc = FxMsg.calCRC(bs, 1, 8);
        FxMsg.toAsciiHexBytes(crc, bs, 9, 2);
        return bs;
    }
    
    public int getRetOffsetBytes() {
        return this.startAddr;
    }
}
