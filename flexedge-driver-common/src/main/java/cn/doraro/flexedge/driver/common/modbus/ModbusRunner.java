

package cn.doraro.flexedge.driver.common.modbus;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ModbusRunner implements Runnable {
    static ILogger log;

    static {
        ModbusRunner.log = LoggerManager.getLogger((Class) ModbusRunner.class);
    }

    String uniqueId;
    Thread thread;
    LinkedList<ModbusCmd> asynCmds;
    transient int cmdErrorNum;
    transient boolean bCmdRun;
    ModbusRunListener runLis;
    int recvTimeout;
    int recvEndTimeout;
    int cmdIntervalMs;
    int ignoreErrCount;
    long lastDoCmd;
    boolean bHalt;
    private List<ModbusCmd> readCmds;
    private ArrayList<ModbusCmd> runOnceCmds;
    private transient long lastPulse;

    public ModbusRunner(final String uniqueid) throws Exception {
        this.uniqueId = null;
        this.thread = null;
        this.readCmds = null;
        this.asynCmds = new LinkedList<ModbusCmd>();
        this.runOnceCmds = new ArrayList<ModbusCmd>();
        this.lastPulse = -1L;
        this.cmdErrorNum = 0;
        this.bCmdRun = false;
        this.runLis = null;
        this.recvTimeout = -1;
        this.recvEndTimeout = -1;
        this.cmdIntervalMs = 10;
        this.ignoreErrCount = 0;
        this.lastDoCmd = -1L;
        this.bHalt = false;
        this.uniqueId = uniqueid;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(final String id) {
        this.uniqueId = id;
    }

    public void setCmdRecvTimeout(final int recv_to, final int recv_end_to) {
        this.recvTimeout = recv_to;
        this.recvEndTimeout = recv_end_to;
    }

    public void setCmdIntervalMS(final int cmd_ims) {
        this.cmdIntervalMs = cmd_ims;
    }

    public void setIgnoreErrCount(final int c) {
        this.ignoreErrCount = c;
    }

    protected abstract boolean checkReady();

    protected abstract InputStream getInputStream();

    protected abstract OutputStream getOutputStream();

    protected abstract boolean beforeRunnerStart();

    protected abstract void onRunnerStopped();

    public abstract boolean isRunningOk();

    public abstract String getRunningInfo();

    public void setRunListener(final ModbusRunListener rl) {
        this.runLis = rl;
    }

    public boolean isCmdRunning() {
        return this.bCmdRun;
    }

    public void stop() {
        this.stopRunner(false);
    }

    synchronized void startRunner() {
        if (this.thread != null) {
            return;
        }
        this.bCmdRun = true;
        (this.thread = new Thread(this, "ModbusRunner")).start();
    }

    public void start() throws Exception {
        if (!this.beforeRunnerStart() && this.runLis != null) {
            this.runLis.onModbusCmdRunError();
        }
        this.startRunner();
    }

    protected synchronized void stopRunner(final boolean interrupt) {
        final Thread th = this.thread;
        if (th == null) {
            return;
        }
        if (interrupt) {
            th.interrupt();
            this.thread = null;
        }
        this.bCmdRun = false;
        this.onRunnerStopped();
    }

    public void stopForce() {
        this.stopRunner(true);
    }

    private void waitInterval() {
        try {
            Thread.sleep(10L);
        } catch (final Exception ex) {
        }
    }

    private void waitInterval(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception ex) {
        }
    }

    private void BEFORE_doCmd() {
        final long t = this.cmdIntervalMs - (System.currentTimeMillis() - this.lastDoCmd);
        if (t > 0L) {
            try {
                Thread.sleep(t);
            } catch (final Exception ex) {
            }
        }
    }

    private void AFTER_doCmd() {
        this.lastDoCmd = System.currentTimeMillis();
    }

    protected void onRunCmdError(final Throwable t) {
    }

    public boolean isRunHalt() {
        return this.bHalt;
    }

    public void setRunHalt(final boolean h) {
        this.bHalt = h;
    }

    @Override
    public void run() {
        try {
            while (this.bCmdRun) {
                if (this.bHalt) {
                    this.waitInterval();
                    if (this.checkEnd(true)) {
                        break;
                    }
                    final InputStream inputs = this.getInputStream();
                    if (inputs == null) {
                        continue;
                    }
                    final int avn = inputs.available();
                    if (avn <= 0) {
                        continue;
                    }
                    inputs.skip(avn);
                } else if (!this.checkReady()) {
                    this.waitInterval();
                } else {
                    if (this.checkEnd(false)) {
                        break;
                    }
                    this.waitInterval(1L);
                    final long tst = System.currentTimeMillis();
                    this.runReadCmds();
                    this.runQueAsynCmd();
                    final ArrayList<ModbusCmd> runone_ends = new ArrayList<ModbusCmd>();
                    for (int run_num = this.runOnceCmds.size(), i = 0; i < run_num; ++i) {
                        final ModbusCmd mc = this.runOnceCmds.get(i);
                        if (mc.getRunOnceTime() < System.currentTimeMillis()) {
                            try {
                                runone_ends.add(mc);
                                this.BEFORE_doCmd();
                                mc.doCmd(this.getOutputStream(), this.getInputStream());
                                this.AFTER_doCmd();
                            } catch (final Throwable ee) {
                                if (ModbusRunner.log.isErrorEnabled()) {
                                    ModbusRunner.log.error("", ee);
                                }
                            }
                        }
                    }
                    this.removeRunOnceCmds(runone_ends);
                }
            }
        } catch (final Exception e) {
            for (final ModbusCmd mc2 : this.readCmds) {
                if (mc2.isReadCmd() && this.runLis != null) {
                    try {
                        this.runLis.onModbusReadFailed(mc2);
                    } catch (final Exception iee) {
                        if (!ModbusRunner.log.isErrorEnabled()) {
                            continue;
                        }
                        ModbusRunner.log.error("", (Throwable) iee);
                    }
                }
            }
            if (ModbusRunner.log.isDebugEnabled()) {
                ModbusRunner.log.debug("\ufffd\ufffd\ufffd\ufffd\u0363\u05b9\ufffd\ufffd\ufffd\ufffd" + e.getMessage());
            }
        } finally {
            this.thread = null;
            this.bCmdRun = false;
            this.onRunnerStopped();
        }
    }

    public void runReadCmds() throws Exception {
        for (final ModbusCmd mc : this.readCmds) {
            if (!mc.tickCanRun()) {
                continue;
            }
            this.runQueAsynCmd();
            try {
                this.BEFORE_doCmd();
                if (ModbusRunner.log.isDebugEnabled()) {
                    ModbusRunner.log.debug(" ****run modbus cmd:" + mc);
                }
                synchronized (this) {
                    mc.doCmd(this.getOutputStream(), this.getInputStream());
                }
                this.AFTER_doCmd();
                if (!mc.isReadCmd() || this.runLis == null) {
                    continue;
                }
                final Object[] ovs = mc.getReadVals();
                if (ovs != null) {
                    this.runLis.onModbusReadData(mc, ovs);
                } else {
                    if (mc.getErrCount() <= this.ignoreErrCount) {
                        continue;
                    }
                    this.runLis.onModbusReadFailed(mc);
                }
            } catch (final Throwable ee) {
                this.onRunCmdError(ee);
                if (!mc.isReadCmd() || this.runLis == null) {
                    continue;
                }
                try {
                    this.runLis.onModbusReadFailed(mc);
                } catch (final Exception iee) {
                    if (ModbusRunner.log.isErrorEnabled()) {
                        ModbusRunner.log.error("", (Throwable) iee);
                    }
                }
                this.runLis.onModbusCmdRunError();
            }
        }
    }

    private int runQueAsynCmd() {
        final int asynn = this.asynCmds.size();
        if (asynn > 0) {
            for (int i = 0; i < asynn; ++i) {
                try {
                    final ModbusCmd mc = this.dequeueAsynFirst();
                    this.BEFORE_doCmd();
                    mc.doCmd(this.getOutputStream(), this.getInputStream());
                    this.AFTER_doCmd();
                } catch (final Throwable ee) {
                    if (ModbusRunner.log.isErrorEnabled()) {
                        ModbusRunner.log.error("", ee);
                    }
                }
            }
        }
        return asynn;
    }

    public List<ModbusCmd> getReadCmds() {
        return this.readCmds;
    }

    public void setReadCmds(List<ModbusCmd> mcmds) {
        if (mcmds == null) {
            mcmds = new ArrayList<ModbusCmd>(0);
        }
        for (final ModbusCmd mc : mcmds) {
            mc.belongToRunner = this;
        }
        this.readCmds = mcmds;
    }

    protected abstract boolean checkEnd(final boolean p0);

    public boolean doModbusCmdSyn(final ModbusCmd mc) throws Exception {
        if (!this.checkReady()) {
            return false;
        }
        synchronized (this) {
            this.BEFORE_doCmd();
            mc.doCmd(this.getOutputStream(), this.getInputStream());
            this.AFTER_doCmd();
        }
        return true;
    }

    public boolean doModbusCmdAsyn(final ModbusCmd mc) throws Exception {
        if (!this.checkReady()) {
            return false;
        }
        synchronized (this.asynCmds) {
            this.asynCmds.addLast(mc);
        }
        return true;
    }

    private ModbusCmd dequeueAsynFirst() {
        synchronized (this.asynCmds) {
            return this.asynCmds.removeFirst();
        }
    }

    public synchronized void doModbusCmdRunOnceDelay(final ModbusCmd mc, final long delay_ms) {
        mc.setRunOnceTime(System.currentTimeMillis() + delay_ms);
        this.runOnceCmds.add(mc);
    }

    private synchronized void removeRunOnceCmds(final List<ModbusCmd> mcs) {
        this.runOnceCmds.removeAll(mcs);
    }
}
