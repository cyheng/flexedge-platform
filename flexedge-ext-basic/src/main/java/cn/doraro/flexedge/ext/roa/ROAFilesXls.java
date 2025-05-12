// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.roa;

import cn.doraro.flexedge.core.router.JoinOut;
import java.util.List;
import cn.doraro.flexedge.core.router.RouterObj;
import cn.doraro.flexedge.core.router.JoinIn;
import cn.doraro.flexedge.core.router.RouterOuterAdp;
import cn.doraro.flexedge.core.router.RouterManager;

public class ROAFilesXls extends ROAFiles
{
    public ROAFilesXls(final RouterManager rm) {
        super(rm);
    }
    
    public String getTp() {
        return "xls";
    }
    
    public RouterOuterAdp newInstance(final RouterManager rm) {
        return null;
    }
    
    protected void RT_onRecvedFromJoinIn(final JoinIn ji, final RouterObj recved_ob) throws Exception {
    }
    
    protected boolean RT_start_ov() {
        return false;
    }
    
    public void RT_stop() {
    }
    
    public boolean RT_isRunning() {
        return false;
    }
    
    public List<JoinIn> getJoinInList() {
        return null;
    }
    
    public List<JoinOut> getJoinOutList() {
        return null;
    }
}
