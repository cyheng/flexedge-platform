// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import java.io.OutputStream;
import java.io.InputStream;
import cn.doraro.flexedge.core.conn.ConnPtStream;

public abstract class MCCmd
{
    static final int RECV_TIMEOUT_DEFAULT = 1000;
    static final int RECV_END_TIMEOUT_DEFAULT = 20;
    MCEthDriver drv;
    protected long lastRunT;
    protected long scanIntervalMS;
    protected int scanErrIntervalMulti;
    protected long recvTimeout;
    protected boolean bFixTO;
    protected long recvEndTimeout;
    protected long reqInterMS;
    
    public MCCmd() {
        this.drv = null;
        this.lastRunT = -1L;
        this.scanIntervalMS = 100L;
        this.scanErrIntervalMulti = 3;
        this.recvTimeout = 1000L;
        this.bFixTO = true;
        this.recvEndTimeout = 20L;
        this.reqInterMS = 0L;
    }
    
    public MCEthDriver getDriver() {
        return this.drv;
    }
    
    public long getScanIntervalMS() {
        return this.scanIntervalMS + 100 * this.scanErrIntervalMulti;
    }
    
    public MCCmd withScanIntervalMS(final long sms) {
        this.scanIntervalMS = sms;
        return this;
    }
    
    public long getRecvTimeout() {
        return this.recvTimeout;
    }
    
    public MCCmd withRecvTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvTimeout = 1000L;
            this.bFixTO = false;
        }
        else {
            this.recvTimeout = rto;
            this.bFixTO = true;
        }
        return this;
    }
    
    public long getRecvEndTimeout() {
        return this.recvEndTimeout;
    }
    
    public MCCmd withRecvEndTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvEndTimeout = 20L;
        }
        else {
            this.recvEndTimeout = rto;
        }
        return this;
    }
    
    void initCmd(final MCEthDriver drv) {
        this.drv = drv;
    }
    
    public boolean tickCanRun() {
        final long ct = System.currentTimeMillis();
        if (ct - this.lastRunT > this.getScanIntervalMS()) {
            this.lastRunT = ct;
            return true;
        }
        return false;
    }
    
    public abstract boolean doCmd(final ConnPtStream p0, final InputStream p1, final OutputStream p2) throws Exception;
    
    public static boolean checkDevReady(final InputStream inputs, final OutputStream outputs, final long timeout) throws Exception {
        final int n = inputs.available();
        if (n > 0) {
            inputs.skip(n);
        }
        return false;
    }
    
    public abstract boolean isRespOk();
}
