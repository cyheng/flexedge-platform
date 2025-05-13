

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.util.Convert;

import java.io.InputStream;
import java.io.OutputStream;

public class FxCmdW extends FxCmd {
    private int baseAddr;
    private byte[] wBytes;
    private int startAddr;
    private transient FxMsgReqW req;
    private transient boolean bAck;

    public FxCmdW(final int base_addr, final int startaddr, final byte[] w_bytes) {
        this.req = null;
        this.bAck = false;
        this.baseAddr = base_addr;
        this.startAddr = startaddr;
        this.wBytes = w_bytes;
    }

    public int getBaseAddr() {
        return this.baseAddr;
    }

    public int getStartAddr() {
        return this.startAddr;
    }

    public byte[] getWriteBytes() {
        return this.wBytes;
    }

    public boolean isAck() {
        return this.bAck;
    }

    @Override
    void initCmd(final FxDriver drv, final boolean b_ext) {
        super.initCmd(drv, b_ext);
        final FxMsgReqW reqw = new FxMsgReqW();
        reqw.asStartAddr(this.baseAddr, this.startAddr).asBytesVal(this.wBytes).asExt(b_ext);
        this.req = reqw;
    }

    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.drv.getCmdInterval());
        final byte[] bs1 = this.req.toBytes();
        if (FxMsg.log_w.isTraceEnabled()) {
            FxMsg.log_w.trace("reqw ->" + Convert.byteArray2HexStr(bs1, " "));
        }
        FxMsg.clearInputStream(inputs, 50L);
        outputs.write(bs1);
        final int c = FxMsg.readCharTimeout(inputs, this.recvTimeout);
        this.bAck = (c == 6);
        return true;
    }

    public FxMsgReqW getReq() {
        return this.req;
    }
}
