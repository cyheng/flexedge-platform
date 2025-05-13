

package cn.doraro.flexedge.driver.s7.ppi;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class PPICmd {
    static final int RECV_TIMEOUT_DEFAULT = 1000;
    static final int RECV_END_TIMEOUT_DEFAULT = 20;
    protected long lastRunT;
    protected long scanIntervalMS;
    protected int scanErrIntervalMulti;
    protected short devAddr;
    protected PPIMemTp ppiMemTp;
    protected long recvTimeout;
    protected boolean bFixTO;
    protected long recvEndTimeout;
    protected long reqInterMS;
    PPIDriver ppiDrv;

    public PPICmd(final short dev_addr, final PPIMemTp ppi_mtp) {
        this.ppiDrv = null;
        this.lastRunT = -1L;
        this.scanIntervalMS = 100L;
        this.scanErrIntervalMulti = 3;
        this.devAddr = 2;
        this.ppiMemTp = null;
        this.recvTimeout = 1000L;
        this.bFixTO = true;
        this.recvEndTimeout = 20L;
        this.reqInterMS = 0L;
        this.devAddr = dev_addr;
        this.ppiMemTp = ppi_mtp;
    }

    public PPIDriver getDriver() {
        return this.ppiDrv;
    }

    public long getScanIntervalMS() {
        return this.scanIntervalMS + 100 * this.scanErrIntervalMulti;
    }

    public PPICmd withScanIntervalMS(final long sms) {
        this.scanIntervalMS = sms;
        return this;
    }

    public long getRecvTimeout() {
        return this.recvTimeout;
    }

    public PPICmd withRecvTimeout(final long rto) {
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

    public PPICmd withRecvEndTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvEndTimeout = 20L;
        } else {
            this.recvEndTimeout = rto;
        }
        return this;
    }

    void initCmd(final PPIDriver drv) {
        this.ppiDrv = drv;
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
