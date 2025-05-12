// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class ModbusParserReqRTU extends ModbusParserReq
{
    int pST;
    private transient int devId;
    private transient int fc;
    
    public ModbusParserReqRTU() {
        this.pST = 0;
        this.devId = -1;
        this.fc = -1;
    }
    
    @Override
    public ModbusCmd parseReqCmdInLoop(final PushbackInputStream inputs) throws IOException {
        final byte[] bs = new byte[2];
        ModbusParser.readFill(inputs, bs, 0, 2);
        this.devId = (bs[0] & 0xFF);
        if (!this.checkLimitDevId(this.devId)) {
            inputs.unread(bs, 1, 1);
            return null;
        }
        this.fc = (bs[1] & 0xFF);
        final int dlen = this.checkReqFCDataLen(this.fc);
        if (dlen < 0) {
            inputs.unread(this.fc);
            return null;
        }
        if (dlen > 0) {
            final byte[] pkbs = new byte[2 + dlen];
            ModbusParser.readFill(inputs, pkbs, 2, dlen);
            pkbs[0] = bs[0];
            pkbs[1] = bs[1];
            final int crc = ModbusCmd.modbus_crc16_check(pkbs, 6);
            if (pkbs[6] != (byte)(crc >> 8 & 0xFF) || pkbs[7] != (byte)(crc & 0xFF)) {
                inputs.unread(pkbs, 2, 6);
                inputs.unread(this.fc);
                return null;
            }
            return this.parseReqFC(pkbs);
        }
        else {
            switch (this.fc) {
                case 16: {
                    return this.parseReqWriteWords(bs, inputs);
                }
                case 15: {
                    return this.parseReqWriteBits(bs, inputs);
                }
                default: {
                    return new ModbusCmdErr(ModbusCmd.Protocol.rtu, null, (short)this.devId, (short)this.fc, (short)4);
                }
            }
        }
    }
    
    private int checkReqFCDataLen(final int fc) throws IOException {
        switch (fc) {
            case 1:
            case 2: {
                return 6;
            }
            case 3:
            case 4: {
                return 6;
            }
            case 5:
            case 6: {
                return 6;
            }
            case 15:
            case 16: {
                return 0;
            }
            default: {
                return -1;
            }
        }
    }
    
    private ModbusCmd parseReqFC(final byte[] pkbs) throws IOException {
        switch (this.fc) {
            case 1:
            case 2: {
                return this.parseReqReadBits(pkbs);
            }
            case 3:
            case 4: {
                return this.parseReqReadInt16s(pkbs);
            }
            case 5: {
                return this.parseReqWriteBit(pkbs);
            }
            case 6: {
                return this.parseReqWriteWord(pkbs);
            }
            default: {
                return new ModbusCmdErr(ModbusCmd.Protocol.rtu, null, (short)this.devId, (short)this.fc, (short)4);
            }
        }
    }
    
    private ModbusCmd parseReqWriteWords(final byte[] bs_h, final PushbackInputStream inputs) throws IOException {
        final byte[] bs = new byte[5];
        ModbusParser.readFill(inputs, bs, 0, 5);
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        int reg_n = bs[2] & 0xFF;
        reg_n <<= 8;
        reg_n += (bs[3] & 0xFF);
        final int byte_n = bs[4] & 0xFF;
        if (byte_n != reg_n * 2 || byte_n > 260) {
            inputs.unread(bs);
            inputs.unread(this.fc);
            return null;
        }
        final byte[] vbs = new byte[byte_n + 2];
        ModbusParser.readFill(inputs, vbs, 0, byte_n + 2);
        final int crc = ModbusCmd.modbus_crc16_check_seg(bs_h, 2, bs, 5, vbs, byte_n);
        if (vbs[vbs.length - 2] != (byte)(crc >> 8 & 0xFF) || vbs[vbs.length - 1] != (byte)(crc & 0xFF)) {
            inputs.unread(vbs);
            inputs.unread(bs);
            inputs.unread(this.fc);
            return null;
        }
        final int[] vals = new int[reg_n];
        for (int i = 0; i < reg_n; ++i) {
            int tmpv = vbs[i * 2] & 0xFF;
            tmpv <<= 8;
            tmpv += (vbs[i * 2 + 1] & 0xFF);
            vals[i] = tmpv;
        }
        final ModbusCmdWriteWords r = new ModbusCmdWriteWords(this.devId, reg_addr, vals);
        return r;
    }
    
    private ModbusCmd parseReqWriteBits(final byte[] bs_h, final PushbackInputStream inputs) throws IOException {
        final byte[] bs = new byte[5];
        ModbusParser.readFill(inputs, bs, 0, 5);
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        int reg_n = bs[2] & 0xFF;
        reg_n <<= 8;
        reg_n += (bs[3] & 0xFF);
        final int byte_n = bs[4] & 0xFF;
        final int cc_bn = reg_n / 8 + ((reg_n % 8 > 0) ? 1 : 0);
        if (byte_n != cc_bn || byte_n > 260) {
            inputs.unread(bs);
            inputs.unread(this.fc);
            return null;
        }
        final byte[] vbs = new byte[byte_n + 2];
        ModbusParser.readFill(inputs, vbs, 0, byte_n + 2);
        final int crc = ModbusCmd.modbus_crc16_check_seg(bs_h, 2, bs, 5, vbs, byte_n);
        if (vbs[vbs.length - 2] != (byte)(crc >> 8 & 0xFF) || vbs[vbs.length - 1] != (byte)(crc & 0xFF)) {
            inputs.unread(vbs);
            inputs.unread(bs);
            inputs.unread(this.fc);
            return null;
        }
        final boolean[] vals = new boolean[reg_n];
        for (int i = 0; i < reg_n; ++i) {
            final int idx = i / 8 + ((i % 8 > 0) ? 1 : 0);
            final int tmpv = vbs[idx] & 0xFF;
            vals[i] = ((tmpv & 1 << i % 8) > 0);
        }
        final ModbusCmdWriteBits r = new ModbusCmdWriteBits(this.devId, reg_addr, vals);
        return r;
    }
    
    private ModbusCmdReadBits parseReqReadBits(final byte[] bs) throws IOException {
        int reg_addr = bs[2] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[3] & 0xFF);
        int reg_num = bs[4] & 0xFF;
        reg_num <<= 8;
        reg_num += (bs[5] & 0xFF);
        final ModbusCmdReadBits r = new ModbusCmdReadBits(this.devId, (short)this.fc);
        r.regAddr = reg_addr;
        r.regNum = reg_num;
        return r;
    }
    
    private ModbusCmdReadWords parseReqReadInt16s(final byte[] bs) throws IOException {
        int reg_addr = bs[2] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[3] & 0xFF);
        int reg_num = bs[4] & 0xFF;
        reg_num <<= 8;
        reg_num += (bs[5] & 0xFF);
        final ModbusCmdReadWords r = new ModbusCmdReadWords(this.devId, (short)this.fc);
        r.regAddr = reg_addr;
        r.regNum = reg_num;
        return r;
    }
    
    private ModbusCmdWriteBit parseReqWriteBit(final byte[] bs) throws IOException {
        int reg_addr = bs[2] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[3] & 0xFF);
        final boolean bv = (bs[4] & 0xFF) == 0xFF;
        final ModbusCmdWriteBit r = new ModbusCmdWriteBit(this.devId);
        r.regAddr = reg_addr;
        r.bwVal = bv;
        return r;
    }
    
    private ModbusCmdWriteWord parseReqWriteWord(final byte[] bs) throws IOException {
        int reg_addr = bs[2] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[3] & 0xFF);
        int v = bs[4] & 0xFF;
        v <<= 8;
        v += (bs[5] & 0xFF);
        final ModbusCmdWriteWord r = new ModbusCmdWriteWord(this.devId);
        r.regAddr = reg_addr;
        r.wVal = v;
        return r;
    }
}
