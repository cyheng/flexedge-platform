

package cn.doraro.flexedge.driver.common.modbus;

import kotlin.NotImplementedError;

import java.io.InputStream;
import java.io.OutputStream;

public class ModbusCmdErr extends ModbusCmd {
    short errCode;
    short reqFC;
    short addr;

    public ModbusCmdErr(final Protocol proto, final byte[] mbap4tcp, final short addr, final short req_fc, final short errcode) {
        this.errCode = 4;
        this.protocal = proto;
        this.mbap4Tcp = mbap4tcp;
        this.reqFC = req_fc;
        this.addr = addr;
        this.errCode = errcode;
    }

    public ModbusCmdErr() {
        this.errCode = 4;
    }

    public byte[] getRespData() {
        return ModbusCmd.createRespError(this, this.addr, this.reqFC);
    }

    @Override
    public short getFC() {
        return this.reqFC;
    }

    @Override
    public int calRespLenRTU() {
        throw new NotImplementedError();
    }

    @Override
    protected int reqRespRTU(final OutputStream ous, final InputStream ins) throws Exception {
        throw new NotImplementedError();
    }

    @Override
    protected int reqRespTCP(final OutputStream ous, final InputStream ins) throws Exception {
        throw new NotImplementedError();
    }
}
