// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fxnet;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class FxNetCmd {
    static final int RECV_TIMEOUT_DEFAULT = 1000;
    static final int RECV_END_TIMEOUT_DEFAULT = 20;
    protected long lastRunT;
    protected long scanIntervalMS;
    protected int scanErrIntervalMulti;
    protected long recvTimeout;
    protected boolean bFixTO;
    protected long recvEndTimeout;
    protected long reqInterMS;
    FxNetDriver drv;

    public FxNetCmd() {
        this.drv = null;
        this.lastRunT = -1L;
        this.scanIntervalMS = 100L;
        this.scanErrIntervalMulti = 3;
        this.recvTimeout = 1000L;
        this.bFixTO = true;
        this.recvEndTimeout = 20L;
        this.reqInterMS = 0L;
    }

    public static boolean checkDevReady(final InputStream inputs, final OutputStream outputs, final long timeout) throws Exception {
        final int n = inputs.available();
        if (n > 0) {
            inputs.skip(n);
        }
        outputs.write(5);
        final int c = FxNetMsg.readCharTimeout(inputs, timeout);
        return c == 6;
    }

    public FxNetDriver getDriver() {
        return this.drv;
    }

    public long getScanIntervalMS() {
        return this.scanIntervalMS + 100 * this.scanErrIntervalMulti;
    }

    public FxNetCmd withScanIntervalMS(final long sms) {
        this.scanIntervalMS = sms;
        return this;
    }

    public long getRecvTimeout() {
        return this.recvTimeout;
    }

    public FxNetCmd withRecvTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvTimeout = 1000L;
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

    public FxNetCmd withRecvEndTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvEndTimeout = 20L;
        } else {
            this.recvEndTimeout = rto;
        }
        return this;
    }

    void initCmd(final FxNetDriver drv) {
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

    public abstract boolean doCmd(final InputStream p0, final OutputStream p1) throws Exception;
}
