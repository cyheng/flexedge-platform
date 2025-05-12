package cn.doraro.flexedge.core.alert;

import cn.doraro.flexedge.core.cxt.UACodeItem;
import cn.doraro.flexedge.core.cxt.UAContext;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_val;

import javax.script.ScriptException;

@data_class
public class AlertOutJS extends AlertOut {
    public static final String TP = "js";

    @data_val(param_name = "js")
    String jsCode = null;
    private transient UACodeItem codeItem = null;

    @Override
    public String getOutTp() {
        return TP;
    }

    @Override
    public String getOutTpTitle() {
        return "JS";
    }

    public String getJsCode() {
        if (jsCode == null)
            return "";

        return jsCode;
    }

    //private transient UAVal midVal = null ;

    UACodeItem getCodeItem() throws ScriptException {
        if (codeItem != null)
            return codeItem;

        if (Convert.isNullOrEmpty(jsCode))
            return null;

        UAContext cxt = this.prj.RT_getContext();
        if (cxt == null)
            return null;

        codeItem = new UACodeItem("", "{" + this.jsCode + "\r\n}");
        synchronized (cxt) {
            codeItem.initItem(cxt, "$uid", "$alert");
        }
        return codeItem;
    }


    @Override
    public void sendAlert(String uid, AlertItem ai) throws Exception {
        UACodeItem ci = getCodeItem();
        if (ci == null)
            return;

        ci.runCodeFunc(uid, ai);
    }
}
