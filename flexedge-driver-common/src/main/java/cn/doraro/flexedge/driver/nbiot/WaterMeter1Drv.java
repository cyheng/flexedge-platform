

package cn.doraro.flexedge.driver.nbiot;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.basic.NameTitleVal;
import cn.doraro.flexedge.core.conn.ConnPtTcpAccepted;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsg;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsgReport;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsgValveReq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class WaterMeter1Drv {
    static transient ILogger log;

    static {
        WaterMeter1Drv.log = LoggerManager.getLogger((Class) WaterMeter1Drv.class);
    }

    String localIP;
    int localPort;
    String ashName;
    XmlData ashParams;
    long freeSockTO;
    int rep_st;
    IOnReport onReport;
    private transient AcceptedSockHandler acceptedSockH;
    private transient ServerSocket serverSock;
    private transient Thread acceptTh;
    private transient ArrayList<AcceptedSockItem> sockItems;
    private Runnable acceptRunner;

    public WaterMeter1Drv() {
        this.localIP = null;
        this.localPort = 25000;
        this.ashName = null;
        this.ashParams = null;
        this.freeSockTO = 300000L;
        this.acceptedSockH = null;
        this.serverSock = null;
        this.acceptTh = null;
        this.sockItems = new ArrayList<AcceptedSockItem>();
        this.rep_st = 0;
        this.onReport = new IOnReport() {
            @Override
            public List<WMMsg> onMsgReport(final WMMsgReport report) {
                if (WaterMeter1Drv.this.rep_st == 0) {
                    WaterMeter1Drv.this.rep_st = 1;
                    return null;
                }
                final ArrayList<WMMsg> rets = new ArrayList<WMMsg>();
                if (WaterMeter1Drv.this.rep_st == 1) {
                    final WMMsgValveReq m = new WMMsgValveReq();
                    m.setMeterAddr(report.getMeterAddr());
                    m.setValveOpen(false);
                    rets.add(m);
                    WaterMeter1Drv.this.rep_st = 2;
                } else if (WaterMeter1Drv.this.rep_st == 2) {
                    final WMMsgValveReq m = new WMMsgValveReq();
                    m.setMeterAddr(report.getMeterAddr());
                    m.setValveOpen(true);
                    rets.add(m);
                    WaterMeter1Drv.this.rep_st = 0;
                }
                return rets;
            }
        };
        this.acceptRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    WaterMeter1Drv.access$1(WaterMeter1Drv.this, new ServerSocket(WaterMeter1Drv.this.localPort, 100));
                    System.out.println("Water Meter1Drv started..<<<<<.,ready to recv client connection on port=" + WaterMeter1Drv.this.localPort);
                    while (WaterMeter1Drv.this.acceptTh != null) {
                        final Socket client = WaterMeter1Drv.this.serverSock.accept();
                        new ASHThread(client).start();
                    }
                } catch (final Exception e) {
                    System.out.println("ConnProTcpServer Stop Error with port=" + WaterMeter1Drv.this.localPort);
                    if (WaterMeter1Drv.log.isDebugEnabled()) {
                        WaterMeter1Drv.log.debug("", (Throwable) e);
                    }
                    return;
                } finally {
                    if (WaterMeter1Drv.log.isDebugEnabled()) {
                        WaterMeter1Drv.log.debug("Modbus Tcp Adapter on port=[" + WaterMeter1Drv.this.localPort + "] Server stoped..");
                    }
                    WaterMeter1Drv.this.stopServer();
                }
                if (WaterMeter1Drv.log.isDebugEnabled()) {
                    WaterMeter1Drv.log.debug("Modbus Tcp Adapter on port=[" + WaterMeter1Drv.this.localPort + "] Server stoped..");
                }
                WaterMeter1Drv.this.stopServer();
            }
        };
    }

    public static void main(final String[] args) throws Exception {
        final WaterMeter1Drv wmd = new WaterMeter1Drv();
        if (args.length > 0) {
            wmd.localPort = Convert.parseToInt32(args[0], 1901);
        } else {
            wmd.localPort = 1901;
        }
        wmd.start();
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("wm->");
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            try {
                final StringTokenizer st = new StringTokenizer(inputLine, " ", false);
                final String[] cmds = new String[st.countTokens()];
                for (int i = 0; i < cmds.length; ++i) {
                    cmds[i] = st.nextToken();
                }
                if (cmds.length == 0) {
                    continue;
                }
                if ("exit".equals(cmds[0]) || "disconnect".equalsIgnoreCase(cmds[0])) {
                    wmd.stop();
                    System.exit(0);
                } else if ("?".equals(cmds[0]) || "help".equalsIgnoreCase(cmds[0])) {
                    System.out.println("exit - stop and exit!");
                    System.out.println("ver - show ver!");
                } else if ("ver".equals(cmds[0])) {
                    System.out.println("Version:" + Config.getVersion());
                } else {
                    System.out.println("unknow cmd , using ? or help !");
                }
            } catch (final Exception _e) {
                _e.printStackTrace();
                continue;
            } finally {
                System.out.print("wm->");
            }
            System.out.print("wm->");
        }
    }

    static /* synthetic */ void access$1(final WaterMeter1Drv waterMeter1Drv, final ServerSocket serverSock) {
        waterMeter1Drv.serverSock = serverSock;
    }

    public String getLocalIP() {
        if (this.localIP == null) {
            return "";
        }
        return this.localIP;
    }

    public int getLocalPort() {
        return this.localPort;
    }

    public String getAshName() {
        if (this.ashName == null) {
            return "";
        }
        return this.ashName;
    }

    public XmlData getAshParams() {
        return this.ashParams;
    }

    public AcceptedSockHandler getASH() {
        return this.acceptedSockH;
    }

    public String getStaticTxt() {
        return String.valueOf(this.getLocalIP()) + ":" + this.localPort;
    }

    public void start() throws Exception {
        synchronized(this) {
            if (this.acceptTh == null) {
                this.acceptTh = new Thread(this.acceptRunner, "iottree-nbiot-watermeter1-tcpserver");
                this.acceptTh.start();
            }
        }
    }

    public void disconnAll() {
        for (final WaterMeterConnAccepted ci : WaterMeterConnAccepted.listConns()) {
            try {
                ci.disconnect();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        synchronized (this) {
            this.stopServer();
            final Thread t = this.acceptTh;
            if (t != null) {
                t.interrupt();
            }
            this.acceptTh = null;
            this.disconnAll();
        }
    }

    private void stopServer() {
        if (this.serverSock != null) {
            try {
                this.serverSock.close();
            } catch (final Exception ex) {
            }
            this.serverSock = null;
        }
        this.acceptTh = null;
    }

    public List<AcceptedSockItem> getFreeSockItems() {
        final ArrayList<AcceptedSockItem> rets = new ArrayList<AcceptedSockItem>();
        for (final AcceptedSockItem asi : this.sockItems) {
            if (asi.isFree()) {
                rets.add(asi);
            }
        }
        return rets;
    }

    public interface AcceptedSockHandler {
        String getName();

        String getTitle();

        NameTitleVal[] getParamDefs();

        XmlData chkAndCreateParams(final HashMap<String, String> p0, final StringBuilder p1);

        void setParams(final XmlData p0);

        String checkSockConnId(final Socket p0) throws Exception;

        int getRecvTimeout();

        int getRecvEndTimeout();
    }

    public static class AcceptedSockItem {
        Socket sock;
        long acceptedDT;
        ConnPtTcpAccepted assignedCPT;

        public AcceptedSockItem(final Socket sk, final ConnPtTcpAccepted assignedcpt) {
            this.sock = null;
            this.acceptedDT = System.currentTimeMillis();
            this.assignedCPT = null;
            this.sock = sk;
            this.assignedCPT = assignedcpt;
        }

        public Socket getSocket() {
            return this.sock;
        }

        public long getAcceptedDT() {
            return this.acceptedDT;
        }

        public ConnPtTcpAccepted getAssignedCPT() {
            return this.assignedCPT;
        }

        public boolean isTimeout(final long to) {
            return System.currentTimeMillis() > this.acceptedDT + to;
        }

        public boolean isFree() {
            return this.assignedCPT == null;
        }
    }

    private class ASHThread extends Thread {
        Socket sock;

        public ASHThread(final Socket sock) {
            this.sock = null;
            this.sock = sock;
        }

        @Override
        public void run() {
            WaterMeterConnAccepted connapt = null;
            try {
                connapt = new WaterMeterConnAccepted();
                connapt.setAcceptedSocket(this.sock);
                connapt.setOnReport(WaterMeter1Drv.this.onReport);
                connapt.runInTh(WaterMeter1Drv.this.freeSockTO);
            } catch (final Exception e) {
                e.printStackTrace();
                if (WaterMeter1Drv.log.isDebugEnabled()) {
                    WaterMeter1Drv.log.debug("ASHThread", (Throwable) e);
                }
                return;
            } finally {
                if (connapt != null) {
                    connapt.disconnect();
                }
            }
            if (connapt != null) {
                connapt.disconnect();
            }
        }
    }
}
