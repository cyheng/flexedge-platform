// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import java.io.IOException;
import java.io.InputStream;

public class ModbusParserResp extends ModbusParser
{
    private static final RespRet ERR;
    private static final RespRet DISCARD;
    private int devId;
    private short fc;
    private int readNum;
    
    private static RespRet createErrRet(final int errcode) {
        final RespRet r = new RespRet(0);
        r.errCode = errcode;
        return r;
    }
    
    public ModbusParserResp() {
        this.devId = -1;
        this.fc = -1;
        this.readNum = -1;
    }
    
    public ModbusParserResp initDevFC(final int devid, final short fc, final int readnum) {
        this.devId = devid;
        this.fc = fc;
        this.readNum = readnum;
        return this;
    }
    
    public RespRet parseRespCmdRTU(final InputStream inputs) throws IOException {
        int c = inputs.read();
        if (c != this.devId) {
            return ModbusParserResp.DISCARD;
        }
        c = inputs.read();
        if (this.fc == c) {
            return this.parseRespFC(inputs);
        }
        if (c == this.fc + 128) {
            c = inputs.read();
            return createErrRet(c);
        }
        return ModbusParserResp.DISCARD;
    }
    
    private RespRet parseRespFC(final InputStream inputs) throws IOException {
        switch (this.fc) {
            case 1:
            case 2: {
                return this.parseRespReadBits(inputs);
            }
            case 3:
            case 4: {
                return this.parseRespReadInt16s(inputs);
            }
            default: {
                return ModbusParserResp.DISCARD;
            }
        }
    }
    
    private RespRet parseRespReadBits(final InputStream inputs) throws IOException {
        final int byte_c = inputs.read();
        if (byte_c * 8 < this.readNum) {
            return ModbusParserResp.DISCARD;
        }
        final int len = 3 + byte_c + 2;
        final byte[] bs = new byte[len];
        ModbusParser.readFill(inputs, bs, 3, byte_c + 2);
        bs[0] = (byte)this.devId;
        bs[1] = (byte)this.fc;
        bs[2] = (byte)byte_c;
        final int crc = ModbusCmd.modbus_crc16_check(bs, len - 2);
        if (bs[len - 2] != (byte)(crc >> 8 & 0xFF) || bs[len - 1] != (byte)(crc & 0xFF)) {
            return ModbusParserResp.DISCARD;
        }
        final boolean[] bvs = new boolean[this.readNum];
        for (int i = 0; i < this.readNum; ++i) {
            final int b = i / 8;
            final int bit = i % 8;
            bvs[i] = ((bs[3 + b] & 0xFF & 1 << bit) > 0);
        }
        return new RespRetReadBits(1, bvs);
    }
    
    private RespRet parseRespReadInt16s(final InputStream inputs) throws IOException {
        final int byte_c = inputs.read();
        if (byte_c / 2 != this.readNum) {
            return ModbusParserResp.DISCARD;
        }
        final int len = 3 + byte_c + 2;
        final byte[] bs = new byte[len];
        ModbusParser.readFill(inputs, bs, 3, byte_c + 2);
        bs[0] = (byte)this.devId;
        bs[1] = (byte)this.fc;
        bs[2] = (byte)byte_c;
        final int crc = ModbusCmd.modbus_crc16_check(bs, len - 2);
        if (bs[len - 2] != (byte)(crc >> 8 & 0xFF) || bs[len - 1] != (byte)(crc & 0xFF)) {
            return ModbusParserResp.DISCARD;
        }
        final int[] bvs = new int[this.readNum];
        for (int i = 0; i < this.readNum; ++i) {
            int w = bs[3 + i * 2] & 0xFF;
            w <<= 8;
            w += (bs[3 + i * 2 + 1] & 0xFF);
            bvs[i] = w;
        }
        return new RespRetReadInt16s(1, bvs);
    }
    
    static {
        ERR = new RespRet(0);
        DISCARD = new RespRet(-1);
    }
    
    public static class RespRet
    {
        int retST;
        int errCode;
        
        public RespRet(final int retst) {
            this.retST = 0;
            this.errCode = -1;
            this.retST = retst;
        }
        
        public int getReturnST() {
            return this.retST;
        }
        
        public boolean isDiscard() {
            return this.retST < 0;
        }
        
        public boolean isErrRet() {
            return this.retST == 0;
        }
        
        public boolean isSuccRet() {
            return this.retST > 0;
        }
        
        public int getErrCode() {
            return this.errCode;
        }
    }
    
    public static class RespRetReadBits extends RespRet
    {
        boolean[] readVals;
        
        public RespRetReadBits(final int ret_st, final boolean[] readvals) {
            super(ret_st);
            this.readVals = null;
            this.readVals = readvals;
        }
        
        public boolean[] getReadVals() {
            return this.readVals;
        }
    }
    
    public static class RespRetReadInt16s extends RespRet
    {
        int[] readVals;
        
        public RespRetReadInt16s(final int ret_st, final int[] readvals) {
            super(ret_st);
            this.readVals = null;
            this.readVals = readvals;
        }
        
        public int[] getReadVals() {
            return this.readVals;
        }
    }
}
