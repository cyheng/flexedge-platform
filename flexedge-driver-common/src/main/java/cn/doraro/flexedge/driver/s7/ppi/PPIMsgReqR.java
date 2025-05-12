// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;

public class PPIMsgReqR extends PPIMsgReq
{
    int offsetBytes;
    int inBit;
    int readNum;
    
    public PPIMsgReqR() {
        this.offsetBytes = 0;
        this.inBit = -1;
        this.readNum = 1;
    }
    
    @Override
    protected short getStartD() {
        return 104;
    }
    
    @Override
    public short getFC() {
        return 108;
    }
    
    public int getOffsetBytes() {
        return this.offsetBytes;
    }
    
    @Override
    public int getRetOffsetBytes() {
        return this.offsetBytes;
    }
    
    public int getInBits() {
        return this.inBit;
    }
    
    public int getOffsetBits() {
        return this.offsetBytes * 8 + ((this.inBit >= 0) ? this.inBit : 0);
    }
    
    public boolean isBitReq() {
        return this.inBit >= 0;
    }
    
    public int getReadNum() {
        return this.readNum;
    }
    
    public PPIMsgReq withAddr(final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final PPIAddr paddr = PPIAddr.parsePPIAddr(addr, vtp, failedr);
        if (paddr != null) {
            this.memTp = paddr.getMemTp();
            this.offsetBytes = paddr.getOffsetBytes();
            this.inBit = paddr.getInBits();
            this.readNum = paddr.getBytesNum();
        }
        return this;
    }
    
    public PPIMsgReq withAddrByte(final PPIMemTp mtp, final int byteoffsets, final int inbit, final int bytesnum) {
        this.memTp = mtp;
        this.offsetBytes = byteoffsets;
        this.inBit = inbit;
        this.readNum = bytesnum;
        return this;
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
        rets[22] = (byte)PPIMemValTp.B.getVal();
        if (this.memTp == PPIMemTp.T || this.memTp == PPIMemTp.C) {
            rets[22] = (byte)PPIMemTp.T.getVal();
        }
        rets[23] = 0;
        rets[24] = (byte)this.getReadNum();
        rets[25] = 0;
        if (this.memTp == PPIMemTp.V) {
            rets[26] = 1;
        }
        else {
            rets[26] = 0;
        }
        rets[27] = (byte)this.memTp.getVal();
        final int offaddr = this.getOffsetBits();
        rets[28] = (byte)(offaddr >> 16 & 0xFF);
        rets[29] = (byte)(offaddr >> 8 & 0xFF);
        rets[30] = (byte)(offaddr & 0xFF);
        rets[31] = PPIMsg.calChkSum(rets, 4, 27);
        rets[32] = 22;
        return rets;
    }
}
