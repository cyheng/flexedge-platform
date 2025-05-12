// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import kotlin.NotImplementedError;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class ModbusCmdReadWords extends ModbusCmdRead {
    int regAddr;
    int regNum;
    int[] up_vals;
    boolean up_val_ok;
    Integer[] last_vals;
    short fc;

    public ModbusCmdReadWords(final int dev_addr, final short fc) {
        super(1000L, dev_addr);
        this.regAddr = 0;
        this.regNum = 0;
        this.up_vals = null;
        this.up_val_ok = false;
        this.last_vals = null;
        this.fc = -1;
        switch (fc) {
            case 3:
            case 4: {
                this.fc = fc;
                return;
            }
            default: {
                throw new IllegalArgumentException("invalid fc=" + fc);
            }
        }
    }

    public ModbusCmdReadWords(final short fc, final long scan_inter_ms, final int s_addr, final int reg_addr, final int reg_num) {
        super(scan_inter_ms, s_addr);
        this.regAddr = 0;
        this.regNum = 0;
        this.up_vals = null;
        this.up_val_ok = false;
        this.last_vals = null;
        this.fc = -1;
        switch (fc) {
            case 3:
            case 4: {
                this.fc = fc;
                this.regAddr = reg_addr;
                this.regNum = reg_num;
                this.up_vals = new int[reg_num];
                this.last_vals = new Integer[reg_num];
                return;
            }
            default: {
                throw new IllegalArgumentException("invalid fc");
            }
        }
    }

    public ModbusCmdReadWords(final short fc, final int s_addr, final int reg_addr, final int reg_num) {
        this(fc, -1L, s_addr, reg_addr, reg_num);
    }

    static ModbusCmdReadWords createReqMC(final byte[] bs, final int[] pl) {
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
        int reg_num = 0xFF & bs[4];
        reg_num <<= 8;
        reg_num += (0xFF & bs[5]);
        if (bs.length > 8) {
            pl[0] = 8;
        } else {
            pl[0] = -1;
        }
        return new ModbusCmdReadWords(fc, -1L, addr, reg_addr, reg_num);
    }

    public static int createRespErr(final byte[] data, final short addr, final short req_fc, final short err_code) {
        data[0] = (byte) addr;
        data[1] = (byte) (req_fc + 128);
        data[2] = (byte) err_code;
        return 3;
    }

    public static byte[] createResp(final ModbusCmd mc, final short addr, final short req_fc, final short[] wdata) {
        switch (mc.getProtocol()) {
            case tcp: {
                return createRespTCP(mc.mbap4Tcp, addr, req_fc, wdata);
            }
            case ascii: {
                throw new NotImplementedError();
            }
            default: {
                return createRespRTU(addr, req_fc, wdata);
            }
        }
    }

    private static byte[] createRespRTU(final short addr, final short req_fc, final short[] wdata) {
        if (wdata == null || wdata.length > 125) {
            return null;
        }
        final int dlen = wdata.length * 2;
        final int rlen = 5 + dlen;
        final byte[] data = new byte[rlen];
        data[0] = (byte) addr;
        data[1] = (byte) req_fc;
        data[2] = (byte) dlen;
        for (int i = 0; i < wdata.length; ++i) {
            final short w = wdata[i];
            data[3 + i * 2] = (byte) (0xFF & w >> 8);
            data[3 + i * 2 + 1] = (byte) (0xFF & w);
        }
        final int crc = ModbusCmd.modbus_crc16_check(data, rlen - 2);
        data[rlen - 2] = (byte) (crc >> 8 & 0xFF);
        data[rlen - 1] = (byte) (crc & 0xFF);
        return data;
    }

    private static byte[] createRespTCP(final byte[] mbap, final short addr, final short req_fc, final short[] wdata) {
        final int dlen = wdata.length * 2;
        int rlen = 9 + dlen;
        final byte[] data = new byte[rlen];
        rlen = dlen + 3;
        data[0] = mbap[0];
        data[1] = mbap[1];
        data[2] = mbap[2];
        data[3] = mbap[3];
        data[4] = (byte) (rlen >> 8);
        data[5] = (byte) rlen;
        data[6] = (byte) addr;
        data[7] = (byte) req_fc;
        data[8] = (byte) dlen;
        for (int i = 0; i < wdata.length; ++i) {
            final short w = wdata[i];
            data[9 + i * 2] = (byte) (0xFF & w >> 8);
            data[9 + i * 2 + 1] = (byte) (0xFF & w);
        }
        return data;
    }

    public static ModbusCmdReadWords createByElement(final Element ele) {
        final boolean b_w = "w".equalsIgnoreCase(ele.getAttribute("w"));
        final int dev_addr = Integer.parseInt(ele.getAttribute("dev"));
        final int reg_addr = Integer.parseInt(ele.getAttribute("reg"));
        final int reg_n = Integer.parseInt(ele.getAttribute("reg_num"));
        return new ModbusCmdReadWords((short) (b_w ? 1 : 2), -1L, dev_addr, reg_addr, reg_n);
    }

    @Override
    public short getFC() {
        return this.fc;
    }

    @Override
    public int getRegAddr() {
        return this.regAddr;
    }

    @Override
    public int getRegNum() {
        return this.regNum;
    }

    private void setRegAddrNum(final int reg_addr, final int reg_num) {
        this.regAddr = reg_addr;
        this.regNum = reg_num;
        this.regAddr = reg_addr;
        this.regNum = reg_num;
        this.up_vals = new int[reg_num];
        this.last_vals = new Integer[reg_num];
    }

    @Override
    public boolean isReadCmd() {
        return true;
    }

    public int[] getRetVals() {
        if (!this.up_val_ok) {
            return null;
        }
        if (this.belongToRunner != null && !this.belongToRunner.isCmdRunning()) {
            return null;
        }
        return this.up_vals;
    }

    @Override
    public Object[] getReadVals() {
        final int[] r = this.getRetVals();
        if (r == null) {
            return null;
        }
        final Object[] rs = new Object[r.length];
        for (int k = 0; k < rs.length; ++k) {
            rs[k] = r[k];
        }
        return rs;
    }

    @Override
    public int calRespLenRTU() {
        return 3 + this.regNum * 2 + 2;
    }

    protected int reqRespRTU1(final OutputStream ous, final InputStream ins) throws Exception {
        final byte[] pdata = {(byte) this.slaveAddr, (byte) this.fc, (byte) (this.regAddr >> 8 & 0xFF), (byte) (this.regAddr & 0xFF), (byte) (this.regNum >> 8), (byte) (this.regNum & 0xFF), 0, 0};
        final int crc = ModbusCmd.modbus_crc16_check(pdata, 6);
        pdata[6] = (byte) (crc >> 8 & 0xFF);
        pdata[7] = (byte) (crc & 0xFF);
        this.clearInputStream(ins);
        ous.write(pdata);
        ous.flush();
        final ModbusParserResp mp_resp = new ModbusParserResp();
        mp_resp.initDevFC(this.slaveAddr, this.fc, this.regNum);
        final ModbusParserResp.RespRet rr = mp_resp.parseRespCmdRTU(ins);
        if (rr.isDiscard()) {
            return -3;
        }
        if (rr.isErrRet()) {
            return -4;
        }
        final ModbusParserResp.RespRetReadInt16s rrrb = (ModbusParserResp.RespRetReadInt16s) rr;
        final int[] bvs = rrrb.getReadVals();
        final HashMap<Integer, Object> addr2val = new HashMap<Integer, Object>();
        for (int i = 0; i < this.regNum; ++i) {
            final int tmpv = bvs[i];
            this.up_vals[i] = tmpv;
            if (!Integer.valueOf(tmpv).equals(this.last_vals[i])) {
                addr2val.put(this.regAddr + i, tmpv);
            }
            this.last_vals[i] = tmpv;
        }
        if (addr2val.size() > 0 && this.belongToRunner != null && this.belongToRunner.runLis != null) {
            this.belongToRunner.runLis.onModbusReadChanged(this, addr2val);
        }
        this.up_val_ok = true;
        return this.regNum;
    }

    @Override
    protected int reqRespRTU(final OutputStream ous, final InputStream ins) throws Exception {
        final byte[] pdata = {(byte) this.slaveAddr, (byte) this.fc, (byte) (this.regAddr >> 8 & 0xFF), (byte) (this.regAddr & 0xFF), (byte) (this.regNum >> 8), (byte) (this.regNum & 0xFF), 0, 0};
        int crc = ModbusCmd.modbus_crc16_check(pdata, 6);
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
                if (this.mbuss_adu[1] != (byte) this.fc) {
                    if (this.mbuss_adu[1] == (byte) (this.fc + 128)) {
                        break;
                    }
                    break;
                } else {
                    mayrlen = (this.mbuss_adu[2] & 0xFF) + 5;
                }
            }
        }
        if (mayrlen <= 0 || rlen < mayrlen) {
            this.com_stream_end();
            this.up_val_ok = false;
            if (rlen <= 0) {
                return -1;
            }
            if (rlen < mayrlen) {
                return -2;
            }
            return 0;
        } else {
            crc = ModbusCmd.modbus_crc16_check(this.mbuss_adu, mayrlen - 2);
            if ((byte) (crc >> 8) != this.mbuss_adu[mayrlen - 2] || (byte) (crc & 0xFF) != this.mbuss_adu[mayrlen - 1]) {
                this.com_stream_end();
                return -3;
            }
            final HashMap<Integer, Object> addr2val = new HashMap<Integer, Object>();
            for (int i = 0; i < this.regNum; ++i) {
                int tmpv = this.mbuss_adu[3 + i * 2] & 0xFF;
                tmpv <<= 8;
                tmpv += (this.mbuss_adu[3 + i * 2 + 1] & 0xFF);
                this.up_vals[i] = tmpv;
                if (!Integer.valueOf(tmpv).equals(this.last_vals[i])) {
                    addr2val.put(this.regAddr + i, tmpv);
                }
                this.last_vals[i] = tmpv;
            }
            if (addr2val.size() > 0 && this.belongToRunner != null && this.belongToRunner.runLis != null) {
                this.belongToRunner.runLis.onModbusReadChanged(this, addr2val);
            }
            this.com_stream_end();
            this.up_val_ok = true;
            return this.regNum;
        }
    }

    @Override
    protected int reqRespTCP(final OutputStream ous, final InputStream ins) throws Exception {
        final byte[] pdata = new byte[12];
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
        pdata[7] = (byte) this.fc;
        pdata[8] = (byte) (this.regAddr >> 8 & 0xFF);
        pdata[9] = (byte) (this.regAddr & 0xFF);
        pdata[10] = (byte) (this.regNum >> 8);
        pdata[11] = (byte) (this.regNum & 0xFF);
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
            this.up_val_ok = false;
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
            this.up_val_ok = false;
            return 0;
        }
        if (recvpdu[0] != (byte) this.slaveAddr) {
            this.up_val_ok = false;
            return 0;
        }
        if (recvpdu[1] != (byte) this.fc) {
            this.up_val_ok = false;
            return 0;
        }
        final HashMap<Integer, Object> addr2val = new HashMap<Integer, Object>();
        for (int i = 0; i < this.regNum; ++i) {
            int tmpv = recvpdu[3 + i * 2] & 0xFF;
            tmpv <<= 8;
            tmpv += (recvpdu[3 + i * 2 + 1] & 0xFF);
            this.up_vals[i] = tmpv;
            if (!Integer.valueOf(tmpv).equals(this.last_vals[i])) {
                addr2val.put(this.regAddr + i, tmpv);
            }
            this.last_vals[i] = tmpv;
        }
        if (addr2val.size() > 0 && this.belongToRunner != null && this.belongToRunner.runLis != null) {
            this.belongToRunner.runLis.onModbusReadChanged(this, addr2val);
        }
        this.up_val_ok = true;
        return this.regNum;
    }

    @Override
    public String toString() {
        return super.toString() + "| word dev_addr=" + this.getDevAddr() + " reg_addr=" + this.regAddr + " reg_num=" + this.regNum;
    }
}
