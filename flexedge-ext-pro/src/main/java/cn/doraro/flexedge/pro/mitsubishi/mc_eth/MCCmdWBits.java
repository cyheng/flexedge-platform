// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.conn.ConnPtStream;

import java.io.InputStream;
import java.io.OutputStream;

public class MCCmdWBits extends MCCmd {
    private MCCode code;
    private int readNum;
    private int startAddr;
    private boolean[] bitVals;
    private boolean bFmtAscii;
    private transient MCMsg3EReqWBits req;
    private transient MCMsg3EResp resp;

    public MCCmdWBits(final MCCode code, final int startaddr, final boolean[] bitvals, final boolean b_fmt_ascii) {
        this.bFmtAscii = false;
        this.req = null;
        this.resp = null;
        this.code = code;
        this.startAddr = startaddr;
        this.bitVals = bitvals;
        this.bFmtAscii = b_fmt_ascii;
    }

    public int getStartAddr() {
        return this.startAddr;
    }

    public int getReadNum() {
        return this.readNum;
    }

    @Override
    void initCmd(final MCEthDriver drv) {
        super.initCmd(drv);
        final MCMsg3EReqWBits reqr = new MCMsg3EReqWBits();
        int t250ms = (int) (this.getRecvTimeout() / 250L);
        if (t250ms <= 0) {
            t250ms = 1;
        }
        reqr.asWaitTimeout(t250ms);
        reqr.asWritePM(this.code, this.startAddr, this.bitVals);
        this.req = reqr;
    }

    @Override
    public boolean doCmd(final ConnPtStream cpt, final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.drv.getCmdInterval());
        this.resp = null;
        byte[] bs1 = null;
        if (this.bFmtAscii) {
            bs1 = this.req.toBytesAscii();
        } else {
            bs1 = this.req.toBytesBin();
        }
        final MCMsg3EResp resp = new MCMsg3EResp();
        resp.asRespLen(this.req);
        MCMsg.clearInputStream(inputs, 50L);
        outputs.write(bs1);
        if (this.bFmtAscii) {
            resp.readFromStreamAscii(cpt, inputs, this.recvTimeout);
        } else {
            resp.readFromStreamBin(cpt, inputs, this.recvTimeout);
        }
        if (!resp.readOk) {
            if (MCMsg.log.isDebugEnabled()) {
                MCMsg.log.debug("read failed," + resp.errInf);
            }
            return false;
        }
        this.resp = resp;
        return true;
    }

    public MCMsg3EReqWBits getReq() {
        return this.req;
    }

    public MCMsg3EResp getResp() {
        return this.resp;
    }

    @Override
    public boolean isRespOk() {
        return this.resp != null;
    }

    @Override
    public String toString() {
        return "CmdWBits " + this.startAddr + " " + this.getReadNum();
    }
}
