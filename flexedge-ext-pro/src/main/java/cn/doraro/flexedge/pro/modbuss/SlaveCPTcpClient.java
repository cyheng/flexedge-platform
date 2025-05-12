// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_val;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

@data_class
public class SlaveCPTcpClient extends SlaveCP {
    static ILogger log;

    static {
        SlaveCPTcpClient.log = LoggerManager.getLogger((Class) SlaveCPTcpClient.class);
    }

    @data_val(param_name = "server_host")
    String serverHost;
    @data_val(param_name = "server_port")
    int serverPort;
    @data_val(param_name = "send_id_when_conn")
    boolean sendIdWhenConn;
    @data_val(param_name = "conn_id_hex")
    boolean bConnIdHex;
    @data_val(param_name = "conn_id")
    String connId;
    long connNotUsingTO;
    private byte[] connIdBS;
    private transient TcpConn tcpConn;

    public SlaveCPTcpClient(final MSBus_M bus) {
        super(bus);
        this.serverHost = null;
        this.serverPort = -1;
        this.sendIdWhenConn = true;
        this.bConnIdHex = false;
        this.connId = null;
        this.connNotUsingTO = 60000L;
        this.connIdBS = null;
        this.tcpConn = null;
    }

    @Override
    public String getConnTitle() {
        return this.serverHost + ":" + this.serverPort;
    }

    @Override
    public String getCPTp() {
        return "tcpc";
    }

    @Override
    public String getCPTpT() {
        return "Tcp Client";
    }

    @Override
    public boolean isValid(final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.serverHost) || this.serverPort <= 0) {
            failedr.append("SlaveCPTcpClient has invalid server host or port set");
            return false;
        }
        if (this.sendIdWhenConn) {
            if (Convert.isNullOrEmpty(this.connId)) {
                failedr.append("SlaveCPTcpClient no conn_id input");
                return false;
            }
            try {
                if (this.bConnIdHex) {
                    this.connIdBS = Convert.hexStr2ByteArray(this.connId);
                } else {
                    this.connIdBS = this.connId.getBytes("UTF-8");
                }
            } catch (final Exception ee) {
                failedr.append("SlaveCPTcpClient conn_id err:" + ee.getMessage());
                return false;
            }
        }
        return true;
    }

    private synchronized boolean connectNor() {
        if (this.tcpConn != null && this.tcpConn.isConnected()) {
            return true;
        }
        if (Convert.isNullOrEmpty(this.serverHost) || this.serverPort <= 0) {
            return false;
        }
        this.disconnect();
        try {
            final Socket sock = new Socket(this.serverHost, this.serverPort);
            sock.setTcpNoDelay(true);
            sock.setKeepAlive(true);
            this.tcpConn = new TcpConn(this.bus, this, sock, (int) this.connNotUsingTO);
            if (this.sendIdWhenConn) {
                this.tcpConn.getConnOutputStream().write(this.connIdBS);
            }
            this.tcpConn.RT_start();
            return true;
        } catch (final Exception ee) {
            if (SlaveCPTcpClient.log.isDebugEnabled()) {
                SlaveCPTcpClient.log.debug(" SlaveCPTcpClient will disconnect by connect err:" + ee.getMessage());
                ee.printStackTrace();
            }
            this.disconnect();
            return false;
        }
    }

    synchronized void disconnect() {
        if (this.tcpConn == null) {
            return;
        }
        this.tcpConn.close();
        this.tcpConn = null;
    }

    @Override
    public String RT_getRunInf() {
        if (this.tcpConn != null) {
            return this.serverHost + ":" + this.serverPort + " <span style='color:green'>connected</span>";
        }
        return this.serverHost + ":" + this.serverPort + " <span style='color:red'>not connect</span>";
    }

    @Override
    public void RT_init() {
    }

    @Override
    public void RT_runInLoop() {
        this.connectNor();
        final TcpConn tc = this.tcpConn;
        if (tc == null) {
            return;
        }
        final long last_dt = tc.RT_getLastUsingDT();
        if (System.currentTimeMillis() - last_dt > this.connNotUsingTO) {
            this.disconnect();
        }
    }

    @Override
    public void RT_stop() {
        this.disconnect();
    }

    @Override
    public List<SlaveConn> getConns() {
        final TcpConn tc = this.tcpConn;
        if (tc == null) {
            return null;
        }
        return Arrays.asList(tc);
    }

    @Override
    public int getConnsNum() {
        return (this.tcpConn != null) ? 1 : 0;
    }

    @Override
    public JSONObject toJO() {
        final JSONObject jo = super.toJO();
        jo.putOpt("server_host", (Object) this.serverHost);
        jo.putOpt("server_port", (Object) this.serverPort);
        jo.putOpt("send_id", (Object) this.sendIdWhenConn);
        jo.putOpt("conn_id_hex", (Object) this.bConnIdHex);
        jo.putOpt("conn_id", (Object) this.connId);
        jo.putOpt("conn_nouse_to", (Object) this.connNotUsingTO);
        return jo;
    }

    @Override
    public boolean fromJO(final JSONObject jo) {
        super.fromJO(jo);
        this.serverHost = jo.optString("server_host", (String) null);
        this.serverPort = jo.optInt("server_port", -1);
        this.sendIdWhenConn = jo.optBoolean("send_id", true);
        this.bConnIdHex = jo.optBoolean("conn_id_hex", false);
        this.connId = jo.optString("conn_id", this.connId);
        this.connNotUsingTO = jo.optLong("conn_nouse_to", 60000L);
        if (this.sendIdWhenConn) {
            if (Convert.isNullOrEmpty(this.connId)) {
                SlaveCPTcpClient.log.debug("no conn_id input");
            }
            try {
                if (this.bConnIdHex) {
                    this.connIdBS = Convert.hexStr2ByteArray(this.connId);
                } else {
                    this.connIdBS = this.connId.getBytes("UTF-8");
                }
            } catch (final Exception ee) {
                SlaveCPTcpClient.log.debug(ee.getMessage());
            }
        }
        return true;
    }

    class TcpConn extends SlaveConn {
        Socket socket;
        InputStream inputs;
        OutputStream outputs;

        public TcpConn(final MSBus_M bus, final SlaveCPTcpClient cp, final Socket sock, final int read_to) throws IOException {
            super(bus, cp);
            this.socket = null;
            this.inputs = null;
            this.outputs = null;
            (this.socket = sock).setSoTimeout(read_to);
            this.inputs = sock.getInputStream();
            this.outputs = sock.getOutputStream();
        }

        public boolean isConnected() {
            return this.socket.isConnected();
        }

        public InputStream getConnInputStream() {
            return this.inputs;
        }

        public OutputStream getConnOutputStream() {
            return this.outputs;
        }

        @Override
        public void pulseConn() throws Exception {
            this.socket.sendUrgentData(0);
        }

        @Override
        public String getConnTitle() {
            return "TCP " + this.socket.getInetAddress() + ":" + this.socket.getPort();
        }

        @Override
        public void close() {
            try {
                super.close();
            } catch (final Exception ex) {
            }
            try {
                this.inputs.close();
            } catch (final Exception ex2) {
            }
            try {
                this.outputs.close();
            } catch (final Exception ex3) {
            }
            try {
                this.socket.close();
            } catch (final Exception ex4) {
            }
        }
    }
}
