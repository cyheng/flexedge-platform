// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.util.Iterator;
import cn.doraro.flexedge.core.util.Convert;
import org.w3c.dom.Element;
import java.net.Socket;
import java.net.ServerSocket;
import cn.doraro.flexedge.core.util.logger.ILogger;

public class MSlaveTcpServer extends MSlave
{
    static ILogger log;
    ServerSocket server;
    int port;
    boolean bRun;
    Thread serverThread;
    Runnable acceptRuner;
    
    public MSlaveTcpServer() {
        this.server = null;
        this.port = -1;
        this.bRun = false;
        this.serverThread = null;
        this.acceptRuner = new Runnable() {
            @Override
            public void run() {
                try {
                    MSlaveTcpServer.this.server = new ServerSocket(MSlaveTcpServer.this.port, 1);
                    System.out.println("SlaveTcpServer start on port=" + MSlaveTcpServer.this.port + "<<");
                    while (MSlaveTcpServer.this.bRun) {
                        if (MSlaveTcpConn.getConnCount() > 0) {
                            Thread.sleep(5L);
                        }
                        else {
                            final Socket client = MSlaveTcpServer.this.server.accept();
                            final MSlaveTcpConn stc = new MSlaveTcpConn(MSlaveTcpServer.this, client);
                            stc.start();
                        }
                    }
                }
                catch (final Exception e) {
                    e.printStackTrace();
                    if (MSlaveTcpServer.log.isErrorEnabled()) {
                        MSlaveTcpServer.log.error("", (Throwable)e);
                    }
                }
                finally {
                    MSlaveTcpServer.this.close();
                    System.out.println("MCmd Asyn Server stoped..");
                    MSlaveTcpServer.this.serverThread = null;
                    MSlaveTcpServer.this.bRun = false;
                }
            }
        };
    }
    
    @Override
    void init(final Element ele) {
        super.init(ele);
        this.port = Convert.parseToInt32(ele.getAttribute("port"), -1);
        if (this.port <= 0) {
            throw new IllegalArgumentException("port not found in slave tcp server");
        }
    }
    
    @Override
    public synchronized void start() {
        if (this.serverThread != null) {
            return;
        }
        this.bRun = true;
        (this.serverThread = new Thread(this.acceptRuner, "mslave_tcp_server")).start();
    }
    
    public void close() {
        this.stop();
    }
    
    @Override
    public synchronized void stop() {
        if (this.server != null) {
            try {
                this.server.close();
            }
            catch (final Exception ex) {}
            this.server = null;
        }
        final Thread st = this.serverThread;
        if (st != null) {
            st.interrupt();
            this.serverThread = null;
        }
        for (final MSlaveTcpConn ep : MSlaveTcpConn.getAllClientsList()) {
            ep.stopForce();
        }
        this.bRun = false;
        this.serverThread = null;
    }
    
    static {
        MSlaveTcpServer.log = LoggerManager.getLogger((Class)MSlaveTcpServer.class);
    }
}
