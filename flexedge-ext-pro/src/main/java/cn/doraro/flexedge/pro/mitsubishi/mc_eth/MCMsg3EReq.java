// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import java.io.OutputStream;

public abstract class MCMsg3EReq extends MCMsg3E
{
    int waitTO;
    
    public MCMsg3EReq() {
        this.waitTO = 1;
    }
    
    public abstract int getCmd();
    
    public abstract int getCmdSub();
    
    public abstract int calRespLenAscii();
    
    public abstract int calRespLenBin();
    
    public MCMsg3EReq asWaitTimeout(final int wait_to) {
        this.waitTO = wait_to;
        return this;
    }
    
    @Override
    public final void writeOutAscii(final OutputStream outputs) throws Exception {
        super.writeOutAscii(outputs);
        final byte[] bs = new byte[16];
        final byte[] databs = this.packDataAscii();
        final int reqlen = databs.length + 12;
        MCMsg.toAsciiHexBytes(reqlen, bs, 0, 4);
        MCMsg.toAsciiHexBytes(this.waitTO, bs, 4, 4);
        MCMsg.toAsciiHexBytes(this.getCmd(), bs, 8, 4);
        MCMsg.toAsciiHexBytes(this.getCmdSub(), bs, 12, 4);
        outputs.write(bs);
        outputs.write(databs);
    }
    
    @Override
    public final void writeOutBin(final OutputStream outputs) throws Exception {
        super.writeOutBin(outputs);
        final byte[] bs = new byte[8];
        final byte[] databs = this.packDataBin();
        final int reqlen = databs.length + 6;
        MCMsg.toBinHexBytes(reqlen, bs, 0, 2);
        MCMsg.toBinHexBytes(this.waitTO, bs, 2, 2);
        final int cmd = this.getCmd();
        MCMsg.toBinHexBytes(cmd, bs, 4, 2);
        final int subcmd = this.getCmdSub();
        MCMsg.toBinHexBytes(subcmd, bs, 6, 2);
        outputs.write(bs);
        outputs.write(databs);
    }
    
    protected abstract byte[] packDataAscii() throws Exception;
    
    protected abstract byte[] packDataBin() throws Exception;
    
    public static class RRItem
    {
        MCCode code;
        int addr;
        
        public RRItem(final MCCode mcc, final int addr) {
            this.code = mcc;
            this.addr = addr;
        }
        
        public MCCode getCode() {
            return this.code;
        }
        
        public int getAddr() {
            return this.addr;
        }
    }
}
