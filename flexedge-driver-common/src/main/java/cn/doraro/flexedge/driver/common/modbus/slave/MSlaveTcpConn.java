

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmd;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdReadBits;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdReadWords;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MSlaveTcpConn implements Runnable {
    static final int BUF_LEN = 255;
    static ILogger log;
    static Object lockObj;
    static ArrayList<MSlaveTcpConn> ALL_CONNS;

    static {
        MSlaveTcpConn.log = LoggerManager.getLogger((Class) MSlaveTcpConn.class);
        MSlaveTcpConn.lockObj = new Object();
        MSlaveTcpConn.ALL_CONNS = new ArrayList<MSlaveTcpConn>();
    }

    Socket socket;
    InputStream serInputs;
    OutputStream serOutputs;
    Thread thread;
    boolean bRun;
    MSlave belongTo;

    public MSlaveTcpConn(final MSlave ms, final Socket socket) throws Exception {
        this.socket = null;
        this.serInputs = null;
        this.serOutputs = null;
        this.thread = null;
        this.bRun = false;
        this.belongTo = null;
        this.belongTo = ms;
        this.socket = socket;
        this.serInputs = socket.getInputStream();
        this.serOutputs = socket.getOutputStream();
        increaseCount(this);
    }

    private static void increaseCount(final MSlaveTcpConn c) {
        synchronized (MSlaveTcpConn.lockObj) {
            MSlaveTcpConn.ALL_CONNS.add(c);
        }
    }

    private static void decreaseCount(final MSlaveTcpConn c) {
        synchronized (MSlaveTcpConn.lockObj) {
            MSlaveTcpConn.ALL_CONNS.remove(c);
        }
    }

    public static int getConnCount() {
        return MSlaveTcpConn.ALL_CONNS.size();
    }

    public static MSlaveTcpConn[] getAllConns() {
        synchronized (MSlaveTcpConn.lockObj) {
            final MSlaveTcpConn[] rets = new MSlaveTcpConn[MSlaveTcpConn.ALL_CONNS.size()];
            MSlaveTcpConn.ALL_CONNS.toArray(rets);
            return rets;
        }
    }

    public static void closeAllConns() {
        for (final MSlaveTcpConn pcf : getAllConns()) {
            pcf.stopForce();
        }
    }

    public static List<MSlaveTcpConn> getAllClientsList() {
        synchronized (MSlaveTcpConn.lockObj) {
            final List<MSlaveTcpConn> rets = new ArrayList<MSlaveTcpConn>();
            rets.addAll(MSlaveTcpConn.ALL_CONNS);
            return rets;
        }
    }

    void closeTcpConn() {
        try {
            if (this.serInputs != null) {
                try {
                    this.serInputs.close();
                } catch (final Exception ex) {
                }
            }
            if (this.serOutputs != null) {
                try {
                    this.serOutputs.close();
                } catch (final Exception ex2) {
                }
            }
            if (this.socket != null) {
                try {
                    this.socket.close();
                } catch (final Exception ex3) {
                }
            }
        } finally {
            this.socket = null;
            decreaseCount(this);
        }
    }

    private void delay(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception ex) {
        }
    }

    private byte[] onReadReqAndResp(final byte[] reqbs, final int[] parseleft) {
        final ModbusCmd mc = ModbusCmd.parseRequest(reqbs, parseleft);
        if (mc == null) {
            return null;
        }
        if (mc instanceof ModbusCmdReadBits) {
            final ModbusCmdReadBits mcb = (ModbusCmdReadBits) mc;
            return this.onReqAndRespBits(mcb);
        }
        if (mc instanceof ModbusCmdReadWords) {
            return this.onReqAndRespWords((ModbusCmdReadWords) mc);
        }
        return null;
    }

    private byte[] onReqAndRespBits(final ModbusCmdReadBits mcb) {
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int req_num = mcb.getRegNum();
        final boolean[] resp = new boolean[req_num];
        for (int i = 0; i < req_num; ++i) {
            resp[i] = false;
        }
        for (final MSlaveDataProvider dp : this.belongTo.getBitDataProviders()) {
            if (dp.getDevAddr() != devid) {
                continue;
            }
            final int dp_regidx = dp.getRegIdx();
            final int dp_regnum = dp.getRegNum();
            if (req_idx + req_num <= dp_regidx) {
                continue;
            }
            if (req_idx > dp_regidx + dp_regnum) {
                continue;
            }
            final MSlaveDataProvider.SlaveData sd = dp.getSlaveData();
            if (sd == null) {
                continue;
            }
            if (!(sd instanceof MSlaveDataProvider.BoolDatas)) {
                continue;
            }
            final MSlaveDataProvider.BoolDatas msb = (MSlaveDataProvider.BoolDatas) sd;
            final boolean[] bs = msb.getBoolUsingDatas();
            if (bs == null) {
                continue;
            }
            if (req_idx < dp_regidx) {
                if (req_idx + req_num < dp_regidx + bs.length) {
                    System.arraycopy(bs, 0, resp, dp_regidx - req_idx, req_num - (dp_regidx - req_idx));
                } else {
                    System.arraycopy(bs, 0, resp, dp_regidx - req_idx, bs.length);
                }
            } else if (req_idx + req_num < dp_regidx + bs.length) {
                System.arraycopy(bs, req_idx - dp_regidx, resp, 0, req_num);
            } else {
                System.arraycopy(bs, req_idx - dp_regidx, resp, 0, bs.length - (req_idx - dp_regidx));
            }
        }
        return ModbusCmdReadBits.createResp(mcb, devid, mcb.getFC(), resp);
    }

    private byte[] onReqAndRespWords(final ModbusCmdReadWords mcb) {
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int req_num = mcb.getRegNum();
        final short[] resp = new short[req_num];
        for (int i = 0; i < req_num; ++i) {
            resp[i] = 0;
        }
        for (final MSlaveDataProvider dp : this.belongTo.getWordDataProviders()) {
            if (dp.getDevAddr() != devid) {
                continue;
            }
            final int dp_regidx = dp.getRegIdx();
            final int dp_regnum = dp.getRegNum();
            if (req_idx + req_num <= dp_regidx) {
                continue;
            }
            if (req_idx > dp_regidx + dp_regnum) {
                continue;
            }
            final MSlaveDataProvider.SlaveData sd = dp.getSlaveData();
            if (sd == null) {
                continue;
            }
            if (!(sd instanceof MSlaveDataProvider.SlaveDataWord)) {
                continue;
            }
            final MSlaveDataProvider.SlaveDataWord msb = (MSlaveDataProvider.SlaveDataWord) sd;
            final short[] bs = msb.getInt16UsingDatas();
            if (bs == null) {
                continue;
            }
            if (req_idx < dp_regidx) {
                if (req_idx + req_num < dp_regidx + bs.length) {
                    System.arraycopy(bs, 0, resp, dp_regidx - req_idx, req_num - (dp_regidx - req_idx));
                } else {
                    System.arraycopy(bs, 0, resp, dp_regidx - req_idx, bs.length);
                }
            } else if (req_idx + req_num < dp_regidx + bs.length) {
                System.arraycopy(bs, req_idx - dp_regidx, resp, 0, req_num);
            } else {
                System.arraycopy(bs, req_idx - dp_regidx, resp, 0, bs.length - (req_idx - dp_regidx));
            }
        }
        return ModbusCmdReadWords.createResp(mcb, devid, mcb.getFC(), resp);
    }

    @Override
    public void run() {
        try {
            System.out.println(">>modbus runner=" + this.getClass().getCanonicalName() + " on port=" + this.socket.getRemoteSocketAddress());
            int last_dlen = 0;
            long last_dt = -1L;
            long last_no_dt = System.currentTimeMillis();
            final byte[] buf = new byte[255];
            final int len = 0;
            Label_0343_Outer:
            while (this.bRun) {
                this.delay(1);
                if (last_dlen == 0) {
                    if (this.serInputs.available() <= 0) {
                        this.delay(5);
                        if (System.currentTimeMillis() - last_no_dt <= 5000L) {
                            continue;
                        }
                        last_no_dt = System.currentTimeMillis();
                        this.socket.sendUrgentData(0);
                    } else {
                        last_dlen = this.serInputs.available();
                        last_dt = System.currentTimeMillis();
                    }
                } else if (this.serInputs.available() > last_dlen) {
                    last_dlen = this.serInputs.available();
                    last_dt = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - last_dt < 10L) {
                        continue Label_0343_Outer;
                    }
                    final int rlen = last_dlen;
                    try {
                        if (last_dlen > 255) {
                            this.serInputs.skip(last_dlen);
                            continue Label_0343_Outer;
                        }
                    } finally {
                        last_dlen = 0;
                        last_dt = 0L;
                    }
                    byte[] rdata = new byte[rlen];
                    this.serInputs.read(rdata);
                    final long st = System.currentTimeMillis();
                    final int[] pl = {0};
                    while (true) {
                        while (pl[0] >= 0) {
                            if (pl[0] > 0) {
                                final byte[] crbs = new byte[rdata.length - pl[0]];
                                System.arraycopy(rdata, pl[0], crbs, 0, crbs.length);
                                rdata = crbs;
                            }
                            final byte[] respbs = this.onReadReqAndResp(rdata, pl);
                            if (respbs != null) {
                                this.serOutputs.write(respbs);
                                this.serOutputs.flush();
                            }
                            if (pl[0] < 0) {
                                continue Label_0343_Outer;
                            }
                        }
                        continue;
                    }
                }
            }
        } catch (final Throwable e) {
            MSlaveTcpConn.log.error("MSlaveTcpConn Broken:" + e.getMessage());
        } finally {
            this.thread = null;
            this.bRun = false;
            this.onRunnerStopped();
        }
    }

    synchronized void startRunner() {
        if (this.thread != null) {
            return;
        }
        this.bRun = true;
        (this.thread = new Thread(this, "MSlaveRunner")).start();
    }

    public void start() throws Exception {
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
        this.onRunnerStopped();
    }

    public void stopForce() {
        this.stopRunner(true);
    }

    protected boolean checkEnd(final boolean bhalt) {
        if (this.socket.isClosed()) {
            return true;
        }
        if (bhalt) {
            try {
                this.socket.sendUrgentData(255);
            } catch (final Exception ex) {
                try {
                    this.socket.close();
                } catch (final Exception ex2) {
                }
                return true;
            }
        }
        return false;
    }

    protected InputStream getInputStream() {
        return this.serInputs;
    }

    protected OutputStream getOutputStream() {
        return this.serOutputs;
    }

    protected void onRunnerStopped() {
        this.closeTcpConn();
    }

    public boolean isRunningOk() {
        return this.socket != null;
    }

    public String getRunningInfo() {
        if (this.isRunningOk()) {
            return "ok";
        }
        return "tcp error";
    }
}
