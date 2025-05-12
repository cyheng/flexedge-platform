// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLException;
import cn.doraro.flexedge.driver.omron.hostlink.HLModel;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsg;
import kotlin.NotImplementedError;

import java.io.InputStream;
import java.io.OutputStream;

public class HLFinsCmdMemR extends HLCmd {
    public static ILogger log;

    static {
        HLFinsCmdMemR.log = LoggerManager.getLogger((Class) HLFinsCmdMemR.class);
    }

    private int readNum;
    private int startAddr;
    private boolean bReadBit;
    private transient HLFinsReqMemR req;
    private transient HLFinsRespMemR resp;

    public HLFinsCmdMemR(final int startaddr, final int readnum, final boolean readbit) {
        this.bReadBit = false;
        this.req = null;
        this.resp = null;
        this.startAddr = startaddr;
        this.readNum = readnum;
        this.bReadBit = readbit;
    }

    public int getStartAddr() {
        return this.startAddr;
    }

    public int getReadNum() {
        return this.readNum;
    }

    public boolean isReadBit() {
        return this.bReadBit;
    }

    @Override
    protected void initCmd(final HLFinsDriver drv, final HLBlock block) {
        super.initCmd(drv, block);
        final HLDevItem devitem = block.devItem;
        final HLModel m = (HLModel) devitem.getUADev().getDrvDevModel();
        final FinsMode fm = m.getFinsMode();
        final HLFinsReqMemR crr = new HLFinsReqMemR(fm);
        crr.asReqR(block.prefix, this.bReadBit, this.startAddr, 0, this.readNum);
        if (!devitem.bNetOrSerial) {
            crr.asFinsHeaderSerial();
            this.req = crr;
            return;
        }
        throw new NotImplementedError();
    }

    @Override
    public boolean doCmd(final InputStream inputs, final OutputStream outputs, final StringBuilder failedr) throws Exception {
        Thread.sleep(this.drv.getCmdInterval());
        this.resp = null;
        HLMsg.clearInputStream(inputs, 50L);
        final String str = this.req.writeTo(outputs);
        if (HLFinsCmdMemR.log.isDebugEnabled()) {
            HLFinsCmdMemR.log.debug("-> [" + str + "]");
        }
        HLFinsRespMemR resp = null;
        try {
            resp = (HLFinsRespMemR) this.req.readRespFrom(inputs, outputs, this.recvTimeout, this.failedRetryC);
            this.onResp(resp);
            if (resp.isFinsEndOk()) {
                if (HLFinsCmdMemR.log.isDebugEnabled()) {
                    HLFinsCmdMemR.log.debug("ok <- [" + resp.getRetTxt() + "]");
                }
            } else if (HLFinsCmdMemR.log.isDebugEnabled()) {
                HLFinsCmdMemR.log.debug("err <- [" + resp.getRetTxt() + "] " + resp.getFinsEndCode().getErrorInf());
            }
        } catch (final HLException ee) {
            if (ee.getErrCode() == 2) {
                failedr.append(ee.getMessage());
                return false;
            }
            if (HLFinsCmdMemR.log.isDebugEnabled()) {
                HLFinsCmdMemR.log.debug("err code:" + ee.getErrCode(), (Throwable) ee);
            }
        }
        return true;
    }

    private void onResp(final HLFinsRespMemR resp) {
        this.resp = resp;
    }

    public HLFinsReqMemR getReq() {
        return this.req;
    }

    public HLFinsRespMemR getResp() {
        return this.resp;
    }
}
