// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.util.Lan;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTTPClientJSONDrv extends DevDriver {
    static final String JS_NAME = "nashorn";
    private static HTTPClientJSONAddr msAddr;

    static {
        HTTPClientJSONDrv.msAddr = new HTTPClientJSONAddr();
    }

    long lastRunDT;
    private ScriptEngine engine;

    public HTTPClientJSONDrv() {
        this.lastRunDT = -1L;
        this.engine = null;
    }

    private static String readStringFromUrl(final String url) throws IOException {
        Throwable t = null;
        try {
            final InputStream is = new URL(url).openStream();
            try {
                final BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                final StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                return sb.toString();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } finally {
            if (t == null) {
                final Throwable exception;
                t = exception;
            } else {
                final Throwable exception;
                if (t != exception) {
                    t.addSuppressed(exception);
                }
            }
        }
    }

    public String getName() {
        return "http_json";
    }

    public String getTitle() {
        return "Http Json";
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    public DevDriver copyMe() {
        return new HTTPClientJSONDrv();
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        gp = new PropGroup("conns", lan);
        gp.addPropItem(new PropItem("title", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) ""));
        gp.addPropItem(new PropItem("url", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) ""));
        gp.addPropItem(new PropItem("js", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) "").withTxtMultiLine(true));
        pgs.add(gp);
        return pgs;
    }

    public List<PropGroup> getPropGroupsForDev() {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        return pgs;
    }

    public DevAddr getSupportAddr() {
        return HTTPClientJSONDrv.msAddr;
    }

    private ScriptEngine getJSEngine() {
        if (this.engine != null) {
            return this.engine;
        }
        final ScriptEngineManager manager = new ScriptEngineManager();
        return this.engine = manager.getEngineByName("nashorn");
    }

    public boolean setRtVal(final String dev_name, final String tag_name, final Object v) {
        final UADev dev = this.getBelongToCh().getDevByName(dev_name);
        if (dev == null) {
            return false;
        }
        final UATag tag = dev.getTagByName(tag_name);
        return tag != null;
    }

    private void runScriptToGetVal(final String jsonstr) throws ScriptException, NoSuchMethodException {
        final String script_txt = "var $in=" + jsonstr + ";";
        this.engine.eval(script_txt);
        this.engine.put("$out", this);
        final Invocable jsInvoke = (Invocable) this.getJSEngine();
        jsInvoke.invokeFunction("_do_http_json_model", new Object[0]);
    }

    public boolean supportDevFinder() {
        return false;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return null;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        final String url = this.getBelongToCh().getOrDefaultPropValueStr("conns", "url", (String) null);
        if (url == null || url.contentEquals("")) {
            return false;
        }
        try {
            final String txt = readStringFromUrl(url);
            if (txt == null || txt.contentEquals("")) {
                return false;
            }
            this.runScriptToGetVal(txt);
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        return false;
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }
}
