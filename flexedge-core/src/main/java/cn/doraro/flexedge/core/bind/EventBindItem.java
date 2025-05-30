package cn.doraro.flexedge.core.bind;

import cn.doraro.flexedge.core.UANodeOCTagsCxt;
import cn.doraro.flexedge.core.cxt.UACodeItem;
import cn.doraro.flexedge.core.cxt.UAContext;

public class EventBindItem {
    String eventName = null;

    String serverJS = null;

    transient UACodeItem code = null;

    public EventBindItem() {
    }

    public EventBindItem(String eventn, String serverjs) {
        this.eventName = eventn;
        this.serverJS = serverjs;
    }

    public String getEventName() {
        return eventName;
    }

    public String getServerJS() {
        return serverJS;
    }


    public boolean RT_runEventJS(UANodeOCTagsCxt tagn, Object val) {
        UAContext cxt = tagn.RT_getContext();
        if (cxt == null)
            return false;

        if (this.code == null) {
            this.code = new UACodeItem("", "{" + this.serverJS + "\r\n}");
            this.code.initItem(cxt);
        }

        if (!code.isValid())
            return false;

        try {
            code.runCodeFunc(val);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
