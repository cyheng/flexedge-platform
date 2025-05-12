// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.nbiot;

import cn.doraro.flexedge.driver.nbiot.msg.WMMsg;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsgReceipt;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsgReport;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsgValveResp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WaterMeterConnAccepted {
    public static String TP;
    private static HashSet<WaterMeterConnAccepted> allCAS;

    static {
        WaterMeterConnAccepted.TP = "tcp_accepted";
        WaterMeterConnAccepted.allCAS = new HashSet<WaterMeterConnAccepted>();
    }

    Socket sock;
    InputStream inputS;
    OutputStream outputS;
    Boolean bValve;
    IOnReport onReport;
    int report_st;
    private long lastChk;

    public WaterMeterConnAccepted() {
        this.sock = null;
        this.inputS = null;
        this.outputS = null;
        this.bValve = null;
        this.onReport = null;
        this.report_st = 0;
        this.lastChk = -1L;
    }

    public static synchronized void addConn(final WaterMeterConnAccepted ca) {
        WaterMeterConnAccepted.allCAS.add(ca);
        System.out.println("new conn found,cur conn num=" + WaterMeterConnAccepted.allCAS.size());
    }

    public static synchronized void removeConn(final WaterMeterConnAccepted ca) {
        WaterMeterConnAccepted.allCAS.remove(ca);
        System.out.println("remove conn ,cur conn num=" + WaterMeterConnAccepted.allCAS.size());
    }

    public static List<WaterMeterConnAccepted> listConns() {
        final ArrayList<WaterMeterConnAccepted> rets = new ArrayList<WaterMeterConnAccepted>();
        rets.addAll(WaterMeterConnAccepted.allCAS);
        return rets;
    }

    public void setOnReport(final IOnReport onrep) {
        this.onReport = onrep;
    }

    public String getConnType() {
        return "tcp_client";
    }

    public boolean setAcceptedSocket(final Socket sock) {
        if (this.sock != null) {
            this.disconnect();
        }
        try {
            this.sock = sock;
            this.inputS = sock.getInputStream();
            this.outputS = sock.getOutputStream();
            addConn(this);
            return true;
        } catch (final Exception ee) {
            this.disconnect();
            return false;
        }
    }

    void runInTh(final long timeout_ms) throws Exception {
        WMMsg msg = null;
        final long st = System.currentTimeMillis();
        int run = 1;
        do {
            Thread.sleep(1L);
            msg = WMMsg.parseMsg(this.inputS);
            if (msg == null) {
                if (System.currentTimeMillis() - st >= timeout_ms) {
                    return;
                }
                continue;
            } else {
                System.out.println("recved msg=\r\n");
                System.out.println(msg);
                if (msg instanceof WMMsgReport) {
                    System.out.println(" find report =" + msg);
                    final List<WMMsg> req_msgs = this.onReport.onMsgReport((WMMsgReport) msg);
                    if (req_msgs == null || req_msgs.size() <= 0) {
                        final WMMsgReceipt receipt = ((WMMsgReport) msg).createReceipt(false);
                        receipt.setMeterAddr(msg.getMeterAddr());
                        receipt.writeOut(this.outputS);
                        return;
                    }
                    run = req_msgs.size();
                    final WMMsgReceipt receipt = ((WMMsgReport) msg).createReceipt(true);
                    receipt.setMeterAddr(msg.getMeterAddr());
                    receipt.writeOut(this.outputS);
                    for (final WMMsg req : req_msgs) {
                        req.writeOut(this.outputS);
                    }
                } else {
                    if (!(msg instanceof WMMsgValveResp)) {
                        continue;
                    }
                    System.out.println(" find valve resp=" + msg.toString());
                    --run;
                }
            }
        } while (run > 0);
    }

    protected InputStream getInputStreamInner() {
        return this.inputS;
    }

    protected OutputStream getOutputStreamInner() {
        return this.outputS;
    }

    synchronized void disconnect() {
        if (this.sock == null) {
            return;
        }
        try {
            try {
                if (this.inputS != null) {
                    this.inputS.close();
                }
            } catch (final Exception ex) {
            }
            try {
                if (this.outputS != null) {
                    this.outputS.close();
                }
            } catch (final Exception ex2) {
            }
            try {
                if (this.sock != null) {
                    this.sock.close();
                }
            } catch (final Exception ex3) {
            }
            removeConn(this);
        } finally {
            this.inputS = null;
            this.outputS = null;
            this.sock = null;
        }
    }

    void checkConn() {
        if (System.currentTimeMillis() - this.lastChk < 5000L) {
            return;
        }
        this.lastChk = System.currentTimeMillis();
    }

    public String getDynTxt() {
        return "";
    }

    public boolean isClosed() {
        return this.sock == null || this.sock.isClosed();
    }

    public boolean isConnReady() {
        return this.sock != null;
    }

    public String getConnErrInfo() {
        if (this.sock == null) {
            return "no connection";
        }
        return null;
    }

    public void close() throws IOException {
        this.disconnect();
    }
}
