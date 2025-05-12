// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

public class PPIMsgReqRTC extends PPIMsgReq
{
    int offset;
    short readNum;
    
    public PPIMsgReqRTC() {
        this.readNum = 1;
    }
    
    public PPIMsgReqRTC withTick(final int offset, final short readnum) {
        this.offset = offset;
        this.readNum = readnum;
        return this;
    }
    
    public PPIMsgReqRTC withMemTp(final PPIMemTp memtp) {
        if (memtp != PPIMemTp.T && memtp != PPIMemTp.C) {
            throw new IllegalArgumentException("invalid mem tp");
        }
        this.memTp = memtp;
        return this;
    }
    
    @Override
    protected short getStartD() {
        return 104;
    }
    
    @Override
    public short getFC() {
        return 108;
    }
    
    @Override
    public int getRetOffsetBytes() {
        if (this.memTp == PPIMemTp.T) {
            return this.offset * 4;
        }
        if (this.memTp == PPIMemTp.C) {
            return this.offset * 2;
        }
        return -1;
    }
    
    private short calLen() {
        return 27;
    }
    
    @Override
    public byte[] toBytes() {
        final short le = this.calLen();
        final byte[] rets = new byte[le + 6];
        rets[0] = (rets[3] = (byte)this.getStartD());
        rets[1] = (rets[2] = (byte)le);
        rets[4] = (byte)this.da;
        rets[5] = (byte)this.sa;
        rets[6] = (byte)this.getFC();
        rets[7] = 50;
        rets[8] = 1;
        final byte[] array = rets;
        final int n = 9;
        final byte[] array2 = rets;
        final int n2 = 10;
        final byte[] array3 = rets;
        final int n3 = 11;
        final byte[] array4 = rets;
        final int n4 = 12;
        final byte b = 0;
        array3[n3] = (array4[n4] = b);
        array[n] = (array2[n2] = b);
        rets[13] = 0;
        rets[14] = 14;
        rets[16] = (rets[15] = 0);
        rets[17] = 4;
        rets[18] = 1;
        rets[19] = 18;
        rets[20] = 10;
        rets[21] = 16;
        rets[22] = (byte)this.memTp.getVal();
        rets[23] = 0;
        rets[24] = (byte)this.readNum;
        rets[26] = (rets[25] = 0);
        rets[27] = (byte)this.memTp.getVal();
        final int offaddr = this.offset;
        rets[28] = (byte)(offaddr >> 16 & 0xFF);
        rets[29] = (byte)(offaddr >> 8 & 0xFF);
        rets[30] = (byte)(offaddr & 0xFF);
        rets[31] = PPIMsg.calChkSum(rets, 4, 27);
        rets[32] = 22;
        return rets;
    }
}
