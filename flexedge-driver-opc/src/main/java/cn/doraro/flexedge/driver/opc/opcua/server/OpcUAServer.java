// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua.server;

import cn.doraro.flexedge.core.util.IServerBootComp;
import cn.doraro.flexedge.core.service.AbstractService;

public class OpcUAServer extends AbstractService implements IServerBootComp
{
    public static final String NAME = "opcua_server";
    DrvServer server;
    
    public OpcUAServer() {
        this.server = null;
    }
    
    public String getBootCompName() {
        return "opcua_server";
    }
    
    public synchronized void startComp() throws Exception {
        if (this.server != null) {
            return;
        }
        (this.server = new DrvServer()).startup();
    }
    
    public synchronized void stopComp() throws Exception {
        if (this.server == null) {
            return;
        }
        this.server.shutdown().thenRun(() -> this.server = null).join();
    }
    
    public boolean isRunning() {
        return this.server != null;
    }
    
    public String getName() {
        return "opcua_server";
    }
    
    public String getTitle() {
        return "OPC UA Server";
    }
    
    public String getBrief() {
        return "OPC UA Server";
    }
    
    public synchronized boolean startService() {
        try {
            this.startComp();
            return true;
        }
        catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public synchronized boolean stopService() {
        try {
            this.stopComp();
            return true;
        }
        catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
