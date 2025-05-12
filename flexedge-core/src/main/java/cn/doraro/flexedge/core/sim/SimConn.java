package cn.doraro.flexedge.core.sim;

import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.*;

public abstract class SimConn implements Closeable {
    Thread connTh = null;
    SimChannel ch = null;
    SimCP cp = null;
    private ILogger log = LoggerManager.getLogger(SimConn.class);
    private transient PushbackInputStream pbInputs = null;


    private transient Object relatedOb = null;
    private Runnable runner = new Runnable() {

        @Override
        public void run() {
            try {
                while (connTh != null) {
                    try {
                        ch.RT_runConnInLoop(SimConn.this);
                    } catch (IOException e) {
                        //e.printStackTrace();
                        if (log.isDebugEnabled())
                            log.debug(" sim conn will close with err:" + e.getMessage());
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } finally {
                connTh = null;
                try {
                    close();
                } catch (Exception e) {
                }
            }
        }
    };

    public SimConn(SimChannel ch, SimCP cp) {
        this.ch = ch;
        this.cp = cp;
    }

    public abstract InputStream getConnInputStream();

    public abstract OutputStream getConnOutputStream();

    public abstract void pulseConn() throws Exception;

    public Object getRelatedOb() {
        return this.relatedOb;
    }

    public void setRelatedOb(Object ob) {
        this.relatedOb = ob;
    }

//	public int readFromPushback() throws IOException
//	{
//		PushbackInputStream pis = getPushbackInputStream();
//		int c = pis.read() ;
//		if(c<0)
//			throw new IOException("end of stream") ;
//		return c ;
//	}

    public PushbackInputStream getPushbackInputStream() {
        if (pbInputs != null)
            return pbInputs;

        synchronized (this) {
            if (pbInputs != null)
                return pbInputs;

            InputStream ins = getConnInputStream();
            if (ins == null)
                return null;
            pbInputs = new PushbackInputStream(ins, 10);
            return pbInputs;
        }
    }

    synchronized public void RT_start() {
        if (connTh != null)
            return;

        connTh = new Thread(runner);
        connTh.start();
    }

    public boolean RT_isRunning() {
        return connTh != null;
    }

    synchronized public void RT_stop() {
        if (connTh == null)
            return;

        connTh.interrupt();
        connTh = null;
    }

    @Override
    public void close() throws IOException {
        RT_stop();
    }
}
