// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNConn;
import org.json.JSONObject;
import cn.doraro.flexedge.core.msgnet.MNNodeEnd;

public class MqttOut_NE extends MNNodeEnd
{
    public static final String TP = "mqtt_out";
    String sendId;
    
    public MqttOut_NE() {
        this.sendId = null;
    }
    
    public String getTP() {
        return "mqtt_out";
    }
    
    public String getTPTitle() {
        return "MQTT Sender";
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
        jo.putOpt("send_id", (Object)this.sendId);
        return jo;
    }
    
    protected void setParamJO(final JSONObject jo) {
        this.sendId = jo.optString("send_id");
    }
    
    protected RTOut RT_onMsgIn(final MNConn in_conn, final MNMsg msg) {
        if (Convert.isNullOrEmpty(this.sendId)) {
            return null;
        }
        final Mqtt_M mm = (Mqtt_M)this.getOwnRelatedModule();
        final Mqtt_M.SendConf sc = mm.getSendConfById(this.sendId);
        if (sc == null) {
            return null;
        }
        try {
            mm.publish(sc.topic, msg.getPayloadStr());
            this.RT_DEBUG_ERR.clear("mqtt_send");
        }
        catch (final Exception e) {
            this.RT_DEBUG_ERR.fire("mqtt_send", e.getMessage(), (Throwable)e);
        }
        return null;
    }
}
