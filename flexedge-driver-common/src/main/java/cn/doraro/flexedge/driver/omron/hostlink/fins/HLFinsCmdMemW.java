// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLModel;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsg;
import kotlin.NotImplementedError;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class HLFinsCmdMemW extends HLCmd {
    public static ILogger log;

    static {
        HLFinsCmdMemW.log = LoggerManager.getLogger((Class) HLFinsCmdMemW.class);
    }

    private int startAddr;
    private int bitPos;
    private boolean bBitOnly;
    private List<Boolean> bitVals;
    private List<Short> wordVals;
    private transient HLFinsReqMemW req;
    private transient HLFinsRespOnlyEnd resp;
    private transient boolean bAck;

    public HLFinsCmdMemW() {
        this.bitPos = -1;
        this.bBitOnly = false;
        this.bitVals = null;
        this.wordVals = null;
        this.req = null;
        this.resp = null;
        this.bAck = false;
    }

    public HLFinsCmdMemW asBitVals(final int startaddr, final int bit_pos, final List<Boolean> bitvals) {
        if (bit_pos < 0 || bit_pos > 15) {
            throw new IllegalArgumentException("invalid bit pos");
        }
        this.startAddr = startaddr;
        this.bitPos = bit_pos;
        this.bitVals = bitvals;
        this.bBitOnly = false;
        return this;
    }

    public HLFinsCmdMemW asBitOnlyVals(final int startaddr, final List<Boolean> bitvals) {
        this.startAddr = startaddr;
        this.bitVals = bitvals;
        this.bBitOnly = true;
        return this;
    }

    public HLFinsCmdMemW asWordVals(final int startaddr, final List<Short> wvals) {
        this.startAddr = startaddr;
        this.bitPos = -1;
        this.wordVals = wvals;
        this.bBitOnly = false;
        return this;
    }

    public int getStartAddr() {
        return this.startAddr;
    }

    public boolean isAck() {
        return this.bAck;
    }

    @Override
    protected void initCmd(final HLFinsDriver drv, final HLBlock block) {
        super.initCmd(drv, block);
        final HLDevItem devitem = block.devItem;
        final HLModel m = (HLModel) devitem.getUADev().getDrvDevModel();
        final FinsMode fm = m.getFinsMode();
        final HLFinsReqMemW reqw = new HLFinsReqMemW(fm);
        if (this.bitPos >= 0) {
            reqw.asReqWBit(block.prefix, this.startAddr, this.bitPos, this.bitVals.size(), this.bitVals);
        } else if (this.bBitOnly) {
            reqw.asReqWBit(block.prefix, this.startAddr, 0, this.bitVals.size(), this.bitVals);
        } else {
            reqw.asReqWWord(block.prefix, this.startAddr, this.wordVals.size(), this.wordVals);
        }
        if (!devitem.bNetOrSerial) {
            reqw.asFinsHeaderSerial();
            this.req = reqw;
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
        if (HLFinsCmdMemW.log.isDebugEnabled()) {
            HLFinsCmdMemW.log.debug("-> [" + str + "]");
        }
        final HLFinsRespOnlyEnd resp = (HLFinsRespOnlyEnd) this.req.readRespFrom(inputs, outputs, this.recvTimeout, this.failedRetryC);
        if (resp != null && resp.isFinsEndOk()) {
            this.onRespOk(resp);
            if (HLFinsCmdMemW.log.isDebugEnabled()) {
                HLFinsCmdMemW.log.debug("<- ok [" + resp.getRetTxt() + "]");
            }
        } else {
            this.onRespErr(resp);
            if (HLFinsCmdMemW.log.isDebugEnabled()) {
                HLFinsCmdMemW.log.debug("<- err [" + resp.getRetTxt() + "] " + resp.getFinsEndCode().getErrorInf());
            }
        }
        return true;
    }

    private void onRespOk(final HLFinsRespOnlyEnd resp) {
        this.resp = resp;
        this.bAck = (resp != null);
    }

    private void onRespErr(final HLFinsRespOnlyEnd resp) {
        this.resp = resp;
        this.bAck = (resp != null);
    }

    public HLFinsReqMemW getReq() {
        return this.req;
    }

    public HLFinsRespOnlyEnd getResp() {
        return this.resp;
    }
}
