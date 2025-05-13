

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.ConnException;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.js.Debug;
import cn.doraro.flexedge.core.util.js.GSys;
import cn.doraro.flexedge.core.util.js.GUtil;
import org.json.JSONObject;

import javax.script.*;
import java.io.File;
import java.io.IOException;

public class CmdLineHandlerJS extends CmdLineHandler {
    public static final String JS_NAME = "graal.js";
    public static final String FN_INIT = "drv_init";
    public static final String FN_ON_CONN = "drv_on_conn";
    public static final String FN_ON_DISCONN = "drv_on_disconn";
    public static final String FN_RUN_IN_LOOP = "drv_run_in_loop";
    public static final String FN_ON_RECV = "drv_on_recv";
    static Debug debug;
    static GSys sys;
    static GUtil util;

    static {
        CmdLineHandlerJS.debug = new Debug();
        CmdLineHandlerJS.sys = new GSys();
        CmdLineHandlerJS.util = new GUtil();
    }

    String name;
    String title;
    String desc;
    int recvMaxLen;
    boolean bJustConn;
    boolean bConnOk;
    String recvTxt;
    private ScriptEngine se;
    private Object jsDrvOb;
    private transient long lastLoopDT;

    public CmdLineHandlerJS() {
        this.recvMaxLen = 1000;
        this.se = null;
        this.bJustConn = false;
        this.bConnOk = false;
        this.recvTxt = null;
        this.jsDrvOb = null;
        this.lastLoopDT = -1L;
    }

    CmdLineHandlerJS(final JSONObject jo) {
        this.recvMaxLen = 1000;
        this.se = null;
        this.bJustConn = false;
        this.bConnOk = false;
        this.recvTxt = null;
        this.jsDrvOb = null;
        this.lastLoopDT = -1L;
        this.name = jo.getString("n");
        this.title = jo.getString("t");
        this.desc = jo.optString("d");
    }

    public CmdLineHandlerJS asBasic(final String name, final String title, final String desc) {
        this.name = name;
        this.title = title;
        this.desc = desc;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    protected CmdLineHandler copyMe() {
        final CmdLineHandlerJS r = new CmdLineHandlerJS();
        r.name = this.name;
        r.title = this.title;
        r.desc = this.desc;
        r.recvMaxLen = this.recvMaxLen;
        return r;
    }

    @Override
    protected int getRecvMaxLen() {
        return this.recvMaxLen;
    }

    private ScriptEngine createJSEngine() throws ScriptException {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("graal.js");
        engine.put("polyglot.js.allowHostAccess", true);
        engine.put("polyglot.js.allowAllAccess", false);
        engine.put("polyglot.js.allowHostClassLookup", s -> true);
        final Bindings bindings = engine.getBindings(100);
        bindings.put("polyglot.js.allowHostAccess", (Object) true);
        bindings.put("polyglot.js.allowHostClassLookup", s -> true);
        engine.put("$_debug_", CmdLineHandlerJS.debug);
        engine.put("$_sys_", CmdLineHandlerJS.sys);
        engine.put("$_util_", CmdLineHandlerJS.util);
        final String init_eval = "const $debug=$_debug_;Object.freeze($debug);const $sys=$_sys_;Object.freeze($sys);const $util=$_util_;Object.freeze($util);";
        engine.eval(init_eval);
        return engine;
    }

    private ScriptEngine getJsEngine() throws ScriptException {
        if (this.se != null) {
            return this.se;
        }
        return this.se = this.createJSEngine();
    }

    public synchronized void scriptEval(final String jstxt) throws ScriptException {
        this.getJsEngine().eval(jstxt);
    }

    public synchronized Object scriptInvoke(final String fn, final Object... paramvals) throws NoSuchMethodException, ScriptException {
        final Invocable inv = (Invocable) this.getJsEngine();
        return inv.invokeFunction(fn, paramvals);
    }

    public synchronized Object scriptInvokeMethod(final Object ob, final String fn, final Object... paramvals) throws NoSuchMethodException, ScriptException {
        final Invocable inv = (Invocable) this.getJsEngine();
        return inv.invokeMethod(ob, fn, paramvals);
    }

    private String loadJsCode() throws IOException {
        final File f = new File(String.valueOf(Config.getDataDirBase()) + "/dev_drv/cmd_line_js/" + this.name + ".js");
        return Convert.readFileTxt(f, "UTF-8");
    }

    @Override
    protected boolean init(final CmdLineDrv cld, final StringBuilder sb) throws Exception {
        if (!super.init(cld, sb)) {
            return false;
        }
        String jscode = this.loadJsCode();
        jscode = String.valueOf(jscode) + "\r\n var __" + this.name + "=new JsDRV()";
        final ScriptEngine se = this.getJsEngine();
        se.eval(jscode);
        this.scriptInvokeMethod(this.jsDrvOb = se.get("__" + this.name), "drv_init", this.name, this.title, this.belongTo, this.belongTo.getBelongToCh(), this);
        return true;
    }

    public void RT_onConned(final ConnPtStream cpt) throws Exception {
        super.RT_onConned(cpt);
        this.bJustConn = true;
    }

    public void RT_onDisconn(final ConnPtStream cpt) throws Exception {
        super.RT_onDisconn(cpt);
        this.scriptInvokeMethod(this.jsDrvOb, "drv_on_disconn", cpt);
        this.bConnOk = false;
    }

    @Override
    protected boolean RT_useNoWait() {
        return true;
    }

    @Override
    public void RT_runInLoop(final ConnPtStream cpt) throws Exception {
        if (this.bJustConn) {
            final Object ret = this.scriptInvokeMethod(this.jsDrvOb, "drv_on_conn", cpt);
            this.bConnOk = (Boolean.TRUE.equals(ret) || "true".equalsIgnoreCase(ret.toString()));
            if (this.bConnOk) {
                this.bJustConn = false;
            }
            return;
        }
        if (!this.bConnOk) {
            return;
        }
        final String recvtxt = this.gitRecvTxt();
        if (Convert.isNotNullEmpty(recvtxt)) {
            this.scriptInvokeMethod(this.jsDrvOb, "drv_on_recv", recvtxt);
        }
        if (System.currentTimeMillis() - this.lastLoopDT >= this.belongTo.getUsingInterval()) {
            try {
                try {
                    this.scriptInvokeMethod(this.jsDrvOb, "drv_run_in_loop", cpt);
                } finally {
                    this.lastLoopDT = System.currentTimeMillis();
                }
                this.lastLoopDT = System.currentTimeMillis();
            } catch (final Exception ee) {
                if (CmdLineHandlerJS.log.isDebugEnabled()) {
                    CmdLineHandlerJS.log.debug("CmdLineHandlerJS drv_run_in_loop error", (Throwable) ee);
                }
                throw new ConnException(ee.getMessage());
            }
        }
    }

    private synchronized String gitRecvTxt() {
        final String r = this.recvTxt;
        this.recvTxt = null;
        return r;
    }

    @Override
    public synchronized void RT_onRecved(final String cmd) throws Exception {
        this.recvTxt = cmd;
    }
}
