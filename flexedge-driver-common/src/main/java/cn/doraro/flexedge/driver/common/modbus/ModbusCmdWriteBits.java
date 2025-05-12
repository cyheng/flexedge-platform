// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import java.io.InputStream;
import java.io.OutputStream;

public class ModbusCmdWriteBits extends ModbusCmd {
    int regAddr;
    boolean[] ret_vals;
    int ret_val_num;
    boolean[] bwVals;

    public ModbusCmdWriteBits(final long scan_inter_ms, final int s_addr, final int reg_addr, final boolean[] bvals) {
        super(scan_inter_ms, s_addr);
        this.regAddr = 0;
        this.ret_vals = new boolean[300];
        this.ret_val_num = 0;
        this.bwVals = null;
        this.regAddr = reg_addr;
        this.bwVals = bvals;
    }

    public ModbusCmdWriteBits(final int s_addr, final int reg_addr, final boolean[] bvals) {
        this(-1L, s_addr, reg_addr, bvals);
    }

    public void setWriteVal(final boolean[] bs) {
        this.bwVals = bs;
    }

    @Override
    public int calRespLenRTU() {
        return -1;
    }

    @Override
    public short getFC() {
        return 15;
    }

    @Override
    protected int reqRespRTU(final OutputStream ous, final InputStream ins) throws Exception {
        this.ret_val_num = 0;
        final int tmplen = this.bwVals.length;
        final int bcount = tmplen / 8 + ((tmplen % 8 > 0) ? 1 : 0);
        final int wlen = 8 + bcount;
        final byte[] pdata = new byte[wlen];
        pdata[0] = (byte) this.slaveAddr;
        pdata[1] = 15;
        pdata[2] = (byte) (this.regAddr >> 8 & 0xFF);
        pdata[3] = (byte) (this.regAddr & 0xFF);
        pdata[4] = (byte) (tmplen >> 8 & 0xFF);
        pdata[5] = (byte) (tmplen & 0xFF);
        for (int i = 0; i < bcount; ++i) {
            pdata[i + 6] = 0;
        }
        for (int i = 0; i < tmplen; ++i) {
            final int idx = i / 8;
            final int lft = i % 8;
            final int tmpv = pdata[idx + 6];
            if (this.bwVals[idx]) {
                pdata[idx + 6] = (byte) ((tmpv | 1 << lft) & 0xFF);
            } else {
                pdata[idx + 6] = (byte) (tmpv & ~(1 << lft) & 0xFF);
            }
        }
        final int crc = ModbusCmd.modbus_crc16_check(pdata, wlen - 2);
        pdata[wlen - 2] = (byte) (crc >> 8 & 0xFF);
        pdata[wlen - 1] = (byte) (crc & 0xFF);
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
                if (this.mbuss_adu[1] != 15) {
                    if (this.mbuss_adu[1] == -113) {
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
        this.ret_val_num = 0;
        final int tmplen = this.bwVals.length;
        final int bcount = tmplen / 8 + ((tmplen % 8 > 0) ? 1 : 0);
        final int wlen = 12 + bcount;
        final byte[] pdata = new byte[wlen];
        ++this.lastTcpCC;
        if (this.lastTcpCC >= 65535) {
            this.lastTcpCC = 1;
        }
        pdata[0] = (byte) (this.lastTcpCC >> 8 & 0xFF);
        pdata[1] = (byte) (this.lastTcpCC & 0xFF);
        pdata[2] = (pdata[3] = 0);
        pdata[4] = 0;
        pdata[5] = 6;
        pdata[6] = (byte) this.slaveAddr;
        pdata[7] = 15;
        pdata[8] = (byte) (this.regAddr >> 8 & 0xFF);
        pdata[9] = (byte) (this.regAddr & 0xFF);
        pdata[10] = (byte) (tmplen >> 8 & 0xFF);
        pdata[11] = (byte) (tmplen & 0xFF);
        for (int i = 0; i < bcount; ++i) {
            pdata[i + 12] = 0;
        }
        for (int i = 0; i < tmplen; ++i) {
            final int idx = i / 8;
            final int lft = i % 8;
            final int tmpv = pdata[idx + 12];
            if (this.bwVals[idx]) {
                pdata[idx + 12] = (byte) ((tmpv | 1 << lft) & 0xFF);
            } else {
                pdata[idx + 12] = (byte) (tmpv & ~(1 << lft) & 0xFF);
            }
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
        return this.ret_val_num = 1;
    }
}
