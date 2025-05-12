// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import kotlin.NotImplementedError;

import java.io.InputStream;
import java.io.OutputStream;

public class ModbusCmdWriteWords extends ModbusCmd {
    int regAddr;
    boolean[] ret_vals;
    int ret_val_num;
    int[] wVals;

    public ModbusCmdWriteWords(final long scan_inter_ms, final int s_addr, final int reg_addr, final int[] vals) {
        super(scan_inter_ms, s_addr);
        this.regAddr = 0;
        this.ret_vals = new boolean[300];
        this.ret_val_num = 0;
        this.wVals = null;
        this.regAddr = reg_addr;
        this.wVals = vals;
    }

    public ModbusCmdWriteWords(final int s_addr, final int reg_addr, final int[] vals) {
        this(-1L, s_addr, reg_addr, vals);
    }

    public static byte[] createResp(final ModbusCmd mc, final short addr, final int reg_addr, final int[] vdata) {
        switch (mc.getProtocol()) {
            case tcp: {
                return createRespTCP(mc.mbap4Tcp, addr, reg_addr, vdata);
            }
            case ascii: {
                throw new NotImplementedError();
            }
            default: {
                return createRespRTU(addr, reg_addr, vdata);
            }
        }
    }

    public static byte[] createRespRTU(final short addr, final int reg_addr, final int[] vdata) {
        final byte[] data = {(byte) addr, 16, (byte) (reg_addr >> 8), (byte) reg_addr, (byte) (vdata.length >> 8), (byte) vdata.length, 0, 0};
        final int crc = ModbusCmd.modbus_crc16_check(data, 6);
        data[6] = (byte) (crc >> 8 & 0xFF);
        data[7] = (byte) (crc & 0xFF);
        return data;
    }

    public static byte[] createRespTCP(final byte[] mbap, final short addr, final int reg_addr, final int[] vdata) {
        final byte[] data = {mbap[0], mbap[1], mbap[2], mbap[3], 0, 6, (byte) addr, 16, (byte) (reg_addr >> 8), (byte) reg_addr, (byte) (vdata.length >> 8), (byte) vdata.length};
        return data;
    }

    public int getRegAddr() {
        return this.regAddr;
    }

    public int[] getWriteVals() {
        return this.wVals;
    }

    public void setWriteVal(final int[] vs) {
        this.wVals = vs;
    }

    @Override
    public int calRespLenRTU() {
        return -1;
    }

    @Override
    public short getFC() {
        return 16;
    }

    @Override
    protected int reqRespRTU(final OutputStream ous, final InputStream ins) throws Exception {
        this.ret_val_num = 0;
        final int bcount = this.wVals.length * 2;
        final byte[] pdata = new byte[9 + bcount];
        pdata[0] = (byte) this.slaveAddr;
        pdata[1] = 16;
        pdata[2] = (byte) (this.regAddr >> 8 & 0xFF);
        pdata[3] = (byte) (this.regAddr & 0xFF);
        pdata[4] = (byte) (this.wVals.length >> 8 & 0xFF);
        pdata[5] = (byte) (this.wVals.length & 0xFF);
        pdata[6] = (byte) bcount;
        for (int i = 0; i < this.wVals.length; ++i) {
            pdata[7 + i * 2] = (byte) (this.wVals[i] >> 8 & 0xFF);
            pdata[7 + i * 2 + 1] = (byte) (this.wVals[i] & 0xFF);
        }
        final int crc = ModbusCmd.modbus_crc16_check(pdata, pdata.length - 2);
        pdata[pdata.length - 2] = (byte) (crc >> 8 & 0xFF);
        pdata[pdata.length - 1] = (byte) (crc & 0xFF);
        this.clearInputStream(ins);
        ous.write(pdata);
        ous.flush();
        int rlen = 0;
        int mayrlen = 0;
        int err_code = -1;
        this.com_stream_recv_start(ins);
        while (this.com_stream_in_recving()) {
            rlen = this.com_stream_recv_chk_len_timeout(ins);
            if (rlen == 0) {
                continue;
            }
            if (mayrlen > 0) {
                if (rlen >= mayrlen) {
                    break;
                }
                continue;
            } else {
                if (this.mbuss_adu[0] != (byte) this.slaveAddr) {
                    break;
                }
                if (rlen < 3) {
                    continue;
                }
                if (this.mbuss_adu[1] != 16) {
                    if (this.mbuss_adu[1] == -112) {
                        err_code = (0xFF & this.mbuss_adu[2]);
                        break;
                    }
                    break;
                } else {
                    mayrlen = 8;
                }
            }
        }
        if (mayrlen <= 0 || rlen < mayrlen) {
            this.com_stream_end();
            if (rlen <= 0) {
                return -1;
            }
            if (rlen < mayrlen) {
                return -2;
            }
            return 0;
        } else {
            if (crc != ModbusCmd.modbus_crc16_check(this.mbuss_adu, 6)) {
                this.com_stream_end();
                return -3;
            }
            this.com_stream_end();
            return 1;
        }
    }

    @Override
    protected int reqRespTCP(final OutputStream ous, final InputStream ins) throws Exception {
        ++this.lastTcpCC;
        if (this.lastTcpCC >= 65535) {
            this.lastTcpCC = 1;
        }
        final int bcount = this.wVals.length * 2;
        final byte[] pdata = new byte[13 + bcount];
        pdata[this.ret_val_num = 0] = (byte) (this.lastTcpCC >> 8 & 0xFF);
        pdata[1] = (byte) (this.lastTcpCC & 0xFF);
        pdata[2] = (pdata[3] = 0);
        pdata[4] = 0;
        pdata[5] = (byte) (7 + bcount);
        pdata[6] = (byte) this.slaveAddr;
        pdata[7] = 16;
        pdata[8] = (byte) (this.regAddr >> 8 & 0xFF);
        pdata[9] = (byte) (this.regAddr & 0xFF);
        pdata[10] = (byte) (this.wVals.length >> 8 & 0xFF);
        pdata[11] = (byte) (this.wVals.length & 0xFF);
        pdata[12] = (byte) bcount;
        for (int i = 0; i < this.wVals.length; ++i) {
            pdata[13 + i * 2] = (byte) (this.wVals[i] >> 8 & 0xFF);
            pdata[13 + i * 2 + 1] = (byte) (this.wVals[i] & 0xFF);
        }
        this.clearInputStream(ins);
        ous.write(pdata);
        ous.flush();
        final byte[] read_mbap = new byte[6];
        do {
            read_mbap[0] = (byte) ins.read();
        } while (pdata[0] != read_mbap[0]);
        int rlen;
        int r;
        for (rlen = 1; (r = ins.read(read_mbap, rlen, 6 - rlen)) > 0; rlen += r) {
        }
        if (rlen != 6) {
            return 0;
        }
        for (int j = 1; j < 4; ++j) {
            if (pdata[j] != read_mbap[j]) {
                return 0;
            }
        }
        int pdulen = read_mbap[4] & 0xFF;
        pdulen <<= 8;
        pdulen += (read_mbap[5] & 0xFF);
        if (pdulen > 255) {
            return 0;
        }
        byte[] recvpdu;
        for (recvpdu = new byte[pdulen], rlen = 0; (r = ins.read(recvpdu, rlen, pdulen - rlen)) > 0; rlen += r) {
        }
        if (rlen != pdulen) {
            return 0;
        }
        if (recvpdu[0] != (byte) this.slaveAddr) {
            return 0;
        }
        if (recvpdu[1] != pdata[7]) {
            return 0;
        }
        this.ret_val_num = 0;
        return 1;
    }
}
