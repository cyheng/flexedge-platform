// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

public class PPIMsgReqConfirm extends PPIMsg {
    static byte FC;

    static {
        PPIMsgReqConfirm.FC = 92;
    }

    short sa;
    short da;

    @Override
    protected short getStartD() {
        return 16;
    }

    public short getSorAddr() {
        return this.sa;
    }

    public short getDestAddr() {
        return this.da;
    }

    public PPIMsgReqConfirm withSorAddr(final short sa) {
        this.sa = sa;
        return this;
    }

    public PPIMsgReqConfirm withDestAddr(final short da) {
        this.da = da;
        return this;
    }

    @Override
    public byte[] toBytes() {
        final byte[] rets = {(byte) this.getStartD(), (byte) this.da, (byte) this.sa, PPIMsgReqConfirm.FC, 0, 0};
        rets[4] = PPIMsg.calChkSum(rets, 1, 3);
        rets[5] = 22;
        return rets;
    }
}
