

package cn.doraro.flexedge.driver.common.modbus;

import java.io.IOException;
import java.io.PushbackInputStream;

public class ModbusParserReqTCP extends ModbusParserReq {
    int pST;
    transient int transId;
    transient int protoId;
    private transient int lastTransId;
    private transient int devId;
    private transient short fc;
    private transient byte[] mbap;
    private transient int len;

    public ModbusParserReqTCP() {
        this.pST = 0;
        this.lastTransId = -1;
        this.devId = -1;
        this.fc = -1;
        this.mbap = null;
        this.transId = -1;
        this.protoId = -1;
        this.len = -1;
    }

    @Override
    public ModbusCmd parseReqCmdInLoop(final PushbackInputStream inputs) throws IOException {
        final byte[] bs = new byte[7];
        ModbusParser.readFill(inputs, bs, 0, 7);
        this.transId = (bs[0] & 0xFF) << 8;
        this.transId += (bs[1] & 0xFF);
        this.protoId = (bs[2] & 0xFF) << 8;
        this.protoId += (bs[3] & 0xFF);
        if (this.protoId != 0) {
            inputs.unread(bs, 1, 6);
            return null;
        }
        this.len = (bs[4] & 0xFF);
        this.len <<= 8;
        this.len += (bs[5] & 0xFF);
        if (this.len > 260) {
            inputs.unread(bs, 1, 6);
            return null;
        }
        this.mbap = bs;
        this.devId = (bs[6] & 0xFF);
        if (!this.checkLimitDevId(this.devId)) {
            inputs.unread(bs, 1, 6);
            return null;
        }
        this.fc = (short) inputs.read();
        if (this.fc < 0) {
            throw new IOException("end of stream");
        }
        int dlen = this.checkReqFCDataLen(this.fc);
        if (dlen < 0 || (dlen > 0 && dlen != this.len - 2)) {
            inputs.unread(this.fc);
            inputs.unread(bs, 1, 6);
            return null;
        }
        if (dlen == 0) {
            dlen = this.len - 2;
        }
        final byte[] data = new byte[dlen];
        ModbusParser.readFill(inputs, data, 0, dlen);
        return this.parseReqFC(data);
    }

    private int checkReqFCDataLen(final int fc) throws IOException {
        switch (fc) {
            case 1:
            case 2: {
                return 4;
            }
            case 3:
            case 4: {
                return 4;
            }
            case 5:
            case 6: {
                return 4;
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

    private ModbusCmd parseReqFC(final byte[] data) throws IOException {
        switch (this.fc) {
            case 1:
            case 2: {
                return this.parseReqReadBits(data);
            }
            case 3:
            case 4: {
                return this.parseReqReadInt16s(data);
            }
            case 5: {
                return this.parseReqWriteBit(data);
            }
            case 6: {
                return this.parseReqWriteWord(data);
            }
            case 16: {
                return this.parseReqWriteWords(data);
            }
            default: {
                return new ModbusCmdErr(ModbusCmd.Protocol.tcp, this.mbap, (short) this.devId, this.fc, (short) 4);
            }
        }
    }

    private ModbusCmdReadBits parseReqReadBits(final byte[] bs) throws IOException {
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        int reg_num = bs[2] & 0xFF;
        reg_num <<= 8;
        reg_num += (bs[3] & 0xFF);
        final ModbusCmdReadBits r = new ModbusCmdReadBits(this.devId, this.fc);
        r.regAddr = reg_addr;
        r.regNum = reg_num;
        r.protocal = ModbusCmd.Protocol.tcp;
        r.mbap4Tcp = this.mbap;
        return r;
    }

    private ModbusCmdReadWords parseReqReadInt16s(final byte[] bs) throws IOException {
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        int reg_num = bs[2] & 0xFF;
        reg_num <<= 8;
        reg_num += (bs[3] & 0xFF);
        final ModbusCmdReadWords r = new ModbusCmdReadWords(this.devId, this.fc);
        r.regAddr = reg_addr;
        r.regNum = reg_num;
        r.protocal = ModbusCmd.Protocol.tcp;
        r.mbap4Tcp = this.mbap;
        return r;
    }

    private ModbusCmdWriteBit parseReqWriteBit(final byte[] bs) throws IOException {
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        final boolean bv = (bs[2] & 0xFF) == 0xFF;
        final ModbusCmdWriteBit r = new ModbusCmdWriteBit(this.devId);
        r.regAddr = reg_addr;
        r.bwVal = bv;
        r.protocal = ModbusCmd.Protocol.tcp;
        r.mbap4Tcp = this.mbap;
        return r;
    }

    private ModbusCmdWriteWord parseReqWriteWord(final byte[] bs) throws IOException {
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        int v = bs[2] & 0xFF;
        v <<= 8;
        v += (bs[3] & 0xFF);
        final ModbusCmdWriteWord r = new ModbusCmdWriteWord(this.devId);
        r.regAddr = reg_addr;
        r.wVal = v;
        r.protocal = ModbusCmd.Protocol.tcp;
        r.mbap4Tcp = this.mbap;
        return r;
    }

    private ModbusCmdWriteWords parseReqWriteWords(final byte[] bs) throws IOException {
        int reg_addr = bs[0] & 0xFF;
        reg_addr <<= 8;
        reg_addr += (bs[1] & 0xFF);
        int reg_n = bs[2] & 0xFF;
        reg_n <<= 8;
        reg_n += (bs[3] & 0xFF);
        final int byte_n = bs[4] & 0xFF;
        if (byte_n != reg_n * 2 || byte_n > 260) {
            return null;
        }
        if (byte_n != bs.length - 5) {
            return null;
        }
        final int[] vals = new int[reg_n];
        for (int i = 0; i < reg_n; ++i) {
            int tmpv = bs[5 + i * 2] & 0xFF;
            tmpv <<= 8;
            tmpv += (bs[5 + i * 2 + 1] & 0xFF);
            vals[i] = tmpv;
        }
        final ModbusCmdWriteWords r = new ModbusCmdWriteWords(this.devId, reg_addr, vals);
        r.protocal = ModbusCmd.Protocol.tcp;
        r.mbap4Tcp = this.mbap;
        return r;
    }
}
