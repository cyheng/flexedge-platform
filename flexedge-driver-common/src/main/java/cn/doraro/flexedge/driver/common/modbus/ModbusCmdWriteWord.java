// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import kotlin.NotImplementedError;

import java.io.InputStream;
import java.io.OutputStream;

public class ModbusCmdWriteWord extends ModbusCmd {
    int regAddr;
    boolean[] ret_vals;
    int ret_val_num;
    int wVal;
    short fc;

    public ModbusCmdWriteWord(final int devid) {
        super(1000L, devid);
        this.regAddr = 0;
        this.ret_vals = new boolean[300];
        this.ret_val_num = 0;
        this.wVal = -1;
        this.fc = 6;
    }

    public ModbusCmdWriteWord(final long scan_inter_ms, final int s_addr, final int reg_addr, final int val) {
        super(scan_inter_ms, s_addr);
        this.regAddr = 0;
        this.ret_vals = new boolean[300];
        this.ret_val_num = 0;
        this.wVal = -1;
        this.fc = 6;
        this.regAddr = reg_addr;
        this.wVal = val;
    }

    public ModbusCmdWriteWord(final int s_addr, final int reg_addr, final int val) {
        this(-1L, s_addr, reg_addr, val);
    }

    public static byte[] createResp(final ModbusCmd mc, final short addr, final int reg_addr, final short vdata) {
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

    public static byte[] createRespRTU(final short addr, final int reg_addr, final short vdata) {
        final byte[] data = {(byte) addr, 6, (byte) (reg_addr >> 8), (byte) reg_addr, (byte) (vdata >> 8), (byte) vdata, 0, 0};
        final int crc = ModbusCmd.modbus_crc16_check(data, 6);
        data[6] = (byte) (crc >> 8 & 0xFF);
        data[7] = (byte) (crc & 0xFF);
        return data;
    }

    public static byte[] createRespTCP(final byte[] mbap, final short addr, final int reg_addr, final short vdata) {
        final byte[] data = {mbap[0], mbap[1], mbap[2], mbap[3], 0, 6, (byte) addr, 6, (byte) (reg_addr >> 8), (byte) reg_addr, (byte) (vdata >> 8), (byte) vdata};
        return data;
    }

    static ModbusCmdWriteWord createReqMC(final byte[] bs, final int[] pl) {
        if (bs.length < 8) {
            return null;
        }
        final int crc = ModbusCmd.modbus_crc16_check(bs, 6);
        if (bs[6] != (byte) (crc >> 8 & 0xFF) || bs[7] != (byte) (crc & 0xFF)) {
            return null;
        }
        final short addr = (short) (bs[0] & 0xFF);
        final short fc = (short) (bs[1] & 0xFF);
        int reg_addr = 0xFF & bs[2];
        reg_addr <<= 8;
        reg_addr += (0xFF & bs[3]);
        int val = 0xFF & bs[4];
        val <<= 8;
        val += (0xFF & bs[5]);
        if (bs.length > 8) {
            pl[0] = 8;
        } else {
            pl[0] = -1;
        }
        return new ModbusCmdWriteWord(addr, reg_addr, val);
    }

    public int getRegAddr() {
        return this.regAddr;
    }

    public int getWriteVal() {
        return this.wVal;
    }

    public void setWriteVal(final int v) {
        this.wVal = v;
    }

    @Override
    public int calRespLenRTU() {
        return -1;
    }

    @Override
    public short getFC() {
        return 6;
    }

    @Override
    protected int reqRespRTU(final OutputStream ous, final InputStream ins) throws Exception {
        this.ret_val_num = 0;
        final byte[] pdata = {(byte) this.slaveAddr, 6, (byte) (this.regAddr >> 8 & 0xFF), (byte) (this.regAddr & 0xFF), (byte) (this.wVal >> 8 & 0xFF), (byte) (this.wVal & 0xFF), 0, 0};
        final int crc = ModbusCmd.modbus_crc16_check(pdata, 6);
        pdata[6] = (byte) (crc >> 8 & 0xFF);
        pdata[7] = (byte) (crc & 0xFF);
        this.clearInputStream(ins);
        ous.write(pdata);
        ous.flush();
        int rlen = 0;
        int mayrlen = 0;
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
                if (this.mbuss_adu[1] != 6) {
                    if (this.mbuss_adu[1] == -122) {
                        final int n = 0xFF & this.mbuss_adu[2];
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
        final byte[] pdata = new byte[12];
        ++this.lastTcpCC;
        if (this.lastTcpCC >= 65535) {
            this.lastTcpCC = 1;
        }
        pdata[this.ret_val_num = 0] = (byte) (this.lastTcpCC >> 8 & 0xFF);
        pdata[1] = (byte) (this.lastTcpCC & 0xFF);
        pdata[2] = (pdata[3] = 0);
        pdata[4] = 0;
        pdata[5] = 6;
        pdata[6] = (byte) this.slaveAddr;
        pdata[7] = 6;
        pdata[8] = (byte) (this.regAddr >> 8 & 0xFF);
        pdata[9] = (byte) (this.regAddr & 0xFF);
        pdata[10] = (byte) (this.wVal >> 8 & 0xFF);
        pdata[11] = (byte) (this.wVal & 0xFF);
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
        for (int i = 1; i < 4; ++i) {
            if (pdata[i] != read_mbap[i]) {
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
