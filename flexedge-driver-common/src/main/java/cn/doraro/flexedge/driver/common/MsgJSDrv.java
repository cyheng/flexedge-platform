// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.cxt.UAContext;
import org.json.JSONObject;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.basic.PropGroup;
import java.util.List;
import cn.doraro.flexedge.core.conn.ConnPtMsg;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.DevDriverMsgOnly;

public class MsgJSDrv extends DevDriverMsgOnly
{
    String onInitJS;
    String onMsgInJS;
    boolean hasMsgInJS;
    boolean msgInStr;
    String onTagWriteJS;
    boolean hasTagWriteJS;
    String onLoopJS;
    boolean hasLoopJS;
    
    public MsgJSDrv() {
        this.onInitJS = null;
        this.onMsgInJS = null;
        this.hasMsgInJS = false;
        this.msgInStr = true;
        this.onTagWriteJS = null;
        this.hasTagWriteJS = false;
        this.onLoopJS = null;
        this.hasLoopJS = false;
    }
    
    public String getName() {
        return "msg_js";
    }
    
    public String getTitle() {
        return "Msg JS Handler";
    }
    
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
    }
    
    public DevDriver copyMe() {
        return (DevDriver)new MsgJSDrv();
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>)ConnPtMsg.class;
    }
    
    public boolean supportDevFinder() {
        return false;
    }
    
    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }
    
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }
    
    public DevAddr getSupportAddr() {
        return null;
    }
    
    public boolean hasDriverConfigPage() {
        return true;
    }
    
    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        if (!super.initDriver(failedr)) {
            return false;
        }
        final UACh ch = this.getBelongToCh();
        if (ch == null) {
            failedr.append("no belong to ch found");
            return false;
        }
        final String txt = ch.getDrvSpcConfigTxt();
        if (Convert.isNullOrEmpty(txt)) {
            failedr.append("ch is not set special configuration");
            return false;
        }
        final JSONObject jo = new JSONObject(txt);
        this.onInitJS = jo.optString("on_init_js", "");
        this.onMsgInJS = jo.optString("on_msgin_js");
        this.msgInStr = jo.optBoolean("msg_in_str", true);
        this.onTagWriteJS = jo.optString("on_tagw_js");
        this.onLoopJS = jo.optString("on_loop_js");
        if (Convert.isNullOrEmpty(this.onMsgInJS) && Convert.isNullOrEmpty(this.onTagWriteJS)) {
            failedr.append("ch is not set special configuration");
            return false;
        }
        final String js_uid = ch.getId();
        final UAContext cxt = ch.RT_getContext();
        String tmps = String.valueOf(this.onInitJS) + "\r\n";
        if (Convert.isNotNullTrimEmpty(this.onMsgInJS)) {
            tmps = String.valueOf(tmps) + "function on_msgin_" + js_uid + "($ch,$connpt,$msg){\r\n";
            tmps = String.valueOf(tmps) + this.onMsgInJS;
            tmps = String.valueOf(tmps) + "\r\n}";
            this.hasMsgInJS = true;
        }
        if (Convert.isNotNullTrimEmpty(this.onTagWriteJS)) {
            tmps = String.valueOf(tmps) + "function on_tagw_" + js_uid + "($ch,$connpt,$tag,$input){\r\n";
            tmps = String.valueOf(tmps) + this.onTagWriteJS;
            tmps = String.valueOf(tmps) + "\r\n}";
            this.hasTagWriteJS = true;
        }
        if (Convert.isNotNullTrimEmpty(this.onLoopJS)) {
            tmps = String.valueOf(tmps) + "function on_loop_" + js_uid + "($ch,$connpt){\r\n";
            tmps = String.valueOf(tmps) + this.onLoopJS;
            tmps = String.valueOf(tmps) + "\r\n}";
            this.hasLoopJS = true;
        }
        cxt.scriptEval(tmps);
        return true;
    }
    
    public void RT_onConnMsgIn(final byte[] msgbs) {
        if (!this.hasMsgInJS) {
            return;
        }
        Object msg = msgbs;
        if (this.msgInStr) {
            try {
                msg = new String(msgbs, "UTF-8");
            }
            catch (final Exception ee) {
                throw new RuntimeException(ee);
            }
        }
        final UACh ch = this.getBelongToCh();
        final String js_uid = ch.getId();
        final ConnPtMsg cpm = (ConnPtMsg)this.getBindedConnPt();
        final UAContext cxt = ch.RT_getContext();
        try {
            cxt.scriptInvoke("on_msgin_" + js_uid, new Object[] { ch, cpm, msg });
        }
        catch (final Exception ee2) {
            ee2.printStackTrace();
        }
    }
    
    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        if (!this.hasTagWriteJS) {
            return false;
        }
        final String js_uid = ch.getId();
        final ConnPtMsg cpm = (ConnPtMsg)this.getBindedConnPt();
        final UAContext cxt = ch.RT_getContext();
        try {
            cxt.scriptInvoke("on_tagw_" + js_uid, new Object[] { ch, cpm, tag, v });
            return true;
        }
        catch (final Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }
    
    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }
    
    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }
    
    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        if (!this.hasLoopJS) {
            return true;
        }
        final String js_uid = ch.getId();
        final ConnPtMsg cpm = (ConnPtMsg)this.getBindedConnPt();
        final UAContext cxt = ch.RT_getContext();
        try {
            cxt.scriptInvoke("on_loop_" + js_uid, new Object[] { ch, cpm });
            return true;
        }
        catch (final Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }
    
    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }
}
