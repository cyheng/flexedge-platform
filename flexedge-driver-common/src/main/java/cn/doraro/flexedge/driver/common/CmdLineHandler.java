

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.ConnException;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.cxt.JSObMap;
import cn.doraro.flexedge.core.cxt.JsDef;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public abstract class CmdLineHandler extends JSObMap {
    protected static ILogger log;

    static {
        CmdLineHandler.log = LoggerManager.getLogger((Class) CmdLineHandler.class);
    }

    protected CmdLineDrv belongTo;
    protected ConnPtStream conn;
    private IRecvCallback<String> curSynRet;
    private String recvRet;
    private boolean bRecvSyn;

    public CmdLineHandler() {
        this.belongTo = null;
        this.conn = null;
        this.curSynRet = null;
        this.recvRet = null;
        this.bRecvSyn = false;
    }

    public abstract String getName();

    public abstract String getTitle();

    public abstract String getDesc();

    protected String getBeginStr() {
        return null;
    }

    protected String getEndStr() {
        return "\r\n";
    }

    protected abstract CmdLineHandler copyMe();

    protected abstract int getRecvMaxLen();

    protected boolean init(final CmdLineDrv cld, final StringBuilder sb) throws Exception {
        this.belongTo = cld;
        return true;
    }

    protected void RT_onConned(final ConnPtStream cpt) throws Exception {
        this.conn = cpt;
    }

    protected void RT_onDisconn(final ConnPtStream cpt) throws Exception {
        if (this.conn == cpt) {
            this.conn = null;
        }
    }

    protected final boolean sendStr(final String str) throws UnsupportedEncodingException, IOException {
        if (this.conn == null) {
            throw new ConnException("conn is null");
        }
        final OutputStream outputs = this.conn.getOutputStream();
        outputs.write(str.getBytes("UTF-8"));
        outputs.flush();
        return true;
    }

    protected final void clearInputBuf() throws IOException {
        if (this.conn == null) {
            return;
        }
        final InputStream inputs = this.conn.getInputStream();
        final int n = inputs.available();
        if (n <= 0) {
            return;
        }
        inputs.skip(n);
    }

    protected synchronized boolean sendRecvSyn(final String str, final IRecvCallback<String> ret) throws Exception {
        try {
            this.curSynRet = ret;
            this.recvRet = null;
            this.clearInputBuf();
            if (!this.sendStr(str)) {
                return false;
            }
            this.wait(this.belongTo.getRecvTimeOut());
            if (this.recvRet == null) {
                ret.onRecved(false, null, "recv time out");
                return false;
            }
            ret.onRecved(true, this.recvRet, null);
            return true;
        } finally {
            this.curSynRet = null;
        }
    }

    @JsDef
    public synchronized String send_recv_syn(final String str) throws Exception {
        this.bRecvSyn = true;
        try {
            this.recvRet = null;
            this.clearInputBuf();
            if (!this.sendStr(str)) {
                return null;
            }
            this.wait(this.belongTo.getRecvTimeOut());
            if (this.recvRet == null) {
                return null;
            }
            return this.recvRet;
        } finally {
            this.bRecvSyn = false;
        }
    }

    @JsDef
    public synchronized void send_asyn(final String str) throws Exception {
        this.sendStr(str);
    }

    final synchronized void RT_onRecved(final byte[] bs) throws Exception {
        final String ss = new String(bs);
        if (this.bRecvSyn || this.curSynRet != null) {
            this.curSynRet = null;
            this.recvRet = ss;
            this.notifyAll();
            return;
        }
        this.RT_onRecved(ss);
    }

    protected abstract boolean RT_useNoWait();

    public abstract void RT_runInLoop(final ConnPtStream p0) throws Exception;

    public abstract void RT_onRecved(final String p0) throws Exception;

    public interface IRecvCallback<T> {
        void onRecved(final boolean p0, final T p1, final String p2);
    }
}
