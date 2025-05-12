// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import java.io.InputStream;
import java.io.OutputStream;

public class ModbusCmdEx extends ModbusCmd {
    byte[] reqData;
    byte[] respData;

    public ModbusCmdEx(final int dev_addr, final byte[] reqdata) {
        super(-1L, dev_addr);
        this.reqData = null;
        this.respData = null;
        this.reqData = reqdata;
    }

    @Override
    public short getFC() {
        return -1;
    }

    public byte[] getRespData() {
        return this.respData;
    }

    @Override
    public int calRespLenRTU() {
        return -1;
    }

    @Override
    protected int reqRespRTU(final OutputStream ous, final InputStream ins) throws Exception {
        final byte[] pdata = new byte[this.reqData.length + 1];
        pdata[0] = (byte) this.slaveAddr;
        System.arraycopy(this.reqData, 0, pdata, 1, this.reqData.length);
        this.clearInputStream(ins);
        ous.write(pdata);
        ous.flush();
        int rlen = 0;
        final int mayrlen = 0;
        this.com_stream_recv_start(ins);
        while (this.com_stream_in_recving()) {
            rlen = this.com_stream_recv_chk_len_timeout(ins);
            if (rlen == 0) {
                continue;
            }
        }
        this.respData = new byte[rlen];
        System.arraycopy(this.mbuss_adu, 0, this.respData, 0, rlen);
        this.com_stream_end();
        return rlen;
    }

    @Override
    protected int reqRespTCP(final OutputStream ous, final InputStream ins) throws Exception {
        return 0;
    }
}
