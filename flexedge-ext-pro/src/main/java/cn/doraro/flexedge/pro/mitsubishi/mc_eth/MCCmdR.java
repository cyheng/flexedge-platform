// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import java.io.OutputStream;
import java.io.InputStream;
import cn.doraro.flexedge.core.conn.ConnPtStream;

public class MCCmdR extends MCCmd
{
    private MCCode code;
    private int readNum;
    private int startAddr;
    private boolean bFmtAscii;
    private transient MCMsg3EReqRWords req;
    private transient MCMsg3ERespRWords resp;
    
    public MCCmdR(final MCCode code, final int startaddr, final int readnum, final boolean b_fmt_ascii) {
        this.bFmtAscii = false;
        this.req = null;
        this.resp = null;
        this.code = code;
        this.startAddr = startaddr;
        this.readNum = readnum;
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
        final MCMsg3EReqRWords reqr = new MCMsg3EReqRWords();
        int t250ms = (int)(this.getRecvTimeout() / 250L);
        if (t250ms <= 0) {
            t250ms = 1;
        }
        reqr.asWaitTimeout(t250ms);
        reqr.asReadPM(this.code, this.startAddr, this.readNum);
        this.req = reqr;
    }
    
    @Override
    public boolean doCmd(final ConnPtStream cpt, final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.drv.getCmdInterval());
        this.resp = null;
        byte[] bs1 = null;
        if (this.bFmtAscii) {
            bs1 = this.req.toBytesAscii();
        }
        else {
            bs1 = this.req.toBytesBin();
        }
        final MCMsg3ERespRWords resp = new MCMsg3ERespRWords();
        resp.asRespLen(this.req);
        MCMsg.clearInputStream(inputs, 50L);
        outputs.write(bs1);
        outputs.flush();
        if (this.bFmtAscii) {
            resp.readFromStreamAscii(cpt, inputs, this.recvTimeout);
        }
        else {
            resp.readFromStreamBin(cpt, inputs, this.recvTimeout);
        }
        this.resp = resp;
        if (!resp.readOk) {
            if (MCMsg.log.isDebugEnabled()) {
                MCMsg.log.debug("read failed," + resp.errInf);
            }
            return false;
        }
        return true;
    }
    
    public MCMsg3EReqRWords getReq() {
        return this.req;
    }
    
    public MCMsg3ERespRWords getResp() {
        return this.resp;
    }
    
    @Override
    public boolean isRespOk() {
        return this.resp != null;
    }
    
    @Override
    public String toString() {
        return "CmdR " + this.startAddr + " " + this.getReadNum();
    }
}
