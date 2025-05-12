// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.util.Convert;
import java.io.OutputStream;
import java.io.InputStream;

public class PPICmdR extends PPICmd
{
    private int readNum;
    private int offsetBytes;
    private transient PPIMsgReq req;
    private transient PPIMsgReqConfirm reqc;
    private transient PPIMsgResp resp;
    
    public PPICmdR(final short dev_addr, final PPIMemTp ppi_mtp, final int offset, final int readnum) {
        super(dev_addr, ppi_mtp);
        this.req = null;
        this.reqc = null;
        this.resp = null;
        this.offsetBytes = offset;
        this.readNum = readnum;
    }
    
    public int getOffsetBytes() {
        return this.offsetBytes;
    }
    
    public int getReadNum() {
        return this.readNum;
    }
    
    @Override
    void initCmd(final PPIDriver drv) {
        super.initCmd(drv);
        if (this.ppiMemTp == PPIMemTp.T || this.ppiMemTp == PPIMemTp.C) {
            final PPIMsgReqRTC reqr = new PPIMsgReqRTC();
            int offset = this.offsetBytes;
            int rc;
            if (this.ppiMemTp == PPIMemTp.T) {
                offset = this.offsetBytes / 4;
                rc = this.readNum / 4;
            }
            else {
                offset = this.offsetBytes / 2;
                rc = this.readNum / 2;
            }
            reqr.withMemTp(this.ppiMemTp).withTick(offset, (short)rc).withSorAddr(this.ppiDrv.getMasterID()).withDestAddr(this.devAddr);
            this.req = reqr;
        }
        else {
            final PPIMsgReqR reqr2 = new PPIMsgReqR();
            reqr2.withAddrByte(this.ppiMemTp, this.offsetBytes, -1, this.readNum).withSorAddr(this.ppiDrv.getMasterID()).withDestAddr(this.devAddr);
            this.req = reqr2;
        }
        this.reqc = new PPIMsgReqConfirm();
        this.reqc.withSorAddr(this.ppiDrv.getMasterID()).withDestAddr(this.devAddr);
    }
    
    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs) throws Exception {
        if (this.ppiMemTp == PPIMemTp.C || this.ppiMemTp == PPIMemTp.T) {
            return this.doCmdTC(inputs, outputs);
        }
        return this.doCmdNor(inputs, outputs);
    }
    
    private boolean doCmdNor(final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.ppiDrv.getCmdInterval());
        this.resp = null;
        PPIMsg.clearInputStream(inputs, 50L);
        final byte[] bs1 = this.req.toBytes();
        final byte[] bs2 = this.reqc.toBytes();
        if (PPIMsg.log.isTraceEnabled()) {
            PPIMsg.log.trace("req " + this.ppiMemTp + " ->" + Convert.byteArray2HexStr(bs1, " "));
        }
        outputs.write(bs1);
        final int c = PPIMsg.readCharTimeout(inputs, this.ppiDrv.getReadTimeout());
        if (c != 229 && c != 249) {
            outputs.write(bs2);
            Thread.sleep(10L);
            return false;
        }
        Thread.sleep(10L);
        outputs.write(bs2);
        Thread.sleep(5L);
        final StringBuilder failedr = new StringBuilder();
        final PPIMsgRespR resp = PPIMsgRespR.parseFromStream(inputs, this.ppiDrv.getReadTimeout(), failedr);
        if (resp == null) {
            if (PPIMsg.log.isDebugEnabled()) {
                PPIMsg.log.debug(" failed=" + (Object)failedr);
            }
            return false;
        }
        this.onResp(resp);
        return true;
    }
    
    public boolean doCmdTC(final InputStream inputs, final OutputStream outputs) throws Exception {
        Thread.sleep(this.ppiDrv.getCmdInterval());
        this.resp = null;
        PPIMsg.clearInputStream(inputs, 50L);
        final byte[] bs1 = this.req.toBytes();
        if (PPIMsg.log.isTraceEnabled()) {
            PPIMsg.log.trace("req " + this.ppiMemTp + "  ->" + Convert.byteArray2HexStr(bs1, " "));
        }
        outputs.write(bs1);
        Thread.sleep(5L);
        final int c = PPIMsg.readCharTimeout(inputs, this.ppiDrv.getReadTimeout());
        if (c != 229 && c != 249) {
            return false;
        }
        Thread.sleep(10L);
        final byte[] bs2 = this.reqc.toBytes();
        outputs.write(bs2);
        final StringBuilder failedr = new StringBuilder();
        final PPIMsgRespRTC resp = PPIMsgRespRTC.parseFromStream(this.ppiMemTp, inputs, this.ppiDrv.getReadTimeout(), failedr);
        if (resp == null) {
            if (PPIMsg.log.isDebugEnabled()) {
                PPIMsg.log.debug(" failed=" + (Object)failedr);
            }
            return false;
        }
        this.onResp(resp);
        return true;
    }
    
    private void onResp(final PPIMsgRespR resp) {
        this.resp = resp;
    }
    
    public PPIMsgReq getReq() {
        return this.req;
    }
    
    private void onResp(final PPIMsgRespRTC resp) {
        this.resp = resp;
    }
    
    public PPIMsgResp getResp() {
        return this.resp;
    }
    
    public PPIMsgRespR getRespR() {
        return (PPIMsgRespR)this.resp;
    }
    
    public PPIMsgRespRTC getRespRTC() {
        return (PPIMsgRespRTC)this.resp;
    }
}
