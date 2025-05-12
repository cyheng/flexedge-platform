// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import org.json.JSONObject;
import cn.doraro.flexedge.core.msgnet.IMNRunner;
import cn.doraro.flexedge.core.msgnet.MNModule;

public class BACnet_M extends MNModule implements IMNRunner
{
    public String getTP() {
        return "bacnet";
    }
    
    public String getTPTitle() {
        return "BACnet";
    }
    
    public String getColor() {
        return "#e5bdd6";
    }
    
    public String getIcon() {
        return "PK_bacnet";
    }
    
    public boolean isParamReady(final StringBuilder failedr) {
        return false;
    }
    
    public JSONObject getParamJO() {
        return null;
    }
    
    protected void setParamJO(final JSONObject jo) {
    }
    
    public boolean RT_start(final StringBuilder failedr) {
        return false;
    }
    
    public void RT_stop() {
    }
    
    public boolean RT_isRunning() {
        return false;
    }
    
    public boolean RT_isSuspendedInRun(final StringBuilder reson) {
        return false;
    }
    
    public boolean RT_runnerEnabled() {
        return false;
    }
    
    public boolean RT_runnerStartInner() {
        return false;
    }
}
