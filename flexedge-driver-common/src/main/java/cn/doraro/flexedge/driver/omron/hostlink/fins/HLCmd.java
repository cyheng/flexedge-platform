// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class HLCmd {
    static final int RECV_TIMEOUT_DEFAULT = 2000;
    static final int RECV_END_TIMEOUT_DEFAULT = 1500;
    protected HLFinsDriver drv;
    protected HLBlock block;
    protected long lastRunT;
    protected long scanIntervalMS;
    protected int scanErrIntervalMulti;
    protected long recvTimeout;
    protected boolean bFixTO;
    protected long recvEndTimeout;
    protected long reqInterMS;
    protected int failedRetryC;

    public HLCmd() {
        this.drv = null;
        this.block = null;
        this.lastRunT = -1L;
        this.scanIntervalMS = 100L;
        this.scanErrIntervalMulti = 3;
        this.recvTimeout = 2000L;
        this.bFixTO = true;
        this.recvEndTimeout = 1500L;
        this.reqInterMS = 0L;
        this.failedRetryC = 3;
    }

    public HLFinsDriver getDriver() {
        return this.drv;
    }

    public long getScanIntervalMS() {
        return this.scanIntervalMS + 100 * this.scanErrIntervalMulti;
    }

    public HLCmd withScanIntervalMS(final long sms) {
        this.scanIntervalMS = sms;
        return this;
    }

    public long getRecvTimeout() {
        return this.recvTimeout;
    }

    public HLCmd withRecvTimeout(final long rto, final int retry_c) {
        this.failedRetryC = retry_c;
        if (this.failedRetryC < 0) {
            this.failedRetryC = 3;
        }
        if (rto <= 0L) {
            this.recvTimeout = 2000L;
            this.bFixTO = false;
        } else {
            this.recvTimeout = rto;
            this.bFixTO = true;
        }
        return this;
    }

    public long getRecvEndTimeout() {
        return this.recvEndTimeout;
    }

    public HLCmd withRecvEndTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvEndTimeout = 1500L;
        } else {
            this.recvEndTimeout = rto;
        }
        return this;
    }

    protected void initCmd(final HLFinsDriver drv, final HLBlock block) {
        this.drv = drv;
        this.block = block;
    }

    public boolean tickCanRun() {
        final long ct = System.currentTimeMillis();
        if (ct - this.lastRunT > this.getScanIntervalMS()) {
            this.lastRunT = ct;
            return true;
        }
        return false;
    }

    public abstract boolean doCmd(final InputStream p0, final OutputStream p1, final StringBuilder p2) throws Exception;
}
