// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.msgnet.MNNodeStart;
import cn.doraro.flexedge.core.util.jt.JSONTemp;
import org.json.JSONObject;

public class MqttIn_NS extends MNNodeStart {
    public static final String TP = "mqtt_in";
    String recvId;

    public MqttIn_NS() {
        this.recvId = null;
    }

    public String getTP() {
        return "mqtt_in";
    }

    public String getTPTitle() {
        return "MQTT Receiver";
    }

    public JSONTemp getInJT() {
        return null;
    }

    public JSONTemp getOutJT() {
        return null;
    }

    public int getOutNum() {
        return 1;
    }

    public String getColor() {
        return "#debed7";
    }

    public String getIcon() {
        return "PK_bridge";
    }

    public boolean isParamReady(final StringBuilder failedr) {
        return true;
    }

    public JSONObject getParamJO() {
        final JSONObject jo = new JSONObject();
        jo.putOpt("recv_id", (Object) this.recvId);
        return jo;
    }

    protected void setParamJO(final JSONObject jo) {
        this.recvId = jo.optString("recv_id");
    }
}
