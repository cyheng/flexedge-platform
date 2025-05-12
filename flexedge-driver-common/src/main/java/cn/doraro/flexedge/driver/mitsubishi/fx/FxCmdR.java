// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import java.io.InputStream;
import java.io.OutputStream;

public class FxCmdR extends FxCmd {
    private int baseAddr;
    private int readNum;
    private int startAddr;
    private transient FxMsgReqR req;
    private transient FxMsgRespR resp;

    public FxCmdR(final int base_addr, final int startaddr, final int readnum) {
        this.req = null;
        this.resp = null;
        this.baseAddr = base_addr;
        this.startAddr = startaddr;
        this.readNum = readnum;
    }

    public int getBaseAddr() {
        return this.baseAddr;
    }

    public int getStartAddr() {
        return this.startAddr;
    }

    public int getReadNum() {
        return this.readNum;
    }

    @Override
    void initCmd(final FxDriver drv, final boolean b_ext) {
        super.initCmd(drv, b_ext);
        final FxMsgReqR reqr = new FxMsgReqR();
        reqr.asStartAddr(this.baseAddr, this.startAddr).asByteNum(this.readNum).asExt(b_ext);
        this.req = reqr;
    }

    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.drv.getCmdInterval());
        this.resp = null;
        final byte[] bs1 = this.req.toBytes();
        final FxMsgRespR resp = new FxMsgRespR(this.readNum);
        FxMsg.clearInputStream(inputs, 50L);
        outputs.write(bs1);
        resp.readFromStream(inputs, this.recvTimeout);
        if (!resp.readOk) {
            if (FxMsg.log.isDebugEnabled()) {
                FxMsg.log.debug("read failed," + resp.errInf);
            }
            return false;
        }
        this.onResp(resp);
        return true;
    }

    private void onResp(final FxMsgRespR resp) {
        this.resp = resp;
    }

    public FxMsgReqR getReq() {
        return this.req;
    }

    public FxMsgRespR getResp() {
        return this.resp;
    }
}
