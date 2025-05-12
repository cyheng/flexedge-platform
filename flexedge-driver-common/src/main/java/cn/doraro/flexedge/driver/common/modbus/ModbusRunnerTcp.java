// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ModbusRunnerTcp extends ModbusRunner
{
    static Object lockObj;
    static ArrayList<ModbusRunnerTcp> ALL_CLIENTS;
    static long TMP_ID_C;
    Socket socket;
    InputStream serInputs;
    OutputStream serOutputs;
    transient Object relatedObj;
    transient StHandler stH;
    transient long lastChkHalt;
    
    static void increaseCount(final ModbusRunnerTcp c) {
        synchronized (ModbusRunnerTcp.lockObj) {
            ModbusRunnerTcp.ALL_CLIENTS.add(c);
        }
    }
    
    static void decreaseCount(final ModbusRunnerTcp c) {
        synchronized (ModbusRunnerTcp.lockObj) {
            ModbusRunnerTcp.ALL_CLIENTS.remove(c);
        }
    }
    
    public static int getClientConnCount() {
        return ModbusRunnerTcp.ALL_CLIENTS.size();
    }
    
    public static ModbusRunnerTcp[] getAllClients() {
        synchronized (ModbusRunnerTcp.lockObj) {
            final ModbusRunnerTcp[] rets = new ModbusRunnerTcp[ModbusRunnerTcp.ALL_CLIENTS.size()];
            ModbusRunnerTcp.ALL_CLIENTS.toArray(rets);
            return rets;
        }
    }
    
    public static void closeAllClients() {
        for (final ModbusRunnerTcp pcf : getAllClients()) {
            pcf.stopForce();
        }
    }
    
    public static List<ModbusRunnerTcp> getAllClientsList() {
        synchronized (ModbusRunnerTcp.lockObj) {
            final List<ModbusRunnerTcp> rets = new ArrayList<ModbusRunnerTcp>();
            rets.addAll(ModbusRunnerTcp.ALL_CLIENTS);
            return rets;
        }
    }
    
    public static List<ModbusRunnerTcp> getClientsByRelatedObj(final Object robj) {
        final ArrayList<ModbusRunnerTcp> rets = new ArrayList<ModbusRunnerTcp>();
        for (final ModbusRunnerTcp rt : ModbusRunnerTcp.ALL_CLIENTS) {
            if (robj.equals(rt.relatedObj)) {
                rets.add(rt);
            }
        }
        return rets;
    }
    
    public static ModbusRunnerTcp getClientById(final String clientid) {
        for (ModbusRunnerTcp modbusRunnerTcp : ModbusRunnerTcp.ALL_CLIENTS) {}
        return null;
    }
    
    static synchronized long newTmpId() {
        return ++ModbusRunnerTcp.TMP_ID_C;
    }
    
    public ModbusRunnerTcp(final String uid, final Socket s, final StHandler sth) throws Exception {
        super(uid);
        this.socket = null;
        this.serInputs = null;
        this.serOutputs = null;
        this.relatedObj = null;
        this.stH = null;
        this.lastChkHalt = -1L;
        this.socket = s;
        this.serInputs = this.socket.getInputStream();
        this.serOutputs = this.socket.getOutputStream();
        this.stH = sth;
        final int t = sth.getCmdInterval();
        if (t > 0) {
            this.setCmdIntervalMS(t);
        }
        this.setIgnoreErrCount(sth.getIgnoreErrCount());
        increaseCount(this);
    }
    
    void closeConn() {
        try {
            if (this.serInputs != null) {
                try {
                    this.serInputs.close();
                }
                catch (final Exception ex) {}
            }
            if (this.serOutputs != null) {
                try {
                    this.serOutputs.close();
                }
                catch (final Exception ex2) {}
            }
            if (this.socket != null) {
                try {
                    this.socket.close();
                }
                catch (final Exception ex3) {}
            }
        }
        finally {
            this.socket = null;
        }
    }
    
    @Override
    protected boolean checkReady() {
        return this.socket != null;
    }
    
    public InputStream getInputStream() {
        return this.serInputs;
    }
    
    public OutputStream getOutputStream() {
        return this.serOutputs;
    }
    
    public Socket getSocket() {
        return this.socket;
    }
    
    @Override
    protected boolean checkEnd(final boolean bhalt) {
        if (this.socket.isClosed()) {
            return true;
        }
        if (bhalt) {
            try {
                if (System.currentTimeMillis() - this.lastChkHalt > 2000L) {
                    this.lastChkHalt = System.currentTimeMillis();
                    this.socket.sendUrgentData(255);
                }
            }
            catch (final Exception ex) {
                try {
                    this.socket.close();
                }
                catch (final Exception ex2) {}
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected boolean beforeRunnerStart() {
        return true;
    }
    
    @Override
    protected void onRunnerStopped() {
        this.closeConn();
        decreaseCount(this);
        this.stH.onDisconnected(this);
    }
    
    public void dispose() {
        this.onRunnerStopped();
    }
    
    @Override
    public boolean isRunningOk() {
        return this.bCmdRun;
    }
    
    @Override
    public String getRunningInfo() {
        return "";
    }
    
    public void setRelatedObj(final Object robj) {
        this.relatedObj = robj;
    }
    
    public Object getRelatedObj() {
        return this.relatedObj;
    }
    
    public void sendStr(final String s) throws IOException {
        this.serOutputs.write(s.getBytes());
    }
    
    @Override
    public void run() {
        try {
            if (!this.stH.checkAuthOk(this)) {
                return;
            }
            if (ModbusRunnerTcp.log.isDebugEnabled()) {
                ModbusRunnerTcp.log.debug(">>modbus tcp checkOk . readcmd num=" + this.getReadCmds().size());
            }
            super.run();
        }
        catch (final Throwable e) {
            if (ModbusRunnerTcp.log.isDebugEnabled()) {
                ModbusRunnerTcp.log.debug("\ufffd\ufffd\ufffd\ufffd\u0363\u05b9\ufffd\ufffd\ufffd\ufffd" + e.getMessage());
                e.printStackTrace();
            }
        }
        finally {
            this.thread = null;
            this.bCmdRun = false;
            this.onRunnerStopped();
        }
    }
    
    @Override
    public String toString() {
        if (this.socket == null) {
            return "";
        }
        return this.socket.getRemoteSocketAddress().toString();
    }
    
    static {
        ModbusRunnerTcp.lockObj = new Object();
        ModbusRunnerTcp.ALL_CLIENTS = new ArrayList<ModbusRunnerTcp>();
        ModbusRunnerTcp.TMP_ID_C = 0L;
    }
    
    public interface StHandler
    {
        boolean checkAuthOk(final ModbusRunnerTcp p0);
        
        void onStarted(final ModbusRunnerTcp p0);
        
        void onDisconnected(final ModbusRunnerTcp p0);
        
        int getRecvTimeout();
        
        int getRecvEndTimeout();
        
        int getCmdInterval();
        
        int getIgnoreErrCount();
    }
}
