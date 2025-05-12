// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import java.util.Iterator;
import cn.doraro.flexedge.core.ConnException;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevDriver;

public class MCEthTCPDriver extends MCEthDriver
{
    boolean justOnConn;
    boolean connNotReadyShowed;
    
    public MCEthTCPDriver() {
        this.justOnConn = false;
        this.connNotReadyShowed = false;
    }
    
    public DevDriver copyMe() {
        return new MCEthTCPDriver();
    }
    
    public String getName() {
        return "mc_eth";
    }
    
    public String getTitle() {
        return "Mitsubishi MC Ethernet TCP";
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>)ConnPtStream.class;
    }
    
    public boolean hasDriverConfigPage() {
        return true;
    }
    
    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
        this.justOnConn = true;
    }
    
    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
        this.justOnConn = false;
    }
    
    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        final ConnPtStream cpt = (ConnPtStream)this.getBindedConnPt();
        if (cpt == null) {
            return true;
        }
        if (!cpt.isConnReady()) {
            if (!this.connNotReadyShowed) {
                for (final MCDevItem mdi : this.devItems) {
                    mdi.doCmdError("connection is not ready");
                }
                this.connNotReadyShowed = true;
            }
            return true;
        }
        this.connNotReadyShowed = false;
        try {
            if (this.justOnConn) {
                this.justOnConn = false;
            }
            for (final MCDevItem mdi : this.devItems) {
                mdi.doCmd(cpt);
            }
            this.checkConnBroken(cpt);
        }
        catch (final ConnException se) {
            if (MCEthTCPDriver.log.isDebugEnabled()) {
                MCEthTCPDriver.log.debug("RT_runInLoop err", (Throwable)se);
            }
            cpt.close();
        }
        catch (final Exception e) {
            if (MCEthTCPDriver.log.isErrorEnabled()) {
                MCEthTCPDriver.log.debug("RT_runInLoop err", (Throwable)e);
            }
        }
        return true;
    }
    
    private void checkConnBroken(final ConnPtStream cpt) throws Exception {
        long lastreadok = -1L;
        for (final MCDevItem mdi : this.devItems) {
            final long tmpdt = mdi.getLastReadOkDT();
            if (tmpdt > 0L && tmpdt > lastreadok) {
                lastreadok = tmpdt;
            }
        }
        if (lastreadok > 0L) {
            final ConnPtStream cpts = (ConnPtStream)this.getBelongToCh().getConnPt();
            final long read_no_to = cpts.getReadNoDataTimeout();
            if (read_no_to > 0L && System.currentTimeMillis() - lastreadok > read_no_to) {
                if (MCEthTCPDriver.log.isDebugEnabled()) {
                    MCEthTCPDriver.log.debug("RT_runInLoop last read ok timeout with " + read_no_to + ",connpt [" + cpts.getName() + "] will be closed");
                }
                cpt.close();
            }
        }
    }
}
