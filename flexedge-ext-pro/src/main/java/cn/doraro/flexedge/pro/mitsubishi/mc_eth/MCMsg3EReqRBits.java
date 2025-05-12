// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

public class MCMsg3EReqRBits extends MCMsg3EReq
{
    MCCode code;
    int startAddr;
    int num;
    
    public MCMsg3EReqRBits() {
        this.code = null;
    }
    
    @Override
    public int getCmd() {
        return 1025;
    }
    
    @Override
    public int getCmdSub() {
        return 1;
    }
    
    @Override
    public int calRespLenAscii() {
        return 4 + this.num;
    }
    
    @Override
    public int calRespLenBin() {
        return 2 + this.num / 2 + ((this.num % 2 > 0) ? 1 : 0);
    }
    
    public MCMsg3EReqRBits asReadPM(final MCCode code, final int startaddr, final int num) {
        this.code = code;
        this.startAddr = startaddr;
        this.num = num;
        return this;
    }
    
    @Override
    protected byte[] packDataAscii() throws Exception {
        final byte[] bs = new byte[12];
        bs[0] = (byte)this.code.asciiVal_Q_L.charAt(0);
        bs[1] = (byte)this.code.asciiVal_Q_L.charAt(1);
        MCMsg.toAsciiHexBytes(this.startAddr, bs, 2, 6);
        MCMsg.toAsciiHexBytes(this.num, bs, 8, 4);
        return bs;
    }
    
    @Override
    protected byte[] packDataBin() throws Exception {
        final byte[] bs = new byte[6];
        MCMsg.toBinHexBytes(this.startAddr, bs, 0, 3);
        bs[3] = (byte)(int)this.code.binVal_Q_L;
        MCMsg.toBinHexBytes(this.num, bs, 4, 2);
        return bs;
    }
}
