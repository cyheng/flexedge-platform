// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.*;

public abstract class SlaveConn implements Closeable {
    Thread connTh;
    MSBus_M bus;
    SlaveCP cp;
    private ILogger log;
    private transient PushbackInputStream pbInputs;
    private transient Object relatedOb;
    private transient long RT_lastUsingDT;
    private Runnable runner;

    public SlaveConn(final MSBus_M bus, final SlaveCP cp) {
        this.log = LoggerManager.getLogger((Class) SlaveConn.class);
        this.connTh = null;
        this.bus = null;
        this.cp = null;
        this.pbInputs = null;
        this.relatedOb = null;
        this.RT_lastUsingDT = System.currentTimeMillis();
        this.runner = new Runnable() {
            @Override
            public void run() {
                try {
                    while (SlaveConn.this.connTh != null) {
                        try {
                            SlaveConn.this.cp.RT_runConnInLoop(SlaveConn.this);
                            continue;
                        } catch (final IOException e) {
                            e.printStackTrace();
                            SlaveConn.this.log.warn(" slave conn will close with err:" + e.getMessage());
                        } catch (final Exception e2) {
                            e2.printStackTrace();
                        }
                        break;
                    }
                } finally {
                    SlaveConn.this.connTh = null;
                    try {
                        SlaveConn.this.close();
                    } catch (final Exception ex) {
                    }
                }
            }
        };
        this.bus = bus;
        this.cp = cp;
    }

    protected abstract InputStream getConnInputStream();

    protected abstract OutputStream getConnOutputStream();

    public abstract void pulseConn() throws Exception;

    public abstract String getConnTitle();

    public Object getRelatedOb() {
        return this.relatedOb;
    }

    public void setRelatedOb(final Object ob) {
        this.relatedOb = ob;
    }

    public OutputStream getOutputStream() {
        this.RT_lastUsingDT = System.currentTimeMillis();
        return this.getConnOutputStream();
    }

    public PushbackInputStream getPushbackInputStream() {
        this.RT_lastUsingDT = System.currentTimeMillis();
        if (this.pbInputs != null) {
            return this.pbInputs;
        }
        synchronized (this) {
            if (this.pbInputs != null) {
                return this.pbInputs;
            }
            final InputStream ins = this.getConnInputStream();
            if (ins == null) {
                return null;
            }
            return this.pbInputs = new PushbackInputStream(ins, 10);
        }
    }

    public long RT_getLastUsingDT() {
        return this.RT_lastUsingDT;
    }

    public synchronized void RT_start() {
        if (this.connTh != null) {
            return;
        }
        (this.connTh = new Thread(this.runner)).start();
    }

    public boolean RT_isRunning() {
        return this.connTh != null;
    }

    public synchronized void RT_stop() {
        if (this.connTh == null) {
            return;
        }
        this.connTh.interrupt();
        this.connTh = null;
    }

    @Override
    public void close() throws IOException {
        this.RT_stop();
    }
}
