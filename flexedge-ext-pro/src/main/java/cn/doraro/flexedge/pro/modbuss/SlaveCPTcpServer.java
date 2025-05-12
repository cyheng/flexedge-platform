// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.util.Iterator;
import java.net.Socket;
import java.net.SocketException;
import org.json.JSONObject;
import cn.doraro.flexedge.core.util.Convert;
import java.util.Collection;
import java.util.List;
import java.net.ServerSocket;
import java.util.ArrayList;
import cn.doraro.flexedge.core.util.xmldata.data_val;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.xmldata.data_class;

@data_class
public class SlaveCPTcpServer extends SlaveCP
{
    static ILogger log;
    public static final int DEF_PORT = 12000;
    @data_val(param_name = "server_ip")
    String serverIp;
    @data_val(param_name = "server_port")
    int serverPort;
    boolean modbusTcpOrRTU;
    ArrayList<SlaveTcpConn> allConns;
    private Thread acceptTh;
    private transient ServerSocket serverSock;
    private Runnable RT_runner;
    
    public SlaveCPTcpServer(final MSBus_M bus) {
        super(bus);
        this.serverIp = null;
        this.serverPort = 12000;
        this.modbusTcpOrRTU = true;
        this.allConns = new ArrayList<SlaveTcpConn>();
        this.acceptTh = null;
        this.serverSock = null;
        this.RT_runner = new Runnable() {
            @Override
            public void run() {
                SlaveCPTcpServer.this.RT_run();
            }
        };
    }
    
    void increaseConn(final SlaveTcpConn tc) {
        synchronized (this.allConns) {
            this.allConns.add(tc);
        }
    }
    
    void decreaseConn(final SlaveTcpConn tc) {
        synchronized (this.allConns) {
            this.allConns.remove(tc);
        }
    }
    
    @Override
    public int getConnsNum() {
        return this.allConns.size();
    }
    
    public synchronized List<SlaveConn> listAllConns() {
        final ArrayList<SlaveConn> rets = new ArrayList<SlaveConn>();
        rets.addAll(this.allConns);
        return rets;
    }
    
    public String getServerIp() {
        if (this.serverIp == null) {
            return "";
        }
        return this.serverIp;
    }
    
    public int getServerPort() {
        return this.serverPort;
    }
    
    @Override
    public String getConnTitle() {
        if (Convert.isNullOrEmpty(this.serverIp)) {
            return ":" + this.serverPort;
        }
        return this.serverIp + ":" + this.serverPort;
    }
    
    @Override
    public String getCPTp() {
        return "tcps";
    }
    
    @Override
    public String getCPTpT() {
        return "Tcp Server";
    }
    
    @Override
    public boolean isValid(final StringBuilder failedr) {
        return true;
    }
    
    @Override
    public JSONObject toJO() {
        final JSONObject jo = super.toJO();
        jo.putOpt("server_ip", (Object)this.serverIp);
        jo.putOpt("server_port", (Object)this.serverPort);
        return jo;
    }
    
    @Override
    public boolean fromJO(final JSONObject jo) {
        super.fromJO(jo);
        this.serverIp = jo.optString("server_ip", (String)null);
        this.serverPort = jo.optInt("server_port", 12000);
        return true;
    }
    
    @Override
    public List<SlaveConn> getConns() {
        return this.listAllConns();
    }
    
    @Override
    public void RT_init() {
    }
    
    private void RT_run() {
        try {
            if (this.serverSock == null) {
                this.serverSock = new ServerSocket(this.serverPort);
                if (SlaveCPTcpServer.log.isDebugEnabled()) {
                    SlaveCPTcpServer.log.debug(" Simulator Tcp Server accepted at port=" + this.serverPort);
                }
            }
            while (this.acceptTh != null) {
                final Socket sock = this.serverSock.accept();
                final SlaveTcpConn tc = new SlaveTcpConn(this.bus, this, sock);
                tc.RT_start();
            }
        }
        catch (final SocketException socke) {
            if (SlaveCPTcpServer.log.isDebugEnabled()) {
                SlaveCPTcpServer.log.debug("SlaveCPTcpServer " + this.getConnTitle() + " err:" + socke.getMessage());
            }
        }
        catch (final Exception ee) {
            ee.printStackTrace();
        }
        finally {
            this.acceptTh = null;
            this.RT_stop();
        }
    }
    
    @Override
    public synchronized void RT_runInLoop() {
        if (this.acceptTh != null) {
            return;
        }
        (this.acceptTh = new Thread(this.RT_runner)).start();
    }
    
    @Override
    public void RT_stop() {
        final Thread th = this.acceptTh;
        if (th != null) {
            th.interrupt();
            this.acceptTh = null;
        }
        if (this.serverSock != null) {
            try {
                this.serverSock.close();
                this.serverSock = null;
            }
            catch (final Exception ex) {}
        }
        for (final SlaveConn sc : this.listAllConns()) {
            try {
                sc.close();
            }
            catch (final Exception ex2) {}
        }
    }
    
    @Override
    public String RT_getRunInf() {
        final StringBuilder sb = new StringBuilder();
        for (final SlaveConn sc : this.listAllConns()) {
            final Socket sock = ((SlaveTcpConn)sc).socket;
            sb.append("<span style='color:green'>\u2192").append(sock.getInetAddress()).append(":").append(sock.getPort()).append("</span><br>");
        }
        return sb.toString();
    }
    
    static {
        SlaveCPTcpServer.log = LoggerManager.getLogger((Class)SlaveCPTcpServer.class);
    }
    
    class SlaveTcpConn extends SlaveConn
    {
        Socket socket;
        InputStream inputs;
        OutputStream outputs;
        
        public SlaveTcpConn(final MSBus_M bus, final SlaveCPTcpServer cp, final Socket sock) throws IOException {
            super(bus, cp);
            this.socket = null;
            this.inputs = null;
            this.outputs = null;
            (this.socket = sock).setSoTimeout(10000);
            this.inputs = sock.getInputStream();
            this.outputs = sock.getOutputStream();
            SlaveCPTcpServer.this.increaseConn(this);
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
        public void close() throws IOException {
            try {
                try {
                    super.close();
                }
                catch (final Exception ex) {}
                try {
                    this.inputs.close();
                }
                catch (final Exception ex2) {}
                try {
                    this.outputs.close();
                }
                catch (final Exception ex3) {}
                try {
                    this.socket.close();
                }
                catch (final Exception ex4) {}
            }
            finally {
                SlaveCPTcpServer.this.decreaseConn(this);
            }
        }
    }
}
