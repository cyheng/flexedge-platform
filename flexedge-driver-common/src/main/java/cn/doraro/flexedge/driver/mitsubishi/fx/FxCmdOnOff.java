// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.util.Convert;

import java.io.InputStream;
import java.io.OutputStream;

public class FxCmdOnOff extends FxCmd {
    private int baseAddr;
    private boolean bOn;
    private int startAddr;
    private transient FxMsgReqOnOff req;
    private transient boolean bAck;

    public FxCmdOnOff(final int base_addr, final int startaddr, final boolean b_on) {
        this.req = null;
        this.bAck = false;
        this.baseAddr = base_addr;
        this.startAddr = startaddr;
        this.bOn = b_on;
    }

    public int getBaseAddr() {
        return this.baseAddr;
    }

    public int getStartAddr() {
        return this.startAddr;
    }

    public boolean isOn() {
        return this.bOn;
    }

    public boolean isOff() {
        return !this.bOn;
    }

    @Override
    void initCmd(final FxDriver drv, final boolean b_ext) {
        super.initCmd(drv, b_ext);
        final FxMsgReqOnOff reqr = new FxMsgReqOnOff();
        reqr.asStartAddr(this.baseAddr, this.startAddr).asOnOrOff(this.bOn).asExt(b_ext);
        this.req = reqr;
    }

    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.drv.getCmdInterval());
        final byte[] bs1 = this.req.toBytes();
        FxMsg.log.trace("req ->" + Convert.byteArray2HexStr(bs1, " "));
        System.out.println("req ->" + Convert.byteArray2HexStr(bs1, " "));
        FxMsg.clearInputStream(inputs, 50L);
        outputs.write(bs1);
        final int c = FxMsg.readCharTimeout(inputs, this.recvTimeout);
        this.bAck = (c == 6);
        System.out.println(" FxCmdOnOff - " + this.bAck);
        return true;
    }

    public boolean isAck() {
        return this.bAck;
    }

    public FxMsgReqOnOff getReq() {
        return this.req;
    }
}
