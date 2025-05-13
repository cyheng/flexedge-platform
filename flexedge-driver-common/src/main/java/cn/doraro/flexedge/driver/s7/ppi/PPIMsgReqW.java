

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.UAVal;

public class PPIMsgReqW extends PPIMsgReq {
    PPIMemValTp memValTp;
    int offsetBytes;
    int inBit;
    int wVal;

    public PPIMsgReqW() {
        this.memValTp = null;
        this.offsetBytes = 0;
        this.inBit = -1;
        this.wVal = 0;
    }

    public PPIMsgReqW withWriteVal(final PPIMemValTp valtp, final int wv) {
        this.memValTp = valtp;
        this.wVal = wv;
        return this;
    }

    public PPIMsgReqW withWriteVal(final PPIAddr addr, final int wv) {
        this.memValTp = addr.getFitMemValTp();
        if (this.memValTp == null) {
            throw new IllegalArgumentException("no fit mem val tp with addr=" + addr);
        }
        this.wVal = wv;
        return this;
    }

    @Override
    protected short getStartD() {
        return 104;
    }

    @Override
    public short getFC() {
        return 124;
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

    @Override
    public int getRetOffsetBytes() {
        return -1;
    }

    public PPIMsgReqW withAddr(final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final PPIAddr paddr = PPIAddr.parsePPIAddr(addr, vtp, failedr);
        if (paddr != null) {
            this.memTp = paddr.getMemTp();
            this.offsetBytes = paddr.getOffsetBytes();
            this.inBit = paddr.getInBits();
        }
        return this;
    }

    public PPIMsgReq withAddrByte(final PPIMemTp mtp, final int byteoffsets, final int inbit) {
        this.memTp = mtp;
        this.offsetBytes = byteoffsets;
        this.inBit = inbit;
        return this;
    }

    private short calLen() {
        final PPIMemValTp vtp = this.memValTp;
        final int byten = vtp.getByteNum();
        return (short) (byten + 31);
    }

    @Override
    public byte[] toBytes() {
        final short le = this.calLen();
        final byte[] rets = new byte[le + 6];
        final PPIMemValTp vtp = this.memValTp;
        final PPIMemTp mtp = this.memTp;
        rets[0] = (rets[3] = (byte) this.getStartD());
        rets[1] = (rets[2] = (byte) le);
        rets[4] = (byte) this.da;
        rets[5] = (byte) this.sa;
        rets[6] = (byte) this.getFC();
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
        rets[15] = 0;
        rets[16] = (byte) (4 + vtp.getByteNum());
        rets[17] = 5;
        rets[18] = 1;
        rets[19] = 18;
        rets[20] = 10;
        rets[21] = 16;
        rets[22] = (byte) vtp.getVal();
        rets[23] = 0;
        rets[24] = 1;
        rets[25] = 0;
        if (mtp == PPIMemTp.V) {
            rets[26] = 1;
        } else {
            rets[26] = 0;
        }
        rets[27] = (byte) mtp.getVal();
        final int offaddr = this.getOffsetBits();
        rets[28] = (byte) (offaddr >> 16 & 0xFF);
        rets[29] = (byte) (offaddr >> 8 & 0xFF);
        rets[30] = (byte) (offaddr & 0xFF);
        rets[31] = 0;
        int dend = 35;
        if (mtp.hasBit() && this.isBitReq()) {
            rets[32] = 3;
            rets[33] = 0;
            rets[34] = 1;
            rets[35] = (byte) this.wVal;
        } else {
            rets[32] = 4;
            rets[33] = 0;
            rets[34] = (byte) vtp.getBitNum();
            switch (vtp) {
                case B: {
                    rets[35] = (byte) (this.wVal & 0xFF);
                    break;
                }
                case W: {
                    rets[35] = (byte) (this.wVal >> 8 & 0xFF);
                    rets[36] = (byte) (this.wVal & 0xFF);
                    dend = 36;
                    break;
                }
                case D: {
                    rets[35] = (byte) (this.wVal >> 24 & 0xFF);
                    rets[36] = (byte) (this.wVal >> 16 & 0xFF);
                    rets[37] = (byte) (this.wVal >> 8 & 0xFF);
                    rets[38] = (byte) (this.wVal & 0xFF);
                    dend = 38;
                    break;
                }
            }
        }
        rets[dend + 1] = PPIMsg.calChkSum(rets, 4, dend - 4 + 1);
        rets[dend + 2] = 22;
        return rets;
    }
}
